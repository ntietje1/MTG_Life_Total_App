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
    displayLarge = defaultTextStyle(),
    displayMedium = defaultTextStyle(),
    displaySmall = defaultTextStyle(),
    headlineLarge = defaultTextStyle(),
    headlineMedium = defaultTextStyle(),
    headlineSmall = defaultTextStyle(),
    titleLarge = defaultTextStyle(),
    titleMedium = defaultTextStyle(),
    titleSmall = defaultTextStyle(),
    bodyLarge = defaultTextStyle(),
    bodyMedium = defaultTextStyle(),
    bodySmall = defaultTextStyle(),
    labelLarge = defaultTextStyle(),
    labelMedium = defaultTextStyle(),
    labelSmall = defaultTextStyle(),
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

@Composable
expect fun defaultTextStyle(fontFamily: FontFamily = RobotoFontFamily()): TextStyle