package composable.playerselect


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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import composable.dialog.SettingsButton
import composable.modifier.routePointerChangesTo
import composable.playerselect.PlayerSelectScreenValues.deselectDuration
import composable.playerselect.PlayerSelectScreenValues.finalDeselectDuration
import composable.playerselect.PlayerSelectScreenValues.goToNormalDuration
import composable.playerselect.PlayerSelectScreenValues.growToScreenDuration
import composable.playerselect.PlayerSelectScreenValues.popInStiffness
import composable.playerselect.PlayerSelectScreenValues.pulseDelay
import composable.playerselect.PlayerSelectScreenValues.pulseDuration1
import composable.playerselect.PlayerSelectScreenValues.pulseDuration2
import composable.playerselect.PlayerSelectScreenValues.pulseFreq
import composable.playerselect.PlayerSelectScreenValues.selectionDelay
import composable.playerselect.PlayerSelectScreenValues.showHelperTextDelay
import getAnimationCorrectionFactor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import theme.allPlayerColors
import theme.scaledSp
import kotlin.coroutines.coroutineContext
import kotlin.math.pow
import kotlin.native.concurrent.ThreadLocal

/**
 * Screen for selecting players
 * @param component The PlayerSelectComponent to use for state and navigation
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun PlayerSelectScreen(component: PlayerSelectComponent) {
    Box(Modifier.fillMaxSize()) {
        val showHelperText = remember { mutableStateOf(true) }
        PlayerSelectScreenValues.animScale = getAnimationCorrectionFactor()
        PlayerSelectScreenBase(component, showHelperText)
        if (showHelperText.value) {
            Text(
                text = "Tap to select player",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 40.scaledSp,
                lineHeight = 50.scaledSp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).rotate(90f)
            )

            SettingsButton(modifier = Modifier.rotate(90f).align(Alignment.TopEnd).size(100.dp),
                shape = RoundedCornerShape(30.dp),
                mainColor = MaterialTheme.colorScheme.onPrimary,
                backgroundColor = Color.Transparent,
                text = "Skip",
                shadowEnabled = false,
                imageResource = painterResource("skip_icon.xml"),
                onTap = {
                    component.goToLifeCounterScreen()
                }) {}
        }
    }
}

/**
 * Values used for animating the PlayerSelectScreen
 */
@ThreadLocal
private object PlayerSelectScreenValues {
    var animScale = 1.0f
    const val pulseDelay = 1000L
    const val pulseFreq = 600L
    const val showHelperTextDelay = 1500L
    const val selectionDelay = pulseDelay + (pulseFreq * 3.35f).toLong()

    val deselectDuration
        get() = (250 / animScale).toInt()
    val finalDeselectDuration
        get() = (400 / animScale).toInt()
    val popInStiffness
        get() = 750f * (animScale * animScale)
    val goToNormalDuration
        get() = (75 / animScale).toInt()
    val pulseDuration1
        get() = (200 / animScale).toInt()
    val pulseDuration2
        get() = (150 / animScale).toInt()
    val growToScreenDuration
        get() = (750 / animScale).toInt()
}

/**
 * Base of the PlayerSelectScreen
 * @param component The PlayerSelectComponent to use for state and navigation.
 * @param showHelperText Whether or not to show the helper text.
 */
@Composable
fun PlayerSelectScreenBase(
    component: PlayerSelectComponent, showHelperText: MutableState<Boolean>
) {
    val circles = remember { mutableStateMapOf<PointerId, Circle>() }
    val lastMoved = remember { mutableMapOf<PointerId, Long>() }
    val disappearingCircles = remember { mutableStateListOf<Circle>() }
    var selectedId: PointerId? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    /**
     * Applies a random default color to the circle that is not already used
     */
    fun applyRandomColor(circle: Circle) {
        do {
            circle.color = allPlayerColors.random()
        } while (circles.values.any { it.color == circle.color })
    }

    /**
     * Makes all circles go to normal size
     */
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

    /**
     * Makes a circle disappear
     */
    fun disappearCircle(id: PointerId, duration: Int) {
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

    /**
     * Called when a pointer goes down
     */
    fun onDown(event: PointerInputChange) {
        if (circles.size < 6 && selectedId == null) {
            circles[event.id] = Circle(
                x = event.position.x, y = event.position.y
            ).apply {
                applyRandomColor(this)
                CoroutineScope(scope.coroutineContext).launch { popIn() }
            }
            lastMoved[event.id] = Clock.System.now().toEpochMilliseconds()
            allGoToNormal()
        }
    }

    /**
     * Called when a pointer moves
     */
    fun onMove(event: PointerInputChange) {
        circles[event.id]?.updatePosition(event.position.x, event.position.y)
        lastMoved[event.id] = Clock.System.now().toEpochMilliseconds()
    }

    /**
     * Called when a pointer goes up
     */
    fun onUp(event: PointerInputChange) {
        CoroutineScope(scope.coroutineContext).launch {
            val circle = circles[event.id]
            if (circles.size == 1 && selectedId != null) {
                println("Going to life counter screen")
                launch {
                    circle?.growToScreen {
                        component.goToLifeCounterScreen()
                    }
                }
            } else {
                println("Removed Circle @ position: x=${circle?.x}, y=${circle?.y}")
                disappearCircle(event.id, deselectDuration)
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                circles.entries.forEach { (id, circle) ->
                    val lastKnownPosition = lastMoved[id]
                    if (lastKnownPosition != null && lastKnownPosition + 500L < Clock.System.now().toEpochMilliseconds()) {
                        // The circle hasn't moved since the last check, so it could be 'bugged'
                        disappearCircle(id, deselectDuration/2)
                    }
                }
                delay(10L)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).pointerInput(circles) {
        routePointerChangesTo(onDown = { onDown(it) }, onMove = { onMove(it) }, onUp = { onUp(it) })
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
                    component.setNumPlayers(circles.size)
                    for (id in circles.keys) {
                        if (selectedId != id) launch {
                            disappearCircle(id, finalDeselectDuration)
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

/**
 * Represents a circle on the screen
 * @param x The x position of the circle
 *  @param y The y position of the circle
 *  @param color The color of the circle
 */
private class Circle(
    x: Float, y: Float, color: Color = Color.Magenta, val predictor: CirclePredictor = CirclePredictor()
) {
    companion object {
        private const val baseRadius = 130f
        private const val pulsedRadius = 150f
        private const val baseWidth = 25f
    }

    private val animatedRadius: Animatable<Float, AnimationVector1D> = Animatable(0f)
    private val animatedWidth: Animatable<Float, AnimationVector1D> = Animatable(baseWidth)

    var x by mutableFloatStateOf(x)
    var y by mutableFloatStateOf(y)
    var color by mutableStateOf(color)
    val radius by mutableStateOf(animatedRadius)
    val width by mutableStateOf(animatedWidth)

    /**
     * Updates the position of the circle
     */
    fun updatePosition(x: Float, y: Float) {
        predictor.addPosition(Offset(x, y))
        val posn = predictor.getNextPosition()
        this.y = posn.y
        this.x = posn.x
    }

    /**
     * Animates the circle to pop in
     */
    suspend fun popIn() {
        radius.animateTo(
            targetValue = baseRadius, animationSpec = spring(
                dampingRatio = 0.5f, stiffness = popInStiffness
            )
        )
    }

    /**
     * Animates the circle go to normal size
     */
    suspend fun goToNormal() {
        if (radius.value > baseRadius) {
            radius.animateTo(
                targetValue = baseRadius, animationSpec = tween(
                    durationMillis = goToNormalDuration, delayMillis = 0, easing = FastOutSlowInEasing
                )
            )
        }
    }

    /**
     * Animates the circle to pulse
     */
    suspend fun pulse() {
        radius.animateTo(
            targetValue = pulsedRadius, animationSpec = tween(
                durationMillis = pulseDuration1, delayMillis = 0, easing = LinearOutSlowInEasing
            )
        )
        radius.animateTo(
            targetValue = baseRadius, animationSpec = tween(
                durationMillis = pulseDuration2, delayMillis = 50, easing = FastOutLinearInEasing
            )
        )
    }

    /**
     * Animates the circle to grow to the screen
     */
    suspend fun growToScreen(onComplete: () -> Unit = {}) {
        val duration = growToScreenDuration
        val target = 10 * baseRadius
        if (duration == Int.MAX_VALUE) {
            onComplete()
            return
        }
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
                delay((duration*0.9f).toLong())
                onComplete()
            }
        }
    }

    /**
     * Animates the circle to pop out
     */
    suspend fun deselect(duration: Int, onComplete: () -> Unit) {
        CoroutineScope(coroutineContext).launch {
            launch {
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
            }
            launch {
                delay((duration * 1.25f).toLong())
                onComplete()
            }
        }
    }
}

/**
 * Predicts the next position of a circle based on its previous positions
 */
class CirclePredictor {
    companion object {
        private const val HISTORY_SIZE = 10f
    }

    private val historyX: MutableList<Float> = mutableListOf()
    private val historyY: MutableList<Float> = mutableListOf()

    /**
     * Adds a position to the predictor
     * @param posn The position to add
     */
    fun addPosition(posn: Offset) {
        historyX.add(posn.x)
        historyY.add(posn.y)

        if (historyX.size > HISTORY_SIZE) {
            historyX.removeAt(0)
            historyY.removeAt(0)
        }
    }

    /**
     * Gets the next predicted position
     * @return The next predicted position
     */
    fun getNextPosition(): Offset {
        val adjX = calculateAdjustment(historyX)
        val adjY = calculateAdjustment(historyY)

        return Offset(
            x = historyX.last() + adjX / HISTORY_SIZE.pow(HISTORY_SIZE / 1.9f), y = historyY.last() + adjY / HISTORY_SIZE.pow(HISTORY_SIZE / 1.9f)
        )
    }

    /**
     * Calculates the adjustment for the next position
     * @param history The history of positions
     */
    private fun calculateAdjustment(history: List<Float>): Float {
        var adj = 0f
        for (i in history.indices) {
            adj += (i + 1f).pow(HISTORY_SIZE - i) * (history.last() - history[i])
        }
        return adj
    }
}

/**
 * Draws the circles on the screen
 * @param circles The circles to draw
 * @param disappearingCircles The circles that are currently disappearing but still visible
 */
@Composable
private fun DrawCircles(circles: List<Circle>, disappearingCircles: List<Circle>) {
    Box(modifier = Modifier.fillMaxSize().drawBehind {
        for (circle in circles.union(disappearingCircles)) {
            drawCircle(
                color = circle.color, center = Offset(circle.x, circle.y), style = Stroke(width = circle.width.value), radius = circle.radius.value
            )
        }
    })
}
