import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import composable.playerselect.PlayerSelectScreen
import composable.lifecounter.LifeCounterScreen
import data.SettingsManager
import theme.LifeLinkedTheme

/**
 * Root composable of the application
 */
@Composable
fun LifeLinkedApp(
    root: RootComponent,
) {
    var darkTheme by remember { mutableStateOf(SettingsManager.darkTheme) }

    LifeLinkedTheme(darkTheme = darkTheme) {
        updateSystemBarsColors(darkTheme)
        val childSlot by root.childSlot.subscribeAsState()
        when (val instance = childSlot.child?.instance) {
            is RootComponent.Child.PlayerSelectScreen -> PlayerSelectScreen(
                component = instance.component
            )

            is RootComponent.Child.LifeCounterScreen -> LifeCounterScreen(
                component = instance.component,
                toggleTheme = {
                    darkTheme = !darkTheme
                    SettingsManager.darkTheme = darkTheme

                }
            )

            else -> {
            }
        }
    }
}


