package mtglifeappcompose.composable.lifecounter


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.mtglifeappcompose.R
import kotlinx.coroutines.launch
import mtglifeappcompose.composable.dialog.MiddleButtonDialog
import mtglifeappcompose.data.Player

@Composable
fun LifeCounterScreen(
    players: MutableList<Player>,
    resetPlayers: () -> Unit,
    setPlayerNum: (Int) -> Unit,
    setStartingLife: (Int) -> Unit,
    goToPlayerSelect: () -> Unit,
    toggleTheme: () -> Unit
) {
    val numPlayers = players.size

    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    val angleConfigurations: Array<Float> = when (numPlayers) {
        1 -> arrayOf(90f)
        2 -> arrayOf(180f, 0f)
        3 -> arrayOf(90f, 270f, 0f)
        4 -> arrayOf(90f, 270f, 90f, 270f)
        5 -> arrayOf(90f, 270f, 90f, 270f, 0f)
        6 -> arrayOf(90f, 270f, 90f, 270f, 90f, 270f)
        else -> throw IllegalArgumentException("invalid number of players: $numPlayers")
    }

    val offset3 = 0.8f
    val offset5 = 0.265f

    val buttonSizes: Array<DpSize> = when (numPlayers) {
        1 -> arrayOf(
            DpSize(screenHeight, screenWidth)
        )

        2 -> arrayOf(
            DpSize(screenWidth, screenHeight / 2), DpSize(screenWidth, screenHeight / 2)
        )

        3 -> arrayOf(
            DpSize(screenHeight - screenWidth * offset3, screenWidth / 2),
            DpSize(screenHeight - screenWidth * offset3, screenWidth / 2),
            DpSize(screenWidth, screenWidth * offset3)
        )

        4 -> arrayOf(
            DpSize(screenHeight / 2, screenWidth / 2),
            DpSize(screenHeight / 2, screenWidth / 2),
            DpSize(screenHeight / 2, screenWidth / 2),
            DpSize(screenHeight / 2, screenWidth / 2),
        )

        5 -> arrayOf(
            DpSize(screenHeight / 2 - screenWidth * offset5, screenWidth / 2),
            DpSize(screenHeight / 2 - screenWidth * offset5, screenWidth / 2),
            DpSize(screenHeight / 2 - screenWidth * offset5, screenWidth / 2),
            DpSize(screenHeight / 2 - screenWidth * offset5, screenWidth / 2),
            DpSize(screenWidth, screenWidth * offset5 * 2)
        )

        6 -> arrayOf(
            DpSize(screenHeight / 3, screenWidth / 2),
            DpSize(screenHeight / 3, screenWidth / 2),
            DpSize(screenHeight / 3, screenWidth / 2),
            DpSize(screenHeight / 3, screenWidth / 2),
            DpSize(screenHeight / 3, screenWidth / 2),
            DpSize(screenHeight / 3, screenWidth / 2),
        )

        else -> throw IllegalArgumentException("invalid number of players")
    }

    val middleButtonOffset: Float = when (numPlayers) {
        1 -> 0.065f
        2 -> 0.5f
        3 -> 0.615f
        4 -> 0.5f
        5 -> 0.364f
        6 -> 0.323f
        else -> throw IllegalArgumentException("invalid number of players")
    }

    var showDialog by remember { mutableStateOf(false) }
    val showButtons = remember { mutableStateOf(false) }
    var blurBackground by remember { mutableStateOf(false) }

    LaunchedEffect(showDialog) {
        if (!showDialog) blurBackground = false
    }

    LaunchedEffect(Unit) {
        showButtons.value = true
    }
    Box(
        Modifier
            .fillMaxSize()
            .then(
                if (blurBackground) {
                    Modifier.blur(radius = 20.dp)
                } else {
                    Modifier
                }
            )
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.Center,
            content = {
                val rowRange = (0 until numPlayers step 2) // first player index for each row
                rowRange.forEach { i ->
                    val playerRange = (i until minOf(i + 2, numPlayers)) // 1 or 2 players
                    if (numPlayers == 2) {
                        playerRange.forEach { j ->
                            item {
                                AnimatedPlayerButton(
                                    visible = showButtons,
                                    player = players[j],
                                    rotation = angleConfigurations[j],
                                    width = buttonSizes[j].width,
                                    height = buttonSizes[j].height
                                )
                            }
                        }
                    } else {
                        item {
                            LazyRow(modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = false,
                                horizontalArrangement = Arrangement.Center,
                                content = {
                                    playerRange.forEach { j ->
                                        item {
                                            AnimatedPlayerButton(
                                                visible = showButtons,
                                                player = players[j],
                                                rotation = angleConfigurations[j],
                                                width = buttonSizes[j].width,
                                                height = buttonSizes[j].height
                                            )
                                        }
                                    }
                                })
                        }
                    }
                }
            })

        Column(Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.weight(0 + middleButtonOffset))
            AnimatedMiddleButton(modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(60.dp),
                visible = showButtons,
                onMiddleButtonClick = {
                    showDialog = true
                })
            Spacer(modifier = Modifier.weight(1 - middleButtonOffset))
        }
    }


    val history = remember { mutableStateListOf<String>() }
    val w = remember { mutableIntStateOf(0) }
    val u = remember { mutableIntStateOf(0) }
    val b = remember { mutableIntStateOf(0) }
    val r = remember { mutableIntStateOf(0) }
    val g = remember { mutableIntStateOf(0) }
    val c = remember { mutableIntStateOf(0) }
    val s = remember { mutableIntStateOf(0) }
    val counters = arrayListOf(w, u, b, r, g, c, s)
    if (showDialog) {
        MiddleButtonDialog(
            modifier = Modifier.onGloballyPositioned { _ ->
                blurBackground = showDialog
            },
            onDismiss = { showDialog = false },
            resetPlayers = { resetPlayers() },
            setStartingLife = { setStartingLife(it) },
            setPlayerNum = {
                showButtons.value = false
                setPlayerNum(it)
            },
            goToPlayerSelect = { goToPlayerSelect() },
            toggleTheme = { toggleTheme() },
            coinFlipHistory = history,
            counters = counters,

            )

    }
}


@Composable
fun AnimatedPlayerButton(
    visible: MutableState<Boolean>, player: Player, rotation: Float, width: Dp, height: Dp
) {
    val multiplesAway = 3
    val duration = 3000
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
            offsetX.animateTo(
                targetValue = if (visible.value) 0f else targetOffsetX,
                animationSpec = tween(durationMillis = duration)
            )
        }

        launch {
            offsetY.animateTo(
                targetValue = if (visible.value) 0f else targetOffsetY,
                animationSpec = tween(durationMillis = duration)
            )
        }
    }

    Box(modifier = Modifier.offset { IntOffset(offsetX.value.toInt(), offsetY.value.toInt()) }) {
        PlayerButton(
            player = player, width = width, height = height, rotation = rotation
        )
    }
}


@Composable
fun AnimatedMiddleButton(
    modifier: Modifier = Modifier, onMiddleButtonClick: () -> Unit, visible: MutableState<Boolean>
) {
    var animationFinished by remember { mutableStateOf(false) }

    val duration = 3000
    var angle by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(0f) }

    // Use Animatable for smooth animation
    val animatableAngle = remember { Animatable(0f) }
    val animatableScale = remember { Animatable(0f) }

    // Observe changes in the 'visible' state
    LaunchedEffect(visible.value) {
        // When 'visible' changes, animate the rotation
        launch {
            animatableAngle.animateTo(
                targetValue = if (visible.value) 360f else 0f, animationSpec = if (visible.value) {
                    tween(durationMillis = duration, easing = LinearOutSlowInEasing)
                } else {
                    tween(durationMillis = duration)
                }
            )
        }

        launch {
            animatableScale.animateTo(
                targetValue = if (visible.value) 1f else 0f, animationSpec = if (visible.value) {
                    tween(durationMillis = duration, easing = LinearOutSlowInEasing)
                } else {
                    tween(durationMillis = duration)
                }
            )
            animationFinished = true
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")

    angle = if (!animationFinished) animatableAngle.value else infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 180_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    ).value

    scale = animatableScale.value

    Box(modifier = modifier
        .background(color = MaterialTheme.colorScheme.background, shape = CircleShape)
        .rotate(angle)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(onPress = {
                onMiddleButtonClick()
            })
        }
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            painter = painterResource(id = R.drawable.middle_icon),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
    }
}

data class DpSize(val width: Dp, val height: Dp)