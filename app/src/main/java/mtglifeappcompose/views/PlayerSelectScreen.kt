package mtglifeappcompose.views


import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mtglifeappcompose.components.PlayerSelectView
import mtglifeappcompose.data.Player
import mtglifeappcompose.views.lifecounter.LifeCounterScreen

/**
 * enum values that represent the screens in the app
 */
enum class MTGScreen(val title: String) {
    PlayerSelectScreen("Player Select Screen"),
    LifeCounterScreen("Life Counter Screen")
}

@Composable
fun MTGLifeTotalApp(
//    viewModel: PlayerViewModel = PlayerViewModel(),
    navController: NavHostController = rememberNavController()
) {
    val numPlayers = remember { mutableIntStateOf(0) }

    fun generatePlayers() {
        while (Player.currentPlayers.size < Player.MAX_PLAYERS) {
            Player.generatePlayer()
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

    fun setStartingLife(life: Int) {
        Player.startingLife = life
        resetPlayers()
    }

    NavHost(
        navController = navController,
        startDestination = MTGScreen.PlayerSelectScreen.name,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        modifier = Modifier.fillMaxSize()
    ) {
        generatePlayers()
        composable(route = MTGScreen.PlayerSelectScreen.name) {
            Surface(Modifier.fillMaxSize(), color = Color.Black) {
            }
            PlayerSelectScreenWrapper(
                setPlayerNum = {
                    setPlayerNum(it, allowOverride = false)
                },
                goToLifeCounter = {
                    goToLifeCounter()
                }
            )
        }
        composable(route = MTGScreen.LifeCounterScreen.name) {
            Surface(Modifier.fillMaxSize(), color = Color.Black) {}
            LifeCounterScreen(
                players = remember { Player.currentPlayers.subList(0, numPlayers.intValue) },
                resetPlayers = { resetPlayers() },
                setStartingLife = { setStartingLife(it) },
                setPlayerNum = { setPlayerNum(it) },
                goToPlayerSelect = { goToPlayerSelect() }
            )
        }
    }
}

@Composable
fun PlayerSelectScreenWrapper(goToLifeCounter: () -> Unit, setPlayerNum: (Int) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { context ->
            PlayerSelectView(context, null, setPlayerNum, goToLifeCounter)
        }, update = { view ->
            // Update the view if needed
        }, modifier = Modifier.fillMaxSize())
        Button(modifier = Modifier.size(50.dp), onClick = {
            setPlayerNum(3)
            goToLifeCounter()
        }) {

        }
    }
}