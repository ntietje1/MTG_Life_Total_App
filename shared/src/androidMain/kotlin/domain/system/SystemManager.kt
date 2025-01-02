package domain.system

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

actual class SystemManager {
    actual companion object {
        @Composable
        actual fun getAnimationCorrectionFactor(): Float {
            val context = LocalView.current.context
            return remember {
                Settings.Global.getFloat(
                    context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f
                )
            }
        }

        @SuppressLint("ComposableNaming")
        @Composable
        actual fun updateSystemBarsColors(isDarkTheme: Boolean) {
            val view = LocalView.current
            val backgroundColor = MaterialTheme.colorScheme.background.toArgb()
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    val insets = WindowCompat.getInsetsController(window, view)
                    window.statusBarColor = backgroundColor
                    window.navigationBarColor = backgroundColor
                    insets.isAppearanceLightStatusBars = !isDarkTheme
                    insets.isAppearanceLightNavigationBars = !isDarkTheme
                }
            }
        }


        actual fun legacyMonarchyIndicator(): Boolean {
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        }


        @SuppressLint("ComposableNaming")
        @Composable
        actual fun keepScreenOn(keepScreenOn: Boolean) {
            val activity = LocalContext.current as Activity
            val flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON

            DisposableEffect(keepScreenOn) {
                if (keepScreenOn) {
                    activity.window.addFlags(flag)
                } else {
                    activity.window.clearFlags(flag)
                }
                onDispose {
                    activity.window.clearFlags(flag)
                }
            }
        }
    }
}