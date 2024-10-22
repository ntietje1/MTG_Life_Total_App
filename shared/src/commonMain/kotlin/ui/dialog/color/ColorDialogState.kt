package ui.dialog.color

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class ColorDialogState(
    val hue: Float = 360f,
    val saturation: Float = 1f,
    val value: Float = 1f,
    val satValPosn: Offset = Offset(0f, 0f),
    val newColor: Color = Color.Black
)