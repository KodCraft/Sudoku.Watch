package com.sudoku.watch.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.sudoku.watch.presentation.theme.SudokuColors

@Composable
fun NumberPicker(
    onNumberSelected: (Int) -> Unit,
    onClear: () -> Unit,
    isNoteMode: Boolean,
    onToggleNoteMode: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = SudokuColors.PickerBackground,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            )
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1: 1-5
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (num in 1..5) {
                NumberButton(
                    number = num,
                    isNoteMode = isNoteMode,
                    enabled = enabled,
                    onClick = { onNumberSelected(num) }
                )
            }
        }

        Spacer(modifier = Modifier.height(1.dp))

        // Row 2: 6-9, note toggle, clear
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (num in 6..9) {
                NumberButton(
                    number = num,
                    isNoteMode = isNoteMode,
                    enabled = enabled,
                    onClick = { onNumberSelected(num) }
                )
            }
            // Note mode toggle (compact)
            ActionButton(
                text = "N",
                color = if (isNoteMode) SudokuColors.NoteModeActive else SudokuColors.NoteModeInactive,
                enabled = true,
                onClick = onToggleNoteMode
            )
            // Clear
            ActionButton(
                text = "✕",
                color = SudokuColors.ClearButton,
                enabled = enabled,
                onClick = onClear
            )
        }
    }
}

@Composable
private fun NumberButton(
    number: Int,
    isNoteMode: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        !enabled -> SudokuColors.ButtonDisabled
        isNoteMode -> SudokuColors.NoteNumberButton
        else -> SudokuColors.NumberButton
    }
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (enabled) color else SudokuColors.ButtonDisabled)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}
