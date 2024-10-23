package di
import androidx.compose.runtime.Composable

@Composable
expect fun updateSystemBarsColors(isDarkTheme: Boolean)

@Composable
expect fun getAnimationCorrectionFactor(): Float

@Composable
expect fun keepScreenOn(keepScreenOn: Boolean)

expect fun legacyMonarchyIndicator(): Boolean