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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.mtglifeappcompose.R
import mtglifeappcompose.composable.SettingsButton
import mtglifeappcompose.composable.VerticalRotation
import mtglifeappcompose.composable.animatedBorderCard
import mtglifeappcompose.composable.bounceClick
import mtglifeappcompose.composable.lifecounter.PlayerButtonStateManager.setDealer
import mtglifeappcompose.composable.repeatingClickable
import mtglifeappcompose.composable.rotateVertically
import mtglifeappcompose.data.ImageManager
import mtglifeappcompose.data.Player
import mtglifeappcompose.data.PlayerDataManager
import mtglifeappcompose.ui.theme.Gold
import mtglifeappcompose.ui.theme.allPlayerColors
import mtglifeappcompose.ui.theme.blendWith
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

    var currentDealer: Player? = null

    fun setDealer(dealer: Player) {
        currentDealer = dealer
    }
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

    val context = LocalContext.current

    fun commanderButtonOnClick() {
        state.value = when (state.value) {
            PlayerButtonState.NORMAL -> {
                setDealer(player)
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

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { selectedUri ->
                val copiedUri = imageManager.copyImageToInternalStorage(selectedUri)
                player.imageUri = copiedUri
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
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Warning")
            .setMessage("This will open the camera roll. Proceed?")
            .setPositiveButton("Proceed") { _, _ ->
                openCameraRoll()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
            .bounceClick()
            .then(
                when (rotation) {
                    90f -> Modifier.rotateVertically(rotation = VerticalRotation.CLOCKWISE)
                    270f -> Modifier.rotateVertically(rotation = VerticalRotation.COUNTER_CLOCKWISE)
                    180f -> Modifier.rotate(180f)
                    else -> Modifier
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

        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .padding(3.dp)
                .background(Color.Transparent)
                .clip(RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            PlayerButtonBackground(player = player, state = state.value)

            LifeChangeButtons(onIncrementLife = { onIncrementLife() },
                onDecrementLife = { onDecrementLife() })

            when (state.value) {
                PlayerButtonState.NORMAL, PlayerButtonState.COMMANDER_RECEIVER -> PlayerInfo(
                    player = player, state = state.value, buttonSize = DpSize(width, height)
                )

                PlayerButtonState.COMMANDER_DEALER -> Text(
                    text = "Deal damage with your commander",
                    color = player.textColor,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )

                PlayerButtonState.SETTINGS -> SettingsMenu(player = player,
                    onColorButtonClick = { /* Handle color button click */ },
                    onChangeNameButtonClick = { /* Handle change name button click */ },
                    onMonarchyButtonClick = { player.toggleMonarch() },
                    onSavePlayerButtonClick = {
                        if (player.name in arrayOf("P1", "P2", "P3", "P4", "P5", "P6")) {
                            false
                        } else {
                            PlayerDataManager(context).savePlayer(player)
                            true
                        }
                    },
                    onLoadPlayerButtonClick = { /* Handle load player button click */ },
                    onImageButtonClick = {
                        if (player.imageUri == null) {
                            showWarningDialog()
                        } else {
                            player.imageUri = null
                        }
                    },
                    closeSettingsMenu = { state.value = PlayerButtonState.NORMAL },
                    buttonSize = DpSize(width, height)
                )
            }

            PlayerButtonStateButtons(
                state = state.value,
                color = player.textColor,
                commanderButtonOnClick = { commanderButtonOnClick() },
                settingsButtonOnClick = { settingsButtonOnClick() },
                buttonSize = DpSize(width, height)
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
fun PlayerButtonStateButtons(
    state: PlayerButtonState,
    commanderButtonOnClick: () -> Unit,
    settingsButtonOnClick: () -> Unit,
    color: Color,
    buttonSize: DpSize
) {
    val commanderButtonVisible =
        state != PlayerButtonState.COMMANDER_RECEIVER && state != PlayerButtonState.SETTINGS
    val settingsButtonVisible =
        state != PlayerButtonState.COMMANDER_DEALER && state != PlayerButtonState.COMMANDER_RECEIVER
    val smallButtonSize = buttonSize.width / 12f + buttonSize.height / 8f
    val smallButtonMargin = smallButtonSize / 10f

    Box(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = smallButtonMargin * 1.2f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            SettingsButton(
                modifier = Modifier.padding(
                    start = smallButtonMargin, bottom = smallButtonMargin
                ),
                size = smallButtonSize - smallButtonMargin,
                backgroundColor = Color.Transparent,
                mainColor = color,
                imageResource = painterResource(id = R.drawable.commander_solid_icon),
                visible = commanderButtonVisible,
                onPress = commanderButtonOnClick
            )

            SettingsButton(
                size = smallButtonSize,
                backgroundColor = Color.Transparent,
                mainColor = color,
                imageResource = painterResource(id = R.drawable.settings_solid_icon),
                visible = settingsButtonVisible,
                onPress = settingsButtonOnClick
            )
        }
    }
}


@Composable
fun PlayerInfo(
    player: Player, state: PlayerButtonState, buttonSize: DpSize
) {
    val iconID = when (state) {
        PlayerButtonState.NORMAL -> R.drawable.heart_solid_icon
        PlayerButtonState.COMMANDER_DEALER -> R.drawable.transparent
        PlayerButtonState.COMMANDER_RECEIVER -> R.drawable.commander_solid_icon_small
        PlayerButtonState.SETTINGS -> R.drawable.transparent
        else -> R.drawable.transparent
    }
    var largeTextSize = buttonSize.height.value.sp / 1.75f
    val smallTextSize = buttonSize.height.value.sp / 12f
    var recentChangeSize = buttonSize.height.value.sp / 17.5f

    val wideButton = (largeTextSize.value.dp + recentChangeSize.value.dp) * 2.75f < buttonSize.width
    if (wideButton) {
        largeTextSize *= 1.3f
        recentChangeSize *= 1.75f
    }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = buttonSize.height / 60f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier.wrapContentSize(unbounded = true),
                text = (when (state) {
                    PlayerButtonState.NORMAL -> player.life
                    PlayerButtonState.COMMANDER_RECEIVER -> {
                        PlayerButtonStateManager.currentDealer?.commanderDamage?.get(
                            player.playerNum - 1
                        )
                    }

                    else -> throw Exception("unsupported state")
                }).toString(),
                color = player.textColor,
                fontSize = largeTextSize,
                fontWeight = FontWeight.Bold,
                style = textShadowStyle

            )
            Spacer(modifier = Modifier.weight(0.1f))
            val recentChangeText =
                if (player.recentChange == 0 || state == PlayerButtonState.COMMANDER_RECEIVER) ""
                else if (player.recentChange > 0) "+${player.recentChange}"
                else "${player.recentChange}"

            Text(
                modifier = Modifier
                    .weight(0.9f)
                    .align(Alignment.CenterVertically),
                text = recentChangeText,
                color = player.textColor,
                fontSize = recentChangeSize,
                style = textShadowStyle
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = buttonSize.height / 30f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = player.name,
                color = player.textColor,
                fontSize = smallTextSize,
                textAlign = TextAlign.Center,
                style = textShadowStyle
            )
            Spacer(modifier = Modifier.height(buttonSize.height / 1.65f))
            SettingsButton(
                size = 24.dp,
                backgroundColor = Color.Transparent,
                mainColor = player.textColor,
                imageResource = painterResource(id = iconID),
                enabled = false
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
    onSavePlayerButtonClick: () -> Boolean,
    onLoadPlayerButtonClick: () -> Unit,
    onImageButtonClick: () -> Unit,
    closeSettingsMenu: () -> Unit,
    buttonSize: DpSize
) {
    val settingsButtonInitialSize = buttonSize.height / 3.75f + buttonSize.width / 24f
    val colorPickerButtonInitialSize = buttonSize.height / 4f + buttonSize.width / 48f
    val margin = buttonSize.width / 12f + buttonSize.height / 8f

    val wideButton =
        settingsButtonInitialSize * 3 < buttonSize.width - (buttonSize.width / 6f + buttonSize.height / 4f) * 1.2f
//    println("player: $player.playerNum is wide button: $wideButton")
    val bottomMargin = if (!wideButton) margin else margin / 6f
    val topMargin = margin / 6f
    val settingsButtonSize =
        if (!wideButton) settingsButtonInitialSize else settingsButtonInitialSize * 1.25f
    val colorPickerButtonSize =
        if (!wideButton) colorPickerButtonInitialSize else colorPickerButtonInitialSize * 1.25f

    val smallMargin = settingsButtonSize / 20f
    var state by remember { mutableStateOf(SettingsState.Default) }
    val gridMarginSize =
        settingsButtonSize / 16f + buttonSize.height / 120f + buttonSize.width / 120f

    var savePlayerColor by remember { mutableStateOf(player.textColor) }

    when (state) {
        SettingsState.Default -> {
            LazyHorizontalGrid(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(bottom = bottomMargin, top = topMargin), rows = GridCells.Fixed(2)
            ) {

                item {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.upload_icon),
                        text = "Save Player",
                        onPress = {
                            savePlayerColor = if (onSavePlayerButtonClick()) {
                                Color.Green.blendWith(player.textColor)
                            } else {
                                Color.Red.blendWith(player.textColor)
                            }
                        },
                        size = settingsButtonSize,
                        mainColor = savePlayerColor,
                        backgroundColor = Color.Transparent
                    )
                }
                item {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.download_icon),
                        text = "Load Player",
                        onPress = {
                            state = SettingsState.LoadPlayer
//                            onLoadPlayerButtonClick()
                        },
                        size = settingsButtonSize,
                        mainColor = player.textColor,
                        backgroundColor = Color.Transparent
                    )
                }
                item {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.monarchy_icon),
                        text = "Monarch",
                        onPress = {
                            onMonarchyButtonClick()
//                            closeSettingsMenu()
                        },
                        size = settingsButtonSize,
                        mainColor = player.textColor,
                        backgroundColor = Color.Transparent
                    )
                }
                item {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.change_name_icon),
                        text = "Change Name",
                        onPress = {
                            state = SettingsState.ChangeName
//                            onChangeNameButtonClick()
                        },
                        size = settingsButtonSize,
                        mainColor = player.textColor,
                        backgroundColor = Color.Transparent
                    )
                }

                item {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.color_picker_icon),
                        text = "Set Color",
                        onPress = {
                            state = SettingsState.ColorPicker
//                            onColorButtonClick()
                        },
                        size = settingsButtonSize,
                        mainColor = player.textColor,
                        backgroundColor = Color.Transparent
                    )
                }

                item {
                    val changeBackgroundText by remember {
                        derivedStateOf {
                            if (player.imageUri != null) {
                                "Undo Background"
                            } else {
                                "Set Background"
                            }
                        }
                    }
                    SettingsButton(
                        imageResource = painterResource(R.drawable.change_background_icon),
                        text = changeBackgroundText,
                        onPress = {
                            onImageButtonClick()
                        },
                        size = settingsButtonSize,
                        mainColor = player.textColor,
                        backgroundColor = Color.Transparent
                    )
                }
            }
        }

        SettingsState.ColorPicker -> {
            val colorList = allPlayerColors
            var modifiedPlayerColor1 = remember(player.color) {
                derivedStateOf {
                    player.color.saturateColor(1.5f).brightenColor(1.35f)
                }
            }
            var modifiedPlayerColor2 = remember(player.color) {
                derivedStateOf {
                    player.color.brightenColor(0.30f).saturateColor(1.5f)
                }
            }

            val textColorList = remember {
                listOf(
                    mutableStateOf(Color.White),
                    modifiedPlayerColor1,
                    modifiedPlayerColor2,
                    mutableStateOf(Color.Black)
                )
            }
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(
                        start = smallMargin,
                        top = smallMargin,
                        end = smallMargin,
                        bottom = smallMargin
//                        bottom = if (!wideButton) margin else smallMargin
                    )
            ) {
                Column(
                    Modifier.wrapContentSize(), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(
                                bottom = buttonSize.height / 30f,
                                start = buttonSize.width / 30f,
                                end = buttonSize.width / 30f
                            ),
                        text = "Choose a background or text color",
                        color = player.textColor,
                        fontSize = buttonSize.height.value.sp / 15f,
                        textAlign = TextAlign.Center
                    )
                    LazyHorizontalGrid(modifier = Modifier
                        .wrapContentWidth()
                        .height((settingsButtonSize * 1.0f) + gridMarginSize)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.15f))
                        .padding(gridMarginSize),
                        rows = GridCells.Fixed(2),
                        state = rememberLazyGridState(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSize.height / 150f),
                        verticalArrangement = Arrangement.spacedBy(buttonSize.height / 150f),
                        content = {
                            item {
                                CustomColorPickerButton(
                                    player = player, settingsButtonSize = colorPickerButtonSize
                                )
                            }
                            items(colorList) { color ->
                                ColorPickerButton(
                                    onClick = {
                                        player.color = color
                                    }, color = color, settingsButtonSize = colorPickerButtonSize
                                )
                            }
                        })
                    LazyHorizontalGrid(modifier = Modifier
                        .wrapContentWidth()
                        .height((settingsButtonSize * 1.0f + gridMarginSize) / 2f)
                        .clip(
                            RoundedCornerShape(
                                topStart = 0.dp,
                                topEnd = 0.dp,
                                bottomStart = 20.dp,
                                bottomEnd = 20.dp
                            )
                        )
                        .background(Color.Black.copy(alpha = 0.15f))
                        .padding(
                            start = gridMarginSize, end = gridMarginSize, bottom = gridMarginSize
                        ),
                        rows = GridCells.Fixed(1),
                        state = rememberLazyGridState(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSize.height / 150f),
                        verticalArrangement = Arrangement.spacedBy(buttonSize.height / 150f),
                        content = {
                            items(textColorList) { color ->
                                ColorPickerButton(
                                    onClick = {
                                        player.textColor = color.value
                                    },
                                    color = color.value,
                                    settingsButtonSize = colorPickerButtonSize
                                )
                            }
                        })
                }


            }
        }


        SettingsState.LoadPlayer -> {
            val context = LocalContext.current
            val playerList = remember { mutableStateListOf<Player>() }

            DisposableEffect(context) {
                playerList.addAll(PlayerDataManager(context).loadPlayers())
                onDispose {}
            }
            Column(
                Modifier
                    .wrapContentSize()
                    .padding(
                        start = if (wideButton) margin else smallMargin,
                        top = smallMargin,
                        end = if (wideButton) margin else smallMargin,
                        bottom = if (!wideButton) margin else smallMargin
                    ), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize(unbounded = true)
                        .padding(bottom = smallMargin),
                    text = "Saved players",
                    color = player.textColor,
                    fontSize = buttonSize.height.value.sp / 15f,
                    textAlign = TextAlign.Center
                )
                LazyHorizontalGrid(modifier = Modifier
                    .wrapContentWidth()
                    .height(buttonSize.height / 2f + smallMargin * 1.5f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.15f))
                    .padding(smallMargin * 2f),
                    rows = GridCells.Fixed(2),
                    state = rememberLazyGridState(),
                    horizontalArrangement = Arrangement.spacedBy(smallMargin),
                    verticalArrangement = Arrangement.spacedBy(smallMargin),
                    content = {
                        items(playerList) { p ->
                            MiniPlayerButton(
                                currPlayer = player,
                                player = p,
                                playerList = playerList,
                                buttonSize = buttonSize
                            )
                        }
                    })
            }
        }

        SettingsState.ChangeName -> {
            ChangeNameField(
                closeSettingsMenu = closeSettingsMenu,
                player = player,
                buttonSize = buttonSize,
                wideButton = wideButton,
                margin = margin
            )
        }

        else -> {}
    }
}

@Composable
fun ChangeNameField(
    closeSettingsMenu: () -> Unit,
    player: Player,
    buttonSize: DpSize,
    wideButton: Boolean,
    margin: Dp
) {
    var newName by remember { mutableStateOf(player.name) }
    val nameFieldMargin = if (wideButton) margin else margin / 3f
    val textColor = player.textColor.invert().copy(alpha = 1.0f)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = nameFieldMargin, vertical = buttonSize.height / 40f),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(value = newName,
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
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                player.name = newName
                closeSettingsMenu()
            }),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(buttonSize.height / 40f)
        )
        Spacer(modifier = Modifier.height(margin / 8f))
        Button(
            onClick = {
                player.name = newName
                closeSettingsMenu()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = player.textColor, contentColor = player.color
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = buttonSize.height / 40f)
                .padding(bottom = buttonSize.height / 10f)
                .wrapContentHeight()

        ) {
            Text("Save Name")
        }
    }
}

@Composable
fun MiniPlayerButton(
    currPlayer: Player, player: Player, playerList: MutableList<Player>, buttonSize: DpSize
) {
    val height = buttonSize.height / 9f * 1.5f
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .height(height)
            .width(height * 3)
            .clip(RoundedCornerShape(15.dp))
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
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
                Surface(modifier = Modifier.fillMaxSize(), color = player.color) {

                }
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
                fontSize = height.value.sp / 1.8f,
                maxLines = 1,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}


@Composable
fun ColorPickerButton(onClick: () -> Unit, color: Color, settingsButtonSize: Dp) {
    Box(modifier = Modifier
        .size(settingsButtonSize / 2f)
        .padding(settingsButtonSize / 50f)
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
fun CustomColorPickerButton(player: Player, settingsButtonSize: Dp) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .size(settingsButtonSize / 2.0f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val initialColor = player.color

                        val colorPickerDialog = AmbilWarnaDialog(context,
                            initialColor.toArgb(),
                            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                                override fun onCancel(dialog: AmbilWarnaDialog?) {
                                    // User canceled the color picker dialog
                                }

                                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                                    player.color = Color(color)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(settingsButtonSize / 80f),
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
        modifier = modifier.repeatingClickable(
            interactionSource = interactionSource,
            enabled = true,
            onClick = {
                onIncrementLife()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            })
    )

}

