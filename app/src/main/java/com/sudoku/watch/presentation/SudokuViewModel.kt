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
    val isInputMode: Boolean = false,
    val showErrors: Boolean = true,
    val elapsedSeconds: Long = 0,
    val isTimerRunning: Boolean = false,
    val showCongrats: Boolean = false,
    val errorsMade: Int = 0,
    val canUndo: Boolean = false,
    val lastMoveWasError: Boolean = false
)

enum class Screen {
    MENU, GAME, CONGRATS
}

class SudokuViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val moveHistory = mutableListOf<Array<IntArray>>()
    private companion object { const val MAX_HISTORY = 20 }

    fun startNewGame(difficulty: Difficulty) {
        val game = SudokuGenerator.generate(difficulty)
        moveHistory.clear()
        _uiState.update {
            it.copy(
                screen = Screen.GAME,
                game = game,
                selectedRow = -1,
                selectedCol = -1,
                isInputMode = false,
                elapsedSeconds = 0,
                isTimerRunning = true,
                showCongrats = false,
                errorsMade = 0,
                canUndo = false,
                lastMoveWasError = false
            )
        }
    }

    fun resetPuzzle() {
        val game = _uiState.value.game ?: return
        val resetBoard = Array(6) { game.puzzle[it].copyOf() }
        moveHistory.clear()
        _uiState.update {
            it.copy(
                game = game.copy(board = resetBoard),
                selectedRow = -1,
                selectedCol = -1,
                isInputMode = false,
                elapsedSeconds = 0,
                isTimerRunning = true,
                errorsMade = 0,
                canUndo = false,
                lastMoveWasError = false
            )
        }
    }

    fun undoMove() {
        if (moveHistory.isEmpty()) return
        val game = _uiState.value.game ?: return
        val previousBoard = moveHistory.removeAt(moveHistory.lastIndex)
        _uiState.update {
            it.copy(
                game = game.copy(board = previousBoard),
                isInputMode = false,
                canUndo = moveHistory.isNotEmpty(),
                lastMoveWasError = false
            )
        }
    }

    private fun pushHistory(board: Array<IntArray>) {
        moveHistory.add(Array(6) { board[it].copyOf() })
        if (moveHistory.size > MAX_HISTORY) moveHistory.removeAt(0)
    }

    /**
     * Tap on a cell:
     * - If in input mode on the SAME cell → increment number
     * - Otherwise → just select cell (exit input mode)
     */
    fun onCellTap(row: Int, col: Int) {
        val state = _uiState.value
        if (state.isInputMode && row == state.selectedRow && col == state.selectedCol) {
            incrementCell()
        } else {
            _uiState.update {
                it.copy(selectedRow = row, selectedCol = col, isInputMode = false)
            }
        }
    }

    /**
     * Long press on a cell → enter input mode.
     * Places 1 if the cell is empty, otherwise keeps current value for incrementing.
     */
    fun onCellLongPress(row: Int, col: Int) {
        val game = _uiState.value.game ?: return
        if (game.isOriginalCell(row, col)) return

        _uiState.update {
            it.copy(selectedRow = row, selectedCol = col, isInputMode = true)
        }

        // If cell is empty, place 1
        if (game.board[row][col] == 0) {
            pushHistory(game.board)
            placeNumber(row, col, 1)
        }
    }

    /**
     * Increment the current cell's value: 1→2→...→9→clear(0)→1→...
     */
    private fun incrementCell() {
        val state = _uiState.value
        val game = state.game ?: return
        val row = state.selectedRow
        val col = state.selectedCol
        if (row < 0 || col < 0) return
        if (game.isOriginalCell(row, col)) return

        val current = game.board[row][col]
        val next = if (current >= 6) 0 else current + 1

        pushHistory(game.board)
        if (next == 0) {
            val newBoard = game.copyBoard()
            newBoard[row][col] = 0
            _uiState.update { it.copy(game = game.copy(board = newBoard), canUndo = true, lastMoveWasError = false) }
        } else {
            placeNumber(row, col, next)
        }
    }

    private fun placeNumber(row: Int, col: Int, number: Int) {
        val game = _uiState.value.game ?: return

        val newBoard = game.copyBoard()
        newBoard[row][col] = number
        val updatedGame = game.copy(board = newBoard)

        val isError = number != game.solution[row][col]
        _uiState.update {
            it.copy(
                game = updatedGame,
                errorsMade = if (isError) it.errorsMade + 1 else it.errorsMade,
                canUndo = true,
                lastMoveWasError = isError
            )
        }

        if (updatedGame.isSolved) {
            _uiState.update {
                it.copy(
                    screen = Screen.CONGRATS,
                    isTimerRunning = false,
                    isInputMode = false,
                    showCongrats = true
                )
            }
        }
    }

    fun goToMenu() {
        _uiState.update {
            GameUiState()
        }
    }

    fun incrementTimer() {
        if (_uiState.value.isTimerRunning) {
            _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
        }
    }
}
