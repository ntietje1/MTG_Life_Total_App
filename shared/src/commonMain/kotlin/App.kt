
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
import composable.tutorial.TutorialScreen
import composable.tutorial.TutorialViewModel
import data.SettingsManager
import org.koin.compose.KoinContext
import org.koin.compose.currentKoinScope
import theme.LifeLinkedTheme


private enum class LifeLinkedScreen(val route: String) {
    PLAYER_SELECT("player select"),
    LIFE_COUNTER("life counter"),
    TUTORIAL("tutorial")
}

@Composable
fun LifeLinkedApp() {
    var darkTheme by remember { mutableStateOf(SettingsManager.instance.darkTheme) }

    LifeLinkedTheme(darkTheme = true) {
        KoinContext {
            updateSystemBarsColors(true)

            val navController = rememberNavController()
            val settingsManager = SettingsManager.instance
            fun getStartScreen(): String {
                return if (!settingsManager.tutorialSkip) {
                    LifeLinkedScreen.TUTORIAL.route
                } else if (!settingsManager.autoSkip) {
                    LifeLinkedScreen.PLAYER_SELECT.route
                } else {
                    LifeLinkedScreen.LIFE_COUNTER.route
                }
            }

            NavHost(
                navController = navController,
                startDestination = getStartScreen()
            ) {
                composable(LifeLinkedScreen.TUTORIAL.route) {
                    val viewModel = koinViewModel<TutorialViewModel>()
                    TutorialScreen(
                        viewModel = viewModel,
                        onFinishTutorial = {
                            settingsManager.tutorialSkip = true
                            println("BACKSTACCCK: ${navController.currentBackStack.value}")
                            if (navController.currentBackStack.value.all { it.destination.route != LifeLinkedScreen.PLAYER_SELECT.route }) {
                                println("NAVIGATING TO START SCREEN")
                                navController.navigate(getStartScreen())
                            } else {
                                println("POPPING BACKSTACK")
                                navController.popBackStack()
                            }
                        }
                    )
                }

                composable(LifeLinkedScreen.PLAYER_SELECT.route) {
                    val viewModel = koinViewModel<PlayerSelectViewModel>()
                    PlayerSelectScreen(
                        viewModel = viewModel,
                        goToLifeCounterScreen = {
                            navController.navigate(LifeLinkedScreen.LIFE_COUNTER.route)
                        },
                        setNumPlayers = { } //TODO: Implement this
                    )
                }

                composable(LifeLinkedScreen.LIFE_COUNTER.route) {
                    val viewModel = koinViewModel<LifeCounterViewModel>()
                    LifeCounterScreen(
                        viewModel = viewModel,
                        toggleTheme = {
                            SettingsManager.instance.darkTheme = !darkTheme
                            darkTheme = !darkTheme
                        },
                        goToPlayerSelectScreen = {
                            navController.navigate(LifeLinkedScreen.PLAYER_SELECT.route)
                        },
                        returnToLifeCounterScreen = {
                            navController.navigate(LifeLinkedScreen.LIFE_COUNTER.route)
                        },
                        goToTutorialScreen = {
                            navController.navigate(LifeLinkedScreen.TUTORIAL.route)
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


