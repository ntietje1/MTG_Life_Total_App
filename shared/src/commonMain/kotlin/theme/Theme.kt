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

private val DarkColorScheme = darkColorScheme(
    primary = Black,
    background = Black,
    onPrimary = White, // Set the text color on the primary background
    onSurface = White     // Set the text color on the surface/background
)

private val LightColorScheme = lightColorScheme(
    primary = White,
    background = White,
    onPrimary = Black, // Set the text color on the primary background
    onSurface = Black     // Set the text color on the surface/background
)

val LocalDimensions = staticCompositionLocalOf<Dimensions> {
    error("No dimensions provided")
}

class Dimensions(val screenWidth: Dp, val screenHeight: Dp) {
    val blurRadius = screenHeight / 50f
    val paddingTiny = screenWidth / 1500f + screenHeight / 2000f
    val paddingSmall = screenWidth / 750f + screenHeight / 1000f
    val paddingMedium = screenWidth / 375f + screenHeight / 500f
    val paddingLarge = screenWidth / 200f + screenHeight / 300f
    val borderThin = screenWidth / 1000f + screenHeight / 1500f
    val borderSmall = screenWidth / 500f + screenHeight / 750f
    val borderMedium = screenWidth / 250f + screenHeight / 375f
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