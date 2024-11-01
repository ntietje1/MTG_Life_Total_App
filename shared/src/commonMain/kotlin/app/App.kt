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
import data.SettingsManager
import di.BackHandler
import di.VersionNumber
import di.keepScreenOn
import di.updateSystemBarsColors
import org.koin.compose.KoinContext
import org.koin.compose.currentKoinScope
import org.koin.compose.koinInject
import theme.LifeLinkedTheme
import ui.lifecounter.LifeCounterScreen
import ui.lifecounter.LifeCounterViewModel
import ui.playerselect.PlayerSelectScreen
import ui.playerselect.PlayerSelectViewModel
import ui.splash.SplashScreen
import ui.tutorial.TutorialScreen2
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
        var darkTheme by remember { mutableStateOf(SettingsManager.instance.darkTheme) }
        var keepScreenOn by remember { mutableStateOf(SettingsManager.instance.keepScreenOn) }
        var allowChangeNumPlayers by remember { mutableStateOf(true) }
        var firstLifeCounterNavigation by remember { mutableStateOf(true) }
        keepScreenOn(keepScreenOn)

        LifeLinkedTheme(darkTheme = darkTheme) {
            updateSystemBarsColors(true)

            val navController = rememberNavController()
            val settingsManager = SettingsManager.instance
            val currentVersionNumber = koinInject<VersionNumber>()
            fun getStartScreen(): String {
                return if (!currentVersionNumber.isSame(VersionNumber(settingsManager.lastSplashScreenShown))) {
                    LifeLinkedScreen.SPLASH.route
                } else if (!settingsManager.autoSkip) {
                    LifeLinkedScreen.PLAYER_SELECT.route
                } else {
                    LifeLinkedScreen.LIFE_COUNTER.route
                }
            }

            val backHandler: BackHandler by currentKoinScope().inject()
            backHandler.attachNavigation(navController)

            NavHost(
                navController = navController,
                startDestination = getStartScreen()
            ) {
                composable(LifeLinkedScreen.SPLASH.route) {
                    SplashScreen(
                        goToTutorial = {
                            settingsManager.lastSplashScreenShown = currentVersionNumber.value
                            navController.navigate(LifeLinkedScreen.TUTORIAL.route)
                        },
                        goToLifeCounter = {
                            settingsManager.lastSplashScreenShown = currentVersionNumber.value
                            navController.navigate(LifeLinkedScreen.LIFE_COUNTER.route)
                        }
                    )
                }
                composable(LifeLinkedScreen.TUTORIAL.route) {
                    val viewModel = koinViewModel<TutorialViewModel>()
                    TutorialScreen2(
                        viewModel = viewModel,
                        onFinishTutorial = {
                            settingsManager.tutorialSkip = true
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
                    println("Navigating to player select screen with allowChangeNumPlayers: $allowChangeNumPlayers")
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
                    val turnTimerEnabled by settingsManager.turnTimer.collectAsState()
                    val numPlayers by settingsManager.numPlayers.collectAsState()
                    LifeCounterScreen(
                        viewModel = viewModel,
                        numPlayers = numPlayers,
                        alt4PlayerLayout = settingsManager.alt4PlayerLayout,
                        toggleTheme = {
                            SettingsManager.instance.darkTheme = !darkTheme
                            darkTheme = !darkTheme
                        },
                        toggleKeepScreenOn = {
                            SettingsManager.instance.keepScreenOn = !keepScreenOn
                            keepScreenOn = !keepScreenOn
                        },
                        toggleAlt4PlayerLayout = {
                            SettingsManager.instance.alt4PlayerLayout = !settingsManager.alt4PlayerLayout
                        },
                        goToPlayerSelectScreen = { changeNumPlayers ->
                            allowChangeNumPlayers = changeNumPlayers
                            navController.navigate(LifeLinkedScreen.PLAYER_SELECT.route)
                            firstLifeCounterNavigation = false
                        },
                        goToTutorialScreen = {
                            navController.navigate(LifeLinkedScreen.TUTORIAL.route)
                        },
                        timerEnabled = turnTimerEnabled,
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


