package com.sudoku.watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
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
                        GameScreen(viewModel = viewModel)
                    }

                    Screen.CONGRATS -> {
                        CongratsScreen(
                            elapsedSeconds = state.elapsedSeconds,
                            onNewGame = { viewModel.goToMenu() }
                        )
                    }
                }
            }
        }
    }
}
