package theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Dark color palette for the app
 */
private val DarkColorScheme = darkColorScheme(
    primary = Black,
    background = Black,
    onPrimary = White, // Set the text color on the primary background
    onSurface = White     // Set the text color on the surface/background
)

/**
 * Light color palette for the app
 */
private val LightColorScheme = lightColorScheme(
    primary = White,
    background = White,
    onPrimary = Black, // Set the text color on the primary background
    onSurface = Black     // Set the text color on the surface/background
)

/**
 * The main theme of the app
 * @param darkTheme whether the theme is dark or not
 * @param content content to apply theme to
 */
@Composable
fun LifeLinkedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme, typography = Typography(), content = content
    )
}