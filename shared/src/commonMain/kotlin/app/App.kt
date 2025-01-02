package app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import domain.storage.ISettingsManager
import di.BackHandler
import domain.system.SystemManager
import model.VersionNumber
import org.koin.compose.KoinContext
import org.koin.compose.currentKoinScope
import org.koin.compose.koinInject
import theme.LifeLinkedTheme
import ui.lifecounter.LifeCounterScreen
import ui.lifecounter.LifeCounterViewModel
import ui.playerselect.PlayerSelectScreen
import ui.playerselect.PlayerSelectViewModel
import ui.splash.SplashScreen
import ui.tutorial.TutorialScreen
import ui.tutorial.TutorialViewModel


private enum class LifeLinkedScreen(val route: String) {
    PLAYER_SELECT("player_select"),
    LIFE_COUNTER("life_counter"),
    TUTORIAL("tutorial"),
    SPLASH("splash")
}

@Composable
fun LifeLinkedApp() {
    KoinContext {
        val settingsManager: ISettingsManager by currentKoinScope().inject()
        val keepScreenOn by settingsManager.keepScreenOn.collectAsState()
        val darkTheme by settingsManager.darkTheme.collectAsState()
        SystemManager.keepScreenOn(keepScreenOn)
        LifeLinkedTheme(darkTheme = darkTheme) {
            SystemManager.updateSystemBarsColors(true)

            val navController = rememberNavController()
            val currentVersionNumber = koinInject<VersionNumber>()
            fun getStartScreen(): String {
                return if (!currentVersionNumber.isSame(VersionNumber(settingsManager.lastSplashScreenShown.value))) {
                    LifeLinkedScreen.SPLASH.route
                } else if (!settingsManager.autoSkip.value) {
                    LifeLinkedScreen.PLAYER_SELECT.route
                } else {
                    LifeLinkedScreen.LIFE_COUNTER.route
                }
            }

            val backHandler: BackHandler by currentKoinScope().inject()
            backHandler.attachNavigation(navController)

            var allowChangeNumPlayers by remember { mutableStateOf(true) }
            var firstLifeCounterNavigation by remember { mutableStateOf(true) }

            NavHost(
                navController = navController,
                startDestination = getStartScreen()
            ) {
                composable(LifeLinkedScreen.SPLASH.route) {
                    SplashScreen(
                        goToTutorial = {
                            settingsManager.setLastSplashScreenShown(currentVersionNumber.value)
                            navController.navigate(LifeLinkedScreen.TUTORIAL.route)
                        },
                        goToLifeCounter = {
                            settingsManager.setLastSplashScreenShown(currentVersionNumber.value)
                            navController.navigate(LifeLinkedScreen.LIFE_COUNTER.route)
                        }
                    )
                }
                composable(LifeLinkedScreen.TUTORIAL.route) {
                    val viewModel = koinViewModel<TutorialViewModel>()
                    TutorialScreen(
                        viewModel = viewModel,
                        onFinishTutorial = {
                            settingsManager.setTutorialSkip(true)
                            if (navController.currentBackStack.value.all {
                                    it.destination.route == null ||
                                            !it.destination.route!!.contains(LifeLinkedScreen.PLAYER_SELECT.route)
                                }) {
                                navController.navigate(LifeLinkedScreen.LIFE_COUNTER.route)
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


