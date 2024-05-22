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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import composable.dialog.MiddleButtonDialog
import composable.lifecounter.playerbutton.PlayerButton
import composable.lifecounter.playerbutton.PlayerButtonViewModel
import getAnimationCorrectionFactor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.middle_icon
import org.jetbrains.compose.resources.vectorResource

@Composable
fun LifeCounterScreen(
    viewModel: LifeCounterViewModel,
    toggleTheme: () -> Unit,
    goToPlayerSelectScreen: () -> Unit,
    returnToLifeCounterScreen: () -> Unit,
    goToTutorialScreen: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.DESTROYED -> {
                viewModel.savePlayerStates()
            }

            Lifecycle.State.INITIALIZED -> {
//                viewModel.generatePlayers()
            }

            Lifecycle.State.CREATED -> {}
            Lifecycle.State.STARTED -> {
                viewModel.savePlayerStates()
            }

            Lifecycle.State.RESUMED -> {}
        }
    }

    LaunchedEffect(showDialog) {
        if (!showDialog) viewModel.setBlurBackground(false)
    }

    LaunchedEffect(state.showButtons) {
        launch {
            delay(1)
            if (!state.showButtons) {
                viewModel.setShowButtons(true)
            }
        }
    }

    BoxWithConstraints(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).then(
            if (state.blurBackground) {
                Modifier.blur(radius = 20.dp)
            } else {
                Modifier
            }
        )
    ) {
        val m = LifeCounterMeasurements(
            maxWidth = maxWidth, maxHeight = maxHeight, numPlayers = state.numPlayers, alt4Layout = viewModel.settingsManager.alt4PlayerLayout
        )
        LazyColumn(modifier = Modifier.fillMaxSize(), userScrollEnabled = false, verticalArrangement = Arrangement.Center, content = {
            m.rows.forEach { buttonPlacements ->
                item {
                    LazyRow(modifier = Modifier.fillMaxSize(), userScrollEnabled = false, horizontalArrangement = Arrangement.Center, content = {
                        buttonPlacements.forEach {
                            item {
                                if (viewModel.settingsManager.loadPlayerStates().isEmpty()) {
                                    viewModel.generatePlayers()
                                }
                                AnimatedPlayerButton(
                                    visible = state.showButtons,
                                    playerButtonViewModel = viewModel.playerButtonViewModels[it.index],
                                    rotation = it.angle,
                                    width = it.width,
                                    height = it.height
                                )
                            }
                        }
//                        viewModel.playerButtonViewModels.forEachIndexed { index, playerButtonViewModel ->
//                        val buttonPlacement = buttonPlacements[index]
//                        item {
////                            println("RENDERING BUTTON: $index")
//                            AnimatedPlayerButton(
//                                visible = state.showButtons,
//                                playerButtonViewModel = playerButtonViewModel,
//                                rotation = buttonPlacement.angle,
//                                width = buttonPlacement.width,
//                                height = buttonPlacement.height
//                            )
//                        }
//                    }
                    })
                }
            }
        })

        if (!state.showButtons) {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        }

        Column(Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(0 + m.middleOffset()))
            AnimatedMiddleButton(modifier = Modifier.align(Alignment.CenterHorizontally).size(50.dp), visible = state.showButtons, onMiddleButtonClick = {
                showDialog = true
            })
            Spacer(modifier = Modifier.weight(1 - m.middleOffset()))
        }
    }

    if (showDialog) {
        MiddleButtonDialog(modifier = Modifier.onGloballyPositioned { _ ->
            viewModel.setBlurBackground(showDialog)
        },
            onDismiss = {
                showDialog = false
            },
            viewModel = viewModel,
            toggleTheme = { toggleTheme() },
            goToPlayerSelectScreen = { goToPlayerSelectScreen() },
            setNumPlayers = {
                viewModel.setNumPlayers(it)
                println("SETNUMOFPLAYERS: $it")
            },
            returnToLifeCounterScreen = { returnToLifeCounterScreen() },
            goToTutorialScreen = goToTutorialScreen
        )
    }

}

@Composable
fun AnimatedPlayerButton(
    visible: Boolean, playerButtonViewModel: PlayerButtonViewModel, rotation: Float, width: Dp, height: Dp
) {
    val multiplesAway = 3
    val duration = (1250 / getAnimationCorrectionFactor()).toInt()
    val targetOffsetY = if (visible) 0f else {
        when (rotation) {
            0f -> height.value * multiplesAway
            90f -> 0f
            180f -> -height.value * multiplesAway
            270f -> 0f
            else -> height.value * multiplesAway
        }
    }

    val targetOffsetX = if (visible) 0f else {
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

    Box(modifier = Modifier.offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }) {
        PlayerButton(
            modifier = Modifier.width(width).height(height), viewModel = playerButtonViewModel, rotation = rotation, onOpenDialog = {
                //TODO: toggle blur
            }
        )
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