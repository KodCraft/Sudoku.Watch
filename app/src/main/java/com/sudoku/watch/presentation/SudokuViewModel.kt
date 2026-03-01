package com.sudoku.watch.presentation

import androidx.lifecycle.ViewModel
import com.sudoku.watch.game.Difficulty
import com.sudoku.watch.game.SudokuGame
import com.sudoku.watch.game.SudokuGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GameUiState(
    val screen: Screen = Screen.MENU,
    val game: SudokuGame? = null,
    val selectedRow: Int = -1,
    val selectedCol: Int = -1,
    val isNoteMode: Boolean = false,
    val showErrors: Boolean = true,
    val elapsedSeconds: Long = 0,
    val isTimerRunning: Boolean = false,
    val showCongrats: Boolean = false
)

enum class Screen {
    MENU, GAME, CONGRATS
}

class SudokuViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun startNewGame(difficulty: Difficulty) {
        val game = SudokuGenerator.generate(difficulty)
        _uiState.update {
            it.copy(
                screen = Screen.GAME,
                game = game,
                selectedRow = -1,
                selectedCol = -1,
                isNoteMode = false,
                elapsedSeconds = 0,
                isTimerRunning = true,
                showCongrats = false
            )
        }
    }

    fun selectCell(row: Int, col: Int) {
        _uiState.update { it.copy(selectedRow = row, selectedCol = col) }
    }

    fun enterNumber(number: Int) {
        val state = _uiState.value
        val game = state.game ?: return
        val row = state.selectedRow
        val col = state.selectedCol

        if (row < 0 || col < 0) return
        if (game.isOriginalCell(row, col)) return

        if (state.isNoteMode) {
            // Toggle note
            val notes = game.notes[row][col]
            if (number in notes) {
                notes.remove(number)
            } else {
                notes.add(number)
            }
            // Force recomposition by creating new game reference
            val newBoard = game.copyBoard()
            _uiState.update {
                it.copy(
                    game = game.copy(board = newBoard)
                )
            }
        } else {
            // Place number
            val newBoard = game.copyBoard()
            newBoard[row][col] = number
            // Clear notes for this cell
            game.notes[row][col].clear()

            val updatedGame = game.copy(board = newBoard)
            _uiState.update {
                it.copy(game = updatedGame)
            }

            // Check if solved
            if (updatedGame.isSolved) {
                _uiState.update {
                    it.copy(
                        screen = Screen.CONGRATS,
                        isTimerRunning = false,
                        showCongrats = true
                    )
                }
            }
        }
    }

    fun clearCell() {
        val state = _uiState.value
        val game = state.game ?: return
        val row = state.selectedRow
        val col = state.selectedCol

        if (row < 0 || col < 0) return
        if (game.isOriginalCell(row, col)) return

        val newBoard = game.copyBoard()
        newBoard[row][col] = 0
        game.notes[row][col].clear()

        _uiState.update {
            it.copy(game = game.copy(board = newBoard))
        }
    }

    fun toggleNoteMode() {
        _uiState.update { it.copy(isNoteMode = !it.isNoteMode) }
    }

    fun goToMenu() {
        _uiState.update {
            GameUiState() // Reset to menu
        }
    }

    fun incrementTimer() {
        if (_uiState.value.isTimerRunning) {
            _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
        }
    }
}
