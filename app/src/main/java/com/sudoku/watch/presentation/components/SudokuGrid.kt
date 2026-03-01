package com.sudoku.watch.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.sudoku.watch.game.SudokuGame
import com.sudoku.watch.presentation.theme.SudokuColors

@Composable
fun SudokuGrid(
    game: SudokuGame,
    selectedRow: Int,
    selectedCol: Int,
    highlightErrors: Boolean,
    onCellSelected: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val cellSize = size.width / 9f
                    val col = (offset.x / cellSize).toInt().coerceIn(0, 8)
                    val row = (offset.y / cellSize).toInt().coerceIn(0, 8)
                    onCellSelected(row, col)
                }
            }
    ) {
        val cellSize = size.width / 9f

        // Draw cell backgrounds
        drawCellBackgrounds(game, selectedRow, selectedCol, cellSize, highlightErrors)

        // Draw grid lines
        drawGridLines(cellSize)

        // Draw numbers
        for (r in 0 until 9) {
            for (c in 0 until 9) {
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

                // Draw notes (small pencil marks)
                val notes = game.notes[r][c]
                if (value == 0 && notes.isNotEmpty()) {
                    val noteSize = cellSize / 3f
                    val noteStyle = TextStyle(
                        fontSize = (noteSize * 0.6f).toSp(),
                        color = SudokuColors.NoteText
                    )
                    for (note in notes) {
                        val nr = (note - 1) / 3
                        val nc = (note - 1) % 3
                        val noteResult = textMeasurer.measure(note.toString(), noteStyle)
                        drawText(
                            textLayoutResult = noteResult,
                            topLeft = Offset(
                                x = c * cellSize + nc * noteSize + (noteSize - noteResult.size.width) / 2f,
                                y = r * cellSize + nr * noteSize + (noteSize - noteResult.size.height) / 2f
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCellBackgrounds(
    game: SudokuGame,
    selectedRow: Int,
    selectedCol: Int,
    cellSize: Float,
    highlightErrors: Boolean
) {
    // Background
    drawRect(color = SudokuColors.GridBackground, size = size)

    // Highlight selected row/column
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
        // 3x3 box highlight
        val boxRow = (selectedRow / 3) * 3
        val boxCol = (selectedCol / 3) * 3
        drawRect(
            color = SudokuColors.HighlightBox,
            topLeft = Offset(boxCol * cellSize, boxRow * cellSize),
            size = Size(cellSize * 3, cellSize * 3)
        )
        // Selected cell
        drawRect(
            color = SudokuColors.SelectedCell,
            topLeft = Offset(selectedCol * cellSize, selectedRow * cellSize),
            size = Size(cellSize, cellSize)
        )
    }

    // Highlight same numbers
    if (selectedRow >= 0 && selectedCol >= 0) {
        val selectedValue = game.board[selectedRow][selectedCol]
        if (selectedValue != 0) {
            for (r in 0 until 9) {
                for (c in 0 until 9) {
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
        for (r in 0 until 9) {
            for (c in 0 until 9) {
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
    // Thin lines
    for (i in 0..9) {
        val pos = i * cellSize
        val strokeWidth = if (i % 3 == 0) 3f else 1f
        val color = if (i % 3 == 0) SudokuColors.ThickLine else SudokuColors.ThinLine

        // Horizontal
        drawLine(color, Offset(0f, pos), Offset(size.width, pos), strokeWidth)
        // Vertical
        drawLine(color, Offset(pos, 0f), Offset(pos, size.height), strokeWidth)
    }
}

private fun Float.toSp() = this.sp / 3.5f  // approximate density scaling for watch
