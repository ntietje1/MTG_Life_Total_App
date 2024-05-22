package composable.modifier

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Modifier.repeatingClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    initialDelayMillis: Long = 500,
    maxDelayMillis: Long = 100,
    minDelayMillis: Long = 50,
    delayDecayFactor: Float = 0.0f,
    onPress: () -> Unit
): Modifier = composed {
    val currentClickListener by rememberUpdatedState(onPress)
    val isEnabled by rememberUpdatedState(enabled)

    val indicationScope = rememberCoroutineScope()
    var press = PressInteraction.Press(Offset.Zero)

    pointerInput(Unit) {
        detectTapGestures(onPress = {
            currentClickListener()
            indicationScope.launch {
                press = PressInteraction.Press(it)
                interactionSource.emit(press)
            }
        })
    }.then(
        Modifier.pointerInput(interactionSource, isEnabled) {
            coroutineScope {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val job = launch {
                        delay(initialDelayMillis)
                        var currentDelayMillis = maxDelayMillis
                        while (isEnabled && down.pressed && !down.isOutOfBounds(size, extendedTouchPadding)) {
                            currentClickListener()
                            delay(currentDelayMillis)
                            val nextMillis =
                                currentDelayMillis - (currentDelayMillis * delayDecayFactor)
                            currentDelayMillis = nextMillis.toLong().coerceAtLeast(minDelayMillis)
                        }
                    }
                    waitForUpOrCancellation()
                    job.cancel()
                    indicationScope.launch {
                        interactionSource.emit(PressInteraction.Release(press))
                    }
                }
            }
        }
    )
}