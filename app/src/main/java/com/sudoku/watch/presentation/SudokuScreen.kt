package com.sudoku.watch.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    GameScreenContent(state, game, { row, col -> viewModel.onCellTap(row, col) } ,{ row, col -> viewModel.onCellLongPress(row, col) })
}

@Composable
private fun GameScreenContent(
    state: GameUiState,
    game: SudokuGame,
    onCellTap: (Int, Int) -> Unit,
    onCellLongPress: (Int, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SudokuColors.Background),
        contentAlignment = Alignment.Center
    ) {
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
                .padding(36.dp)
        )
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
            onCellLongPress = { _, _ -> }
        )
    }
}
