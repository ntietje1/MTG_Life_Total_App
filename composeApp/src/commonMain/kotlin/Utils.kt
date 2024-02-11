import androidx.compose.runtime.Composable

@Composable
expect fun updateSystemBarsColors(isDarkTheme: Boolean)

@Composable
expect fun getAnimationCorrectionFactor(): Float

expect fun legacyMonarchyIndicator(): Boolean