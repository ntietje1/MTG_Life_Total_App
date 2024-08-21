package ui.modifier

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Indication
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class PressState { Pressed, Idle }

@Composable
fun Modifier.repeatingClickable(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = null,
    enabled: Boolean = true,
    initialDelayMillis: Long = 500,
    maxDelayMillis: Long = 100, //TODO: 100
    minDelayMillis: Long = 50,
    delayDecayFactor: Float = 0.0f,
    onPress: () -> Unit
): Modifier = repeatingClickable(
    interactionSource = interactionSource,
    indication = indication,
    enabled = enabled,
    initialDelayMillis = initialDelayMillis,
    maxDelayMillis = maxDelayMillis,
    minDelayMillis = minDelayMillis,
    delayDecayFactor = delayDecayFactor,
    bounceAmount = 0f,
    bounceDuration = 0L,
    onPress = onPress
)

@Composable
fun Modifier.repeatingClickable(
    interactionSource: MutableInteractionSource,
    indication: Indication? = null,
    enabled: Boolean = true,
    initialDelayMillis: Long = 500,
    maxDelayMillis: Long = 100,
    minDelayMillis: Long = 50,
    delayDecayFactor: Float = 0.0f,
    bounceAmount: Float = 0.0075f,
    bounceDuration: Long = 60L,
    onPress: () -> Unit
): Modifier = composed {
    val currentClickListener by rememberUpdatedState(onPress)
    val isEnabled by rememberUpdatedState(enabled)
    val indicationScope = rememberCoroutineScope()
    val bounceScope = rememberCoroutineScope()
    var press by remember { mutableStateOf<PressInteraction.Press?>(null) }
    var buttonState by remember { mutableStateOf(PressState.Idle) }
    var currentDelayMillis by remember { mutableStateOf(maxDelayMillis) }
    var currentlyRepeating by remember { mutableStateOf(false) }
    var bounceJob: Job? by remember { mutableStateOf (null) }

    val scale by animateFloatAsState( //TODO: looks a little janky on ios
        targetValue = if (buttonState == PressState.Pressed) {
            1.0f + (if (currentlyRepeating) bounceAmount * 0.5f else bounceAmount)
        } else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )


    fun triggerBounce() {
        bounceJob = bounceScope.launch {
            buttonState = PressState.Pressed
            bounceScope.launch {
                delay(bounceDuration)
                buttonState = PressState.Idle
            }
        }
    }

    fun cancelBounce() {
        buttonState = PressState.Idle
        bounceJob?.cancel()
    }

    pointerInput(Unit) {
        routePointerChangesTo(
            onDown = {
                currentClickListener()
                indicationScope.launch {
                    press = PressInteraction.Press(it.position).apply {
                        interactionSource.emit(this)
                    }
                    buttonState = PressState.Pressed
                }
            },
            onUp = {
                press?.let {
                    indicationScope.launch {
                        interactionSource.emit(PressInteraction.Release(it))
                        press = null
                        cancelBounce()
                    }
                }
            },
        )
    }.then(
        Modifier.indication(
            interactionSource = interactionSource,
            indication = indication
        ).pointerInput(interactionSource, isEnabled) {
            coroutineScope {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val job = launch {
                        delay(initialDelayMillis)
                        currentDelayMillis = maxDelayMillis
                        while (isEnabled && down.pressed) {
                            currentlyRepeating = true
                            currentClickListener()
                            triggerBounce()
                            delay(currentDelayMillis)
                            val nextMillis =
                                currentDelayMillis - (currentDelayMillis * delayDecayFactor)
                            currentDelayMillis = nextMillis.toLong().coerceAtLeast(minDelayMillis)
                        }
                    }
                    waitForUpOrCancellation()
                    job.cancel()
                    currentlyRepeating = false
                    indicationScope.launch {
                        press?.let {
                            interactionSource.emit(PressInteraction.Release(it))
                        }
                        press = null
                        cancelBounce()
                    }
                }
            }
        }.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}