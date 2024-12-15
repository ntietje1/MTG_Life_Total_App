package ui.dialog.color

import androidx.compose.ui.graphics.Color

data class ColorDialogState(
    val oldColor: Color = Color.White,
    val hue: Float = 0f,
    val saturation: Float = 0f,
    val value: Float = 0f,
    val newColor: Color = Color.White
)