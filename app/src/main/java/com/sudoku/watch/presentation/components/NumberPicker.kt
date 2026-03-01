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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = SudokuColors.PickerBackground,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            )
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row: 1-5
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (num in 1..5) {
                NumberButton(
                    number = num,
                    isNoteMode = isNoteMode,
                    onClick = { onNumberSelected(num) }
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Bottom row: 6-9 + controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (num in 6..9) {
                NumberButton(
                    number = num,
                    isNoteMode = isNoteMode,
                    onClick = { onNumberSelected(num) }
                )
            }
            // Clear button
            ActionButton(
                text = "✕",
                color = SudokuColors.ClearButton,
                onClick = onClear
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Note mode toggle
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isNoteMode) SudokuColors.NoteModeActive else SudokuColors.NoteModeInactive)
                .clickable { onToggleNoteMode() }
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isNoteMode) "📝 Notes ON" else "✏️ Notes OFF",
                fontSize = 10.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun NumberButton(
    number: Int,
    isNoteMode: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(if (isNoteMode) SudokuColors.NoteNumberButton else SudokuColors.NumberButton)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
