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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import composable.SettingsButton
import composable.modifier.ShakeConfig
import composable.modifier.bounceClick
import composable.modifier.rememberShakeController
import composable.modifier.shake
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.d10_icon
import lifelinked.shared.generated.resources.d12_icon
import lifelinked.shared.generated.resources.d20_icon
import lifelinked.shared.generated.resources.d4_icon
import lifelinked.shared.generated.resources.d6_icon
import lifelinked.shared.generated.resources.d8_icon
import lifelinked.shared.generated.resources.enter_icon
import org.jetbrains.compose.resources.vectorResource
import theme.scaledSp


@Composable
fun DiceRollDialogContent(
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf("100") }
    var customDieValue by remember { mutableStateOf(100) }
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
        4 to vectorResource(Res.drawable.d4_icon),
        6 to vectorResource(Res.drawable.d6_icon),
        8 to vectorResource(Res.drawable.d8_icon),
        10 to vectorResource(Res.drawable.d10_icon),
        12 to vectorResource(Res.drawable.d12_icon),
        20 to vectorResource(Res.drawable.d20_icon)
    )

    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    fun setLastResult(r: Int, fv: Int) {
        size = 1.0f
        lastResult = r
        faceValue = fv
        size = pressedSize
    }

    BoxWithConstraints(modifier = modifier) {
        val diceRollButtonSize = remember(Unit) { maxWidth / 11f + maxHeight / 12f }
        val resultTextSize = remember(Unit) { diceRollButtonSize.value / 6f }
        val textFieldHeight = remember(Unit) { diceRollButtonSize / 4f + maxHeight / 25f }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
//            Spacer(modifier = Modifier.weight(0.2f))
            val diceRollButtonModifier = Modifier.size(diceRollButtonSize).padding(bottom = diceRollButtonSize / 5f)
            Box(Modifier.weight(1.0f)) {
                Column(
                    modifier = Modifier.wrapContentSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GridDialogContent(modifier = Modifier.wrapContentSize(), title = "Tap to roll", items = listOf({
                        DiceRollButton(modifier = diceRollButtonModifier, value = 4, imageVector = vectorResource(Res.drawable.d4_icon), resultCallBack = {
                            setLastResult(it, 4)
                            focusManager.clearFocus()
                        })
                    }, {
                        DiceRollButton(modifier = diceRollButtonModifier, value = 6, imageVector = vectorResource(Res.drawable.d6_icon), resultCallBack = {
                            setLastResult(it, 6)
                            focusManager.clearFocus()
                        })
                    }, {
                        DiceRollButton(modifier = diceRollButtonModifier, value = 8, imageVector = vectorResource(Res.drawable.d8_icon), resultCallBack = {
                            setLastResult(it, 8)
                            focusManager.clearFocus()
                        })
                    }, {
                        DiceRollButton(modifier = diceRollButtonModifier, value = 10, imageVector = vectorResource(Res.drawable.d10_icon), resultCallBack = {
                            setLastResult(it, 10)
                            focusManager.clearFocus()
                        })
                    }, {
                        DiceRollButton(modifier = diceRollButtonModifier, value = 12, imageVector = vectorResource(Res.drawable.d12_icon), resultCallBack = {
                            setLastResult(it, 12)
                            focusManager.clearFocus()
                        })
                    }, {
                        DiceRollButton(modifier = diceRollButtonModifier, value = 20, imageVector = vectorResource(Res.drawable.d20_icon), resultCallBack = {
                            setLastResult(it, 20)
                            focusManager.clearFocus()
                        })
                    }, {

                    }, {
                        DiceRollButton(modifier = diceRollButtonModifier, value = customDieValue, imageVector = vectorResource(Res.drawable.d20_icon), resultCallBack = {
                            setLastResult(it, 20)
                            focusManager.clearFocus()
                        })
                    }
                    ))
                }
            }
            Spacer(modifier = Modifier.weight(0.005f))

            TextFieldWithButton(modifier = Modifier.width(diceRollButtonSize * 3).height(textFieldHeight), value = textFieldValue, onValueChange = {
                if (it.toIntOrNull() != null) {
                    textFieldValue = it
                    customDieValue = it.toInt()
                } else if (it.isEmpty()) {
                    textFieldValue = ""
                    customDieValue = 0
                }
            }, label = "Custom Die Value", keyboardType = KeyboardType.Number, onDone = {
                focusManager.clearFocus()
            }) {
                IconButton(
                    onClick = {
                        focusManager.clearFocus()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }, modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.enter_icon), contentDescription = "Enter", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.05f))
            Text(
                text = "Last result", color = MaterialTheme.colorScheme.onPrimary, fontSize = resultTextSize.scaledSp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().weight(0.1f)
            )
            Spacer(modifier = Modifier.weight(0.005f))
            Box(
                modifier = Modifier.weight(0.35f).aspectRatio(1.0f).align(Alignment.CenterHorizontally).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f), RoundedCornerShape(15))
                    .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f), RoundedCornerShape(15))
            ) {
                SettingsButton(modifier = Modifier.fillMaxSize().bounceClick(bounceAmount = 0.02f).graphicsLayer(scaleX = animatedSize, scaleY = animatedSize).then(
                    if (lastResult == null) {
                        Modifier.alpha(0.001f)
                    } else {
                        Modifier
                    }
                ),
                    imageVector = if (faceValue != null) resources[faceValue]!! else vectorResource(Res.drawable.d20_icon),
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
                                fontSize = if (lastResult.toString().length >= 3) maxHeight.value.scaledSp / (5 + (lastResult.toString().length - 2) * 1.3f) else maxHeight.value.scaledSp / 5,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.wrapContentHeight().fillMaxWidth().align(Alignment.Center)
                            )
                        }
                    })
            }
            Spacer(modifier = Modifier.weight(0.025f))
        }
    }
}

@Composable
fun DiceRollButton(
    value: Int,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    backgroundColor: Color = Color.Transparent,
    imageVector: ImageVector = vectorResource(Res.drawable.d20_icon),
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
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(60)
            }
            resultCallBack(faceValue)
            isEnabled = true
        }
    }

    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.Center,
    ) {
        SettingsButton(modifier = modifier.bounceClick(bounceAmount = 0.04f).shake(shakeController),
            shape = shape,
            backgroundColor = backgroundColor,
            imageVector = imageVector,
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
                        fontSize = if (faceValue.toString().length >= 3) maxHeight.value.scaledSp / (5 + (faceValue.toString().length - 2) * 1.3f) else maxHeight.value.scaledSp / 5,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.wrapContentHeight().fillMaxWidth().align(Alignment.Center)
                    )
                }
            })
    }
}

