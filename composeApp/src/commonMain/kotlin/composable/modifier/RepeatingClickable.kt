package composable.modifier

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A modifier that adds a repeating click effect to a clickable
 * @param interactionSource The interaction source to use
 * @param enabled Whether the modifier is enabled
 * @param initialDelayMillis The initial delay before the first click
 * @param maxDelayMillis The maximum delay between clicks
 * @param minDelayMillis The minimum delay between clicks
 * @param delayDecayFactor The amount to decay the delay by
 * @param onPress The callback for when the button is pressed
 * @return The modifier
 */
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

    pointerInput(Unit) {
        detectTapGestures(onPress = {
            currentClickListener()
        })
    }.then(Modifier.pointerInput(interactionSource, isEnabled) {
        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val job = launch {
                    delay(initialDelayMillis)
                    var currentDelayMillis = maxDelayMillis
                    while (isEnabled && down.pressed) {
                        currentClickListener()
                        delay(currentDelayMillis)
                        val nextMillis =
                            currentDelayMillis - (currentDelayMillis * delayDecayFactor)
                        currentDelayMillis = nextMillis.toLong().coerceAtLeast(minDelayMillis)
                    }
                }
                waitForUpOrCancellation()
                job.cancel()
            }
        }
    })
}