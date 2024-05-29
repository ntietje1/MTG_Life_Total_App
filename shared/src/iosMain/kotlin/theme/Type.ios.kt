package theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

@Composable
actual fun textShadowStyle(fontFamily: FontFamily): TextStyle {
    return TextStyle(
        fontFamily = fontFamily,
        shadow = Shadow(
            color = generateShadow(),
            offset = Offset(2f, 2f),
            blurRadius = 8f
        )
    )
}