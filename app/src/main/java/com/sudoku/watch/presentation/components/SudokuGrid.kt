package com.sudoku.watch.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.sudoku.watch.game.SudokuGame
import com.sudoku.watch.presentation.theme.SudokuColors
import kotlinx.coroutines.launch

private const val GRID_SIZE = 6
private const val BOX_ROWS = 2  // rows per box
private const val BOX_COLS = 3  // cols per box
private const val MAX_ZOOM = 2.5f
private const val MIN_ZOOM = 1f
private const val ZOOM_ANIM_MS = 300

@Composable
fun SudokuGrid(
    game: SudokuGame,
    selectedRow: Int,
    selectedCol: Int,
    isInputMode: Boolean,
    highlightErrors: Boolean,
    onCellTap: (row: Int, col: Int) -> Unit,
    onCellLongPress: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()

    val zoomAnim = remember { Animatable(MIN_ZOOM) }
    val panXAnim = remember { Animatable(0f) }
    val panYAnim = remember { Animatable(0f) }

    val isZoomed = zoomAnim.value > MIN_ZOOM + 0.01f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clipToBounds()
            .pointerInput(Unit) {
                val canvasSize = size.width.toFloat()
                detectTapGestures(
                    onDoubleTap = { offset ->
                        scope.launch {
                            if (zoomAnim.value > MIN_ZOOM + 0.01f) {
                                launch { zoomAnim.animateTo(MIN_ZOOM, tween(ZOOM_ANIM_MS)) }
                                launch { panXAnim.animateTo(0f, tween(ZOOM_ANIM_MS)) }
                                launch { panYAnim.animateTo(0f, tween(ZOOM_ANIM_MS)) }
                            } else {
                                val center = canvasSize / 2f
                                val targetPanX = (center - offset.x) * (MAX_ZOOM - 1f)
                                val targetPanY = (center - offset.y) * (MAX_ZOOM - 1f)
                                val clamped = clampPan(targetPanX, targetPanY, MAX_ZOOM, canvasSize)
                                launch { zoomAnim.animateTo(MAX_ZOOM, tween(ZOOM_ANIM_MS)) }
                                launch { panXAnim.animateTo(clamped.first, tween(ZOOM_ANIM_MS)) }
                                launch { panYAnim.animateTo(clamped.second, tween(ZOOM_ANIM_MS)) }
                            }
                        }
                    },
                    onTap = { offset ->
                        val transformed = inverseTransform(
                            offset, zoomAnim.value, panXAnim.value, panYAnim.value, canvasSize
                        )
                        val cellSize = canvasSize / GRID_SIZE
                        val col = (transformed.x / cellSize).toInt().coerceIn(0, GRID_SIZE - 1)
                        val row = (transformed.y / cellSize).toInt().coerceIn(0, GRID_SIZE - 1)
                        onCellTap(row, col)
                    },
                    onLongPress = { offset ->
                        val transformed = inverseTransform(
                            offset, zoomAnim.value, panXAnim.value, panYAnim.value, canvasSize
                        )
                        val cellSize = canvasSize / GRID_SIZE
                        val col = (transformed.x / cellSize).toInt().coerceIn(0, GRID_SIZE - 1)
                        val row = (transformed.y / cellSize).toInt().coerceIn(0, GRID_SIZE - 1)
                        onCellLongPress(row, col)
                    }
                )
            }
            .pointerInput(isZoomed) {
                if (isZoomed) {
                    val canvasSize = size.width.toFloat()
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val newX = panXAnim.value + dragAmount.x
                            val newY = panYAnim.value + dragAmount.y
                            val clamped = clampPan(newX, newY, zoomAnim.value, canvasSize)
                            panXAnim.snapTo(clamped.first)
                            panYAnim.snapTo(clamped.second)
                        }
                    }
                }
            }
            .graphicsLayer {
                scaleX = zoomAnim.value
                scaleY = zoomAnim.value
                translationX = panXAnim.value
                translationY = panYAnim.value
            }
    ) {
        val cellSize = size.width / GRID_SIZE

        drawCellBackgrounds(game, selectedRow, selectedCol, isInputMode, cellSize, highlightErrors)
        drawGridLines(cellSize)

        // Draw numbers
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                val value = game.board[r][c]
                if (value != 0) {
                    val isOriginal = game.isOriginalCell(r, c)
                    val hasError = highlightErrors && game.hasConflict(r, c)

                    val color = when {
                        hasError -> SudokuColors.Error
                        isOriginal -> SudokuColors.OriginalNumber
                        else -> SudokuColors.UserNumber
                    }

                    val style = TextStyle(
                        fontSize = (cellSize * 0.55f).toSp(),
                        fontWeight = if (isOriginal) FontWeight.Bold else FontWeight.Normal,
                        color = color
                    )

                    val textResult = textMeasurer.measure(value.toString(), style)
                    drawText(
                        textLayoutResult = textResult,
                        topLeft = Offset(
                            x = c * cellSize + (cellSize - textResult.size.width) / 2f,
                            y = r * cellSize + (cellSize - textResult.size.height) / 2f
                        )
                    )
                }

                // Draw notes — 2×3 layout for 6 notes
                val notes = game.notes[r][c]
                if (value == 0 && notes.isNotEmpty()) {
                    val noteCols = 3
                    val noteWidth = cellSize / noteCols
                    val noteHeight = cellSize / 2f
                    val noteStyle = TextStyle(
                        fontSize = (noteHeight * 0.6f).toSp(),
                        color = SudokuColors.NoteText
                    )
                    for (note in notes) {
                        val nr = (note - 1) / noteCols
                        val nc = (note - 1) % noteCols
                        val noteResult = textMeasurer.measure(note.toString(), noteStyle)
                        drawText(
                            textLayoutResult = noteResult,
                            topLeft = Offset(
                                x = c * cellSize + nc * noteWidth + (noteWidth - noteResult.size.width) / 2f,
                                y = r * cellSize + nr * noteHeight + (noteHeight - noteResult.size.height) / 2f
                            )
                        )
                    }
                }
            }
        }
    }
}

// ─── Zoom/Pan Helpers ───

private fun inverseTransform(
    pointer: Offset,
    zoom: Float,
    panX: Float,
    panY: Float,
    canvasSize: Float
): Offset {
    val center = canvasSize / 2f
    return Offset(
        x = (pointer.x - center - panX) / zoom + center,
        y = (pointer.y - center - panY) / zoom + center
    )
}

private fun clampPan(
    panX: Float,
    panY: Float,
    zoom: Float,
    canvasSize: Float
): Pair<Float, Float> {
    val maxPan = canvasSize * (zoom - 1f) / 2f
    return Pair(
        panX.coerceIn(-maxPan, maxPan),
        panY.coerceIn(-maxPan, maxPan)
    )
}

// ─── Drawing Functions ───

private fun DrawScope.drawCellBackgrounds(
    game: SudokuGame,
    selectedRow: Int,
    selectedCol: Int,
    isInputMode: Boolean,
    cellSize: Float,
    highlightErrors: Boolean
) {
    drawRect(color = SudokuColors.GridBackground, size = size)

    if (selectedRow >= 0 && selectedCol >= 0) {
        // Row highlight
        drawRect(
            color = SudokuColors.HighlightLine,
            topLeft = Offset(0f, selectedRow * cellSize),
            size = Size(size.width, cellSize)
        )
        // Column highlight
        drawRect(
            color = SudokuColors.HighlightLine,
            topLeft = Offset(selectedCol * cellSize, 0f),
            size = Size(cellSize, size.height)
        )
        // 2×3 box highlight
        val boxRow = (selectedRow / BOX_ROWS) * BOX_ROWS
        val boxCol = (selectedCol / BOX_COLS) * BOX_COLS
        drawRect(
            color = SudokuColors.HighlightBox,
            topLeft = Offset(boxCol * cellSize, boxRow * cellSize),
            size = Size(cellSize * BOX_COLS, cellSize * BOX_ROWS)
        )
        // Selected cell
        drawRect(
            color = SudokuColors.SelectedCell,
            topLeft = Offset(selectedCol * cellSize, selectedRow * cellSize),
            size = Size(cellSize, cellSize)
        )
        // Input mode border
        if (isInputMode) {
            drawRect(
                color = SudokuColors.InputModeBorder,
                topLeft = Offset(selectedCol * cellSize + 1f, selectedRow * cellSize + 1f),
                size = Size(cellSize - 2f, cellSize - 2f),
                style = Stroke(width = 3f)
            )
        }
    }

    // Highlight same numbers
    if (selectedRow >= 0 && selectedCol >= 0) {
        val selectedValue = game.board[selectedRow][selectedCol]
        if (selectedValue != 0) {
            for (r in 0 until GRID_SIZE) {
                for (c in 0 until GRID_SIZE) {
                    if (game.board[r][c] == selectedValue && !(r == selectedRow && c == selectedCol)) {
                        drawRect(
                            color = SudokuColors.SameNumber,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }
    }

    // Highlight error cells
    if (highlightErrors) {
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                if (game.board[r][c] != 0 && game.hasConflict(r, c)) {
                    drawRect(
                        color = SudokuColors.ErrorBackground,
                        topLeft = Offset(c * cellSize, r * cellSize),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawGridLines(cellSize: Float) {
    // Horizontal lines: thick at box row boundaries (every 2 rows)
    for (i in 0..GRID_SIZE) {
        val pos = i * cellSize
        val isThickH = i % BOX_ROWS == 0
        val strokeH = if (isThickH) 3f else 1.5f
        val colorH = if (isThickH) SudokuColors.ThickLine else SudokuColors.ThinLine
        drawLine(colorH, Offset(0f, pos), Offset(size.width, pos), strokeH)

        // Vertical lines: thick at box col boundaries (every 3 cols)
        val isThickV = i % BOX_COLS == 0
        val strokeV = if (isThickV) 3f else 1.5f
        val colorV = if (isThickV) SudokuColors.ThickLine else SudokuColors.ThinLine
        drawLine(colorV, Offset(pos, 0f), Offset(pos, size.height), strokeV)
    }
}

private fun Float.toSp() = this.sp / 3.5f
