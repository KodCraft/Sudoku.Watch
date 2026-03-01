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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Text
import com.sudoku.watch.game.Difficulty
import com.sudoku.watch.presentation.components.SudokuGrid
import com.sudoku.watch.presentation.theme.SudokuColors
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
                    Difficulty.EASY -> SudokuColors.Success.copy(alpha = 0.8f)
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
            color = SudokuColors.TextPrimary,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SudokuColors.Background)
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer display
        Text(
            text = formatTime(state.elapsedSeconds),
            fontSize = 12.sp,
            color = SudokuColors.TimerText,
            modifier = Modifier.padding(top = 6.dp)
        )

        // Sudoku grid
        SudokuGrid(
            game = game,
            selectedRow = state.selectedRow,
            selectedCol = state.selectedCol,
            isInputMode = state.isInputMode,
            highlightErrors = state.showErrors,
            onCellTap = { row, col -> viewModel.onCellTap(row, col) },
            onCellLongPress = { row, col -> viewModel.onCellLongPress(row, col) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .weight(1f)
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
                    color = SudokuColors.TextPrimary
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
                    .background(SudokuColors.NoteModeInactive)
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
