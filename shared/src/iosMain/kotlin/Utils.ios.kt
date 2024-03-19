import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Typeface
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface


@Composable
actual fun updateSystemBarsColors(isDarkTheme: Boolean) {
    // no op
}

@Composable
actual fun getAnimationCorrectionFactor(): Float {
    return 1f
}

actual fun legacyMonarchyIndicator(): Boolean {
    return false
}

private fun loadCustomFont(name: String, fontStyle: FontStyle): Typeface {
    return Typeface.makeFromName(name, fontStyle)
}

actual fun robotoFontFamily(): FontFamily = FontFamily(
    Typeface(loadCustomFont("roboto", FontStyle.NORMAL))
)