import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
expect fun updateSystemBarsColors(isDarkTheme: Boolean)

@Composable
expect fun getAnimationCorrectionFactor(): Float

expect fun legacyMonarchyIndicator(): Boolean

expect val robotoFontFamily: FontFamily
