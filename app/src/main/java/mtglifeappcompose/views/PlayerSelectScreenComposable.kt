package mtglifeappcompose.views


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
import mtglifeappcompose.data.Player
import mtglifeappcompose.fragments.LifeCounterScreen
import mtglifeappcompose.fragments.PlayerSelectScreen

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

    NavHost(
        navController = navController,
        startDestination = MTGScreen.PlayerSelectScreen.name,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = MTGScreen.PlayerSelectScreen.name) {
            Surface(Modifier.fillMaxSize(), color = Color.Black) {
            }
            PlayerSelectScreenWrapper(numPlayers = numPlayers, goToLifeCounter = { navController.navigate(
                MTGScreen.LifeCounterScreen.name) })
        }
        composable(route = MTGScreen.LifeCounterScreen.name) {
            Surface(Modifier.fillMaxSize(), color = Color.Black) {
            }
            LifeCounterScreen(
                mutableListOf(
                    Player.generatePlayer(),
                    Player.generatePlayer(),
                    Player.generatePlayer(),
                )
            )
        }
    }
}

@Composable
fun PlayerSelectScreenWrapper(goToLifeCounter: () -> Unit, numPlayers: MutableState<Int>) {
    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { context ->
            PlayerSelectScreen(context, null, numPlayers, goToLifeCounter)
        }, update = { view ->
            // Update the view if needed
        }, modifier = Modifier.fillMaxSize())
        Button(modifier = Modifier.size(50.dp), onClick = {
            numPlayers.value = 3
            goToLifeCounter()
        }) {

        }
    }
}