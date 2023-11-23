package mtglifeappcompose.views.lifecounter


import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.VectorConverter
import mtglifeappcompose.views.MiddleButtonDialogComposable
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.mtglifeappcompose.R
import mtglifeappcompose.data.Player


//@Preview
//@Composable
//fun ExampleLifeCounterScreen() {
//    val players = remember { mutableListOf<Player>() }
//    repeat(4) {
//        players.add(Player.generatePlayer())
//    }
//    LifeCounterScreen(players)
//}

@Composable
fun LifeCounterScreen(players: MutableList<Player>, resetPlayers: () -> Unit, setPlayerNum: (Int) -> Unit, setStartingLife: (Int) -> Unit, goToPlayerSelect: () -> Unit) {
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
    val offset5 = 0.3f

    val buttonSizes: Array<DpSize> = when (numPlayers) {
        1 -> arrayOf(
            DpSize(screenHeight, screenWidth)
        )
        2 -> arrayOf(
            DpSize(screenWidth, screenHeight / 2),
            DpSize(screenWidth, screenHeight / 2)
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

    var showDialog by remember { mutableStateOf(false) }
    var showButtons = remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().onGloballyPositioned {
                showButtons.value = true
            },
        verticalArrangement = Arrangement.Center,
        content = {
            val rowRange = (0 until numPlayers step 2) // first player index for each row
            rowRange.forEach { i ->
//                val weight = weights[i]
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
                        LazyRow(
                            modifier = Modifier.fillMaxSize(),
//                    .weight(1f / numPlayers * weight),
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
                            }
                        )
                    }
                }
            }
        }
    )

    Box(Modifier.fillMaxSize()) {
        MiddleDialogButton(
            modifier = Modifier.align(Alignment.Center),
            onMiddleButtonClick = {
                showDialog = true
            }
        )

        if (showDialog) {
            MiddleButtonDialogComposable(
                onDismiss = { showDialog = false },
                resetPlayers = { resetPlayers() },
                setStartingLife = { setStartingLife(it) },
                setPlayerNum = {
                    showButtons.value = false
                    setPlayerNum(it)
                               },
                goToPlayerSelect = { goToPlayerSelect() }
            )
        }
    }
}


@Composable
fun AnimatedPlayerButton(visible: MutableState<Boolean>, player: Player, rotation: Float, width: Dp, height: Dp) {
    val targetOffset = if (visible.value) Offset(0f, 0f) else {
        when (rotation) {
            0f -> Offset(0f, height.value*3)
            90f -> Offset(-width.value*3, 0f)
            180f -> Offset(0f, -height.value*3)
            270f -> Offset(width.value*3, 0f)
            else -> Offset(0f, height.value*3)
        }
    }

//    val targetAlpha = if (visible.value) 1.0f else 0.0f

    val easing = CubicBezierEasing(0f, 0.1f, 0.3f, 0.9f)

    val offset by animateOffsetAsState(
        targetValue = targetOffset,
        animationSpec = tween(durationMillis = 2500, easing = easing),
        label = ""
    )

//    val alpha by animateValueAsState(
//        targetValue = targetAlpha,
//        typeConverter = Float.VectorConverter,
//        animationSpec = tween(durationMillis = 1500),
//        label = ""
//    )

    Box(
        modifier = Modifier.offset{ IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .graphicsLayer(
//                alpha = if (visible.value) alpha else 0f,
//                translationY = if (!visible.value) height.value else 0f
            )
    ) {
        PlayerButton(
            player = player,
            rotation = rotation,
            width = width,
            height = height
        )
    }
}

@Composable
fun MiddleDialogButton(modifier: Modifier = Modifier, onMiddleButtonClick: () -> Unit) {
    IconButton(
        modifier = modifier.size(48.dp).background(Color.Transparent),
        onClick = {
            onMiddleButtonClick()
        }
    ) {
        Icon(
            modifier = Modifier.fillMaxSize().background(Color.Black).padding(2.dp),
            imageVector = ImageVector.vectorResource(id = R.drawable.middle_solid_icon),
            contentDescription = null,
            tint = Color.White
        )
    }
}

data class DpSize(val width: Dp, val height: Dp)