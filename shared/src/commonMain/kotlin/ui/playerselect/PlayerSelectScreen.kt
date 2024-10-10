package ui.playerselect


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import data.Player.Companion.allPlayerColors
import di.getAnimationCorrectionFactor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.one_finger_hold
import lifelinked.shared.generated.resources.skip_icon
import org.jetbrains.compose.resources.vectorResource
import theme.scaledSp
import ui.SettingsButton
import ui.modifier.routePointerChangesTo
import ui.playerselect.PlayerSelectScreenValues.deselectDuration
import ui.playerselect.PlayerSelectScreenValues.finalDeselectDuration
import ui.playerselect.PlayerSelectScreenValues.goToNormalDuration
import ui.playerselect.PlayerSelectScreenValues.growToScreenDuration
import ui.playerselect.PlayerSelectScreenValues.popInStiffness
import ui.playerselect.PlayerSelectScreenValues.pulseDelay
import ui.playerselect.PlayerSelectScreenValues.pulseDuration1
import ui.playerselect.PlayerSelectScreenValues.pulseDuration2
import ui.playerselect.PlayerSelectScreenValues.pulseFreq
import ui.playerselect.PlayerSelectScreenValues.selectionDelay
import ui.playerselect.PlayerSelectScreenValues.showHelperTextDelay
import kotlin.coroutines.coroutineContext
import kotlin.math.pow
import kotlin.native.concurrent.ThreadLocal

@Composable
fun PlayerSelectScreen(
    viewModel: PlayerSelectViewModel,
    allowChangeNumPlayers: Boolean,
    goToLifeCounterScreen: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        PlayerSelectScreenValues.animScale = getAnimationCorrectionFactor()

        val textSize = remember(Unit) { (maxWidth / 30f + maxHeight / 30f).value }
        val buttonSize = remember(Unit) { (maxWidth / 10f + maxHeight / 10f) }

        PlayerSelectScreenBase(
            setHelperText = { viewModel.setHelperText(it) },
            goToLifeCounterScreen = goToLifeCounterScreen,
            setNumPlayers = { viewModel.setNumPlayers(allowChangeNumPlayers, it) }
        )

        if (state.showHelperText != HelperTextState.HIDDEN) {
            Column(
                modifier = Modifier.wrapContentSize().align(Alignment.Center).rotate(90f).then(
                    if (state.showHelperText == HelperTextState.FADED) Modifier.alpha(0.35f) else Modifier.alpha(1f)
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.size(buttonSize * 0.4f))
                Text(
                    text = state.showHelperText.text,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = textSize.scaledSp,
                    lineHeight = textSize.scaledSp * 1.2f,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                )
                Spacer(modifier = Modifier.size(buttonSize * 0.2f))
                Row(
                    modifier = Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(buttonSize * 0.6f).rotate(17.5f).scale(scaleX = -1f, scaleY = 1f),
                        imageVector = vectorResource(Res.drawable.one_finger_hold),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.size(buttonSize * 0.3f))
                    Icon(
                        modifier = Modifier.size(buttonSize * 0.6f).rotate(-15f),
                        imageVector = vectorResource(Res.drawable.one_finger_hold),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        if (state.showHelperText == HelperTextState.FULL) {
            SettingsButton(modifier = Modifier.rotate(90f).align(Alignment.TopEnd).size(buttonSize),
                mainColor = MaterialTheme.colorScheme.onPrimary,
                backgroundColor = Color.Transparent,
                text = "Skip",
                shadowEnabled = false,
                imageVector = vectorResource(Res.drawable.skip_icon),
                onTap = {
                    goToLifeCounterScreen()
                })
        }
    }
}

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

@Composable
fun PlayerSelectScreenBase(
    setHelperText: (HelperTextState) -> Unit,
    goToLifeCounterScreen: () -> Unit,
    setNumPlayers: (Int) -> Unit
) {
    val circles = remember { mutableStateMapOf<PointerId, Circle>() }
    val disappearingCircles = remember { mutableStateListOf<Circle>() }
    var selectedId: PointerId? by remember { mutableStateOf(null) }
    var previousCircleCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val circleSize: Dp = 80.dp + (20.dp * LocalDensity.current.density )

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

    fun disappearCircle(id: PointerId, duration: Int = deselectDuration) {
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

//    val maxPresses = remember { if (platform == Platform.IOS) 5 else 6 }
    val maxPresses = 6
    fun onDown(event: PointerInputChange, baseSize: Dp) {
        if (circles.size < maxPresses && selectedId == null) {
            circles[event.id] = Circle(
                baseSize = baseSize,
                x = event.position.x,
                y = event.position.y,
                triggerHaptic = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
            ).apply {
                applyRandomColor(this)
                CoroutineScope(scope.coroutineContext).launch {
                    popIn()
                }
            }
            allGoToNormal()
        }
    }

    fun onMove(event: PointerInputChange) {
        circles[event.id]?.updatePosition(event.position.x, event.position.y)
    }

    fun onUp(id: PointerId) {
        CoroutineScope(scope.coroutineContext).launch {
            val circle = circles[id]
            if (circles.size == 1 && selectedId != null) {
                launch {
                    circle?.growToScreen {
                        goToLifeCounterScreen()
                    }
                }
            } else {
                disappearCircle(id)
            }
        }
    }

    fun onUp(event: PointerInputChange) {
        onUp(event.id)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).pointerInput(circles) {
        routePointerChangesTo(onDown = { onDown(it, circleSize) }, onMove = { onMove(it) }, onUp = { onUp(it) }, countCallback = {
            for (id in circles.keys) {
                if (!it.contains(id)) {
                    onUp(id) // secondary check to remove circles that should be removed
                }
            }
        })
    })
    {
        LaunchedEffect(circles.size) {
            val selectionScope = CoroutineScope(coroutineContext)
            val pulseScope = CoroutineScope(coroutineContext)
            val helperTextScope = CoroutineScope(coroutineContext)

            if (selectedId == null) {
                when(circles.size) {
                    0 -> helperTextScope.launch {
                        delay(showHelperTextDelay)
                        setHelperText(HelperTextState.FULL)
                    }
                    1 -> {
                        if (previousCircleCount > 0) helperTextScope.launch {
                            delay(showHelperTextDelay / 4)
                            setHelperText(HelperTextState.FADED)
                        }
                        else setHelperText(HelperTextState.FADED)
                    }
                    else -> setHelperText(HelperTextState.HIDDEN)
                }
            }
            previousCircleCount = circles.size

            if (circles.size >= 2) {
                selectionScope.launch {
                    delay(selectionDelay)
                    selectedId = circles.keys.random()
                    setNumPlayers(circles.size)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    for (id in circles.keys) {
                        if (selectedId != id) launch {
//                            setHelperText(HelperTextState.HIDDEN)
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

private class Circle(
    baseSize: Dp,
    x: Float,
    y: Float,
    color: Color = Color.Magenta,
    val predictor: CirclePredictor = CirclePredictor(),
    private val triggerHaptic: () -> Unit
) {
    private val baseRadius = baseSize.value
    private val pulsedRadius = baseRadius * 1.15f
    private val baseWidth = baseRadius * 0.2f

    private val animatedRadius: Animatable<Float, AnimationVector1D> = Animatable(0f)
    private val animatedWidth: Animatable<Float, AnimationVector1D> = Animatable(baseWidth)

    var x by mutableFloatStateOf(x)
    var y by mutableFloatStateOf(y)
    var color by mutableStateOf(color)
    val radius by mutableStateOf(animatedRadius)
    val width by mutableStateOf(animatedWidth)

    fun updatePosition(x: Float, y: Float) {
        predictor.addPosition(Offset(x, y))
        val posn = predictor.getNextPosition()
        this.y = posn.y
        this.x = posn.x
    }

    suspend fun popIn() {
        triggerHaptic()
        radius.animateTo(
            targetValue = baseRadius, animationSpec = spring(
                dampingRatio = 0.5f, stiffness = popInStiffness
            )
        )
    }

    suspend fun goToNormal() {
        if (radius.value > baseRadius) {
            radius.animateTo(
                targetValue = baseRadius, animationSpec = tween(
                    durationMillis = goToNormalDuration, delayMillis = 0, easing = FastOutSlowInEasing
                )
            )
        }
    }

    suspend fun pulse() {
        radius.animateTo(
            targetValue = pulsedRadius, animationSpec = tween(
                durationMillis = pulseDuration1, delayMillis = 0, easing = LinearOutSlowInEasing
            )
        )
        triggerHaptic()
        radius.animateTo(
            targetValue = baseRadius, animationSpec = tween(
                durationMillis = pulseDuration2, delayMillis = 50, easing = FastOutLinearInEasing
            )
        )
    }

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
                delay((duration * 0.9f).toLong())
                onComplete()
            }
        }
    }

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
private fun DrawCircles(circles: List<Circle>, disappearingCircles: List<Circle>) {
    Box(modifier = Modifier.fillMaxSize().drawBehind {
        for (circle in circles.union(disappearingCircles)) {
            drawCircle(
                color = circle.color, center = Offset(circle.x, circle.y), style = Stroke(width = circle.width.value), radius = circle.radius.value
            )
        }
    })
}
