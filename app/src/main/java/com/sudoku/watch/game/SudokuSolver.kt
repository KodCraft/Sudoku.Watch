package com.sudoku.watch.game

/**
 * Sudoku solver using backtracking algorithm.
 * Also used to validate puzzles have a unique solution.
 */
object SudokuSolver {

    /**
     * Solves the given board in-place.
     * @return true if a solution was found
     */
    fun solve(board: Array<IntArray>): Boolean {
        val emptyCell = findEmptyCell(board) ?: return true
        val (row, col) = emptyCell

        for (num in 1..9) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num
                if (solve(board)) return true
                board[row][col] = 0
            }
        }
        return false
    }

    /**
     * Counts solutions up to a limit (used for unique-solution validation).
     * @return number of solutions found (capped at [limit])
     */
    fun countSolutions(board: Array<IntArray>, limit: Int = 2): Int {
        val emptyCell = findEmptyCell(board) ?: return 1
        val (row, col) = emptyCell
        var count = 0

        for (num in 1..9) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num
                count += countSolutions(board, limit - count)
                board[row][col] = 0
                if (count >= limit) break
            }
        }
        return count
    }

    /**
     * Checks if placing [num] at [row],[col] is valid.
     */
    fun isValid(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        // Check row
        if (num in board[row]) return false

        // Check column
        for (r in 0 until 9) {
            if (board[r][col] == num) return false
        }

        // Check 3x3 box
        val boxRow = (row / 3) * 3
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 3) {
            for (c in boxCol until boxCol + 3) {
                if (board[r][c] == num) return false
            }
        }
        return true
    }

    private fun findEmptyCell(board: Array<IntArray>): Pair<Int, Int>? {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (board[r][c] == 0) return r to c
            }
        }
        return null
    }
}
