package theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.roboto_bold
import lifelinked.shared.generated.resources.roboto_medium
import lifelinked.shared.generated.resources.roboto_regular
import org.jetbrains.compose.resources.Font

@Composable
fun Typography() = Typography(
    bodyLarge = TextStyle(
        fontFamily = RobotoFontFamily(), //TODO: gets overridden by phone's set font
//        fontWeight = FontWeight.Normal,
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

@Composable
fun Int.scaledSp(): TextUnit {
    val value = this.toFloat()
    return with(LocalDensity.current) {
        val fontScale = this.fontScale
        val textSize = value / fontScale
        textSize.sp
    }
}

@Composable
fun Float.scaledSp(): TextUnit {
    val value = this
    return with(LocalDensity.current) {
        val fontScale = this.fontScale
        val textSize = value / fontScale
        textSize.sp
    }
}

val Int.scaledSp: TextUnit
    @Composable get() =  scaledSp()

val Float.scaledSp: TextUnit
    @Composable get() =  scaledSp()

@Composable
fun RobotoFontFamily(): FontFamily {
    return FontFamily(
        Font(resource = Res.font.roboto_regular, weight = FontWeight.Normal, style = FontStyle.Normal),
        Font(resource = Res.font.roboto_medium, weight = FontWeight.Medium, style = FontStyle.Normal),
        Font(resource = Res.font.roboto_bold, weight = FontWeight.Bold, style = FontStyle.Normal),
    )
}

@Composable
expect fun textShadowStyle(fontFamily: FontFamily = RobotoFontFamily()): TextStyle