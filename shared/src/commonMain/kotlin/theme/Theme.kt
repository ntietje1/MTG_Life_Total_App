package theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Black,
    background = Black,
    onPrimary = White,
    surface = Black.copy(alpha = 0.1f),
    onSurface = White.copy(alpha = 0.2f),
)

private val LightColorScheme = lightColorScheme(
    primary = White,
    background = White,
    onPrimary = Black,
    surface = White.copy(alpha = 0.3f),
    onSurface = White.copy(alpha = 0.5f),
)

val LocalDimensions = staticCompositionLocalOf<Dimensions> {
    error("No dimensions provided")
}

class Dimensions(val screenWidth: Dp, val screenHeight: Dp) {
    val blurRadius = screenHeight / 50f
    val paddingTiny = screenWidth / 750f + screenHeight / 1000f
    val paddingSmall = screenWidth / 200f + screenHeight / 300f
    val paddingMedium = screenWidth / 100f + screenHeight / 150f
    val borderThin = screenWidth / 1000f + screenHeight / 1500f
    val borderSmall = screenWidth / 500f + screenHeight / 750f
    val borderMedium = screenWidth / 250f + screenHeight / 375f
    val infoButtonSize = screenWidth / 32f + screenHeight / 40f
    val textMedium = (screenHeight / 80f + screenWidth / 120f + 5.dp).value
    val textSmall = (screenHeight / 110f + screenWidth / 160f + 5.dp).value
}

@Composable
fun LifeLinkedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    BoxWithConstraints {
        val dimensions = remember(maxWidth, maxHeight) {
            Dimensions(
                screenWidth = maxWidth,
                screenHeight = maxHeight
            )
        }

        val colorScheme = when {
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

        CompositionLocalProvider(LocalDimensions provides dimensions) {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography(),
            ) {
                content()
            }
        }
    }
}