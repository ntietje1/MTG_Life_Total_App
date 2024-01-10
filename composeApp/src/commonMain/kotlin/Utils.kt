import androidx.compose.runtime.Composable

expect fun currentTimeMillis(): Long

@Composable
expect fun UpdateSystemBarsColors(isDarkTheme: Boolean)