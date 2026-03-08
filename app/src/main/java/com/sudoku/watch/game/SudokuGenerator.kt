package com.sudoku.watch.game

/**
 * Generates valid 6×6 Sudoku puzzles with unique solutions.
 */
object SudokuGenerator {

    fun generate(difficulty: Difficulty): SudokuGame {
        val solution = generateFullBoard()
        val puzzle = createPuzzle(solution, difficulty)
        val board = Array(6) { puzzle[it].copyOf() }

        return SudokuGame(
            puzzle = puzzle,
            solution = solution,
            board = board,
            difficulty = difficulty
        )
    }

    private fun generateFullBoard(): Array<IntArray> {
        val board = Array(6) { IntArray(6) }
        fillBoard(board)
        return board
    }

    private fun fillBoard(board: Array<IntArray>): Boolean {
        val emptyCell = findEmptyCell(board) ?: return true
        val (row, col) = emptyCell
        val numbers = (1..6).shuffled()

        for (num in numbers) {
            if (SudokuSolver.isValid(board, row, col, num)) {
                board[row][col] = num
                if (fillBoard(board)) return true
                board[row][col] = 0
            }
        }
        return false
    }

    private fun createPuzzle(
        solution: Array<IntArray>,
        difficulty: Difficulty
    ): Array<IntArray> {
        val puzzle = Array(6) { solution[it].copyOf() }
        val positions = mutableListOf<Pair<Int, Int>>()

        for (r in 0 until 6) {
            for (c in 0 until 6) {
                positions.add(r to c)
            }
        }
        positions.shuffle()

        var removed = 0
        for ((r, c) in positions) {
            if (removed >= difficulty.cellsToRemove) break

            val backup = puzzle[r][c]
            puzzle[r][c] = 0

            val testBoard = Array(6) { puzzle[it].copyOf() }
            if (SudokuSolver.countSolutions(testBoard) == 1) {
                removed++
            } else {
                puzzle[r][c] = backup
            }
        }

        return puzzle
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
