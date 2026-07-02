package app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import domain.storage.PreferencesRepository
import domain.system.SystemManager
import model.VersionNumber
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

@Composable
fun LifeLinkedApp() {
    val preferencesRepository: PreferencesRepository by currentKoinScope().inject()
    val keepScreenOn by preferencesRepository.keepScreenOn.collectAsState()
    val darkTheme by preferencesRepository.darkTheme.collectAsState()
    SystemManager.keepScreenOn(keepScreenOn)
    LifeLinkedTheme(darkTheme = darkTheme) {
        SystemManager.updateSystemBarsColors(true)

        val navController = rememberNavController()
        val currentVersionNumber = koinInject<VersionNumber>()
        val startRoute = remember {
            startupDestination(
                currentVersion = currentVersionNumber,
                lastSplashScreenShown = preferencesRepository.lastSplashScreenShown.value,
                autoSkip = preferencesRepository.autoSkip.value,
                gameStarted = preferencesRepository.gameStarted.value
            )
        }

        var allowChangeNumPlayers by remember { mutableStateOf(true) }
        var firstLifeCounterNavigation by remember { mutableStateOf(true) }

        NavHost(
            navController = navController,
            startDestination = startRoute.route
        ) {
            composable(LifeLinkedRoute.Splash.route) {
                SplashScreen(
                    goToTutorial = {
                        preferencesRepository.setLastSplashScreenShown(currentVersionNumber.value)
                        navController.navigate(LifeLinkedRoute.Tutorial.route)
                    },
                    goToLifeCounter = {
                        preferencesRepository.setLastSplashScreenShown(currentVersionNumber.value)
                        preferencesRepository.setGameStarted(true)
                        navController.navigate(LifeLinkedRoute.LifeCounter.route)
                    }
                )
            }
            composable(LifeLinkedRoute.Tutorial.route) {
                val viewModel = koinViewModel<TutorialViewModel>()
                TutorialScreen(
                    viewModel = viewModel,
                    onFinishTutorial = {
                        preferencesRepository.setTutorialSkip(true)
                        preferencesRepository.setGameStarted(true)
                        if (navController.currentBackStack.value.all {
                                it.destination.route != LifeLinkedRoute.PlayerSelect.route
                            }) {
                            navController.navigate(LifeLinkedRoute.LifeCounter.route)
                        } else {
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(LifeLinkedRoute.PlayerSelect.route) {
                val viewModel = koinViewModel<PlayerSelectViewModel>()
                PlayerSelectScreen(
                    viewModel = viewModel,
                    allowChangeNumPlayers = allowChangeNumPlayers,
                    goToLifeCounterScreen = {
                        preferencesRepository.setGameStarted(true)
                        navController.navigate(LifeLinkedRoute.LifeCounter.route)
                    }
                )
            }

            composable(LifeLinkedRoute.LifeCounter.route) {
                val viewModel = koinViewModel<LifeCounterViewModel>()
                LaunchedEffect(Unit) {
                    preferencesRepository.setGameStarted(true)
                }
                LifeCounterScreen(
                    viewModel = viewModel,
                    goToPlayerSelectScreen = { changeNumPlayers ->
                        allowChangeNumPlayers = changeNumPlayers
                        navController.navigate(LifeLinkedRoute.PlayerSelect.route)
                        firstLifeCounterNavigation = false
                    },
                    goToTutorialScreen = {
                        navController.navigate(LifeLinkedRoute.Tutorial.route)
                    },
                    firstNavigation = firstLifeCounterNavigation
                )
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


