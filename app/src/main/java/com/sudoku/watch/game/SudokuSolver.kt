package com.sudoku.watch.game

/**
 * 6×6 Sudoku solver using backtracking algorithm.
 * Uses 2×3 boxes (2 rows × 3 columns).
 */
object SudokuSolver {

    fun solve(board: Array<IntArray>): Boolean {
        val emptyCell = findEmptyCell(board) ?: return true
        val (row, col) = emptyCell

        for (num in 1..6) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num
                if (solve(board)) return true
                board[row][col] = 0
            }
        }
        return false
    }

    fun countSolutions(board: Array<IntArray>, limit: Int = 2): Int {
        val emptyCell = findEmptyCell(board) ?: return 1
        val (row, col) = emptyCell
        var count = 0

        for (num in 1..6) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num
                count += countSolutions(board, limit - count)
                board[row][col] = 0
                if (count >= limit) break
            }
        }
        return count
    }

    fun isValid(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        // Check row
        if (num in board[row]) return false

        // Check column
        for (r in 0 until 6) {
            if (board[r][col] == num) return false
        }

        // Check 2×3 box (2 rows, 3 columns)
        val boxRow = (row / 2) * 2
        val boxCol = (col / 3) * 3
        for (r in boxRow until boxRow + 2) {
            for (c in boxCol until boxCol + 3) {
                if (board[r][c] == num) return false
            }
        }
        return true
    }

    private fun findEmptyCell(board: Array<IntArray>): Pair<Int, Int>? {
        for (r in 0 until 6) {
            for (c in 0 until 6) {
                if (board[r][c] == 0) return r to c
            }
        }
        return null
    }
}
