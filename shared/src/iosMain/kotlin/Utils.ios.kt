
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect


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

@Composable
actual fun keepScreenOn(keepScreenOn: Boolean) {
    DisposableEffect(keepScreenOn) {
        UIApplication.shared.isIdleTimerDisabled = keepScreenOn
//        UIApplication.sharedApplication.idleTimerDisabled = true
        onDispose {
            UIApplication.shared.isIdleTimerDisabled = false
        }
    }
}