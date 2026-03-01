package com.sudoku.watch.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Color palette optimized for AMOLED watch displays.
 * Dark theme with high-contrast elements for small screens.
 */
object SudokuColors {
    // Grid
    val GridBackground = Color(0xFF1A1A2E)
    val ThickLine = Color(0xFFE0E0E0)
    val ThinLine = Color(0xFF4A4A5A)

    // Cell highlights
    val SelectedCell = Color(0xFF3D5AFE).copy(alpha = 0.5f)
    val InputModeBorder = Color(0xFFFFD54F)
    val HighlightLine = Color(0xFF1E3A5F).copy(alpha = 0.25f)
    val HighlightBox = Color(0xFF1E3A5F).copy(alpha = 0.15f)
    val SameNumber = Color(0xFF3D5AFE).copy(alpha = 0.2f)
    val ErrorBackground = Color(0xFFFF1744).copy(alpha = 0.15f)

    // Numbers
    val OriginalNumber = Color(0xFFFFFFFF)
    val UserNumber = Color(0xFF64B5F6)
    val NoteText = Color(0xFFB0BEC5)
    val Error = Color(0xFFFF5252)

    // Number picker
    val PickerBackground = Color(0xFF16213E)
    val NumberButton = Color(0xFF3D5AFE)
    val NoteNumberButton = Color(0xFF7C4DFF)
    val ClearButton = Color(0xFFFF5252)
    val ButtonDisabled = Color(0xFF2A2A3E)

    // Note mode
    val NoteModeActive = Color(0xFF7C4DFF)
    val NoteModeInactive = Color(0xFF37474F)

    // UI
    val Background = Color(0xFF0A0A1A)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB0BEC5)
    val TimerText = Color(0xFFCFD8DC)
    val Success = Color(0xFF00E676)
    val CongratsGold = Color(0xFFFFD54F)
    val ButtonPrimary = Color(0xFF3D5AFE)
    val ButtonDanger = Color(0xFFFF5252)
}
