package mtglifeappcompose.composable

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mtglifeappcompose.composable.lifecounter.LifeCounterScreen
import mtglifeappcompose.data.AppViewModel
import mtglifeappcompose.data.SharedPreferencesManager
import mtglifeappcompose.ui.theme.MTGLifeAppComposeTheme


enum class MTGScreen(val title: String) {
    PlayerSelectScreen("Player Select Screen"), LifeCounterScreen("Life Counter Screen")
}

@Composable
fun MTGLifeTotalApp(
    navController: NavHostController = rememberNavController()
) {

    SharedPreferencesManager.initialize(LocalContext.current)
    var darkTheme by remember { mutableStateOf(SharedPreferencesManager.loadTheme()) }

    MTGLifeAppComposeTheme(darkTheme = darkTheme) {
        if (SharedPreferencesManager.loadKeepScreenOn()) {
            KeepScreenOn()
        }
        NavHost(
            navController = navController,
            startDestination = MTGScreen.PlayerSelectScreen.name,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            composable(route = MTGScreen.PlayerSelectScreen.name) { backStackEntry ->
                val viewModel: AppViewModel = backStackEntry.parentViewModel(navController)
                PlayerSelectScreenWrapper(setPlayerNum = { viewModel.setPlayerNum(it) }, goToLifeCounter = { navController.navigate(MTGScreen.LifeCounterScreen.name) })
            }
            composable(route = MTGScreen.LifeCounterScreen.name) { backStackEntry ->
                BackHandler(enabled = true) {}
                val viewModel: AppViewModel = backStackEntry.parentViewModel(navController)
                viewModel.generatePlayers()
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
                LifeCounterScreen(players = remember { viewModel.getActivePlayers() }, resetPlayers = {
                    viewModel.resetPlayers()
                    navController.navigate(MTGScreen.LifeCounterScreen.name)
                }, setStartingLife = {
                    viewModel.setStartingLife(SharedPreferencesManager, it)
                    navController.navigate(MTGScreen.LifeCounterScreen.name)
                }, setPlayerNum = {
                    viewModel.setPlayerNum(it, allowOverride = true)
                }, goToPlayerSelect = { navController.navigate(MTGScreen.PlayerSelectScreen.name) }, set4PlayerLayout = {
                    viewModel.toggle4PlayerLayout(it)
                    navController.navigate(MTGScreen.LifeCounterScreen.name)
                }, toggleTheme = {
                    darkTheme = !darkTheme
                    SharedPreferencesManager.saveTheme(darkTheme)
                })
            }
        }
    }
}

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}

@Composable
inline fun <reified VM : ViewModel> NavBackStackEntry.parentViewModel(
    navController: NavController
): VM {
    val parentId = destination.parent!!.id
    val parentBackStackEntry = navController.getBackStackEntry(parentId)
    return ViewModelProvider(parentBackStackEntry).get()
}
