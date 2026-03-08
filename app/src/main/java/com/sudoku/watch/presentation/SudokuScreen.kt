package com.sudoku.watch.presentation

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Text
import com.sudoku.watch.game.Difficulty
import com.sudoku.watch.game.SudokuGame
import com.sudoku.watch.presentation.components.SudokuGrid
import com.sudoku.watch.presentation.theme.SudokuColors
import com.sudoku.watch.presentation.theme.SudokuWatchTheme
import kotlinx.coroutines.delay

// ─── Main Menu ───

@Composable
fun MenuScreen(
    onStartGame: (Difficulty) -> Unit
) {
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SudokuColors.Background),
        state = listState,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text(
                text = "SUDOKU",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SudokuColors.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        for (difficulty in Difficulty.entries) {
            item {
                DifficultyButton(
                    difficulty = difficulty,
                    onClick = { onStartGame(difficulty) }
                )
            }
        }
    }
}

@Composable
private fun DifficultyButton(
    difficulty: Difficulty,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.65f)
            .padding(vertical = 3.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                when (difficulty) {
                    Difficulty.EASY -> SudokuColors.Success
                    Difficulty.MEDIUM -> SudokuColors.ButtonPrimary
                    Difficulty.HARD -> SudokuColors.ButtonDanger
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = difficulty.label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = SudokuColors.ButtonText,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Game Screen ───

@Composable
fun GameScreen(
    viewModel: SudokuViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val game = state.game ?: return

    // Timer
    LaunchedEffect(state.isTimerRunning) {
        while (state.isTimerRunning) {
            delay(1000L)
            viewModel.incrementTimer()
        }
    }

    val haptic = LocalHapticFeedback.current
    val vibrator = LocalContext.current.getSystemService<Vibrator>()

    // Vibrate on error
    LaunchedEffect(state.lastMoveWasError, state.errorsMade) {
        if (state.lastMoveWasError) {
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    // Vibrate on solve
    LaunchedEffect(state.screen) {
        if (state.screen == Screen.CONGRATS) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 80, 50, 80, 120), -1))
        }
    }

    GameScreenContent(
        state = state,
        game = game,
        onCellTap = { row, col ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.onCellTap(row, col)
        },
        onCellLongPress = { row, col ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onCellLongPress(row, col)
        },
        onUndo = { viewModel.undoMove() },
        onReset = { viewModel.resetPuzzle() }
    )
}

@Composable
private fun GameScreenContent(
    state: GameUiState,
    game: SudokuGame,
    onCellTap: (Int, Int) -> Unit,
    onCellLongPress: (Int, Int) -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    if (showResetConfirm) {
        Dialog(onDismissRequest = { showResetConfirm = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SudokuColors.Background)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reset puzzle?",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SudokuColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "All progress will be lost",
                    fontSize = 11.sp,
                    color = SudokuColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SudokuColors.ButtonSecondary)
                            .clickable { showResetConfirm = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SudokuColors.TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SudokuColors.ButtonDanger)
                            .clickable {
                                showResetConfirm = false
                                onReset()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Reset",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SudokuColors.ButtonText
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SudokuColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Timer
        Text(
            text = formatTime(state.elapsedSeconds),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = SudokuColors.TimerText,
            textAlign = TextAlign.Center
        )

        // Grid
        SudokuGrid(
            game = game,
            selectedRow = state.selectedRow,
            selectedCol = state.selectedCol,
            isInputMode = state.isInputMode,
            highlightErrors = state.showErrors,
            onCellTap = { row, col -> onCellTap(row, col) },
            onCellLongPress = { row, col -> onCellLongPress(row, col) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
        )

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            // Undo
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.canUndo) SudokuColors.ButtonSecondary
                        else SudokuColors.ButtonSecondary.copy(alpha = 0.4f)
                    )
                    .then(if (state.canUndo) Modifier.clickable { onUndo() } else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u21A9",
                    fontSize = 14.sp,
                    color = if (state.canUndo) SudokuColors.TextPrimary
                    else SudokuColors.TextSecondary.copy(alpha = 0.5f)
                )
            }
            // Reset
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(SudokuColors.ButtonSecondary)
                    .clickable { showResetConfirm = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u21BB",
                    fontSize = 14.sp,
                    color = SudokuColors.TextPrimary
                )
            }
        }
    }
}

// ─── Congrats Screen ───

@Composable
fun CongratsScreen(
    elapsedSeconds: Long,
    difficulty: Difficulty?,
    errorsMade: Int,
    onNewGame: () -> Unit,
    onMenu: () -> Unit
) {
    ScalingLazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SudokuColors.Background),
        state = rememberScalingLazyListState(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Text(
                text = "Solved!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SudokuColors.CongratsGold
            )
        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }
        item {
            Text(
                text = formatTime(elapsedSeconds),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = SudokuColors.TextPrimary
            )
        }
        item {
            Text(
                text = "${difficulty?.label ?: ""} · ${if (errorsMade == 0) "No errors" else "$errorsMade error${if (errorsMade > 1) "s" else ""}"}",
                fontSize = 11.sp,
                color = SudokuColors.TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SudokuColors.ButtonPrimary)
                    .clickable { onNewGame() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "New Game",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SudokuColors.ButtonText
                )
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(top = 4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SudokuColors.ButtonSecondary)
                    .clickable { onMenu() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Menu",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SudokuColors.TextPrimary
                )
            }
        }
    }
}

// ─── Utility ───

private fun formatTime(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

@Composable
@Preview(device = "id:wearos_small_round", showSystemUi = true)
fun GameScreenPreview() {
    val samplePuzzle = arrayOf(
        intArrayOf(0, 2, 0, 5, 0, 4),
        intArrayOf(0, 0, 3, 0, 2, 0),
        intArrayOf(2, 0, 0, 0, 0, 5),
        intArrayOf(4, 0, 0, 0, 0, 1),
        intArrayOf(0, 5, 0, 4, 0, 0),
        intArrayOf(3, 0, 4, 0, 5, 0)
    )
    val sampleSolution = arrayOf(
        intArrayOf(1, 2, 6, 5, 3, 4),
        intArrayOf(5, 4, 3, 1, 2, 6),
        intArrayOf(2, 1, 5, 6, 4, 3),  // Note: This is simplified sample data
        intArrayOf(4, 6, 2, 3, 1, 5),  // Note: This is simplified sample data
        intArrayOf(6, 5, 1, 4, 3, 2),  // Note: This is simplified sample data
        intArrayOf(3, 3, 4, 2, 5, 1)   // Note: This is simplified sample data
    )
    val board = Array(6) { samplePuzzle[it].copyOf() }
    board[0][0] = 1

    SudokuWatchTheme {
        GameScreenContent(
            state = GameUiState(
                selectedRow = 0,
                selectedCol = 0,
                elapsedSeconds = 42
            ),
            game = SudokuGame(
                puzzle = samplePuzzle,
                solution = sampleSolution,
                board = board,
                difficulty = Difficulty.EASY
            ),
            onCellTap = { _, _ -> },
            onCellLongPress = { _, _ -> },
            onUndo = {},
            onReset = {}
        )
    }
}
