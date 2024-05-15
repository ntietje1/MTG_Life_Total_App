
import androidx.compose.runtime.Composable


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