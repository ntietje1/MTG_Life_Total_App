package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinmtglifetotalapp.R
import kotlinmtglifetotalapp.ui.lifecounter.AnimatedBorderCard
import kotlinmtglifetotalapp.ui.lifecounter.SettingsButton
import kotlinmtglifetotalapp.ui.lifecounter.playerButton.PlayerButtonStateManager.getDamageToPlayer
import kotlinmtglifetotalapp.ui.lifecounter.playerButton.PlayerButtonStateManager.setDealer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import yuku.ambilwarna.AmbilWarnaDialog


//enum class PlayerButtonState {
//    NORMAL, COMMANDER_DEALER, COMMANDER_RECEIVER, SETTINGS
//}


object PlayerButtonStateManager {
    private val buttonStates = arrayListOf<MutableState<PlayerButtonState>>()

    fun registerButtonState(state: MutableState<PlayerButtonState>) {
        buttonStates.add(state)
    }

    fun updateAllStates(state: PlayerButtonState) {
        buttonStates.forEach { it.value = state }
    }

    var currentDealer: Player? = null

    fun setDealer(dealer: Player) {
        currentDealer = dealer
    }

    fun addDamageToPlayer(receiver: Int, damage: Int) {
        val dealer = currentDealer!!
        dealer.commanderDamage[receiver] = dealer.commanderDamage[receiver] + damage
        println("Added $damage damage to player $receiver : ${dealer.commanderDamage[receiver]}")
    }

    fun getDamageToPlayer(receiver: Int): Int {
        val dealer = currentDealer ?: return 0
        val damage = dealer.commanderDamage[receiver]
        println("Getting damage ($damage) to player $receiver")
        return damage
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
            initialState = PlayerButtonState.NORMAL
        )
    }
}

@Composable
fun PlayerButton(
    player: Player,
    initialState: PlayerButtonState,
    width: Dp = 400.dp,
    height: Dp = 300.dp
) {

    val state = remember { mutableStateOf(initialState) }
    PlayerButtonStateManager.registerButtonState(state)
    val id = player.playerNum

    val life by remember(player) {
        derivedStateOf { player.life }
    }

    val name by remember(player) {
        derivedStateOf { player.name }
    }

    val baseColor by remember(player) {
        derivedStateOf { Color(player.playerColor) }
    }

    val monarch by remember(player) {
        derivedStateOf { player.monarch }
    }

    val borderCardWidth = remember(monarch) {
        mutableStateOf(if (monarch) 0.dp else 0.dp)
    }

    val recentChange by remember(player) {
        derivedStateOf { player.recentChange }
    }

    val commanderDamage = remember { mutableIntStateOf(getDamageToPlayer(id)) }

    val context = LocalContext.current

    LaunchedEffect(life) {
        if (state.value == PlayerButtonState.COMMANDER_RECEIVER) {
            commanderDamage.intValue = getDamageToPlayer(id)
        }
    }
//    Color(baseColor.toArgb().desaturateColor(0.4f).darkenColor(1.1f))
    val visibleColor by remember(baseColor, state) {
        derivedStateOf {
            var c = when (state.value) {
                PlayerButtonState.NORMAL -> baseColor
                PlayerButtonState.COMMANDER_RECEIVER -> Color.DarkGray
                PlayerButtonState.COMMANDER_DEALER -> Color(
                    baseColor.toArgb().desaturateColor().darkenColor()
                )

                else -> Color(baseColor.toArgb().desaturateColor(0.8f).darkenColor(0.8f))
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

    Box(modifier = Modifier.bounceClick()) {
        if (monarch) {
            AnimatedBorderCard(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(all = 0.dp),
                borderWidth = borderCardWidth,
                gradient = Brush.sweepGradient(
                    listOf(
                        Color.White.copy(alpha = 0.1f),
                        Color(255, 191, 8),
                        Color(255, 191, 8),
                        Color(255, 191, 8),
                    )
                ),
                animationDuration = 6500
            ) {
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(height)
                        .background(Color.Transparent)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(30.dp))
                )
            }
        }

        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .background(Color.Transparent)
                .padding(5.dp)
                .clip(RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            LifeChangeButtons(
                onIncrementLife = { onIncrementLife() },
                onDecrementLife = { onDecrementLife() },
                color = visibleColor
            )


            when (state.value) {
                PlayerButtonState.NORMAL -> PlayerInfo(
                    playerName = name,
                    life = life,
                    recentChange = recentChange,
                    state = state.value
                )

                PlayerButtonState.COMMANDER_RECEIVER -> PlayerInfo(
                    playerName = name,
                    life = commanderDamage.intValue,
                    recentChange = recentChange,
                    state = state.value
                )

                PlayerButtonState.COMMANDER_DEALER -> Text(
                    text = "Deal damage with your commander",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Center)
                )

                PlayerButtonState.SETTINGS -> SettingsMenu(
                    player = player,
                    onColorButtonClick = { /* Handle color button click */ },
                    onChangeNameButtonClick = { /* Handle change name button click */ },
                    onMonarchyButtonClick = {
                        player.monarch = !player.monarch},
                    onSavePlayerButtonClick = {
                        PlayerDataManager(context).savePlayer(player)},
                    onLoadPlayerButtonClick = { /* Handle load player button click */ },
                    onImageButtonClick = { /* Handle image button click */ },
                    closeSettingsMenu = { state.value = PlayerButtonState.NORMAL }
                )
            }

            // Overlay PlayerButtonStateIcon on top-right corner
            PlayerButtonStateButtons(
                state = state.value,
                commanderButtonOnClick = { commanderButtonOnClick() },
                settingsButtonOnClick = { settingsButtonOnClick() }
            )

        }

    }
}

@Composable
fun PlayerButtonStateButtons(
    state: PlayerButtonState,
    commanderButtonOnClick: () -> Unit,
    settingsButtonOnClick: () -> Unit
) {
    val commanderButtonVisible =
        state != PlayerButtonState.COMMANDER_RECEIVER && state != PlayerButtonState.SETTINGS
    val settingsButtonVisible =
        state != PlayerButtonState.COMMANDER_DEALER && state != PlayerButtonState.COMMANDER_RECEIVER

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
                        .size(65.dp)
                        .padding(bottom = 2.5.dp, start = 2.5.dp)
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
                        .size(65.dp)
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
fun PlayerInfo(playerName: String, life: Int, recentChange: Int, state: PlayerButtonState) {
    val iconID = when (state) {
        PlayerButtonState.NORMAL -> R.drawable.heart_solid_icon
        PlayerButtonState.COMMANDER_DEALER -> R.drawable.transparent
        PlayerButtonState.COMMANDER_RECEIVER -> R.drawable.commander_solid_icon_small
        PlayerButtonState.SETTINGS -> R.drawable.transparent
        else -> R.drawable.transparent
    }

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
                fontSize = 150.sp
            )
            Spacer(modifier = Modifier.weight(0.1f))
            val recentChangeText =
                if (recentChange == 0) ""
                else if (recentChange > 0) "+$recentChange"
                else "$recentChange"
            Box(modifier = Modifier
                .weight(0.9f)
                .fillMaxWidth()) {
                Text(
                    modifier = Modifier,
                    text = recentChangeText,
                    color = Color.White,
                    fontSize = 24.sp
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = playerName, color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(150.dp))
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
    closeSettingsMenu: () -> Unit
) {
    val size = 100.dp

    var state by remember { mutableStateOf(SettingsState.Default) }

    when (state) {
        SettingsState.Default -> {
            LazyHorizontalGrid(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 50.dp, vertical = 30.dp),
                rows = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                        size = size,
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
                        size = size,
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
                        size = size,
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
                        size = size,
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
                        size = size,
                        backgroundColor = Color.Transparent
                    )
                }

                item {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.change_background_icon),
                        text = "Set Background",
                        onClick = {
//                            onImageButtonClick()
                        },
                        size = size,
                        backgroundColor = Color.Transparent
                    )
                }
            }
        }

        SettingsState.ColorPicker -> {
            // Define a list of colors
            val colorList = listOf(
                Color.Magenta,
                Color.Green,
                Color.Blue,
                Color.Cyan,
                Color.Yellow,
                Color.Red,
                // ... add more colors as needed
            )

            LazyHorizontalGrid(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(126.dp),
                rows = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                content = {
                    items(colorList) { color ->
                        ColorPickerButton(player = player, color = color)
                    }
                    item {
                        CustomColorPickerButton(player = player)
                    }
                }
            )
        }
        SettingsState.LoadPlayer -> {
            val context = LocalContext.current
            val playerList = remember { mutableStateListOf<Player>() }

            // Load players initially
            DisposableEffect(context) {
                playerList.addAll(PlayerDataManager(context).loadPlayers())
                onDispose {
                    // Cleanup if needed
                }
            }

            LazyHorizontalGrid(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(252.dp)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.Black.copy(alpha = 0.15f)),
                rows = GridCells.Fixed(3),
                state = rememberLazyGridState(),
                content = {
                    items(playerList) { p ->
                        MiniPlayerButton(currPlayer = player, player = p, playerList = playerList)
                    }
                }
            )
        }

        SettingsState.ChangeName -> {
            var newName by remember { mutableStateOf(player.name) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
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
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        player.name = newName
                        closeSettingsMenu()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),

                ) {
                    Text("Save Name")
                }
            }
        }


        else -> {}
    }
}

@Composable
fun MiniPlayerButton(currPlayer: Player, player: Player, playerList: MutableList<Player>) {
    val height = 50.dp
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .height(height)
            .width(height * 3)
            .padding(10.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color(player.playerColor))
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
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}



@Composable
fun ColorPickerButton(player: Player, color: Color) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .padding(4.dp)
            .background(color)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        player.playerColor = color.toArgb()
                    },
                )
            }
    ) {
    }
}

@Composable
fun CustomColorPickerButton(player: Player) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(60.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val initialColor = player.playerColor

                        val colorPickerDialog = AmbilWarnaDialog(
                            context,
                            initialColor,
                            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                                override fun onCancel(dialog: AmbilWarnaDialog?) {
                                    // User canceled the color picker dialog
                                }

                                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                                    player.playerColor = color
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
                .padding(4.dp),
            tint = Color.White // Set the tint to make the button's background transparent
        )
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

    pointerInput(interactionSource, isEnabled) {
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

@Composable
fun LifeChangeButtons(
    onIncrementLife: () -> Unit,
    onDecrementLife: () -> Unit,
    color: Color
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = Modifier.fillMaxWidth()) {

        CustomIncrementButton(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f),
            onIncrementLife = onIncrementLife,
            color = color,
            interactionSource = interactionSource
        )

        CustomIncrementButton(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1.0f),
            onIncrementLife = onDecrementLife,
            color = Color(color.toArgb().darkenColor(0.985f)),
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
        targetValue = if (buttonState == ButtonState.Pressed) 1.0f else 0.9925f,
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

private object NoRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor() = Color.Unspecified

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(0.0f, 0.0f, 0.0f, 0.0f)
}
