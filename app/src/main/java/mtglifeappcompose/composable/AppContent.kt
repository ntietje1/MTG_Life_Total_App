package mtglifeappcompose.composable

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mtglifeappcompose.composable.lifecounter.LifeCounterScreen
import mtglifeappcompose.data.Player
import mtglifeappcompose.data.PlayerDataManager
import mtglifeappcompose.ui.theme.MTGLifeAppComposeTheme


/**
 * enum values that represent the screens in the app
 */
enum class MTGScreen(val title: String) {
    PlayerSelectScreen("Player Select Screen"), LifeCounterScreen("Life Counter Screen")
}

@Composable
fun MTGLifeTotalApp(
//    viewModel: PlayerViewModel = PlayerViewModel(),
    navController: NavHostController = rememberNavController()
) {
    val numPlayers = remember { mutableIntStateOf(0) }

    fun generatePlayers(playerDataManager: PlayerDataManager) {
        val startingLife = playerDataManager.loadStartingLife()
        while (Player.currentPlayers.size < Player.MAX_PLAYERS) {
            Player.generatePlayer(Player(life = startingLife))
        }
    }

    fun goToLifeCounter() {
//        while (Player.currentPlayers.size < numPlayers.intValue) {
//            Player.currentPlayers.add(Player.generatePlayer())
//        }
//        while (Player.currentPlayers.size > numPlayers.intValue) {
//            Player.currentPlayers.removeLast()
//        }
        navController.navigate(MTGScreen.LifeCounterScreen.name)
    }

    fun goToPlayerSelect() {
        navController.navigate(MTGScreen.PlayerSelectScreen.name)
    }

    fun resetPlayers() {
        Player.resetPlayers()
        goToLifeCounter()
    }

    fun setPlayerNum(num: Int, allowOverride: Boolean = true) {
        if (!allowOverride && numPlayers.intValue != 0) return
        numPlayers.intValue = num
    }

    fun setStartingLife(playerDataManager: PlayerDataManager, life: Int) {
        playerDataManager.saveStartingLife(life)
        resetPlayers()
    }

    val initialTheme = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(initialTheme) }


    MTGLifeAppComposeTheme(darkTheme = darkTheme) {
        NavHost(
            navController = navController,
            startDestination = MTGScreen.PlayerSelectScreen.name,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
//        modifier = Modifier.consumeWindowInsets(PaddingValues(0.dp))
        ) {
            composable(route = MTGScreen.PlayerSelectScreen.name) {
                val playerDataManager = PlayerDataManager(LocalContext.current)

                generatePlayers(playerDataManager)

                PlayerSelectScreenWrapper(setPlayerNum = {
                    setPlayerNum(it, allowOverride = false)
                }, goToLifeCounter = {
                    goToLifeCounter()
                })
            }
            composable(route = MTGScreen.LifeCounterScreen.name) {
                val playerDataManager = PlayerDataManager(LocalContext.current)
                LaunchedEffect(Unit) {
                    if (numPlayers.intValue == 0) {
                        goToPlayerSelect()
                    }
                }
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
                LifeCounterScreen(players = remember {
                    Player.currentPlayers.subList(0, numPlayers.intValue)
                },
                    resetPlayers = { resetPlayers() },
                    setStartingLife = { setStartingLife(playerDataManager, it) },
                    setPlayerNum = { setPlayerNum(it) },
                    goToPlayerSelect = { goToPlayerSelect() },
                    toggleTheme = { darkTheme = !darkTheme })
            }
        }
    }
}
