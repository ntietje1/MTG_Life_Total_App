package theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

expect fun robotoFontFamily(): FontFamily

/**
 * Typography for the application
 */
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = robotoFontFamily(),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

/**
 * Scaled Sp that ignores system font scaling
 * @return scaled Sp value
 */
@Composable
fun Int.scaledSp(): TextUnit {
    val value = this.toFloat()
    return with(LocalDensity.current) {
        val fontScale = this.fontScale
        val textSize = value / fontScale
        textSize.sp
    }
}

/**
 * Scaled Sp that ignores system font scaling
 * @return scaled Sp value
 */
@Composable
fun Float.scaledSp(): TextUnit {
    val value = this
    return with(LocalDensity.current) {
        val fontScale = this.fontScale
        val textSize = value / fontScale
        textSize.sp
    }
}

/**
 * Convert int to scaled Sp
 * @return scaled Sp value
 */
val Int.scaledSp: TextUnit
    @Composable get() =  scaledSp()

/**
 * Convert float to scaled Sp
 * @return scaled Sp value
 */
val Float.scaledSp: TextUnit
    @Composable get() =  scaledSp()

/**
 * Text style with shadow effect
 */
val textShadowStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal
).copy(
    shadow = Shadow(
        color = generateShadow(),
        offset = Offset(2f, 2f),
        blurRadius = 8f
    )
)