package mtglifeappcompose.composable.dialog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtglifeappcompose.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mtglifeappcompose.composable.SettingsButton
import mtglifeappcompose.composable.bounceClick
import kotlin.math.roundToInt

@Composable
fun DiceRollDialogContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit
) {
    GridDialogContent(modifier, items = listOf({
        DiceRollButton(value = 4, imageResource = painterResource(id = R.drawable.d4_icon))
    }, {
        DiceRollButton(value = 6, imageResource = painterResource(id = R.drawable.d6_icon))
    }, {
        DiceRollButton(value = 8, imageResource = painterResource(id = R.drawable.d8_icon))
    }, {
        DiceRollButton(value = 10, imageResource = painterResource(id = R.drawable.d10_icon))
    }, {
        DiceRollButton(value = 12, imageResource = painterResource(id = R.drawable.d12_icon))
    }, {
        DiceRollButton(value = 20, imageResource = painterResource(id = R.drawable.d20_icon))
    }))
}

@Composable
fun DiceRollButton(
    value: Int,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    shape: Shape = RectangleShape,
    backgroundColor: Color = Color.Transparent,
    imageResource: Painter = painterResource(id = R.drawable.d20_icon),
    mainColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
    visible: Boolean = true
) {

    var faceValue: Int by remember { mutableIntStateOf(value) }
    val haptic = LocalHapticFeedback.current

    val scope = rememberCoroutineScope()

    val shuffleScope = CoroutineScope(scope.coroutineContext)
    val shakeController = rememberShakeController()

    fun roll() {
        shuffleScope.launch {
            for (i in 1..5) {
                val result = (1..value).random()
                faceValue = result
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(60)
            }
        }
    }

    SettingsButton(modifier = modifier
        .bounceClick(amount = 0.04f)
        .shake(shakeController),
        size = size,
        shape = shape,
        backgroundColor = backgroundColor,
        imageResource = imageResource,
        text = "D$value",
        mainColor = mainColor,
        shadowEnabled = false,
        enabled = enabled,
        visible = visible,
        onPress = {
            roll()
            shakeController.shake(ShakeConfig(6, translateX = 7.5f, rotate = 0.5f, rotateY = 12.5f))
        },
        overlay = {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = faceValue.toString(),
                    color = MaterialTheme.colorScheme.background,
                    fontWeight = FontWeight.Bold,
                    fontSize = size.value.sp / 6,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .align(Alignment.Center)
                )
            }

        })

}

@Composable
fun rememberShakeController(): ShakeController {
    return remember { ShakeController() }
}

class ShakeController {
    var shakeConfig: ShakeConfig? by mutableStateOf(null)
        private set

    fun shake(shakeConfig: ShakeConfig) {
        this.shakeConfig = shakeConfig
    }
}

data class ShakeConfig(
    val iterations: Int,
    val intensity: Float = 100_000f,
    val rotate: Float = 0f,
    val rotateX: Float = 0f,
    val rotateY: Float = 0f,
    val scaleX: Float = 0f,
    val scaleY: Float = 0f,
    val translateX: Float = 0f,
    val translateY: Float = 0f,
    val trigger: Long = System.currentTimeMillis(),
)

fun Modifier.shake(shakeController: ShakeController) = composed {
    shakeController.shakeConfig?.let { shakeConfig ->
        val shake = remember { Animatable(0f) }
        LaunchedEffect(shakeController.shakeConfig) {
            for (i in 0..shakeConfig.iterations) {
                when (i % 2) {
                    0 -> shake.animateTo(1f, spring(stiffness = shakeConfig.intensity))
                    else -> shake.animateTo(-1f, spring(stiffness = shakeConfig.intensity))
                }
            }
            shake.animateTo(0f)
        }

        this
            .rotate(shake.value * shakeConfig.rotate)
            .graphicsLayer {
                rotationX = shake.value * shakeConfig.rotateX
                rotationY = shake.value * shakeConfig.rotateY
            }
            .scale(
                scaleX = 1f + (shake.value * shakeConfig.scaleX),
                scaleY = 1f + (shake.value * shakeConfig.scaleY),
            )
            .offset {
                IntOffset(
                    (shake.value * shakeConfig.translateX).roundToInt(),
                    (shake.value * shakeConfig.translateY).roundToInt(),
                )
            }
    } ?: this
}

