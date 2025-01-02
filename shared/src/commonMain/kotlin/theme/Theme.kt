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
    // Base size - using shorter screen dimension to ensure consistent proportions
    private val baseSize = minOf(screenWidth, screenHeight)

    val blurRadius = screenHeight / 50f
    val paddingSmall = screenWidth / 750f + screenHeight / 750f
    val borderThin = screenWidth / 1500f + screenHeight / 1500f
    val borderSmall = screenWidth / 1000f + screenHeight / 1000f

    // Calculate proportional values
    // Example: on a 360px wide phone, paddingSmall would be ~4dp
//    val paddingSmall = (baseSize * 0.011f)    // ≈ 1.1% of screen size
    val paddingMedium = (baseSize * 0.022f)   // ≈ 2.2% of screen size
    val paddingLarge = (baseSize * 0.044f)    // ≈ 4.4% of screen size
    val paddingXLarge = (baseSize * 0.066f)   // ≈ 6.6% of screen size

//    val borderThin = (baseSize * 0.003f)      // ≈ 0.3% of screen size
    val borderMedium = (baseSize * 0.005f)    // ≈ 0.5% of screen size
    val borderThick = (baseSize * 0.008f)     // ≈ 0.8% of screen size
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