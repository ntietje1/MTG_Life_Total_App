package composable.modifier


import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import legacyMonarchyIndicator
import kotlin.math.max

/**
 * Modifier that adds a border to a card that animates a list of colors
 * @param shape The shape of the card
 * @param borderWidth The width of the border
 * @param colors The list of colors to animate
 * @param animationDuration The duration of the animation
 * @return The modifier
 */
fun Modifier.animatedBorderCard(
    shape: Shape = RoundedCornerShape(size = 0.dp),
    borderWidth: Dp = 2.dp,
    colors: List<Color> = listOf(Color.Gray, Color.White),
    animationDuration: Int = 10000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "Infinite Color Animation")
    if (!legacyMonarchyIndicator()) {
        val degrees by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = animationDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "Infinite Rotating Colors"
        )
        val gradient = Brush.sweepGradient(colors)

        this.then(
            Modifier
                .clip(shape)
                .then(Modifier
                    .fillMaxWidth()
                    .padding(borderWidth)
                    .drawBehind {
                        rotate(degrees = degrees) {
                            drawCircle(
                                brush = gradient,
                                radius = max(size.width, size.height),
                                blendMode = BlendMode.SrcIn,
                            )
                        }
                    }
                    .clip(shape)))
    } else {
        val minScale = 1.25f
        val maxScale= 0.0f
        val scale by infiniteTransition.animateFloat(
            initialValue = minScale,
            targetValue = maxScale,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = animationDuration/8, delayMillis = animationDuration/32, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "Infinite Pulsing Colors"
        )
        this.then(
            Modifier
                .clip(shape)
                .then(Modifier
                    .fillMaxWidth()
                    .padding(borderWidth)
                    .border(
                        width = borderWidth*scale,
                        brush = Brush.radialGradient(colors),
                        shape = shape)
                    .clip(shape)))
    }
}








