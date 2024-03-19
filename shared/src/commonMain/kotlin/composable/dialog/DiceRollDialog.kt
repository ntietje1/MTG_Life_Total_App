package composable.dialog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import composable.modifier.ShakeConfig
import composable.modifier.bounceClick
import composable.modifier.rememberShakeController
import composable.modifier.shake
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import theme.scaledSp

/**
 * A dialog that allows the user to roll dice
 * @param modifier the modifier for this composable
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun DiceRollDialogContent(
    modifier: Modifier = Modifier
) {
    var lastResult: Int? by remember { mutableStateOf(null) }
    var faceValue: Int? by remember { mutableStateOf(null) }
    var size by remember { mutableStateOf(1.0f) }
    val pressedSize = 1.05f
    val coroutineScope = rememberCoroutineScope()
    val animatedSize by animateFloatAsState(targetValue = size, animationSpec = tween(durationMillis = 20), finishedListener = {
        coroutineScope.launch {
            delay(0)
            size = 1.0f
        }
    })
    val resources = mapOf(
        4 to painterResource("d4_icon.xml"),
        6 to painterResource("d6_icon.xml"),
        8 to painterResource("d8_icon.xml"),
        10 to painterResource("d10_icon.xml"),
        12 to painterResource("d12_icon.xml"),
        20 to painterResource("d20_icon.xml")
    )

    fun setLastResult(r: Int, fv: Int) {
        size = 1.0f
        lastResult = r
        faceValue = fv
        size = pressedSize
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        Spacer(modifier = Modifier.weight(0.1f))
        GridDialogContent(modifier = Modifier.weight(0.8f), title = "Tap to roll", items = listOf({
            DiceRollButton(value = 4, imageResource = painterResource("d4_icon.xml"), resultCallBack = { setLastResult(it, 4) })
        }, {
            DiceRollButton(value = 6, imageResource = painterResource("d6_icon.xml"), resultCallBack = { setLastResult(it, 6) })
        }, {
            DiceRollButton(value = 8, imageResource = painterResource("d8_icon.xml"), resultCallBack = { setLastResult(it, 8) })
        }, {
            DiceRollButton(value = 10, imageResource = painterResource("d10_icon.xml"), resultCallBack = { setLastResult(it, 10) })
        }, {
            DiceRollButton(value = 12, imageResource = painterResource("d12_icon.xml"), resultCallBack = { setLastResult(it, 12) })
        }, {
            DiceRollButton(value = 20, imageResource = painterResource("d20_icon.xml"), resultCallBack = { setLastResult(it, 20) })
        }))

        Text(
            text = "Last result", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.scaledSp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier.weight(0.35f).aspectRatio(1.0f).align(Alignment.CenterHorizontally).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        ) {
            SettingsButton(modifier = Modifier.fillMaxSize().bounceClick(amount = 0.02f).graphicsLayer(scaleX = animatedSize, scaleY = animatedSize).then(
                if (lastResult == null) {
                    Modifier.alpha(0.001f)
                } else {
                    Modifier
                }
            ),
                imageResource = if (faceValue != null) resources[faceValue]!! else painterResource("d20_icon.xml"),
                text = "",
                mainColor = MaterialTheme.colorScheme.onPrimary,
                shadowEnabled = false,
                enabled = false,
                overlay = {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = if (lastResult != null) lastResult.toString() else "",
                            color = MaterialTheme.colorScheme.background,
                            fontWeight = FontWeight.Bold,
                            fontSize = maxHeight.value.scaledSp / 5,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.wrapContentHeight().fillMaxWidth().align(Alignment.Center)
                        )
                    }
                })
//                DiceRollButton(value = lastResult!!, modifier = Modifier.fillMaxSize(), imageResource = resources[faceValue]!!)

//            if (lastResult != null && faceValue != null) {
//                Text(
//                    modifier = Modifier.align(Alignment.Center).wrapContentSize(),
//                    text = lastResult.toString(),
//                    color = MaterialTheme.colorScheme.onPrimary,
//                    fontSize = 50.scaledSp,
//                    lineHeight = 50.scaledSp,
//                    fontWeight = FontWeight.Bold,
//                    textAlign = TextAlign.Center,
//                )
//            }
        }
        Spacer(modifier = Modifier.weight(0.05f))
    }
}

/**
 * A button that rolls a die
 * @param value the value of the die
 * @param modifier the modifier for this composable
 * @param shape the shape of the button
 * @param backgroundColor the background color of the button
 * @param imageResource the image resource of the button
 * @param mainColor the color of the text
 * @param enabled whether the button is enabled
 * @param visible whether the button is visible
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun DiceRollButton(
    value: Int,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    backgroundColor: Color = Color.Transparent,
    imageResource: Painter = painterResource("d20_icon.xml"),
    mainColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
    visible: Boolean = true,
    resultCallBack: (Int) -> Unit = {}
) {

    var faceValue: Int by remember { mutableIntStateOf(value) }
    var isEnabled by remember { mutableStateOf(enabled) }
    val haptic = LocalHapticFeedback.current

    val scope = rememberCoroutineScope()

    val shuffleScope = CoroutineScope(scope.coroutineContext)
    val shakeController = rememberShakeController()

    fun roll() {
        shuffleScope.launch {
            for (i in 1..5) {
                isEnabled = false
                val result = (1..value).random()
                faceValue = result
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(60)
            }
            resultCallBack(faceValue)
            isEnabled = true
        }
    }

    SettingsButton(modifier = modifier.bounceClick(amount = 0.04f).shake(shakeController),
        shape = shape,
        backgroundColor = backgroundColor,
        imageResource = imageResource,
        text = "D$value",
        mainColor = mainColor,
        shadowEnabled = false,
        enabled = isEnabled,
        visible = visible,
        onPress = {
            roll()
            shakeController.shake(ShakeConfig(6, translateX = 7.5f, rotate = 0.5f, rotateY = 12.5f))
        },
        overlay = {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = faceValue.toString(),
                    color = MaterialTheme.colorScheme.background,
                    fontWeight = FontWeight.Bold,
                    fontSize = maxHeight.value.scaledSp / 5,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.wrapContentHeight().fillMaxWidth().align(Alignment.Center)
                )
            }
        })
}

