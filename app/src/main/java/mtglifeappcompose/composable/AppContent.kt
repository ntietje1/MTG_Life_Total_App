package mtglifeappcompose.composable

import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import coil.Coil
import coil.ImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mtglifeappcompose.composable.lifecounter.LifeCounterScreen
import mtglifeappcompose.data.AppViewModel
import mtglifeappcompose.data.SharedPreferencesManager
import mtglifeappcompose.ui.theme.MTGLifeAppComposeTheme
import okhttp3.Cache
import okhttp3.OkHttpClient


enum class MTGScreen(val title: String) {
    PlayerSelectScreen("Player Select Screen"), LifeCounterScreen("Life Counter Screen")
}

@Composable
fun MTGLifeTotalApp(
    navController: NavHostController = rememberNavController()
) {
    val  context = LocalContext.current
    SharedPreferencesManager.initialize(context)
    var darkTheme by remember { mutableStateOf(SharedPreferencesManager.loadTheme()) }

    EnableImageCache(10)

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
                val scope = rememberCoroutineScope()

                LaunchedEffect(Unit) {
                    if (viewModel.autoSkip && viewModel.firstPlayerSelect) {
                        CoroutineScope(scope.coroutineContext).launch {
                            //TODO: implement a loading screen??
                            delay(100L)
                            navController.navigate(MTGScreen.LifeCounterScreen.name)
                            viewModel.firstPlayerSelect = false
                        }
                    }
                }
                if (viewModel.autoSkip && viewModel.firstPlayerSelect) {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
                }
                else {
                    PlayerSelectScreenWrapper(
                        setPlayerNum = { viewModel.setPlayerNum(it) },
                        goToLifeCounter = { navController.navigate(MTGScreen.LifeCounterScreen.name) })
                }
            }
            composable(route = MTGScreen.LifeCounterScreen.name) { backStackEntry ->
                BackHandler(enabled = true) {}
                val viewModel: AppViewModel = backStackEntry.parentViewModel(navController)
                viewModel.generatePlayers()
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
                LifeCounterScreen(
                    players = remember { viewModel.getActivePlayers() },
                    resetPlayers = {
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
fun EnableImageCache(sizeMB: Int = 10) {
    val  context = LocalContext.current
    val cacheSize = sizeMB * 1024 * 1024
    val cache = Cache(context.cacheDir, cacheSize.toLong())
    val okHttpClient = OkHttpClient.Builder().cache(cache).build()
    val imageLoader = ImageLoader.Builder(context).okHttpClient(okHttpClient).build()
    Coil.setImageLoader(imageLoader)
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
