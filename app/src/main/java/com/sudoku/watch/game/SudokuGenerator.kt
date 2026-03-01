package com.sudoku.watch.game

import kotlin.random.Random

/**
 * Generates valid Sudoku puzzles with unique solutions.
 */
object SudokuGenerator {

    /**
     * Creates a new Sudoku game with the specified difficulty.
     */
    fun generate(difficulty: Difficulty): SudokuGame {
        val solution = generateFullBoard()
        val puzzle = createPuzzle(solution, difficulty)
        val board = Array(9) { puzzle[it].copyOf() }

        return SudokuGame(
            puzzle = puzzle,
            solution = solution,
            board = board,
            difficulty = difficulty
        )
    }

    /**
     * Generates a complete valid Sudoku board using backtracking
     * with randomized number ordering for variety.
     */
    private fun generateFullBoard(): Array<IntArray> {
        val board = Array(9) { IntArray(9) }
        fillBoard(board)
        return board
    }

    private fun fillBoard(board: Array<IntArray>): Boolean {
        val emptyCell = findEmptyCell(board) ?: return true
        val (row, col) = emptyCell
        val numbers = (1..9).shuffled()

        for (num in numbers) {
            if (SudokuSolver.isValid(board, row, col, num)) {
                board[row][col] = num
                if (fillBoard(board)) return true
                board[row][col] = 0
            }
        }
        return false
    }

    /**
     * Creates a puzzle by removing cells from a complete board.
     * Ensures the puzzle has a unique solution.
     */
    private fun createPuzzle(
        solution: Array<IntArray>,
        difficulty: Difficulty
    ): Array<IntArray> {
        val puzzle = Array(9) { solution[it].copyOf() }
        val positions = mutableListOf<Pair<Int, Int>>()

        for (r in 0 until 9) {
            for (c in 0 until 9) {
                positions.add(r to c)
            }
        }
        positions.shuffle()

        var removed = 0
        for ((r, c) in positions) {
            if (removed >= difficulty.cellsToRemove) break

            val backup = puzzle[r][c]
            puzzle[r][c] = 0

            // Check unique solution
            val testBoard = Array(9) { puzzle[it].copyOf() }
            if (SudokuSolver.countSolutions(testBoard) == 1) {
                removed++
            } else {
                puzzle[r][c] = backup
            }
        }

        return puzzle
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
