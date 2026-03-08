package com.sudoku.watch.game

/**
 * Represents the state of a 6×6 Sudoku game with 2×3 boxes.
 */
data class SudokuGame(
    val puzzle: Array<IntArray>,
    val solution: Array<IntArray>,
    val board: Array<IntArray>,
    val difficulty: Difficulty,
    val notes: Array<Array<MutableSet<Int>>> = Array(6) { Array(6) { mutableSetOf() } }
) {
    val isComplete: Boolean
        get() = board.all { row -> row.all { it != 0 } }

    val isSolved: Boolean
        get() = board.indices.all { r ->
            board[r].indices.all { c -> board[r][c] == solution[r][c] }
        }

    fun isOriginalCell(row: Int, col: Int): Boolean = puzzle[row][col] != 0

    fun isCorrect(row: Int, col: Int): Boolean =
        board[row][col] == 0 || board[row][col] == solution[row][col]

    fun hasConflict(row: Int, col: Int): Boolean {
        val value = board[row][col]
        if (value == 0) return false

        // Check row
        for (c in 0 until 6) {
            if (c != col && board[row][c] == value) return true
        }
        // Check column
        for (r in 0 until 6) {
            if (r != row && board[r][col] == value) return true
        }
        // Check 2×3 box
        val boxRow = (row / 2) * 2
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 2) {
            for (c in boxCol until boxCol + 3) {
                if (r != row && c != col && board[r][c] == value) return true
            }
        }
        return false
    }

    fun countErrors(): Int {
        var errors = 0
        for (r in 0 until 6) {
            for (c in 0 until 6) {
                if (board[r][c] != 0 && board[r][c] != solution[r][c]) errors++
            }
        }
        return errors
    }

    fun copyBoard(): Array<IntArray> = Array(6) { board[it].copyOf() }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SudokuGame) return false
        return puzzle.contentDeepEquals(other.puzzle) &&
                board.contentDeepEquals(other.board) &&
                difficulty == other.difficulty
    }

    override fun hashCode(): Int {
        var result = puzzle.contentDeepHashCode()
        result = 31 * result + board.contentDeepHashCode()
        result = 31 * result + difficulty.hashCode()
        return result
    }
}

enum class Difficulty(val cellsToRemove: Int, val label: String) {
    EASY(14, "Easy"),
    MEDIUM(18, "Medium"),
    HARD(22, "Hard");
}
