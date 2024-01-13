import android.app.Activity
import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

@Composable
actual fun getAnimationCorrectionFactor(): Float {
    val context = LocalView.current.context
    return Settings.Global.getFloat(
        context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f
    )
}