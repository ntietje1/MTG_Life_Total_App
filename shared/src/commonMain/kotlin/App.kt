import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import composable.lifecounter.LifeCounterScreen
import composable.lifecounter.LifeCounterViewModel
import composable.playerselect.PlayerSelectScreen
import composable.playerselect.PlayerSelectViewModel
import data.SettingsManager
import org.koin.compose.KoinContext
import org.koin.compose.currentKoinScope
import theme.LifeLinkedTheme

@Composable
fun LifeLinkedApp() {
    var darkTheme by remember { mutableStateOf(SettingsManager.instance.darkTheme) }

    LifeLinkedTheme(darkTheme = true) {
        KoinContext {
            updateSystemBarsColors(true)

            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "player select"
            ) {
                composable("player select") {
                    val viewModel = koinViewModel<PlayerSelectViewModel>()
                    PlayerSelectScreen(
                        viewModel = viewModel,
                        goToLifeCounterScreen = {
                            navController.navigate("life counter")
                        },
                        setNumPlayers = { }
                    )
                }

                composable("life counter") {
                    val viewModel = koinViewModel<LifeCounterViewModel>()
                    LifeCounterScreen(
                        viewModel = viewModel,
                        toggleTheme = {
                            SettingsManager.instance.darkTheme = !darkTheme
                            darkTheme = !darkTheme
                        },
                        goToPlayerSelectScreen = {
                            navController.navigate("player select")
                        },
                        returnToLifeCounterScreen = {
                            navController.navigate("life counter")
                        }
                    )
                }
            }
        }
    }
}

@Composable
inline fun <reified T : ViewModel> koinViewModel(): T {
    val scope = currentKoinScope()
    return viewModel {
        scope.get<T>()
    }
}


