package ui.dialog.dice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import org.koin.compose.koinInject
import theme.LocalDimensions
import theme.halfAlpha
import theme.scaledSp
import ui.components.SettingsButton
import ui.components.TextFieldWithButton
import ui.dialog.GridDialogContent
import ui.modifier.ShakeConfig
import ui.modifier.bounceClick
import ui.modifier.rememberShakeController
import ui.modifier.shake

@Composable
fun DiceRollDialogContent(
    modifier: Modifier = Modifier,
    viewModel: DiceRollViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current

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
        4u to vectorResource(Res.drawable.d4_icon),
        6u to vectorResource(Res.drawable.d6_icon),
        8u to vectorResource(Res.drawable.d8_icon),
        10u to vectorResource(Res.drawable.d10_icon),
        12u to vectorResource(Res.drawable.d12_icon),
        20u to vectorResource(Res.drawable.d20_icon)
    )

    fun setLastResult(r: UInt, fv: UInt) {
        size = 1.0f
        viewModel.setLastResult(r, fv)
        size = pressedSize
    }

    BoxWithConstraints(modifier = modifier) {
        val diceRollButtonSize = remember(Unit) { maxWidth / 11f + maxHeight / 12f }
        val resultTextSize = remember(Unit) { diceRollButtonSize.value / 6f }
        val textFieldHeight = remember(Unit) { diceRollButtonSize / 3f + maxWidth / 15f }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
            val diceRollButtonModifier = Modifier.size(diceRollButtonSize).padding(bottom = diceRollButtonSize / 5f)
            Box(Modifier.weight(1.0f)) {
                Column(
                    modifier = Modifier.wrapContentSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GridDialogContent(modifier = Modifier.wrapContentSize(), title = "Tap to roll", items = listOf({
                        DiceRollButton(modifier = diceRollButtonModifier, value = 4u, imageVector = vectorResource(Res.drawable.d4_icon),
                            resultCallBack = {
                                setLastResult(it, 4u)
                            })
                    }, {
                        DiceRollButton(modifier = diceRollButtonModifier, value = 6u, imageVector = vectorResource(Res.drawable.d6_icon),
                            resultCallBack = {
                                setLastResult(it, 6u)
                            })
                    }, {
                        DiceRollButton(modifier = diceRollButtonModifier, value = 8u, imageVector = vectorResource(Res.drawable.d8_icon),
                            resultCallBack = {
                                setLastResult(it, 8u)
                            })
                    }, {
                        DiceRollButton(modifier = diceRollButtonModifier, value = 10u, imageVector = vectorResource(Res.drawable.d10_icon),
                            resultCallBack = {
                                setLastResult(it, 10u)
                            })
                    }, {
                        DiceRollButton(modifier = diceRollButtonModifier, value = 12u, imageVector = vectorResource(Res.drawable.d12_icon),
                            resultCallBack = {
                                setLastResult(it, 12u)
                            })
                    }, {
                        DiceRollButton(modifier = diceRollButtonModifier, value = 20u, imageVector = vectorResource(Res.drawable.d20_icon),
                            resultCallBack = {
                                setLastResult(it, 20u)
                            })
                    }, {

                    }, {
                        DiceRollButton(modifier = diceRollButtonModifier, value = state.customDieValue, imageVector = vectorResource(Res.drawable.d20_icon),
                            enabled = state.textFieldValue.text != "" && state.textFieldValue.text.toUIntOrNull() != null && state.textFieldValue.text.toUInt() > 0u,
                            resultCallBack = {
                                setLastResult(it, 20u)
                            })
                    }))
                }
            }
            Spacer(modifier = Modifier.weight(0.005f))

            TextFieldWithButton(
                modifier = Modifier.width(diceRollButtonSize * 3).height(textFieldHeight)
                    .border(
                        dimensions.borderThin, MaterialTheme.colorScheme.onPrimary.halfAlpha(), RoundedCornerShape(15)
                    ), value = state.textFieldValue, onValueChange = viewModel::setTextFieldValue, label = "Custom Die Value", keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                ), keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                })
            ) {
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
            Column(modifier.weight(0.45f)) {
                Text(
                    text = "Last result",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = dimensions.textSmall.scaledSp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(dimensions.paddingSmall))
                Box(
                    modifier = Modifier.aspectRatio(1.0f).align(Alignment.CenterHorizontally)
                        .border(dimensions.borderThin, MaterialTheme.colorScheme.onPrimary.halfAlpha(), RoundedCornerShape(15))
                ) {
                    SettingsButton(modifier = Modifier.fillMaxSize().bounceClick(bounceAmount = 0.02f).graphicsLayer(scaleX = animatedSize, scaleY = animatedSize).then(
                        if (state.lastResult == null) {
                            Modifier.alpha(0.001f)
                        } else {
                            Modifier
                        }
                    ),
                        imageVector = if (state.faceValue != null) resources[state.faceValue]!! else vectorResource(Res.drawable.d20_icon),
                        text = "",
                        mainColor = MaterialTheme.colorScheme.onPrimary,
                        shadowEnabled = false,
                        enabled = false,
                        overlay = {
                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = if (state.lastResult != null) state.lastResult.toString() else "",
                                    color = MaterialTheme.colorScheme.background,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (state.lastResult.toString().length >= 3) maxHeight.value.scaledSp / (5 + (state.lastResult.toString().length - 2) * 1.3f) else maxHeight.value.scaledSp / 5,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.wrapContentHeight().fillMaxWidth().align(Alignment.Center)
                                )
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.weight(0.025f))
        }
    }
}

@Composable
fun DiceRollButton(
    value: UInt,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    backgroundColor: Color = Color.Transparent,
    imageVector: ImageVector = vectorResource(Res.drawable.d20_icon),
    mainColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
    visible: Boolean = true,
    resultCallBack: (UInt) -> Unit = {}
) {
    var faceValue: UInt by remember { mutableStateOf(value) }
    var isEnabled by remember { mutableStateOf(enabled) }
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val scope = rememberCoroutineScope()

    val shuffleScope = CoroutineScope(scope.coroutineContext)
    val shakeController = rememberShakeController()

    fun roll() {
        shuffleScope.launch {
            for (i in 1..5) {
                isEnabled = false
                val result = if (value == 0u) {
                    0u
                } else {
                    (1u..value).random()
                }
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
                focusManager.clearFocus()
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
