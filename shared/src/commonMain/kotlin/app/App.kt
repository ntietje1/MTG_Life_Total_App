package app
import di.BackHandler
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
import ui.lifecounter.LifeCounterScreen
import ui.lifecounter.LifeCounterViewModel
import ui.playerselect.PlayerSelectScreen
import ui.playerselect.PlayerSelectViewModel
import ui.tutorial.TutorialScreen
import ui.tutorial.TutorialViewModel
import data.SettingsManager
import di.keepScreenOn
import org.koin.compose.KoinContext
import org.koin.compose.currentKoinScope
import theme.LifeLinkedTheme
import di.updateSystemBarsColors


private enum class LifeLinkedScreen(val route: String) {
    PLAYER_SELECT("player_select"),
    LIFE_COUNTER("life_counter"),
    TUTORIAL("tutorial")
}

@Composable
fun LifeLinkedApp() {
    KoinContext {
        var darkTheme by remember { mutableStateOf(SettingsManager.instance.darkTheme) }
        var keepScreenOn by remember { mutableStateOf(SettingsManager.instance.keepScreenOn) }
        keepScreenOn(keepScreenOn)

        LifeLinkedTheme(darkTheme = darkTheme) {
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

            val backHandler: BackHandler by currentKoinScope().inject()
            backHandler.attachNavigation(navController)

            var firstLifeCounterNavigation by remember { mutableStateOf(true) }
            var allowChangeNumPlayers by remember { mutableStateOf(true) }

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
                            if (navController.currentBackStack.value.all {
                                it.destination.route == null ||
                                !it.destination.route!!.contains(LifeLinkedScreen.PLAYER_SELECT.route)
                            }) {
                                navController.navigate(getStartScreen())
                            } else {
                                navController.popBackStack()
                            }
                        }
                    )
                }

                composable(LifeLinkedScreen.PLAYER_SELECT.route) {
                    val viewModel = koinViewModel<PlayerSelectViewModel>()
                    PlayerSelectScreen(
                        viewModel = viewModel,
                        allowChangeNumPlayers = allowChangeNumPlayers,
                        goToLifeCounterScreen = {
                            navController.navigate(LifeLinkedScreen.LIFE_COUNTER.route)
                        }
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
                        toggleKeepScreenOn = {
                            SettingsManager.instance.keepScreenOn = !keepScreenOn
                            keepScreenOn = !keepScreenOn
                        },
                        goToPlayerSelectScreen = { changeNumPlayers ->
                            allowChangeNumPlayers = changeNumPlayers
                            navController.navigate(LifeLinkedScreen.PLAYER_SELECT.route)
                            firstLifeCounterNavigation = false
                        },
                        goToTutorialScreen = {
                            navController.navigate(LifeLinkedScreen.TUTORIAL.route)
                        },
                        firstNavigation = firstLifeCounterNavigation
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


