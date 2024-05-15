package composable.lifecounter


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import composable.dialog.MiddleButtonDialog
import data.Player
import data.SettingsManager.alt4PlayerLayout
import data.SettingsManager.numPlayers
import getAnimationCorrectionFactor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.middle_icon
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.vectorResource

/**
 * The main life counting screen of the app
 * @param component The life counter component
 * @param toggleTheme Callback to toggle the theme
 */
@Composable
fun LifeCounterScreen(
    component: LifeCounterComponent, toggleTheme: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showDialog) {
        if (!showDialog) component.blurBackground.value = false
    }

    LaunchedEffect(component.showButtons.value) {
        launch {
            delay(1)
            if (!component.showButtons.value) {
                component.showButtons.value = true
            }
        }
    }

    BoxWithConstraints(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).then(
            if (component.blurBackground.value) {
                Modifier.blur(radius = 20.dp)
            } else {
                Modifier
            }
        )
    ) {
        val m = LifeCounterMeasurements(
            maxWidth = maxWidth, maxHeight = maxHeight, numPlayers = numPlayers, alt4Layout = alt4PlayerLayout
        )
        LazyColumn(modifier = Modifier.fillMaxSize(), userScrollEnabled = false, verticalArrangement = Arrangement.Center, content = {
            m.rows.forEach { buttonPlacements ->
                item {
                    LazyRow(modifier = Modifier.fillMaxSize(), userScrollEnabled = false, horizontalArrangement = Arrangement.Center, content = {
                        buttonPlacements.forEach {
                            item {
                                AnimatedPlayerButton(
                                    visible = component.showButtons, player = component.activePlayers[it.index], rotation = it.angle, width = it.width, height = it.height, component
                                )
                            }
                        }
                    })
                }
            }
        })

        if (!component.showButtons.value) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        }

        Column(Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(0 + m.middleOffset()))
            AnimatedMiddleButton(modifier = Modifier.align(Alignment.CenterHorizontally).size(50.dp), visible = component.showButtons, onMiddleButtonClick = {
                showDialog = true
            })
            Spacer(modifier = Modifier.weight(1 - m.middleOffset()))
        }
    }

    if (showDialog) {
        MiddleButtonDialog(modifier = Modifier.onGloballyPositioned { _ ->
            component.blurBackground.value = showDialog
        }, onDismiss = { showDialog = false }, component = component, toggleTheme = { toggleTheme() })
    }
}

/**
 * A wrapper for the player button that animates it in and out
 * @param visible Whether the button is visible
 * @param player The player to display
 * @param rotation The rotation of the button
 * @param width The width of the button
 * @param height The height of the button
 * @param component The life counter component
 */
@Composable
fun AnimatedPlayerButton(
    visible: MutableState<Boolean>, player: Player, rotation: Float, width: Dp, height: Dp, component: LifeCounterComponent
) {
    val multiplesAway = 3
    val duration = (1250 / getAnimationCorrectionFactor()).toInt()
    val targetOffsetY = if (visible.value) 0f else {
        when (rotation) {
            0f -> height.value * multiplesAway
            90f -> 0f
            180f -> -height.value * multiplesAway
            270f -> 0f
            else -> height.value * multiplesAway
        }
    }

    val targetOffsetX = if (visible.value) 0f else {
        when (rotation) {
            0f -> 0f
            90f -> -width.value * multiplesAway
            180f -> 0f
            270f -> width.value * multiplesAway
            else -> 0f
        }
    }

    val offsetX = remember { Animatable(targetOffsetX) }
    val offsetY = remember { Animatable(targetOffsetY) }

    LaunchedEffect(visible.value) {
        launch {
            if (visible.value) {
                offsetX.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = duration))
            } else {
                offsetX.snapTo(targetOffsetX)
            }
        }

        launch {
            if (visible.value) {
                offsetY.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = duration))
            } else {
                offsetY.snapTo(targetOffsetY)
            }
        }
    }

    Box(modifier = Modifier.offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }) {
        PlayerButton(
            modifier = Modifier.width(width).height(height), player = player, rotation = rotation, component = component
        )
    }
}

/**
 * A wrapper for the middle settings button that animates it in and out
 * @param modifier The modifier to apply to the button
 * @param onMiddleButtonClick Callback for when the button is clicked
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun AnimatedMiddleButton(
    modifier: Modifier = Modifier, onMiddleButtonClick: () -> Unit, visible: MutableState<Boolean>
) {
    var animationFinished by remember { mutableStateOf(false) }

    val popInDuration = (1100 / getAnimationCorrectionFactor()).toInt()
    var angle by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(0f) }

    // Use Animatable for smooth animation
    val animatableAngle = remember { Animatable(0f) }
    val animatableScale = remember { Animatable(0f) }

    LaunchedEffect(visible.value) {
        launch {
            if (visible.value) {
                animatableAngle.animateTo(
                    targetValue = 360f, animationSpec = tween(durationMillis = popInDuration, easing = LinearOutSlowInEasing)
                )
            } else {
                animatableAngle.snapTo(0f)
            }
        }

        launch {
            if (visible.value) {
                animationFinished = false
                animatableScale.animateTo(
                    targetValue = 1f, animationSpec = tween(durationMillis = popInDuration, easing = LinearOutSlowInEasing)
                )
            } else {
                animatableScale.snapTo(0f)
            }
            animationFinished = true
        }
    }

    scale = animatableScale.value
    angle = animatableAngle.value

    Box(modifier = modifier.background(
        color = MaterialTheme.colorScheme.background, shape = CircleShape
    ).rotate(angle).graphicsLayer {
        scaleX = scale
        scaleY = scale
    }.pointerInput(Unit) {
        detectTapGestures(onPress = {
            onMiddleButtonClick()
        })
    }) {
        Image(
            modifier = Modifier.fillMaxSize().align(Alignment.Center), imageVector = vectorResource(Res.drawable.middle_icon), contentScale = ContentScale.Crop, contentDescription = null
        )
    }
}