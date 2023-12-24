package mtglifeappcompose.composable.lifecounter

import android.app.AlertDialog
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.mtglifeappcompose.R
import mtglifeappcompose.composable.ScryfallSearchDialog
import mtglifeappcompose.composable.SettingsButton
import mtglifeappcompose.composable.VerticalRotation
import mtglifeappcompose.composable.animatedBorderCard
import mtglifeappcompose.composable.bounceClick
import mtglifeappcompose.composable.repeatingClickable
import mtglifeappcompose.composable.rotateVertically
import mtglifeappcompose.data.ImageManager
import mtglifeappcompose.data.Player
import mtglifeappcompose.data.PlayerDataManager
import mtglifeappcompose.ui.theme.Gold
import mtglifeappcompose.ui.theme.allPlayerColors
import mtglifeappcompose.ui.theme.brightenColor
import mtglifeappcompose.ui.theme.deadDealerColorMatrix
import mtglifeappcompose.ui.theme.deadNormalColorMatrix
import mtglifeappcompose.ui.theme.deadReceiverColorMatrix
import mtglifeappcompose.ui.theme.deadSettingsColorMatrix
import mtglifeappcompose.ui.theme.dealerColorMatrix
import mtglifeappcompose.ui.theme.invert
import mtglifeappcompose.ui.theme.normalColorMatrix
import mtglifeappcompose.ui.theme.receiverColorMatrix
import mtglifeappcompose.ui.theme.saturateColor
import mtglifeappcompose.ui.theme.settingsColorMatrix
import mtglifeappcompose.ui.theme.textShadowStyle
import yuku.ambilwarna.AmbilWarnaDialog
import java.lang.Float.min


enum class PlayerButtonState {
    NORMAL, COMMANDER_DEALER, COMMANDER_RECEIVER, SETTINGS, COUNTERS
}


object PlayerButtonStateManager {
    private val buttonStates = arrayListOf<MutableState<PlayerButtonState>>()

    fun registerButtonState(state: MutableState<PlayerButtonState>) {
        buttonStates.add(state)
    }

    fun updateAllStates(state: PlayerButtonState) {
        buttonStates.forEach {
            it.value = state
        }
    }

    var currentDealer: Player? = null
}

@Composable
fun PlayerButton(
    player: Player,
    initialState: PlayerButtonState = PlayerButtonState.NORMAL,
    width: Dp = 200.dp,
    height: Dp = 150.dp,
    rotation: Float = 0f,
    blurBackground: MutableState<Boolean> = mutableStateOf(false)
) {

    val state = remember { mutableStateOf(initialState) }
    PlayerButtonStateManager.registerButtonState(state)

    val context = LocalContext.current

    fun commanderButtonOnClick() {
        state.value = when (state.value) {
            PlayerButtonState.NORMAL -> {
                PlayerButtonStateManager.currentDealer = player
                PlayerButtonStateManager.updateAllStates(PlayerButtonState.COMMANDER_RECEIVER)
                PlayerButtonState.COMMANDER_DEALER
            }

            PlayerButtonState.COMMANDER_DEALER -> {
                PlayerButtonStateManager.updateAllStates(PlayerButtonState.NORMAL)
                PlayerButtonState.NORMAL
            }

            else -> throw Exception("Invalid state for commanderButtonOnClick")
        }
    }

    fun settingsButtonOnClick() {
        state.value = when (state.value) {
            PlayerButtonState.NORMAL -> PlayerButtonState.SETTINGS
            PlayerButtonState.SETTINGS -> PlayerButtonState.NORMAL
            PlayerButtonState.COUNTERS -> PlayerButtonState.NORMAL
            else -> throw Exception("Invalid state for settingsButtonOnClick")
        }
    }

    fun addDamageToPlayer(receiver: Int, damage: Int) {
        println("${PlayerButtonStateManager.currentDealer!!.name} dealing damage to: $receiver")
        PlayerButtonStateManager.currentDealer!!.commanderDamage[receiver - 1] += damage
    }

    fun onIncrementLife() {
        when (state.value) {
            PlayerButtonState.NORMAL -> player.incrementLife(1)
            PlayerButtonState.COMMANDER_RECEIVER -> {
                addDamageToPlayer(player.playerNum, 1)
                player.incrementLife(-1)
            }

            else -> {}
        }
    }


    fun onDecrementLife() {
        when (state.value) {
            PlayerButtonState.NORMAL -> player.incrementLife(-1)
            PlayerButtonState.COMMANDER_RECEIVER -> {
                addDamageToPlayer(player.playerNum, -1)
                player.incrementLife(1)
            }

            else -> {}
        }
    }

    val imageManager = ImageManager(context, player)

    var showScryfallSearch by remember { mutableStateOf(false) }

    val commanderButtonVisible =
        state.value in listOf(PlayerButtonState.NORMAL, PlayerButtonState.COMMANDER_DEALER)
    val settingsButtonVisible = state.value in listOf(
        PlayerButtonState.NORMAL, PlayerButtonState.SETTINGS, PlayerButtonState.COUNTERS
    )

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { selectedUri ->
                val copiedUri = imageManager.copyImageToInternalStorage(selectedUri)
                player.imageUri = copiedUri
                PlayerDataManager(context).savePlayer(player)
            }
        }


    fun openCameraRoll() {
        launcher.launch(
            PickVisualMediaRequest(
                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    fun showWarningDialog() {
        val alertDialog = AlertDialog.Builder(context).setTitle("Warning")
            .setMessage("This will open the camera roll. Proceed?")
            .setPositiveButton("Proceed") { _, _ ->
                openCameraRoll()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.create()

        alertDialog.show()
    }

    if (showScryfallSearch) {
        ScryfallSearchDialog(modifier = Modifier.onGloballyPositioned { _ ->
            blurBackground.value = true
        }, onDismiss = {
            showScryfallSearch = false
            blurBackground.value = false
        }, player = player
        )
    }

//    val navController = rememberNavController()
//
//    NavHost(navController = navController, startDestination = "normal") {
//        composable("normal") {  }
//        composable("settings") {  }
//    navController.navigate(route)
//    navController.popBackStack()
//
//    }


    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
            .then(
                when (rotation) {
                    90f -> Modifier.rotateVertically(rotation = VerticalRotation.CLOCKWISE)
                    270f -> Modifier.rotateVertically(rotation = VerticalRotation.COUNTER_CLOCKWISE)
                    180f -> Modifier.rotate(180f)
                    else -> Modifier
                }
            )
            .then(
                if (state.value == PlayerButtonState.NORMAL || state.value == PlayerButtonState.COMMANDER_RECEIVER) {
                    Modifier.bounceClick()
                } else {
                    Modifier
                }
            )
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .clip(RoundedCornerShape(30.dp))
                .background(color = MaterialTheme.colorScheme.background)
                .then(
                    if (player.monarch) {
                        Modifier.animatedBorderCard(
                            shape = RoundedCornerShape(30.dp),
                            borderWidth = 4.dp,
                            gradient = Brush.sweepGradient(
                                listOf(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                                    Gold,
                                    Gold,
                                    Gold,
                                )
                            ),
                            animationDuration = 10000
                        )
                    } else {
                        Modifier
                    }
                )
        )

        BoxWithConstraints(
            modifier = Modifier
                .width(width)
                .height(height)
                .padding(3.dp)
                .background(Color.Transparent)
                .clip(RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            PlayerButtonBackground(player = player, state = state.value)

            LifeChangeButtons(
                onIncrementLife = { onIncrementLife() },
                onDecrementLife = { onDecrementLife() }
            )


            val smallButtonSize = width / 15f + height / 10f
            val settingsStateMargin = smallButtonSize / 7f
            val commanderStateMargin = settingsStateMargin * 1.4f

            val aspectRatio = maxWidth / maxHeight
            val wideButton = aspectRatio > 1.4

            val playerInfoPadding =
                if (wideButton) Modifier.padding(bottom = smallButtonSize / 2) else Modifier.padding(
                    top = smallButtonSize / 2
                )

            val settingsPadding =
                if (wideButton) Modifier.padding(bottom = smallButtonSize / 4) else Modifier.padding(
                    top = smallButtonSize / 4
                )

            val countersPadding =
                if (wideButton) Modifier.padding(vertical = smallButtonSize / 4) else Modifier
                    .padding(
                        top = smallButtonSize / 4
                    )
                    .padding(horizontal = 5.dp)

            @Composable
            fun PlayerButtonContent(modifier: Modifier = Modifier) {
                Box(modifier.fillMaxSize()) {
                    when (state.value) {
                        PlayerButtonState.NORMAL, PlayerButtonState.COMMANDER_RECEIVER ->
                            PlayerInfo(
                                modifier = playerInfoPadding,
                                player = player, state = state.value
                            )

                        PlayerButtonState.COMMANDER_DEALER -> Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "Deal damage with your commander",
                            color = player.textColor,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        PlayerButtonState.SETTINGS -> SettingsMenu(
                            modifier = settingsPadding,
                            player = player,
                            onMonarchyButtonClick = { player.toggleMonarch() },
                            onFromCameraRollButtonClick = { showWarningDialog() },
                            closeSettingsMenu = { state.value = PlayerButtonState.NORMAL },
                            onScryfallButtonClick = { showScryfallSearch = !showScryfallSearch },
                            onCounterButtonClicked = { state.value = PlayerButtonState.COUNTERS }
                        )

                        PlayerButtonState.COUNTERS -> {
                            Counters(modifier = countersPadding, player = player)
                        }
                    }
                }
            }

            @Composable
            fun CommanderStateButton(modifier: Modifier = Modifier) {
                PlayerStateButton(
                    modifier = modifier
                        .padding(
                            start = commanderStateMargin,
                            bottom = commanderStateMargin
                        ),
                    visible = commanderButtonVisible,
                    onPress = { commanderButtonOnClick() },
                    icon = painterResource(id = R.drawable.commander_solid_icon),
                    color = player.textColor,
                    size = smallButtonSize
                )
            }

            @Composable
            fun SettingsStateButton(modifier: Modifier = Modifier) {
                PlayerStateButton(
                    modifier = modifier
                        .padding(end = settingsStateMargin, bottom = settingsStateMargin),
                    visible = settingsButtonVisible,
                    onPress = { settingsButtonOnClick() },
                    icon = painterResource(id = R.drawable.settings_icon),
                    color = player.textColor,
                    size = smallButtonSize
                )
            }

            if (wideButton) {
                // WIDE
                Row(
                    Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CommanderStateButton(Modifier.align(Alignment.Bottom))
                    PlayerButtonContent(Modifier.weight(0.5f))
                    SettingsStateButton(Modifier.align(Alignment.Bottom))
                }
            } else {
                // TALL
                Column(Modifier.fillMaxSize()) {
                    PlayerButtonContent(Modifier.weight(0.5f))
                    Row(
                        Modifier
                            .wrapContentHeight()
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CommanderStateButton(Modifier.align(Alignment.Bottom))
                        SettingsStateButton(Modifier.align(Alignment.Bottom))
                    }
                }
            }
        }
    }
}

private enum class CounterMenuState {
    DEFAULT, ADD_COUNTER
}

@Composable
fun Counters(modifier: Modifier = Modifier, player: Player) {
    val activeCounters =
        remember { mutableStateListOf<Int>() } // indexes of counters that are visible
    var state by remember { mutableStateOf(CounterMenuState.DEFAULT) }
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(25.dp))
    ) {
        val exitButtonSize = maxHeight / 3f
        when (state) {
            CounterMenuState.DEFAULT -> {
                LazyRow(
                    Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    items(activeCounters) { index ->
                        Counter(
                            player = player,
                            icon = painterResource(id = CounterType.values()[index].resId),
                            value = player.counters[index],
                            onIncrement = { player.counters[index]++ },
                            onDecrement = { player.counters[index]-- }
                        )
                    }
                    item {
                        AddCounter(
                            player = player,
                            onTap = {
                                state = CounterMenuState.ADD_COUNTER
                            },
                        )
                    }
                }
            }

            CounterMenuState.ADD_COUNTER -> {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    LazyHorizontalGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(5.dp)
                            .weight(0.5f),
                        rows = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center
                    ) {
                        items(CounterType.values()) { counterType ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.5.dp)
                                    .background(
                                        if (counterType.idx in activeCounters) {
                                            Color.Green.copy(alpha = 0.5f)
                                        } else {
                                            Color.Transparent
                                        }
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures {
                                            if (counterType.idx in activeCounters) {
                                                activeCounters.remove(counterType.idx)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            } else {
                                                activeCounters.add(counterType.idx)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                    }
                            ) {
                                Image(
                                    painter = painterResource(id = counterType.resId),
                                    contentDescription = counterType.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(5.dp)
                                )
                            }
                        }
                    }
                    SettingsButton(
                        modifier = Modifier.padding(5.dp),
                        size = exitButtonSize,
                        backgroundColor = Color.Transparent,
                        mainColor = player.textColor,
                        imageResource = painterResource(id = R.drawable.enter_icon),
                        onPress = {
                            state = CounterMenuState.DEFAULT
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun AddCounter(
    player: Player,
    onTap: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(
        Modifier
            .fillMaxHeight()
            .aspectRatio(0.70f)
            .padding(5.dp)
            .bounceClick(0.0125f)
            .background(Color.Black.copy(0.2f), shape = RoundedCornerShape(30.dp))
            .pointerInput(Unit) {
                detectTapGestures {
                    onTap()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
    ) {
        val iconSize = maxHeight / 2.5f
        SettingsButton(
            modifier = Modifier.align(Alignment.Center),
            size = iconSize,
            imageResource = painterResource(id = R.drawable.add_icon),
            backgroundColor = Color.Transparent,
            mainColor = player.textColor,
            shadowEnabled = false,
            enabled = false
        )
    }
}

@Composable
fun Counter(
    player: Player,
    icon: Painter,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(
        Modifier
            .fillMaxHeight()
            .aspectRatio(0.70f)
            .padding(5.dp)
            .bounceClick(0.0125f)
            .background(Color.Black.copy(0.2f), shape = RoundedCornerShape(30.dp))
            .clip(RoundedCornerShape(30.dp))
    ) {
        val textSize = (maxHeight.value / 2.8f + maxWidth.value / 6f + 30).sp / 1.5f
        val iconSize = maxHeight / 4f
        val topPadding = maxHeight / 10f
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .background(Color.White.copy(alpha = 0.04f))
                .pointerInput(Unit) {
                    detectTapGestures {
                        onIncrement()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                })
            Box(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1.0f)
                .background(Color.Black.copy(alpha = 0.04f))
                .pointerInput(Unit) {
                    detectTapGestures {
                        onDecrement()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                })
        }

        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(topPadding))
            Text(
                text = value.toString(),
                color = player.textColor,
                fontSize = textSize,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.wrapContentSize()
            )
            Image(
                painter = icon,
                contentDescription = "Counter Icon",
                modifier = Modifier
                    .size(iconSize)
                    .padding(bottom = 15.dp)
            )
        }
    }
}

@Composable
fun PlayerButtonBackground(player: Player, state: PlayerButtonState) {
    if (player.imageUri == null) {
        var c = when (state) {
            PlayerButtonState.NORMAL -> player.color
            PlayerButtonState.COMMANDER_RECEIVER -> player.color.saturateColor(0.2f)
                .brightenColor(0.3f)

            PlayerButtonState.COMMANDER_DEALER -> player.color.saturateColor(0.5f)
                .brightenColor(0.6f)

            PlayerButtonState.SETTINGS -> player.color.saturateColor(0.6f).brightenColor(0.8f)

            PlayerButtonState.COUNTERS -> player.color.saturateColor(0.6f).brightenColor(0.8f)

            else -> throw Exception("unsupported state")
        }
        if (player.isDead) {
            c = c.saturateColor(0.4f).brightenColor(1.1f)
        }
        Surface(
            modifier = Modifier.fillMaxSize(), color = c
        ) {}
    } else {
        val colorMatrix = when (state) {
            PlayerButtonState.NORMAL -> {
                if (player.isDead) deadNormalColorMatrix else normalColorMatrix
            }

            PlayerButtonState.COMMANDER_RECEIVER -> {
                if (player.isDead) deadReceiverColorMatrix else receiverColorMatrix
            }

            PlayerButtonState.COMMANDER_DEALER -> {
                if (player.isDead) deadDealerColorMatrix else dealerColorMatrix
            }

            PlayerButtonState.SETTINGS -> {
                if (player.isDead) deadSettingsColorMatrix else settingsColorMatrix
            }

            PlayerButtonState.COUNTERS -> {
                if (player.isDead) deadSettingsColorMatrix else settingsColorMatrix
            }

            else -> throw Exception("unsupported state")
        }
        AsyncImage(
            ImageRequest.Builder(LocalContext.current).data(data = player.imageUri).build(),
            modifier = Modifier.fillMaxSize(),
            contentDescription = "Player uploaded image",
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(
                colorMatrix = colorMatrix
            )
        )
    }
}

@Composable
fun PlayerStateButton(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    visible: Boolean,
    onPress: () -> Unit,
    icon: Painter,
    color: Color
) {
    SettingsButton(
        modifier = modifier,
        size = size,
        backgroundColor = Color.Transparent,
        mainColor = color,
        imageResource = icon,
        visible = visible,
        onPress = onPress
    )
}

@Composable
fun PlayerInfo(
    modifier: Modifier = Modifier,
    player: Player,
    state: PlayerButtonState
) {
    val iconID = when (state) {
        PlayerButtonState.NORMAL -> R.drawable.heart_solid_icon
        PlayerButtonState.COMMANDER_RECEIVER -> R.drawable.commander_solid_icon_small
        else -> R.drawable.transparent
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        var largeTextSize = (maxHeight.value / 2.8f + maxWidth.value / 6f + 30).sp
        val largeTextPadding = largeTextSize.value.dp / 6f
        val largeText = (when (state) {
            PlayerButtonState.NORMAL -> player.life
            PlayerButtonState.COMMANDER_RECEIVER -> {
                PlayerButtonStateManager.currentDealer?.commanderDamage?.get(
                    player.playerNum - 1
                )
            }

            else -> throw Exception("unsupported state")
        }).toString()

        if (largeText.length >= 3) {
            for (i in 0 until largeText.length - 2) {
                largeTextSize /= 1.15f
            }
        }
        val smallTextSize = (maxHeight.value / 14f + 4).sp
        val smallTextPadding = min(maxHeight.value / 1.5f, largeTextSize.value * 0.9f).dp

        val recentChangeSize = maxHeight.value.sp / 12.5f
        val recentChangeText =
            if (player.recentChange == 0 || state == PlayerButtonState.COMMANDER_RECEIVER) ""
            else if (player.recentChange > 0) "+${player.recentChange}"
            else "${player.recentChange}"

        val iconSize = maxHeight / 7f
        val iconPadding = (smallTextPadding.value / 1.2f).dp

        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = smallTextPadding),
            text = player.name,
            color = player.textColor,
            fontSize = smallTextSize,
            textAlign = TextAlign.Center,
            style = textShadowStyle
        )

        SettingsButton(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = iconPadding),
            size = iconSize,
            backgroundColor = Color.Transparent,
            mainColor = player.textColor,
            imageResource = painterResource(id = iconID),
            enabled = false
        )

        Row(
            modifier = Modifier
                .wrapContentSize(unbounded = true)
                .padding(top = largeTextPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier.wrapContentHeight(unbounded = true),
                text = largeText,
                color = player.textColor,
                fontSize = largeTextSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = textShadowStyle
            )
            Spacer(modifier = Modifier.weight(0.2f))
            Text(
                modifier = Modifier
                    .weight(0.8f)
                    .padding(start = 20.dp)
                    .wrapContentSize(unbounded = true),
                text = recentChangeText,
                color = player.textColor,
                fontSize = recentChangeSize,
                style = textShadowStyle
            )
        }

    }
}


enum class SettingsState { Default, ChangeBackground, ColorPicker, ChangeName, LoadPlayer }

@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    player: Player,
    onMonarchyButtonClick: () -> Unit,
    onFromCameraRollButtonClick: () -> Unit,
    onScryfallButtonClick: () -> Unit,
    closeSettingsMenu: () -> Unit,
    onCounterButtonClicked: () -> Unit
) {
    var state by remember { mutableStateOf(SettingsState.Default) }
    val context = LocalContext.current

    BoxWithConstraints(modifier.fillMaxSize()) {
        var settingsButtonSize = if (maxHeight / 2 * 3 < maxWidth) {
            maxHeight / 2
        } else {
            maxWidth / 3
        }
        settingsButtonSize = min(115F, settingsButtonSize.value).dp
        println("settingsButtonSize: $settingsButtonSize")
        val smallPadding = settingsButtonSize / 10f
        val smallTextSize = maxHeight.value.sp / 12f

        @Composable
        fun FormattedSettingsButton(imageResource: Painter, text: String, onPress: () -> Unit) {
            SettingsButton(
                imageResource = imageResource,
                text = text,
                onPress = onPress,
                size = settingsButtonSize,
                mainColor = player.textColor,
                backgroundColor = Color.Transparent
            )
        }

        when (state) {
            SettingsState.Default -> {
                LazyHorizontalGrid(
                    modifier = Modifier.fillMaxSize(),
                    rows = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.monarchy_icon),
                            text = "Monarch"
                        ) { onMonarchyButtonClick() }
                    }
                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.download_icon),
                            text = "Load Player"
                        ) { state = SettingsState.LoadPlayer }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.mana_icon),
                            text = "Player Counters"
                        ) { onCounterButtonClicked() }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.change_background_icon),
                            text = "Set Background"
                        ) { state = SettingsState.ChangeBackground }
                    }
                    item {
//                        FormattedSettingsButton(
//                            imageResource = painterResource(R.drawable.placeholder_icon),
//                            text = "Placeholder"
//                        ) {}
                        SettingsButton(
                            size = settingsButtonSize,
                            visible = false,
                        )
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.change_name_icon),
                            text = "Change Name"
                        ) { state = SettingsState.ChangeName }
                    }
                }
            }

            SettingsState.ChangeBackground -> {
                LazyHorizontalGrid(
                    modifier = Modifier.fillMaxSize(),
                    rows = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.color_picker_icon),
                            text = "Solid Color"
                        ) { state = SettingsState.ColorPicker }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.x_icon),
                            text = "Reset Background"
                        ) { player.imageUri = null }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.change_background_icon),
                            text = "Camera Roll"
                        ) { onFromCameraRollButtonClick() }
                    }

                    item {
//                        FormattedSettingsButton(
//                            imageResource = painterResource(R.drawable.gradient_icon),
//                            text = "Gradient"
//                        ) {}
                        SettingsButton(
                            size = settingsButtonSize,
                            visible = false,
                        )
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.search_icon),
                            text = "Scryfall"
                        ) { onScryfallButtonClick() }
                    }
                }
            }

            SettingsState.ColorPicker -> {
                val colorList = allPlayerColors
                val playerColor = remember(player.color) {
                    derivedStateOf {
                        player.color
                    }
                }
                val modifiedPlayerColor1 = remember(player.color) {
                    derivedStateOf {
                        player.color.saturateColor(1.5f).brightenColor(1.35f)
                    }
                }
                val modifiedPlayerColor2 = remember(player.color) {
                    derivedStateOf {
                        player.color.brightenColor(0.30f).saturateColor(1.5f)
                    }
                }

                val textColorList = remember {
                    listOf(
                        mutableStateOf(Color.White),
                        modifiedPlayerColor1,
                        playerColor,
                        modifiedPlayerColor2,
                        mutableStateOf(Color.Black)
                    )
                }

                Column(
                    Modifier.wrapContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val colorPickerPadding = smallPadding / 3f
                    val textPadding = smallPadding / 5f
                    val containerTopPadding = Modifier
                        .padding(horizontal = colorPickerPadding * 2)
                        .padding(top = colorPickerPadding * 2, bottom = colorPickerPadding)
                    val containerBottomPadding = Modifier
                        .padding(horizontal = colorPickerPadding * 2)
                        .padding(bottom = colorPickerPadding * 2, top = colorPickerPadding)

                    val colorPickerTopPadding = Modifier
                        .padding(horizontal = colorPickerPadding)
                        .padding(top = colorPickerPadding)
                    val colorPickerBottomPadding = Modifier
                        .padding(horizontal = colorPickerPadding)
                        .padding(bottom = colorPickerPadding)
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(bottom = textPadding, top = smallPadding),
                        text = "Choose a background color",
                        color = player.textColor,
                        fontSize = smallTextSize / 1.25f,
                        textAlign = TextAlign.Center
                    )
                    Column(
                        Modifier
                            .wrapContentSize()
                            .weight(0.5f)
                            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    )
                    {
                        LazyRow(
                            modifier = containerTopPadding
                                .wrapContentSize()
                                .weight(0.5f),
                            state = rememberLazyListState(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            item {
                                CustomColorPickerButton(
                                    modifier = colorPickerTopPadding,
                                    player = player
                                )
                            }
                            items(colorList.subList(0, colorList.size / 2)) { color ->
                                ColorPickerButton(
                                    modifier = colorPickerTopPadding,
                                    onClick = {
                                        player.color = color
                                        PlayerDataManager(context).savePlayer(player)
                                    }, color = color
                                )
                            }
                        }
                        LazyRow(
                            modifier = containerBottomPadding
                                .wrapContentSize()
                                .weight(0.5f),
                            state = rememberLazyListState(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            items(colorList.subList(colorList.size / 2, colorList.size)) { color ->
                                ColorPickerButton(
                                    modifier = colorPickerBottomPadding,
                                    onClick = {
                                        player.color = color
                                        PlayerDataManager(context).savePlayer(player)
                                    }, color = color
                                )
                            }
                        }
                    }
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(bottom = textPadding),
                        text = "Choose a text color",
                        color = player.textColor,
                        fontSize = smallTextSize / 1.25f,
                        textAlign = TextAlign.Center
                    )
                    Box(
                        Modifier
                            .wrapContentSize()
                            .weight(0.25f)
                            .padding(bottom = textPadding)
                            .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    )
                    {
                        LazyRow(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(colorPickerPadding * 2),
                            state = rememberLazyListState(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            items(textColorList) { color ->
                                ColorPickerButton(
                                    modifier = Modifier.padding(colorPickerPadding),
                                    onClick = {
                                        player.textColor = color.value
                                        PlayerDataManager(context).savePlayer(player)
                                    },
                                    color = color.value
                                )
                            }
                        }
                    }
                }
            }


            SettingsState.LoadPlayer -> {
                val playerList = remember { mutableStateListOf<Player>() }

                DisposableEffect(context) {
                    playerList.addAll(PlayerDataManager(context).loadPlayers())
                    onDispose {}
                }

                Column(
                    modifier
                        .fillMaxSize()
                        .padding(bottom = smallPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentSize(unbounded = true)
                            .padding(top = smallPadding),
                        text = "Saved players",
                        color = player.textColor,
                        fontSize = smallTextSize,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        modifier = Modifier
                            .wrapContentSize(unbounded = true)
                            .padding(bottom = smallPadding),
                        text = "(hold to delete)",
                        color = player.textColor,
                        fontSize = smallTextSize / 2,
                        textAlign = TextAlign.Center
                    )

                    LazyHorizontalGrid(modifier = Modifier
                        .fillMaxSize()
                        .weight(0.5f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.15f))
                        .padding(smallPadding),
                        rows = GridCells.Fixed(2),
                        state = rememberLazyGridState(),
                        horizontalArrangement = Arrangement.spacedBy(smallPadding),
                        verticalArrangement = Arrangement.spacedBy(smallPadding),
                        content = {
                            items(playerList) { p ->
                                MiniPlayerButton(
                                    currPlayer = player,
                                    player = p,
                                    playerList = playerList
                                )
                            }
                        })
                }
            }

            SettingsState.ChangeName -> {
                ChangeNameField(
                    modifier = modifier,
                    closeSettingsMenu = closeSettingsMenu,
                    player = player
                )
            }

            else -> {}
        }
    }
}

@Composable
fun ChangeNameField(
    modifier: Modifier = Modifier,
    closeSettingsMenu: () -> Unit,
    player: Player
) {
    var newName by remember { mutableStateOf(player.name) }
    val textColor = player.textColor.invert().copy(alpha = 1.0f)
    val context = LocalContext.current
    BoxWithConstraints(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = newName,
                onValueChange = { newName = it },
                label = {
                    Text(
                        "New Name", color = player.color, fontSize = 12.sp
                    )
                },
                textStyle = TextStyle(fontSize = 15.sp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedContainerColor = player.textColor,
                    unfocusedContainerColor = player.textColor,
                    cursorColor = player.color,
                    unfocusedIndicatorColor = player.textColor,
                    focusedIndicatorColor = player.color
                ),
                keyboardOptions = KeyboardOptions.Default.copy(
                    autoCorrect = false,
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    player.name = newName
                    closeSettingsMenu()
                    PlayerDataManager(context).savePlayer(player)
                }),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .wrapContentHeight()
                    .padding(top = 20.dp)
                    .padding(horizontal = 5.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    player.name = newName
                    closeSettingsMenu()
                    PlayerDataManager(context).savePlayer(player)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = player.textColor, contentColor = player.color
                ),
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth(0.7f)
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 20.dp)

            ) {
                Text("Save Name")
            }
        }
    }
}

@Composable
fun MiniPlayerButton(
    currPlayer: Player, player: Player, playerList: MutableList<Player>
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(2.5f)
            .clip(RoundedCornerShape(15.dp))
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    currPlayer.copySettings(player)
                }, onLongPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    PlayerDataManager(context).deletePlayer(player)
                    playerList.remove(player)
                })
            }) {
        if (player.imageUri == null) {
            Surface(modifier = Modifier.fillMaxSize(), color = player.color) {}
        } else {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current).data(data = player.imageUri).build()
            )
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
        }
        Text(
            text = player.name,
            color = player.textColor,
            fontSize = maxHeight.value.sp / 2f,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center)
        )
    }

}


@Composable
fun ColorPickerButton(modifier: Modifier = Modifier, onClick: () -> Unit, color: Color) {
    BoxWithConstraints(modifier = modifier
        .fillMaxHeight()
        .aspectRatio(1f)
        .background(color)
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    onClick()
                },
            )
        }) {}
}

@Composable
fun CustomColorPickerButton(modifier: Modifier = Modifier, player: Player) {
    val context = LocalContext.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        val initialColor = player.color

                        val colorPickerDialog = AmbilWarnaDialog(context,
                            initialColor.toArgb(),
                            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                                override fun onCancel(dialog: AmbilWarnaDialog?) {
                                    // User canceled the color picker dialog
                                }

                                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                                    player.imageUri = null
                                    player.color = Color(color)
                                    PlayerDataManager(context).savePlayer(player)
                                }
                            })

                        val corner = 60.dp.value
                        val corners = FloatArray(8)
                        for (i in 0..7) corners[i] = corner

                        colorPickerDialog.dialog?.window?.setBackgroundDrawable(ShapeDrawable(
                            RoundRectShape(corners, null, null)
                        ).apply {
                            setTint(Color.DarkGray.toArgb())
                        })

                        colorPickerDialog.show()
                    },
                )
            },
    ) {
        Icon(
            painter = painterResource(id = R.drawable.custom_color_icon),
            contentDescription = null, // provide a localized description if needed
            modifier = Modifier.fillMaxSize(),
            tint = player.textColor
        )
    }
}

@Composable
fun LifeChangeButtons(
    onIncrementLife: () -> Unit, onDecrementLife: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = Modifier.fillMaxWidth()) {

        CustomIncrementButton(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            onIncrementLife = onIncrementLife,
            color = Color.White.copy(alpha = 0.02f),
            interactionSource = interactionSource
        )

        CustomIncrementButton(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1.0f),
            onIncrementLife = onDecrementLife,
            color = Color.Black.copy(alpha = 0.02f),
            interactionSource = interactionSource
        )

    }
}

@Composable
private fun CustomIncrementButton(
    modifier: Modifier = Modifier,
    onIncrementLife: () -> Unit = {},
    color: Color = Color.Unspecified,
    interactionSource: MutableInteractionSource
) {
    val haptic = LocalHapticFeedback.current


    Box(
        modifier = modifier.repeatingClickable(interactionSource = interactionSource,
            enabled = true,
            onPress = {
                onIncrementLife()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            })
    )

}

