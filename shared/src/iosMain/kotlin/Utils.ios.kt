
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication


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
        UIApplication.sharedApplication.idleTimerDisabled = keepScreenOn
//        UIApplication.sharedApplication.idleTimerDisabled = true
        onDispose {
            UIApplication.sharedApplication.idleTimerDisabled = false
        }
    }
}