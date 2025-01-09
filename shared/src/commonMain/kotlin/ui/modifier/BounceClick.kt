package ui.modifier

import androidx.compose.foundation.Indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun Modifier.bounceClick(
    initialBounceFactor: Float = 2.0f,
    bounceAmount: Float = 0.0075f,
    bounceDuration: Long = 60L,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    indication: Indication? = null,
    repeatEnabled: Boolean = false,
    initialDelayMillis: Long = 500,
    maxDelayMillis: Long = 100,
    minDelayMillis: Long = 50,
    delayDecayFactor: Float = 0.0f,
    onPress: () -> Unit = {}
): Modifier {
    return repeatingClickable(
        interactionSource = interactionSource,
        indication = indication,
        onPress = onPress,
        enabled = repeatEnabled,
        initialDelayMillis = initialDelayMillis,
        maxDelayMillis = maxDelayMillis,
        minDelayMillis = minDelayMillis,
        delayDecayFactor = delayDecayFactor,
        initialBounceFactor = initialBounceFactor,
        bounceAmount = bounceAmount,
        bounceDuration = bounceDuration
    )
}