package ui.lifecounter


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import di.getAnimationCorrectionFactor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.middle_icon
import org.jetbrains.compose.resources.vectorResource
import theme.blendWith
import ui.dialog.MiddleButtonDialog
import ui.lifecounter.playerbutton.PlayerButton
import ui.modifier.routePointerChangesTo

@Composable
fun LifeCounterScreen(
    viewModel: LifeCounterViewModel,
    toggleTheme: () -> Unit,
    toggleKeepScreenOn: () -> Unit,
    goToPlayerSelectScreen: (Boolean) -> Unit,
    goToTutorialScreen: () -> Unit,
    numPlayers: Int,
    timerEnabled: Boolean,
    firstNavigation: Boolean
) {
    val state by viewModel.state.collectAsState()
    var showMiddleDialog by remember { mutableStateOf(false) }
    var firstPlayerSelectionActive by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun promptFirstPlayer() {
        if (state.numPlayers == 1) {
            viewModel.setFirstPlayer(0)
            viewModel.setTimerEnabled(timerEnabled)
        } else if (state.firstPlayer == null) {
            viewModel.askForFirstPlayer()
            firstPlayerSelectionActive = true
        } else {
            viewModel.setTimerEnabled(timerEnabled)
        }
    }

    if (timerEnabled) { // Features that require first player
        if (state.firstPlayer == null && !firstPlayerSelectionActive) {
            promptFirstPlayer()
        }
    } else {
        viewModel.killTimer()
    }

    LaunchedEffect(Unit) {
        viewModel.setNumPlayers(numPlayers)
    }

    if (showMiddleDialog) {
        MiddleButtonDialog(modifier = Modifier.onGloballyPositioned { _ ->
            viewModel.setBlurBackground(showMiddleDialog)
        },
            onDismiss = { showMiddleDialog = false },
            viewModel = viewModel,
            toggleTheme = { toggleTheme() },
            toggleKeepScreenOn = { toggleKeepScreenOn() },
            goToPlayerSelectScreen = { changeNumPlayers ->
                viewModel.setShowButtons(false)
                goToPlayerSelectScreen(changeNumPlayers)
            },
            setNumPlayers = { viewModel.setNumPlayers(it) },
            triggerEnterAnimation = {
                scope.launch {
                    showMiddleDialog = false
                    viewModel.setShowButtons(false)
                    delay(10)
                    viewModel.setShowButtons(true)
                }
            },
            updateTurnTimerEnabled = { viewModel.setTimerEnabled(it) },
            goToTutorialScreen = goToTutorialScreen
        )
    }

    LaunchedEffect(Unit) {
        viewModel.onNavigate(firstNavigation)
    }

    LaunchedEffect(showMiddleDialog) {
        viewModel.setBlurBackground(showMiddleDialog)
    }

    BoxWithConstraints(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        val m = LifeCounterMeasurements(
            maxWidth = maxWidth, maxHeight = maxHeight, numPlayers = state.numPlayers, alt4Layout = viewModel.settingsManager.alt4PlayerLayout
        )
        val buttonPadding = maxWidth / 750f + maxHeight / 750f
        val buttonPlacements = m.buttonPlacements()
        val blurRadius = maxHeight / 50f
        val middleButtonSize = (30.dp + (maxWidth / 15f + maxHeight / 30f) * 4) / 5
        Box(
            Modifier.fillMaxSize().then(
                if (state.blurBackground) {
                    Modifier.blur(radius = blurRadius)
                } else {
                    Modifier
                }
            )
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize(), userScrollEnabled = false, verticalArrangement = Arrangement.Center, content = {
                items(buttonPlacements, key = { it.hashCode() }) { buttonPlacements ->
                    LazyRow(modifier = Modifier.fillMaxSize(), userScrollEnabled = false, horizontalArrangement = Arrangement.Center, content = {
                        items(buttonPlacements, key = { it.index }) { placement ->
                            val width = placement.width - buttonPadding * 4
                            val height = placement.height - buttonPadding * 4
                            val rotation = placement.angle
                            val turnTimerAlignment = placement.timerAlignment
                            val playerButtonViewModel = viewModel.playerButtonViewModels[placement.index]
                            val timerColor = playerButtonViewModel.state.value.player.textColor
                            val topCornerRadius = (min(width, height) * 0.1f + max(width, height) * 0.01f)
                            AnimatedPlayerButton(modifier = Modifier.padding(buttonPadding),
                                visible = state.showButtons,
                                rotation = placement.angle,
                                width = placement.width - buttonPadding * 4,
                                height = placement.height - buttonPadding * 4,
                                playerButton = {
                                    PlayerButton(modifier = Modifier.size(width, height),
                                        turnTimerModifier = Modifier.align(turnTimerAlignment).pointerInput(Unit) {
                                            routePointerChangesTo(onDown = {
                                                playerButtonViewModel.moveTimer()
                                            })
                                        }.then(
                                            when (turnTimerAlignment) {
                                                Alignment.TopStart -> Modifier.align(Alignment.TopStart).background(color = timerColor.blendWith(Color.Black).copy(alpha = 0.25f), shape = RoundedCornerShape(topCornerRadius, 0.dp, topCornerRadius, 0.dp)).border(buttonPadding, timerColor.copy(alpha = 0.8f), shape = RoundedCornerShape(topCornerRadius, 0.dp, topCornerRadius, 0.dp))
                                                Alignment.TopEnd -> Modifier.align(Alignment.TopEnd).background(color = timerColor.blendWith(Color.Black).copy(alpha = 0.25f), shape = RoundedCornerShape(0.dp, topCornerRadius, 0.dp, topCornerRadius)).border(buttonPadding, timerColor.copy(alpha = 0.8f), shape = RoundedCornerShape(0.dp, topCornerRadius, 0.dp, topCornerRadius))
                                                else -> Modifier
                                            }
                                        ),
                                        viewModel = playerButtonViewModel,
                                        rotation = rotation,
                                        borderWidth = buttonPadding,
                                        setBlurBackground = { viewModel.setBlurBackground(it) },
                                        setFirstPlayer = {
                                            viewModel.setFirstPlayer(placement.index)
                                            viewModel.setTimerEnabled(timerEnabled)
                                            firstPlayerSelectionActive = false
                                        })
                                })
                        }
                    })
                }
            })
            val middleButtonOffset = m.middleButtonOffset(middleButtonSize)

            AnimatedMiddleButton(modifier = Modifier.offset(middleButtonOffset.first, middleButtonOffset.second).size(middleButtonSize), visible = state.showButtons, onMiddleButtonClick = {
                showMiddleDialog = true
            })

            if (!state.showButtons || state.showLoadingScreen) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
            }

            Box(
                Modifier.fillMaxSize().then(
                    if (state.blurBackground) {
                        Modifier.background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                    } else {
                        Modifier
                    }
                )
            )
        }
    }
}

@Composable
fun AnimatedPlayerButton(
    modifier: Modifier = Modifier,
    visible: Boolean,
    rotation: Float,
    width: Dp,
    height: Dp,
    playerButton: @Composable (Modifier) -> Unit,
) {
    val multiplesAway = 3f
    val duration = (1250 / getAnimationCorrectionFactor()).toInt()
    val targetOffsetY = remember(visible, rotation) {
        if (visible) 0f else {
            when (rotation) {
                0f -> height.value * multiplesAway
                90f -> 0f
                180f -> -height.value * multiplesAway
                270f -> 0f
                else -> height.value * multiplesAway
            }
        }
    }

    val targetOffsetX = remember(visible, rotation) {
        if (visible) 0f else {
            when (rotation) {
                0f -> 0f
                90f -> -width.value * multiplesAway
                180f -> 0f
                270f -> width.value * multiplesAway
                else -> 0f
            }
        }
    }

    val offsetX = remember { Animatable(targetOffsetX) }
    val offsetY = remember { Animatable(targetOffsetY) }

    LaunchedEffect(visible) {
        launch {
            if (visible) {
                offsetX.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = duration))
            } else {
                offsetX.snapTo(targetOffsetX)
            }
        }

        launch {
            if (visible) {
                offsetY.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = duration))
            } else {
                offsetY.snapTo(targetOffsetY)
            }
        }
    }

    Box(modifier = modifier.graphicsLayer {
        translationX = offsetX.value
        translationY = offsetY.value
    }) {
        playerButton(Modifier.size(width, height))
//        PlayerButton(
//            modifier = Modifier.size(width, height), viewModel = playerButtonViewModel, rotation = rotation,
//            borderWidth = borderWidth,
//            setBlurBackground = {
//                setBlurBackground(it)
//            }
//        )
    }
}

@Composable
fun AnimatedMiddleButton(
    modifier: Modifier = Modifier, onMiddleButtonClick: () -> Unit, visible: Boolean
) {
    var animationFinished by remember { mutableStateOf(false) }

    val popInDuration = (1100 / getAnimationCorrectionFactor()).toInt()
    var angle by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(0f) }

    // Use Animatable for smooth animation
    val animatableAngle = remember { Animatable(0f) }
    val animatableScale = remember { Animatable(0f) }

    LaunchedEffect(visible) {
        launch {
            if (visible) {
                animatableAngle.animateTo(
                    targetValue = 360f, animationSpec = tween(durationMillis = popInDuration, easing = LinearOutSlowInEasing)
                )
            } else {
                animatableAngle.snapTo(0f)
            }
        }

        launch {
            if (visible) {
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