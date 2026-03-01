package com.sudoku.watch.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Clean light color palette for watch Sudoku.
 */
object SudokuColors {
    // Grid
    val GridBackground = Color(0xFFFAFAFA)
    val ThickLine = Color(0xFF37474F)
    val ThinLine = Color(0xFFCFD8DC)

    // Cell highlights
    val SelectedCell = Color(0xFF90CAF9).copy(alpha = 0.55f)
    val InputModeBorder = Color(0xFFFFA726)
    val HighlightLine = Color(0xFFBBDEFB).copy(alpha = 0.45f)
    val HighlightBox = Color(0xFFBBDEFB).copy(alpha = 0.25f)
    val SameNumber = Color(0xFF90CAF9).copy(alpha = 0.35f)
    val ErrorBackground = Color(0xFFFFCDD2).copy(alpha = 0.6f)

    // Numbers
    val OriginalNumber = Color(0xFF263238)
    val UserNumber = Color(0xFF1565C0)
    val NoteText = Color(0xFF78909C)
    val Error = Color(0xFFD32F2F)

    // UI
    val Background = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)
    val TimerText = Color(0xFF546E7A)
    val Success = Color(0xFF43A047)
    val CongratsGold = Color(0xFFF57C00)
    val ButtonPrimary = Color(0xFF1E88E5)
    val ButtonDanger = Color(0xFFE53935)
    val ButtonSecondary = Color(0xFFECEFF1)
    val ButtonText = Color(0xFFFFFFFF)
}
