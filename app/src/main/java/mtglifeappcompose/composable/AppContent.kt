package mtglifeappcompose.composable

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mtglifeappcompose.composable.lifecounter.LifeCounterScreen
import mtglifeappcompose.data.AppViewModel
import mtglifeappcompose.data.PlayerDataManager
import mtglifeappcompose.ui.theme.MTGLifeAppComposeTheme


enum class MTGScreen(val title: String) {
    PlayerSelectScreen("Player Select Screen"), LifeCounterScreen("Life Counter Screen")
}

@Composable
fun MTGLifeTotalApp(
    navController: NavHostController = rememberNavController()
) {
    val viewModel: AppViewModel = viewModel()

//    val initialTheme = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(true) }
    PlayerDataManager.initialize(LocalContext.current)

    MTGLifeAppComposeTheme(darkTheme = darkTheme) {
        NavHost(
            navController = navController,
            startDestination = MTGScreen.PlayerSelectScreen.name,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            composable(route = MTGScreen.PlayerSelectScreen.name) {
                viewModel.generatePlayers(PlayerDataManager)
                PlayerSelectScreenWrapper(
                    setPlayerNum = { viewModel.setPlayerNum(it, false) },
                    goToLifeCounter = { navController.navigate(MTGScreen.LifeCounterScreen.name) }
                )
            }
            composable(route = MTGScreen.LifeCounterScreen.name) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
                LifeCounterScreen(
                    players = remember { viewModel.getActivePlayers() },
                    resetPlayers = {
                        viewModel.resetPlayers(PlayerDataManager)
                        navController.navigate(MTGScreen.LifeCounterScreen.name)
                    },
                    setStartingLife = { viewModel.setStartingLife(PlayerDataManager, it) },
                    setPlayerNum = { viewModel.setPlayerNum(it) },
                    goToPlayerSelect = { navController.navigate(MTGScreen.PlayerSelectScreen.name) },
                    toggleTheme = { darkTheme = !darkTheme })
            }
        }
    }
}
