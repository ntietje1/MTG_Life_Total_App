import android.app.Activity
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import com.hypeapps.lifelinked.R

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

actual fun legacyMonarchyIndicator(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
}

actual val robotoFontFamily: FontFamily = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal),
//    Font(R.font.roboto_medium, FontWeight.Medium),
//    Font(R.font.roboto_bold, FontWeight.Bold)
)