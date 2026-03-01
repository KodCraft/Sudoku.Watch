package com.sudoku.watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.rememberSwipeToDismissBoxState
import com.sudoku.watch.presentation.*
import com.sudoku.watch.presentation.theme.SudokuWatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SudokuWatchTheme {
                val viewModel: SudokuViewModel = viewModel()
                val state by viewModel.uiState.collectAsState()

                when (state.screen) {
                    Screen.MENU -> {
                        MenuScreen(
                            onStartGame = { difficulty ->
                                viewModel.startNewGame(difficulty)
                            }
                        )
                    }

                    Screen.GAME -> {
                        val swipeState = rememberSwipeToDismissBoxState()
                        SwipeToDismissBox(
                            state = swipeState,
                            onDismissed = { viewModel.goToMenu() }
                        ) { isBackground ->
                            if (!isBackground) {
                                GameScreen(viewModel = viewModel)
                            }
                        }
                    }

                    Screen.CONGRATS -> {
                        val swipeState = rememberSwipeToDismissBoxState()
                        SwipeToDismissBox(
                            state = swipeState,
                            onDismissed = { viewModel.goToMenu() }
                        ) { isBackground ->
                            if (!isBackground) {
                                CongratsScreen(
                                    elapsedSeconds = state.elapsedSeconds,
                                    difficulty = state.game?.difficulty,
                                    errorsMade = state.errorsMade,
                                    onNewGame = { viewModel.startNewGame(state.game?.difficulty ?: return@CongratsScreen) },
                                    onMenu = { viewModel.goToMenu() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
