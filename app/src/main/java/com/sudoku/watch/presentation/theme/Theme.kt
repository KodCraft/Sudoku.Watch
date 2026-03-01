package com.sudoku.watch.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Colors

private val WatchColors = Colors(
    primary = SudokuColors.ButtonPrimary,
    primaryVariant = SudokuColors.ButtonPrimary,
    secondary = SudokuColors.Success,
    secondaryVariant = SudokuColors.Success,
    error = SudokuColors.Error,
    onPrimary = SudokuColors.ButtonText,
    onSecondary = SudokuColors.ButtonText,
    onError = SudokuColors.ButtonText,
    background = SudokuColors.Background,
    onBackground = SudokuColors.TextPrimary,
    surface = SudokuColors.GridBackground,
    onSurface = SudokuColors.TextPrimary,
    onSurfaceVariant = SudokuColors.TextSecondary
)

@Composable
fun SudokuWatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WatchColors,
        content = content
    )
}
