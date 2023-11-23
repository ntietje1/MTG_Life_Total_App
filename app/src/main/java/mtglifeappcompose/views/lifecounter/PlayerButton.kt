package mtglifeappcompose.views.lifecounter

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtglifeappcompose.R
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mtglifeappcompose.data.Player
import mtglifeappcompose.data.PlayerDataManager
import mtglifeappcompose.ui.theme.Gold
import mtglifeappcompose.ui.theme.allPlayerColors
import mtglifeappcompose.ui.theme.darkenColor
import mtglifeappcompose.ui.theme.deadDealerColorMatrix
import mtglifeappcompose.ui.theme.deadNormalColorMatrix
import mtglifeappcompose.ui.theme.deadReceiverColorMatrix
import mtglifeappcompose.ui.theme.deadSettingsColorMatrix
import mtglifeappcompose.ui.theme.dealerColorMatrix
import mtglifeappcompose.ui.theme.desaturateColor
import mtglifeappcompose.ui.theme.normalColorMatrix
import mtglifeappcompose.ui.theme.receiverColorMatrix
import mtglifeappcompose.ui.theme.settingsColorMatrix
import mtglifeappcompose.utilities.ImageLoader
import mtglifeappcompose.views.AnimatedBorderCard
import mtglifeappcompose.views.SettingsButton
import mtglifeappcompose.views.lifecounter.PlayerButtonStateManager.getDamageToPlayer
import mtglifeappcompose.views.lifecounter.PlayerButtonStateManager.setDealer
import yuku.ambilwarna.AmbilWarnaDialog


enum class PlayerButtonState {
    NORMAL, COMMANDER_DEALER, COMMANDER_RECEIVER, SETTINGS
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

    private var currentDealer: Player? = null

    fun setDealer(dealer: Player) {
        currentDealer = dealer
    }

    fun addDamageToPlayer(receiver: Int, damage: Int) {
        val dealer = currentDealer!!
        dealer.commanderDamage[receiver - 1] = dealer.commanderDamage[receiver - 1] + damage
    }

    fun getDamageToPlayer(receiver: Int): Int {
        val dealer = currentDealer ?: return 0
        return dealer.commanderDamage[receiver - 1]
    }
}


@Preview
@Composable
fun ExampleScreen() {
    Column() {
        PlayerButton(
            player = Player.generatePlayer(),
            initialState = PlayerButtonState.NORMAL
        )
        PlayerButton(
            player = Player.generatePlayer(),
            initialState = PlayerButtonState.NORMAL,
            width = 200.dp,
            height = 200.dp
        )
        PlayerButton(
            player = Player.generatePlayer(),
            initialState = PlayerButtonState.NORMAL,
            rotation = 90f
        )
    }
}

enum class PlayerButtonBackgroundMode {
    SOLID, IMAGE
}


@Composable
fun PlayerButton(
    player: Player,
    initialState: PlayerButtonState = PlayerButtonState.NORMAL,
    width: Dp = 200.dp,
    height: Dp = 150.dp,
    rotation: Float = 0f
) {

    val state = remember { mutableStateOf(initialState) }
    PlayerButtonStateManager.registerButtonState(state)

    val backgroundType = remember { mutableStateOf(PlayerButtonBackgroundMode.SOLID) }

    val player by remember { mutableStateOf(player) }

    val id by remember(player) {
        derivedStateOf { player.playerNum }
    }

    val life by remember(player) {
        derivedStateOf { player.life }
    }

    val name by remember(player) {
        derivedStateOf { player.name }
    }

    val baseColor by remember(player) {
        derivedStateOf { player.playerColor }
    }

    val monarch by remember(player) {
        derivedStateOf { player.monarch }
    }

    val recentChange by remember(player) {
        derivedStateOf { player.recentLifeChange }
    }

    val context = LocalContext.current

    val bitmap = remember {
        ImageLoader.decodeSampledBitmapFromResource(context.resources, R.drawable.sqrl, 500, 200)
    }

    val commanderDamage by remember(life) {
        derivedStateOf { getDamageToPlayer(id) }
    }

    val visibleColor by remember(baseColor, state) {
        derivedStateOf {
            var c = when (state.value) {
                PlayerButtonState.NORMAL -> baseColor
                PlayerButtonState.COMMANDER_RECEIVER -> Color.DarkGray
                PlayerButtonState.COMMANDER_DEALER -> Color(
                    baseColor.toArgb().desaturateColor().darkenColor()
                )

                PlayerButtonState.SETTINGS -> Color(
                    baseColor.toArgb().desaturateColor(0.8f).darkenColor(0.8f)
                )

                else -> throw Exception("unsupported state")
            }
            if (player.isDead) {
                c = Color(c.toArgb().desaturateColor(0.4f).darkenColor(1.1f))
            }
            c
        }
    }

    fun commanderButtonOnClick() {
        state.value = when (state.value) {
            PlayerButtonState.NORMAL -> {
                PlayerButtonStateManager.updateAllStates(PlayerButtonState.COMMANDER_RECEIVER)
                setDealer(player)
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
            else -> throw Exception("Invalid state for settingsButtonOnClick")
        }
    }

    fun onIncrementLife() {
        println("onIncrementLife")
        when (state.value) {
            PlayerButtonState.NORMAL -> player.incrementLife(1)
            PlayerButtonState.COMMANDER_RECEIVER -> {
                PlayerButtonStateManager.addDamageToPlayer(id, 1)
                player.incrementLife(-1)
            }

            else -> {}
        }
    }

    fun onDecrementLife() {
        println("onDecrementLife")
        when (state.value) {
            PlayerButtonState.NORMAL -> player.incrementLife(-1)
            PlayerButtonState.COMMANDER_RECEIVER -> {
                PlayerButtonStateManager.addDamageToPlayer(id, -1)
                player.incrementLife(1)
            }

            else -> {}
        }
    }

    val colorMatrix = when (state.value) {
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

        else -> throw Exception("unsupported state")
    }

    BoxWithConstraints(
        modifier = Modifier
            .wrapContentSize()
            .bounceClick()
            .then(
                when (rotation) {
                    90f -> {
                        Modifier.rotateVertically(rotation = VerticalRotation.CLOCKWISE)
                    }

                    270f -> {
                        Modifier.rotateVertically(rotation = VerticalRotation.COUNTER_CLOCKWISE)
                    }

                    180f -> {
                        Modifier.rotate(180f)
                    }

                    else -> {
                        Modifier
                    }
                }
            )
    ) {
        if (monarch) {
            AnimatedBorderCard(
                modifier = Modifier
                    .padding(all = 0.dp),
                shape = RoundedCornerShape(60.dp),
                borderWidth = 1.dp,
                gradient = Brush.sweepGradient(
                    listOf(
                        Color.White.copy(alpha = 0.1f),
                        Gold,
                        Gold,
                        Gold,
                    )
                ),
                animationDuration = 6500
            ) {
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(height)
                        .padding(3.dp)
                        .background(Color.Transparent)
                        .clip(RoundedCornerShape(60.dp))
                )
            }
        }

        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .padding(3.dp)
                .background(Color.Transparent)
                .clip(RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            when (backgroundType.value) {
                PlayerButtonBackgroundMode.SOLID -> {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = visibleColor
                    ) {}
                }

                PlayerButtonBackgroundMode.IMAGE -> {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Player uploaded image",
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.colorMatrix(
                            colorMatrix = colorMatrix
                        )
                    )
                }
            }

            LifeChangeButtons(
                onIncrementLife = { onIncrementLife() },
                onDecrementLife = { onDecrementLife() }
            )

            when (state.value) {
                PlayerButtonState.NORMAL -> PlayerInfo(
                    playerName = name,
                    life = life,
                    recentChange = recentChange,
                    state = state.value,
                    buttonSize = height
                )

                PlayerButtonState.COMMANDER_RECEIVER -> PlayerInfo(
                    playerName = name,
                    life = commanderDamage,
                    recentChange = 0,
                    state = state.value,
                    buttonSize = height
                )

                PlayerButtonState.COMMANDER_DEALER -> Text(
                    text = "Deal damage with your commander",
                    color = Color.White,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )

                PlayerButtonState.SETTINGS -> SettingsMenu(
                    player = player,
                    onColorButtonClick = { /* Handle color button click */ },
                    onChangeNameButtonClick = { /* Handle change name button click */ },
                    onMonarchyButtonClick = {
                        player.monarch = !player.monarch
                    },
                    onSavePlayerButtonClick = {
                        PlayerDataManager(context).savePlayer(player)
                    },
                    onLoadPlayerButtonClick = { /* Handle load player button click */ },
                    onImageButtonClick = {
                        if (backgroundType.value == PlayerButtonBackgroundMode.SOLID) {
                            backgroundType.value = PlayerButtonBackgroundMode.IMAGE
                        } else {
                            backgroundType.value = PlayerButtonBackgroundMode.SOLID

                        }
                    },
                    closeSettingsMenu = { state.value = PlayerButtonState.NORMAL },
                    buttonSize = height
                )
            }

            PlayerButtonStateButtons(
                state = state.value,
                commanderButtonOnClick = { commanderButtonOnClick() },
                settingsButtonOnClick = { settingsButtonOnClick() },
                buttonSize = height
            )
        }
    }

}

@Composable
fun PlayerButtonStateButtons(
    state: PlayerButtonState,
    commanderButtonOnClick: () -> Unit,
    settingsButtonOnClick: () -> Unit,
    buttonSize: Dp
) {
    val commanderButtonVisible =
        state != PlayerButtonState.COMMANDER_RECEIVER && state != PlayerButtonState.SETTINGS
    val settingsButtonVisible =
        state != PlayerButtonState.COMMANDER_DEALER && state != PlayerButtonState.COMMANDER_RECEIVER
    val smallButtonSize = buttonSize / 4.6f

    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { commanderButtonOnClick() },
                    modifier = Modifier
                        .size(smallButtonSize)
                        .padding(bottom = smallButtonSize / 30f, start = smallButtonSize / 30f)
                        .clip(RoundedCornerShape(0.dp))
                        .background(Color.Transparent)
                        .alpha(if (commanderButtonVisible) 1f else 0f),
                    enabled = commanderButtonVisible,
                    content = {
                        Icon(
                            modifier = Modifier.fillMaxSize(0.75f),
                            imageVector = ImageVector.vectorResource(id = R.drawable.commander_solid_icon),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                )

                IconButton(
                    onClick = { settingsButtonOnClick() },
                    modifier = Modifier
                        .size(smallButtonSize)
                        .background(Color.Transparent)
                        .alpha(if (settingsButtonVisible) 1f else 0f),
                    enabled = settingsButtonVisible,
                    content = {
                        Icon(
                            modifier = Modifier.fillMaxSize(0.825f),
                            imageVector = ImageVector.vectorResource(id = R.drawable.settings_solid_icon),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                )
            }

        }
    }
}


@Composable
fun PlayerInfo(
    playerName: String,
    life: Int,
    recentChange: Int,
    state: PlayerButtonState,
    buttonSize: Dp
) {
    val iconID = when (state) {
        PlayerButtonState.NORMAL -> R.drawable.heart_solid_icon
        PlayerButtonState.COMMANDER_DEALER -> R.drawable.transparent
        PlayerButtonState.COMMANDER_RECEIVER -> R.drawable.commander_solid_icon_small
        PlayerButtonState.SETTINGS -> R.drawable.transparent
        else -> R.drawable.transparent
    }

    val largeTextSize = buttonSize.value.sp / 1.8f
    val smallTextSize = buttonSize.value.sp / 12f
    val recentChangeSize = buttonSize.value.sp / 15f

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween, // Center large text
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = life.toString(),
                color = Color.White,
                fontSize = largeTextSize,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(0.1f))
            val recentChangeText =
                if (recentChange == 0) ""
                else if (recentChange > 0) "+$recentChange"
                else "$recentChange"
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier,
                    text = recentChangeText,
                    color = Color.White,
                    fontSize = recentChangeSize
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = buttonSize / 30f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = playerName,
                color = Color.White,
                fontSize = smallTextSize,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(buttonSize / 1.65f))
            Icon(
                painter = painterResource(iconID),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}


enum class SettingsState { Default, ColorPicker, ChangeName, LoadPlayer }

@Composable
fun SettingsMenu(
    player: Player,
    onColorButtonClick: () -> Unit,
    onChangeNameButtonClick: () -> Unit,
    onMonarchyButtonClick: () -> Unit,
    onSavePlayerButtonClick: () -> Unit,
    onLoadPlayerButtonClick: () -> Unit,
    onImageButtonClick: () -> Unit,
    closeSettingsMenu: () -> Unit,
    buttonSize: Dp
) {
    val smallButtonSize = buttonSize / 3f
    val margin = buttonSize / 5f

    var state by remember { mutableStateOf(SettingsState.Default) }

    when (state) {
        SettingsState.Default -> {
            LazyHorizontalGrid(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(bottom = margin, top = margin / 3f),
                rows = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(buttonSize / 37.5f)
            )
            {

                item {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.upload_icon),
                        text = "Save Player",
                        onClick = {
                            onSavePlayerButtonClick()
                            closeSettingsMenu()
                        },
                        size = smallButtonSize,
                        backgroundColor = Color.Transparent
                    )
                }
                item {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.download_icon),
                        text = "Load Player",
                        onClick = {
                            state = SettingsState.LoadPlayer
//                            onLoadPlayerButtonClick()
                        },
                        size = smallButtonSize,
                        backgroundColor = Color.Transparent
                    )
                }
                item {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.monarchy_icon),
                        text = "Monarch",
                        onClick = {
                            onMonarchyButtonClick()
                            closeSettingsMenu()
                        },
                        size = smallButtonSize,
                        backgroundColor = Color.Transparent
                    )
                }
                item {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.change_name_icon),
                        text = "Change Name",
                        onClick = {
                            state = SettingsState.ChangeName
//                            onChangeNameButtonClick()
                        },
                        size = smallButtonSize,
                        backgroundColor = Color.Transparent
                    )
                }

                item {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.color_picker_icon),
                        text = "Set Color",
                        onClick = {
                            state = SettingsState.ColorPicker
//                            onColorButtonClick()
                        },
                        size = smallButtonSize,
                        backgroundColor = Color.Transparent
                    )
                }

                item {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.change_background_icon),
                        text = "Set Background",
                        onClick = {
                            onImageButtonClick()
                        },
                        size = smallButtonSize,
                        backgroundColor = Color.Transparent
                    )
                }
            }
        }

        SettingsState.ColorPicker -> {
            val colorList = allPlayerColors
            Box(
                modifier =
                Modifier
                    .wrapContentSize()
                    .padding(buttonSize / 30f)
            )
            {
                LazyHorizontalGrid(
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(buttonSize / 2.0f)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.Black.copy(alpha = 0.15f))
                        .padding(buttonSize / 30f),
                    rows = GridCells.Fixed(2),
                    state = rememberLazyGridState(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSize / 150f),
                    verticalArrangement = Arrangement.spacedBy(buttonSize / 150f),
                    content = {
                        item {
                            CustomColorPickerButton(player = player, buttonSize = buttonSize)
                        }
                        items(colorList) { color ->
                            ColorPickerButton(
                                player = player,
                                color = color,
                                buttonSize = buttonSize
                            )
                        }
                    }
                )
            }
        }

        SettingsState.LoadPlayer -> {
            val context = LocalContext.current
            val playerList = remember { mutableStateListOf<Player>() }

            DisposableEffect(context) {
                playerList.addAll(PlayerDataManager(context).loadPlayers())
                onDispose {
                }
            }

            LazyHorizontalGrid(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(buttonSize / 1.2f)
                    .padding(
                        start = buttonSize / 30f,
                        top = buttonSize / 30f,
                        end = buttonSize / 30f,
                        bottom = margin
                    )
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.Black.copy(alpha = 0.15f)),
                rows = GridCells.Fixed(3),
                state = rememberLazyGridState(),
                content = {
                    items(playerList) { p ->
                        MiniPlayerButton(
                            currPlayer = player,
                            player = p,
                            playerList = playerList,
                            buttonSize = buttonSize
                        )
                    }
                }
            )
        }

        SettingsState.ChangeName -> {
            ChangeNameField(
                closeSettingsMenu = closeSettingsMenu,
                player = player,
                buttonSize = buttonSize
            )
        }

        else -> {}
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeNameField(closeSettingsMenu: () -> Unit, player: Player, buttonSize: Dp) {
    var newName by remember { mutableStateOf(player.name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = buttonSize / 20f,
                start = buttonSize / 20f,
                end = buttonSize / 20f,
                bottom = buttonSize / 5f
            ),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = newName,
            onValueChange = { newName = it },
            label = {
                Text(
                    "New Name",
                    color = player.playerColor,
                    fontSize = 12.sp
                )
            },
            textStyle = TextStyle(
                color = Color(player.playerColor.toArgb().darkenColor(0.1f)),
                fontSize = 15.sp
            ),
            singleLine = true,
//            colors = Color(player.playerColor),
////            TextFieldDefaults.colors(
////                cursorColor = Color(player.playerColor),
////                focusedIndicatorColor = Color(player.playerColor)
////            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    player.name = newName
                    closeSettingsMenu()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(buttonSize / 40f)
        )

        Spacer(modifier = Modifier.height(buttonSize / 20f))

        Button(
            onClick = {
                player.name = newName
                closeSettingsMenu()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.60f)),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()

        ) {
            Text("Save Name")
        }
    }
}

@Composable
fun MiniPlayerButton(
    currPlayer: Player,
    player: Player,
    playerList: MutableList<Player>,
    buttonSize: Dp
) {
    val height = buttonSize / 9f
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .height(height)
            .width(height * 4)
            .padding(buttonSize / 40f)
            .clip(RoundedCornerShape(15.dp))
            .background(player.playerColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            currPlayer.playerColor = player.playerColor
                            currPlayer.name = player.name
                        },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            PlayerDataManager(context).deletePlayer(player)
                            playerList.remove(player)
                        }
                    )
                }
        ) {
            Text(
                text = player.name,
                color = Color.White,
                fontSize = height.value.sp / 2f,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


@Composable
fun ColorPickerButton(player: Player, color: Color, buttonSize: Dp) {
    Box(
        modifier = Modifier
            .size(buttonSize / 5f)
            .padding(buttonSize / 50f)
            .background(color)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        player.playerColor = color
                    },
                )
            }
    ) {
    }
}

@Composable
fun CustomColorPickerButton(player: Player, buttonSize: Dp) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(buttonSize / 5f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val initialColor = player.playerColor

                        val colorPickerDialog = AmbilWarnaDialog(
                            context,
                            initialColor.toArgb(),
                            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                                override fun onCancel(dialog: AmbilWarnaDialog?) {
                                    // User canceled the color picker dialog
                                }

                                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                                    player.playerColor = Color(color)
                                }
                            })

//                            colorPickerDialog.dialog?.window?.setBackgroundDrawable(
//                                RoundedCornerDrawable.create(
//                                    LocalContext.current
//                                )
//                            )

                        colorPickerDialog.show()
                    },
                )
            },

        ) {
        Icon(
            painter = painterResource(id = R.drawable.custom_color_icon),
            contentDescription = null, // provide a localized description if needed
            modifier = Modifier
                .fillMaxSize()
                .padding(buttonSize / 60f),
            tint = Color.White // Set the tint to make the button's background transparent
        )
    }
}

@Composable
fun LifeChangeButtons(
    onIncrementLife: () -> Unit,
    onDecrementLife: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = Modifier.fillMaxWidth()) {

        CustomIncrementButton(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .alpha(0.015f),
            onIncrementLife = onIncrementLife,
            color = Color.White,
            interactionSource = interactionSource
        )

        CustomIncrementButton(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1.0f)
                .alpha(0.015f),
            onIncrementLife = onDecrementLife,
            color = Color.Black, // darken this one?
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

    CompositionLocalProvider(LocalRippleTheme provides NoRippleTheme) {
        Box(
            modifier = modifier
                .background(color)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onIncrementLife()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                    )
                }
                .then(
                    Modifier
                        .constantRepeatingClickable(
                            interactionSource = interactionSource,
                            enabled = true,
                            onClick = {
                                onIncrementLife()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                )
        )
    }
}

enum class ButtonState { Pressed, Idle }

fun Modifier.bounceClick() = composed {
    var buttonState by remember { mutableStateOf(ButtonState.Idle) }
    val scale by animateFloatAsState(
        targetValue = if (buttonState == ButtonState.Pressed) 1.0075f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { }
        )
        .pointerInput(buttonState) {
            awaitPointerEventScope {
                buttonState = if (buttonState == ButtonState.Pressed) {
                    waitForUpOrCancellation()
                    ButtonState.Idle
                } else {
                    awaitFirstDown(false)
                    ButtonState.Pressed
                }
            }
        }
}

fun Modifier.constantRepeatingClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    initialDelayMillis: Long = 500,
    repeatingDelayMillis: Long = 100,
    onClick: () -> Unit
): Modifier = composed {

    val currentClickListener by rememberUpdatedState(onClick)
    val isEnabled by rememberUpdatedState(enabled)

    this.pointerInput(interactionSource, isEnabled) {
        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val job = launch {
                    delay(initialDelayMillis)
                    while (isEnabled && down.pressed) {
                        currentClickListener() // Repeating click after initial delay
                        delay(repeatingDelayMillis)
                    }
                }
                waitForUpOrCancellation()
                job.cancel()
            }
        }
        detectTapGestures(onPress = { onClick() }) { }
    }
}

private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(0.0f, 0.0f, 0.0f, 0.0f)
}

fun Modifier.rotateVertically(rotation: VerticalRotation) = then(
    object : LayoutModifier {
        override fun MeasureScope.measure(
            measurable: Measurable,
            constraints: Constraints
        ): MeasureResult {
            val placeable = measurable.measure(constraints)
            return layout(placeable.height, placeable.width) {
                placeable.place(
                    x = -(placeable.width / 2 - placeable.height / 2),
                    y = -(placeable.height / 2 - placeable.width / 2)
                )
            }
        }

        override fun IntrinsicMeasureScope.minIntrinsicHeight(
            measurable: IntrinsicMeasurable,
            width: Int
        ): Int {
            return measurable.maxIntrinsicWidth(width)
        }

        override fun IntrinsicMeasureScope.maxIntrinsicHeight(
            measurable: IntrinsicMeasurable,
            width: Int
        ): Int {
            return measurable.maxIntrinsicWidth(width)
        }

        override fun IntrinsicMeasureScope.minIntrinsicWidth(
            measurable: IntrinsicMeasurable,
            height: Int
        ): Int {
            return measurable.minIntrinsicHeight(height)
        }

        override fun IntrinsicMeasureScope.maxIntrinsicWidth(
            measurable: IntrinsicMeasurable,
            height: Int
        ): Int {
            return measurable.maxIntrinsicHeight(height)
        }
    })
    .then(rotate(rotation.value))

enum class VerticalRotation(val value: Float) {
    CLOCKWISE(90f), COUNTER_CLOCKWISE(270f)
}

