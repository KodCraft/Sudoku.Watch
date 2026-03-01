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
import com.sudoku.watch.presentation.components.NumberPicker
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
            .fillMaxWidth(0.7f)
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                when (difficulty) {
                    Difficulty.EASY -> SudokuColors.Success.copy(alpha = 0.8f)
                    Difficulty.MEDIUM -> SudokuColors.ButtonPrimary
                    Difficulty.HARD -> SudokuColors.ButtonDanger
                }
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
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
            .background(SudokuColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer display
        Text(
            text = formatTime(state.elapsedSeconds),
            fontSize = 11.sp,
            color = SudokuColors.TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )

        // Sudoku grid - takes most of the screen
        SudokuGrid(
            game = game,
            selectedRow = state.selectedRow,
            selectedCol = state.selectedCol,
            highlightErrors = state.showErrors,
            onCellSelected = { row, col -> viewModel.selectCell(row, col) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .weight(1f)
        )

        // Number picker at bottom
        if (state.selectedRow >= 0 && state.selectedCol >= 0 &&
            !game.isOriginalCell(state.selectedRow, state.selectedCol)
        ) {
            NumberPicker(
                onNumberSelected = { viewModel.enterNumber(it) },
                onClear = { viewModel.clearCell() },
                isNoteMode = state.isNoteMode,
                onToggleNoteMode = { viewModel.toggleNoteMode() }
            )
        }
    }
}

// ─── Congrats Screen ───

@Composable
fun CongratsScreen(
    elapsedSeconds: Long,
    onNewGame: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SudokuColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Solved!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = SudokuColors.Success
        )
        Text(
            text = formatTime(elapsedSeconds),
            fontSize = 14.sp,
            color = SudokuColors.TextSecondary,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(SudokuColors.ButtonPrimary)
                .clickable { onNewGame() }
                .padding(horizontal = 20.dp, vertical = 8.dp),
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
}

// ─── Utility ───

private fun formatTime(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
