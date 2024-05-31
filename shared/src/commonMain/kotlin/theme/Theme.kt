package theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}