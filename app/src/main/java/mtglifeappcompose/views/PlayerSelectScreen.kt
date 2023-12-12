package mtglifeappcompose.views


import android.util.Log
import android.view.MotionEvent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mtglifeappcompose.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mtglifeappcompose.data.Player
import mtglifeappcompose.views.lifecounter.LifeCounterScreen

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
//        modifier = Modifier.consumeWindowInsets(PaddingValues(0.dp))
    ) {
        generatePlayers()
        composable(route = MTGScreen.PlayerSelectScreen.name) {
            Surface(Modifier.fillMaxSize(), color = Color.Black) {}
            PlayerSelectScreenWrapper(setPlayerNum = {
                setPlayerNum(it, allowOverride = false)
            }, goToLifeCounter = {
                goToLifeCounter()
            })
        }
        composable(route = MTGScreen.LifeCounterScreen.name) {
            Surface(Modifier.fillMaxSize(), color = Color.Black) {}
            LifeCounterScreen(players = remember {
                Player.currentPlayers.subList(
                    0, numPlayers.intValue
                )
            },
                resetPlayers = { resetPlayers() },
                setStartingLife = { setStartingLife(it) },
                setPlayerNum = { setPlayerNum(it) },
                goToPlayerSelect = { goToPlayerSelect() })
        }
    }
}

@Composable
fun PlayerSelectScreenWrapper(goToLifeCounter: () -> Unit, setPlayerNum: (Int) -> Unit) {
    Box(Modifier.fillMaxSize()) {

//        AndroidView(factory = { context ->
//            PlayerSelectView(context, null, setPlayerNum, goToLifeCounter)
//        }, update = { view ->
//            // Update the view if needed
//        }, modifier = Modifier.fillMaxSize())
        PlayerSelectView2(goToLifeCounter, setPlayerNum)
        SettingsButton(modifier = Modifier
            .rotate(90f)
            .align(Alignment.TopEnd),
            size = 100.dp,
            backgroundColor = Color.Black,
            text = "Skip",
            imageResource = painterResource(id = R.drawable.enter_icon),
            onTap = {
                setPlayerNum(4)
                goToLifeCounter()
            }) {

        }
    }
}
//TODO: random color, hollow circle
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerSelectView2(goToLifeCounter: () -> Unit, setPlayerNum: (Int) -> Unit) {
    val circles = remember { mutableStateMapOf<Int, Circle>() }
    val disappearingCircles = remember { mutableStateListOf<Circle>() }
    val pulseDelay = 1000L
    val pulseFreq = 800L
    val showHelperTextDelay = 500L
    val selectionDelay = pulseDelay + (pulseFreq * 3.35f).toLong()
    var selectedId: Int? by remember { mutableStateOf(null) }

    val scope = rememberCoroutineScope()

    fun allGoToNormal() {
        if (selectedId != null) {
            return
        }
        for (circle in circles.values) {
            CoroutineScope(scope.coroutineContext).launch {
                circle.goToNormal()
            }
        }
    }

    fun disappearCircle(id: Int, duration: Int = 300) {
        CoroutineScope(scope.coroutineContext).launch {
            if (circles.containsKey(id)) {
                val circle = circles[id]!!
                disappearingCircles.add(circle)
                circles.remove(id)
                circle.deselect(duration = duration, onComplete = {
                    disappearingCircles.remove(circle)
                    allGoToNormal()
                })
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
        .pointerInteropFilter { event ->
            val pointerIndex = event.actionIndex
            val id = event.getPointerId(pointerIndex)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    if (circles.size < 6 && selectedId == null) {
                        allGoToNormal()
                        circles[id] = Circle(Offset(event.getX(id), event.getY(id))).apply {
                            CoroutineScope(scope.coroutineContext).launch { popIn() }
                        }
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    for (i in 0 until event.pointerCount) {
                        val id = event.getPointerId(i)
                        if (circles.containsKey(event.getPointerId(i))) {
                            circles[id] = circles[id]!!.copy(Offset(event.getX(i), event.getY(i)))
                        }
                    }
                    true
                }

                MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    CoroutineScope(scope.coroutineContext).launch {
                        if (circles.size == 1 && selectedId != null) {
                            launch {
                                setPlayerNum(4)
                                circles[id]?.growToScreen(goToLifeCounter)
                            }
                        } else {
                            disappearCircle(id)
                        }
                    }
                    true
                }

                else -> false
            }
        }) {

        LaunchedEffect(circles.size) {
            val selectionScope = CoroutineScope(coroutineContext)
            val pulseScope = CoroutineScope(coroutineContext)

            if (circles.size >= 2) {
                selectionScope.launch {
                    delay(selectionDelay)
                    selectedId = circles.keys.random()
                    for (id in circles.keys) {
                        launch {
                            if (selectedId != id) {
                                disappearCircle(id, duration = 1500)
                            }
                        }
                    }
                    Log.i("debug", "SELECTED: $selectedId")
                }
                pulseScope.launch {
                    delay(pulseDelay)
                    while (selectedId == null) {
                        circles.values.forEach { circle ->
                            launch {
                                circle.pulse()
                                Log.i("debug", "PULSING: ${circle.posn}")
                            }
                        }
                        delay(pulseFreq)
                    }
                }

            }
        }
        DrawCircles(circles.values.toList(), disappearingCircles)
    }
}

data class Circle(
    var posn: Offset,
    var color: Color = Color.Black,
    var radiusMultiplier: Animatable<Float, AnimationVector1D> = Animatable(0.0f)
) {
    suspend fun popIn() {
        radiusMultiplier.stop()
        radiusMultiplier.snapTo(0f)
        radiusMultiplier.animateTo(
            targetValue = 1.0f, animationSpec = spring(
                dampingRatio = 0.5f, stiffness = 150f
            )
        )
    }

    suspend fun goToNormal() {
        color = Color.Black
        if (radiusMultiplier.value > 1.0f) {
            radiusMultiplier.animateTo(
                targetValue = 1.0f, animationSpec = tween(
                    durationMillis = 150, delayMillis = 0, easing = FastOutSlowInEasing
                )
            )
        }

    }

    suspend fun pulse() {
        color = Color.Red
        radiusMultiplier.animateTo(
            targetValue = 1.2f, animationSpec = tween(
                durationMillis = 500, delayMillis = 0, easing = FastOutLinearInEasing
            )
        )
        radiusMultiplier.animateTo(
            targetValue = 1.0f, animationSpec = tween(
                durationMillis = 500, delayMillis = 0, easing = FastOutLinearInEasing
            )
        )
    }

    suspend fun growToScreen(onComplete: () -> Unit = {}) {
        radiusMultiplier.animateTo(
            targetValue = 20f, animationSpec = tween(
                durationMillis = 3000, delayMillis = 0, easing = FastOutSlowInEasing
            )
        )
        onComplete()
    }

    suspend fun deselect(duration: Int = 300, onComplete: () -> Unit) {
        color = Color.Black
        radiusMultiplier.animateTo(
            targetValue = 1.2f, animationSpec = tween(
                durationMillis = duration/2, delayMillis = 0, easing = FastOutSlowInEasing
            )
        )
        radiusMultiplier.animateTo(
            targetValue = 0.0f, animationSpec = tween(
                durationMillis = duration, delayMillis = 0, easing = FastOutSlowInEasing
            )
        )
        onComplete()
    }
}

@Composable
fun DrawCircles(circles: List<Circle>, disappearingCircles: List<Circle>) {
    Box(modifier = Modifier
        .fillMaxSize()
        .drawBehind {
            for (circle in circles) {
                drawCircle(
                    color = circle.color,
                    center = circle.posn,
                    radius = 150f * circle.radiusMultiplier.value
                )
            }
            for (circle in disappearingCircles) {
                drawCircle(
                    color = circle.color,
                    center = circle.posn,
                    radius = 150f * circle.radiusMultiplier.value
                )
            }
        })
}
