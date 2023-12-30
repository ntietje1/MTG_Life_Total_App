package mtglifeappcompose.composable


import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtglifeappcompose.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mtglifeappcompose.ui.theme.allPlayerColors
import kotlin.coroutines.coroutineContext
import kotlin.math.pow

@Composable
fun PlayerSelectScreenWrapper(goToLifeCounter: () -> Unit, setPlayerNum: (Int) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        val showHelperText = remember { mutableStateOf(true) }
        PlayerSelectScreen(goToLifeCounter, setPlayerNum, showHelperText)
        if (showHelperText.value) {
            Text(
                text = "Tap to select player", color = MaterialTheme.colorScheme.onPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold, modifier = Modifier
                    .align(Alignment.Center)
                    .rotate(90f)
            )

            SettingsButton(modifier = Modifier
                .rotate(90f)
                .align(Alignment.TopEnd)
                .size(100.dp),
                shape = RoundedCornerShape(30.dp),
                mainColor = MaterialTheme.colorScheme.onPrimary,
                backgroundColor = Color.Transparent,
                text = "Skip",
                shadowEnabled = false,
                imageResource = painterResource(id = R.drawable.skip_icon),
                onTap = {
                    setPlayerNum(4)
                    goToLifeCounter()
                }) {}
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlayerSelectScreen(
    goToLifeCounter: () -> Unit, setPlayerNum: (Int) -> Unit, showHelperText: MutableState<Boolean>
) {
    val circles = remember { mutableStateMapOf<Int, Circle>() }
    val disappearingCircles = remember { mutableStateListOf<Circle>() }
    val pulseDelay = 1000L
    val pulseFreq = 750L
    val showHelperTextDelay = 1500L
    val selectionDelay = pulseDelay + (pulseFreq * 3.35f).toLong()
    var selectedId: Int? by remember { mutableStateOf(null) }

    val scope = rememberCoroutineScope()

    fun applyRandomColor(circle: Circle) {
        do {
            circle.color = allPlayerColors.random()
        } while (circles.values.any { it.color == circle.color })
    }

    fun allGoToNormal() {
        if (selectedId != null) return
        for (id in circles.keys) {
            CoroutineScope(scope.coroutineContext).launch {
                if (id != selectedId) {
                    circles[id]?.goToNormal()
                }
            }
        }
    }

    fun disappearCircle(id: Int, duration: Int = 300) {
        if (circles.containsKey(id)) {
            val circle = circles[id]!!
            disappearingCircles.add(circle)
            circles.remove(id)
            CoroutineScope(scope.coroutineContext).launch {
                circle.deselect(duration = duration, onComplete = {
                    disappearingCircles.remove(circle)
                })
            }
        }
    }


    Box(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .pointerInteropFilter { event ->
            val pointerIndex = event.actionIndex
            val id = event.getPointerId(pointerIndex)

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    if (circles.size < 6 && selectedId == null) {
                        circles[id] = Circle(
                            x = event.getX(pointerIndex), y = event.getY(pointerIndex)
                        ).apply {
                            applyRandomColor(this)
                            CoroutineScope(scope.coroutineContext).launch { popIn() }
                        }
                        allGoToNormal()
                    }
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    for (i in 0 until event.pointerCount) {
                        circles[event.getPointerId(i)]?.updatePosition(
                            event.getX(i), event.getY(i)
                        )
                    }
                    true
                }

                MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    CoroutineScope(scope.coroutineContext).launch {
                        val circle = circles[id]
                        if (circles.size == 1 && selectedId != null) {
                            launch {
                                circle?.growToScreen(goToLifeCounter)
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
            val helperTextScope = CoroutineScope(coroutineContext)

            if (circles.size == 0) {
                helperTextScope.launch {
                    delay(showHelperTextDelay)
                    showHelperText.value = true
                }
            } else {
                showHelperText.value = false
            }

            if (circles.size >= 2) {
                selectionScope.launch {
                    delay(selectionDelay)
                    selectedId = circles.keys.random()
                    setPlayerNum(circles.size)
                    for (id in circles.keys) {
                        if (selectedId != id) launch {
                            disappearCircle(id, duration = 1500)
                        } else launch {
                            circles[id]?.pulse()
                        }
                    }
                }
                pulseScope.launch {
                    delay(pulseDelay)
                    while (selectedId == null) {
                        circles.values.forEach { circle ->
                            launch {
                                circle.pulse()
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

class Circle(
    x: Float, y: Float, color: Color = Color.Magenta
) {

    private val baseRadius = 130f
    private val pulsedRadius = 155f
    private val baseWidth = 25f

    private val animatedRadius: Animatable<Float, AnimationVector1D> = Animatable(0f)
    private val animatedWidth: Animatable<Float, AnimationVector1D> = Animatable(baseWidth)

    var x by mutableFloatStateOf(x)
    var y by mutableFloatStateOf(y)
    var color by mutableStateOf(color)
    val radius by mutableStateOf(animatedRadius)
    val width by mutableStateOf(animatedWidth)


    private val predictor = CirclePredictor()

    fun updatePosition(x: Float, y: Float) {
        predictor.addPosition(Offset(x, y))
        val posn = predictor.getNextPosition()
        this.y = posn.y
        this.x = posn.x
    }

    suspend fun popIn() {
        radius.animateTo(
            targetValue = baseRadius, animationSpec = spring(
                dampingRatio = 0.5f, stiffness = 150f
            )
        )
    }

    suspend fun goToNormal() {
        if (radius.value > baseRadius) {
            radius.animateTo(
                targetValue = baseRadius, animationSpec = tween(
                    durationMillis = 150, delayMillis = 0, easing = FastOutSlowInEasing
                )
            )
        }
    }

    suspend fun pulse() {
        radius.animateTo(
            targetValue = pulsedRadius, animationSpec = tween(
                durationMillis = 400, delayMillis = 0, easing = FastOutLinearInEasing
            )
        )
        radius.animateTo(
            targetValue = baseRadius, animationSpec = tween(
                durationMillis = 800, delayMillis = 50, easing = LinearOutSlowInEasing
            )
        )
    }

    suspend fun growToScreen(onComplete: () -> Unit = {}) {
        val duration = 1500
        val target = 10 * baseRadius
        CoroutineScope(coroutineContext).launch {
            launch {
                width.animateTo(
                    targetValue = target * 2, animationSpec = tween(
                        durationMillis = duration * 2, delayMillis = 0, easing = FastOutSlowInEasing
                    )
                )
            }
            launch {
                radius.animateTo(
                    targetValue = target, animationSpec = tween(
                        durationMillis = duration * 2, delayMillis = 0, easing = FastOutSlowInEasing
                    )
                )
            }
            launch {
                delay(duration.toLong())
                onComplete()
            }
        }
    }

    suspend fun deselect(duration: Int = 300, onComplete: () -> Unit) {
        radius.animateTo(
            targetValue = pulsedRadius, animationSpec = tween(
                durationMillis = duration / 2, delayMillis = 0, easing = FastOutSlowInEasing
            )
        )
        radius.animateTo(
            targetValue = 0f, animationSpec = tween(
                durationMillis = duration, delayMillis = 0, easing = FastOutSlowInEasing
            )
        )
        onComplete()
    }
}

class CirclePredictor {
    companion object {
        private const val HISTORY_SIZE = 10f
    }

    private val historyX: MutableList<Float> = mutableListOf()
    private val historyY: MutableList<Float> = mutableListOf()

    fun addPosition(posn: Offset) {
        historyX.add(posn.x)
        historyY.add(posn.y)

        if (historyX.size > HISTORY_SIZE) {
            historyX.removeAt(0)
            historyY.removeAt(0)
        }
    }

    fun getNextPosition(): Offset {
        val adjX = calculateAdjustment(historyX)
        val adjY = calculateAdjustment(historyY)

        return Offset(
            x = historyX.last() + adjX / HISTORY_SIZE.pow(HISTORY_SIZE / 1.9f), y = historyY.last() + adjY / HISTORY_SIZE.pow(HISTORY_SIZE / 1.9f)
        )
    }

    private fun calculateAdjustment(history: List<Float>): Float {
        var adj = 0f
        for (i in history.indices) {
            adj += (i + 1f).pow(HISTORY_SIZE - i) * (history.last() - history[i])
        }
        return adj
    }
}

@Composable
fun DrawCircles(circles: List<Circle>, disappearingCircles: List<Circle>) {
    Box(modifier = Modifier
        .fillMaxSize()
        .drawBehind {
            for (circle in circles.union(disappearingCircles)) {
                drawCircle(
                    color = circle.color, center = Offset(circle.x, circle.y), style = Stroke(width = circle.width.value), radius = circle.radius.value
                )
            }
        })
}
