package mtglifeappcompose.views

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.lang.Float.max

// replace outer surface with clipToBounds modifier
@Composable
fun AnimatedBorderCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(30.dp),
    borderWidth: Dp = 4.dp,
    gradient: Brush = Brush.sweepGradient(listOf(Color.Magenta, Color.Cyan)),
    animationDuration: Int = 10000,
    content: @Composable (Size) -> Unit

) {
    val contentSize = remember { mutableStateOf(Size(0f, 0f)) }
    val infiniteTransition = rememberInfiniteTransition(label = "Infinite Color Animation")
    val degrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Infinite Colors"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .padding(borderWidth)
            .drawBehind {
                rotate(degrees = degrees) {
                    // Calculate the radius based on content size
                    val radius = (max(size.width, size.height))
                    drawCircle(
                        brush = gradient,
                        radius = radius,
                        blendMode = BlendMode.SrcIn,
                    )
                }
            }
    ) {
        content(contentSize.value)
    }
}





