package domain.system

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

actual class SystemManager {
    actual companion object {
        @Composable
        actual fun getAnimationCorrectionFactor(): Float {
            return 1f
        }
    }

    @Composable
    actual fun updateSystemBarsColors(isDarkTheme: Boolean) {
        // no op
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
}