package composable.lifecounter


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import composable.dialog.ColorDialog
import composable.dialog.ScryfallSearchDialog
import composable.dialog.SettingsButton
import composable.dialog.WarningDialog
import composable.modifier.VerticalRotation
import composable.modifier.animatedBorderCard
import composable.modifier.bounceClick
import composable.modifier.repeatingClickable
import composable.modifier.rotateVertically
import data.Player
import data.SettingsManager
import data.SettingsManager.cameraRollDisabled
import data.initImageManager
import getAnimationCorrectionFactor
import kotlinx.coroutines.launch
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.add_icon
import lifelinked.shared.generated.resources.back_icon
import lifelinked.shared.generated.resources.change_background_icon
import lifelinked.shared.generated.resources.change_name_icon
import lifelinked.shared.generated.resources.color_picker_icon
import lifelinked.shared.generated.resources.commander_solid_icon
import lifelinked.shared.generated.resources.custom_color_icon
import lifelinked.shared.generated.resources.download_icon
import lifelinked.shared.generated.resources.enter_icon
import lifelinked.shared.generated.resources.heart_solid_icon
import lifelinked.shared.generated.resources.mana_icon
import lifelinked.shared.generated.resources.monarchy_icon
import lifelinked.shared.generated.resources.reset_icon
import lifelinked.shared.generated.resources.search_icon
import lifelinked.shared.generated.resources.settings_icon
import lifelinked.shared.generated.resources.skull_icon
import lifelinked.shared.generated.resources.sword_icon
import lifelinked.shared.generated.resources.sword_icon_double
import lifelinked.shared.generated.resources.text_icon
import lifelinked.shared.generated.resources.transparent
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import theme.allPlayerColors
import theme.brightenColor
import theme.deadDealerColorMatrix
import theme.deadNormalColorMatrix
import theme.deadReceiverColorMatrix
import theme.deadSettingsColorMatrix
import theme.dealerColorMatrix
import theme.ghostify
import theme.invert
import theme.normalColorMatrix
import theme.receiverColorMatrix
import theme.saturateColor
import theme.scaledSp
import theme.settingsColorMatrix
import theme.textShadowStyle
import kotlin.math.min

/**
 * Possible states for the [PlayerButton]  composable
 */
enum class PlayerButtonState {
    NORMAL, COMMANDER_DEALER, COMMANDER_RECEIVER, SETTINGS
}

/**
 * Player Button composable
 * @param modifier Modifier to apply to the layout
 * @param player The player to display
 * @param initialState The initial state of the button
 * @param rotation The rotation of the button
 * @param component The [LifeCounterComponent] that this button belongs to
 * @return The composable
 */
@Composable
fun PlayerButton(
    modifier: Modifier = Modifier, player: Player, initialState: PlayerButtonState = PlayerButtonState.NORMAL, rotation: Float = 0f, component: LifeCounterComponent
) {
    val haptic = LocalHapticFeedback.current

    val imageManager = initImageManager()
    var showScryfallSearch by remember { mutableStateOf(false) }
    val backStack = remember { mutableStateListOf<() -> Unit>() }
    val scryfallBackStack = remember {
        mutableStateListOf({
            showScryfallSearch = false
            component.blurBackground.value = false
        })
    }
    val state = remember { mutableStateOf(initialState) }
    component.registerButtonState(state)
    val commanderButtonVisible = state.value in listOf(
        PlayerButtonState.NORMAL,
        PlayerButtonState.COMMANDER_DEALER
    )
    val settingsButtonVisible = state.value in listOf(
        PlayerButtonState.NORMAL,
        PlayerButtonState.SETTINGS
    )
    val scope = rememberCoroutineScope()
    var showFilePicker by remember { mutableStateOf(false) }
    var showCameraWarning by remember { mutableStateOf(false) }
    var showResetPrefsDialog by remember { mutableStateOf(false) }

    val fileType = listOf("jpg", "png")
    FilePicker(show = showFilePicker, fileExtensions = fileType) { file ->
        showFilePicker = false
        if (file != null) {
            scope.launch {
                val copiedUri = imageManager.copyImageToLocalStorage(file.path, player.name)
                player.imageUri = copiedUri
                SettingsManager.savePlayerPref(player)
            }
        }
    }

    if (showResetPrefsDialog) {
        WarningDialog(
            title = "Reset Preferences",
            message = "Are you sure you want to reset your customizations?",
            optionOneEnabled = true,
            optionTwoEnabled = true,
            optionOneMessage = "Reset",
            optionTwoMessage = "Cancel",
            onOptionOne = {
                component.resetPlayerPrefs(player)
                showResetPrefsDialog = false
            },
            onDismiss = {
                showResetPrefsDialog = false
            }
        )
    }

    if (showCameraWarning) {
        if (cameraRollDisabled) {
            WarningDialog(
                title = "Info",
                message = "Camera roll access is disabled. Enable in settings.",
                optionOneEnabled = false,
                optionTwoEnabled = true,
                onDismiss = {
                    showCameraWarning = false
                }
            )
        } else {
            WarningDialog(
                title = "Warning",
                message = "This will open the camera roll. Proceed?",
                optionOneEnabled = true,
                optionTwoEnabled = true,
                optionOneMessage = "Proceed",
                optionTwoMessage = "Cancel",
                onOptionOne = {
                    showFilePicker = true
                    showCameraWarning = false
                },
                onDismiss = {
                    showCameraWarning = false
                }
            )
        }
    }

    LaunchedEffect(state.value) {
        if (state.value == PlayerButtonState.COMMANDER_RECEIVER) backStack.clear()
    }

    BoxWithConstraints(
        modifier = Modifier.wrapContentSize().then(
            when (rotation) {
                90f -> Modifier.rotateVertically(rotation = VerticalRotation.CLOCKWISE)
                270f -> Modifier.rotateVertically(rotation = VerticalRotation.COUNTER_CLOCKWISE)
                180f -> Modifier.rotate(180f)
                else -> Modifier
            }
        ).then(
            if (state.value == PlayerButtonState.NORMAL || state.value == PlayerButtonState.COMMANDER_RECEIVER) {
                Modifier.bounceClick()
            } else {
                Modifier
            }
        )
    ) {
        if (showScryfallSearch) {
            ScryfallSearchDialog(
                modifier = Modifier.onGloballyPositioned { _ ->
                    component.blurBackground.value = true
                },
                onDismiss = {
                    showScryfallSearch = false
                    component.blurBackground.value = false
                },
                backStack = scryfallBackStack,
                player = player
            )
        }

        MonarchyIndicator(
            modifier = modifier,
            monarch = player.monarch
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().background(Color.Transparent).clip(RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                PlayerButtonBackground(
                    player = player,
                    state = state.value
                )


                val smallButtonSize = (maxWidth / 15f) + (maxHeight / 10f)
                val settingsStateMargin = smallButtonSize / 7f
                val commanderStateMargin = settingsStateMargin * 1.4f

                val wideButton = maxWidth / maxHeight > 1.4

                val playerInfoPadding = if (wideButton) Modifier.padding(bottom = smallButtonSize / 2) else Modifier.padding(
                    top = smallButtonSize / 2
                )

                val settingsPadding = if (wideButton) Modifier.padding(
                    bottom = smallButtonSize / 4,
                    top = smallButtonSize / 8
                ) else Modifier.padding(
                    top = smallButtonSize / 2
                )
                if (!player.setDead) {
                    when (state.value) {
                        PlayerButtonState.NORMAL -> {
                            LifeChangeButtons(Modifier.fillMaxWidth(),
                                onIncrementLife = {
                                    player.incrementLife(1)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDecrementLife = {
                                    player.incrementLife(-1)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                })
                        }

                        PlayerButtonState.COMMANDER_RECEIVER -> {
                            Row(Modifier.fillMaxSize()) {
                                LifeChangeButtons(Modifier.then(if (component.currentDealerIsPartnered()) Modifier.fillMaxWidth(0.5f) else Modifier.fillMaxWidth()),
                                    onIncrementLife = {
                                        component.currentDealer?.incrementCommanderDamage(
                                            player,
                                            1
                                        )
                                        player.incrementLife(-1)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDecrementLife = {
                                        component.currentDealer?.incrementCommanderDamage(
                                            player,
                                            -1
                                        )
                                        player.incrementLife(1)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    })
                                if (component.currentDealerIsPartnered()) {
                                    LifeChangeButtons(Modifier.fillMaxWidth(),
                                        onIncrementLife = {
                                            component.currentDealer?.incrementCommanderDamage(
                                                player,
                                                1,
                                                true
                                            )
                                            player.incrementLife(-1)
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDecrementLife = {
                                            component.currentDealer?.incrementCommanderDamage(
                                                player,
                                                -1,
                                                true
                                            )
                                            player.incrementLife(1)
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        })
                                }
                            }
                        }

                        else -> {
                        }
                    }
                }

                @Composable
                fun Skull() {
                    SettingsButton(
                        modifier = playerInfoPadding.align(Alignment.Center).size(smallButtonSize * 4).padding(top = maxHeight / 9f),
                        backgroundColor = Color.Transparent,
                        mainColor = player.textColor,
                        imageVector = vectorResource(Res.drawable.skull_icon),
                        enabled = false
                    )
                }

                @Composable
                fun PlayerButtonContent(modifier: Modifier = Modifier) {
                    Box(modifier.fillMaxSize()) {
                        when (state.value) {
                            PlayerButtonState.NORMAL -> {
                                if (player.setDead || (SettingsManager.autoKo && player.isDead)) {
                                    Skull()
                                } else {
                                    LifeNumber(
                                        modifier = playerInfoPadding.fillMaxSize(),
                                        player = player
                                    )
                                }
                            }

                            PlayerButtonState.COMMANDER_RECEIVER -> {
                                if (player.setDead || (SettingsManager.autoKo && player.isDead)) {
                                    Skull()
                                } else {
                                    CommanderDamageNumber(
                                        modifier = playerInfoPadding.fillMaxSize(),
                                        player = player,
                                        currentDealer = component.currentDealer,
                                        partnerMode = component.currentDealerIsPartnered()
                                    )
                                }

                            }

                            PlayerButtonState.COMMANDER_DEALER -> {
                                val iconResource = if (component.currentDealerIsPartnered()) Res.drawable.sword_icon_double else Res.drawable.sword_icon
                                Column(
                                    Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Spacer(modifier = Modifier.height(40.dp))
                                    Text(
                                        modifier = Modifier,
                                        text = "Deal damage with your commander",
                                        color = player.textColor,
                                        fontSize = 20.scaledSp,
                                        lineHeight = 20.scaledSp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    SettingsButton(modifier = Modifier.size(smallButtonSize),
                                        imageVector = vectorResource(iconResource),
                                        backgroundColor = Color.Transparent,
                                        mainColor = player.textColor,
                                        onPress = {
                                            player.partnerMode = !player.partnerMode
                                        })
                                    Text(
                                        modifier = Modifier.wrapContentSize(unbounded = true),
                                        text = "Toggle Partner Mode",
                                        color = player.textColor,
                                        fontSize = 10.scaledSp,
                                        textAlign = TextAlign.Center
                                    )
                                }

                            }

                            PlayerButtonState.SETTINGS -> {
                                SettingsMenu(
                                    modifier = settingsPadding,
                                    player = player,
                                    backStack = backStack,
                                    onMonarchyButtonClick = { component.toggleMonarch(player) },
                                    onFromCameraRollButtonClick = { showCameraWarning = true },
                                    closeSettingsMenu = { state.value = PlayerButtonState.NORMAL },
                                    onScryfallButtonClick = {
                                        showScryfallSearch = !showScryfallSearch
                                    },
                                    onResetPrefsClick = {
                                        showResetPrefsDialog = true
//                                        component.resetPlayerPrefs(player)
                                                        },
                                    setBlurBackground = { component.blurBackground.value = it },
                                )
                            }
                        }
                    }
                }

                @Composable
                fun BackButton(modifier: Modifier = Modifier) {
                    SettingsButton(modifier = modifier.size(smallButtonSize * 1.1f).padding(
                        start = settingsStateMargin,
                        bottom = settingsStateMargin
                    ),
                        backgroundColor = Color.Transparent,
                        mainColor = player.textColor,
                        visible = backStack.isNotEmpty(),
                        imageVector = vectorResource(Res.drawable.back_icon),
                        onPress = { backStack.removeLast().invoke() })
                }

                @Composable
                fun CommanderStateButton(modifier: Modifier = Modifier) {
                    PlayerStateButton(
                        modifier = modifier.padding(
                            start = commanderStateMargin,
                            bottom = commanderStateMargin,
                        ),
                        visible = commanderButtonVisible,
                        iconResource = Res.drawable.commander_solid_icon,
                        color = player.textColor,
                        size = smallButtonSize
                    ) {
                        state.value = when (state.value) {
                            PlayerButtonState.NORMAL -> {
                                println("currentDealer: ${component.currentDealer?.name}")
                                component.currentDealer = player
                                println("currentDealer: ${component.currentDealer?.name}")
                                component.updateAllStates(PlayerButtonState.COMMANDER_RECEIVER)
                                PlayerButtonState.COMMANDER_DEALER
                            }

                            PlayerButtonState.COMMANDER_DEALER -> {
                                component.updateAllStates(PlayerButtonState.NORMAL)
                                PlayerButtonState.NORMAL
                            }

                            else -> throw Exception("Invalid state for commanderButtonOnClick")
                        }
                    }
                }

                @Composable
                fun BackButtonOrCommanderButton(modifier: Modifier = Modifier) {
                    if (commanderButtonVisible) {
                        CommanderStateButton(modifier)
                    } else if (backStack.isNotEmpty()) {
                        BackButton(modifier)
                    } else {
                        PlayerStateButton(
                            modifier = modifier.padding(
                                start = commanderStateMargin,
                                bottom = commanderStateMargin,
                            ),
                            visible = false,
                            iconResource = Res.drawable.commander_solid_icon,
                            color = player.textColor,
                            size = smallButtonSize
                        ) {}
                    }
                }

                @Composable
                fun SettingsStateButton(modifier: Modifier = Modifier) {
                    PlayerStateButton(
                        modifier = modifier.padding(
                            end = settingsStateMargin,
                            bottom = settingsStateMargin
                        ),
                        visible = settingsButtonVisible,
                        iconResource = Res.drawable.settings_icon,
                        color = player.textColor,
                        size = smallButtonSize
                    ) {
                        when (state.value) {
                            PlayerButtonState.SETTINGS -> {
                                state.value = PlayerButtonState.NORMAL
                                backStack.clear()
                            }

                            PlayerButtonState.NORMAL -> {
                                state.value = PlayerButtonState.SETTINGS
                                backStack.add { state.value = PlayerButtonState.NORMAL }
                            }

                            else -> throw Exception("Invalid state for settingsButtonOnClick")
                        }
                    }
                }

                if (wideButton) {
                    Row( // WIDE
                        Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BackButtonOrCommanderButton(Modifier.align(Alignment.Bottom))
                        PlayerButtonContent(Modifier.weight(0.5f))
                        SettingsStateButton(Modifier.align(Alignment.Bottom))
                    }
                } else {
                    Column(
                        Modifier.fillMaxSize() // TALL
                    ) {
                        PlayerButtonContent(Modifier.weight(0.5f))
                        Row(
                            Modifier.wrapContentHeight().fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BackButtonOrCommanderButton(Modifier.align(Alignment.Bottom))
                            SettingsStateButton(Modifier.align(Alignment.Bottom))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Monarchy indicator composable
 * @param modifier Modifier to apply to the layout
 * @param monarch Whether or not the player is the monarch
 * @param content The content to display
 */
@Composable
fun MonarchyIndicator(
    modifier: Modifier = Modifier, monarch: Boolean = false, content: @Composable () -> Unit = {}
) {
    val width = 2.5.dp
    val duration = (7500 / getAnimationCorrectionFactor()).toInt()
//    val colors = if (viewModel.getAnimationScale(context) != 0.0f) {
//        listOf(
//            Color.Transparent,
//            Color(255, 191, 8),
//            Color(255, 191, 8),
//            Color(255, 191, 8),
//        )
//    } else {
//        listOf(
//            Color(255, 191, 8),
//            Color(255, 191, 8),
//        )
//    }
//    val duration = 7500
    val colors = listOf(
        Color.Transparent,
        Color(
            255,
            191,
            8
        ),
        Color(
            255,
            191,
            8
        ),
        Color(
            255,
            191,
            8
        ),
    )

    Box(
        modifier = modifier.clip(RoundedCornerShape(30.dp)).then(
            if (monarch) {
                Modifier.animatedBorderCard(
                    shape = RoundedCornerShape(30.dp),
                    borderWidth = width,
                    colors = colors,
                    animationDuration = duration
                ).clip(RoundedCornerShape(30.dp))
            } else {
                Modifier.padding(width)
            }
        )
    ) {
        content()
    }
}

/**
 * Possible states for the [Counters] composable
 */
private enum class CounterMenuState {
    DEFAULT, ADD_COUNTER
}

/**
 * Settings menu composable
 * @param modifier Modifier to apply to the layout
 * @param player The player to display
 * @param backStack The back stack for the settings menu
 */
@Composable
fun Counters(
    modifier: Modifier = Modifier, player: Player, backStack: SnapshotStateList<() -> Unit>
) {
    var state by remember { mutableStateOf(CounterMenuState.DEFAULT) }
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(modifier.fillMaxSize()) {
        val smallPadding = maxHeight / 20f
        val smallTextSize = maxHeight.value.scaledSp / 12f
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.wrapContentSize(unbounded = true).padding(
                    top = smallPadding,
                    bottom = smallPadding / 2f
                ),
                text = if (state == CounterMenuState.DEFAULT) "Counters" else "Select Counters",
                color = player.textColor,
                fontSize = smallTextSize,
                textAlign = TextAlign.Center
            )
            Box(
                Modifier.fillMaxSize().weight(0.5f).background(
                    Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(25.dp)
                ).border(
                    0.5.dp,
                    player.textColor.copy(alpha = 0.9f),
                    RoundedCornerShape(25.dp)
                ).clip(RoundedCornerShape(25.dp))
            ) {
                when (state) {
                    CounterMenuState.DEFAULT -> {
                        LazyRow(
                            Modifier.fillMaxSize().padding(5.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            items(player.activeCounters) { counterType ->
                                Counter(player = player,
                                    iconResource = counterType.resource,
                                    value = player.getCounterValue(counterType),
                                    onIncrement = {
                                        player.incrementCounterValue(
                                            counterType,
                                            1
                                        )
                                    },
                                    onDecrement = {
                                        player.incrementCounterValue(
                                            counterType,
                                            -1
                                        )
                                    })
                            }
                            item {
                                AddCounter(
                                    player = player,
                                    onTap = {
                                        state = CounterMenuState.ADD_COUNTER
                                        backStack.add { state = CounterMenuState.DEFAULT }
                                    },
                                )
                            }
                        }
                    }

                    CounterMenuState.ADD_COUNTER -> {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LazyHorizontalGrid(
                                modifier = Modifier.fillMaxSize().padding(5.dp).clip(RoundedCornerShape(25.dp)),
                                rows = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.Center,
                                verticalArrangement = Arrangement.Center
                            ) {
                                items(CounterType.values()) { counterType ->
                                    Box(modifier = Modifier.fillMaxSize().aspectRatio(1.0f).padding(0.5.dp).background(
                                        if (counterType in player.activeCounters) {
                                            Color.Green.copy(alpha = 0.5f)
                                        } else {
                                            Color.Transparent
                                        }
                                    ).pointerInput(Unit) {
                                        detectTapGestures {
                                            if (counterType in player.activeCounters) {
                                                player.activeCounters.remove(counterType)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            } else {
                                                player.activeCounters.add(counterType)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                    }) {
                                        SettingsButton(
                                            imageVector = vectorResource(counterType.resource),
                                            modifier = Modifier.fillMaxSize().padding(5.dp),
                                            mainColor = player.textColor,
                                            backgroundColor = Color.Transparent,
                                            shadowEnabled = true,
                                            enabled = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Add counter composable
 * @param player The player to modify
 * @param onTap The callback for when the button is tapped
 */
@Composable
fun AddCounter(
    player: Player,
    onTap: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(Modifier.fillMaxHeight().aspectRatio(0.70f).padding(5.dp).bounceClick(0.0125f).background(
        Color.Black.copy(0.2f),
        shape = RoundedCornerShape(20.dp)
    ).border(
        0.5.dp,
        player.textColor.copy(alpha = 0.9f),
        RoundedCornerShape(20.dp)
    ).pointerInput(Unit) {
        detectTapGestures {
            onTap()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }) {
        val iconSize = maxHeight / 2.5f
        SettingsButton(
            modifier = Modifier.align(Alignment.Center).size(iconSize),
            imageVector = vectorResource(Res.drawable.add_icon),
            backgroundColor = Color.Transparent,
            mainColor = player.textColor,
            shadowEnabled = false,
            enabled = false
        )
    }
}

/**
 * Counter composable
 * @param player The player to modify
 * @param icon The icon to display
 * @param value The value of the counter
 * @param onIncrement The callback for when the increment button is pressed
 * @param onDecrement The callback for when the decrement button is pressed
 */
@Composable
fun Counter(
    player: Player, iconResource: DrawableResource, value: Int, onIncrement: () -> Unit, onDecrement: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(
        Modifier.fillMaxHeight().aspectRatio(0.70f).padding(5.dp).bounceClick(0.0125f).background(
            Color.Black.copy(0.2f),
            shape = RoundedCornerShape(20.dp)
        ).border(
            0.5.dp,
            player.textColor.copy(alpha = 0.9f),
            RoundedCornerShape(20.dp)
        ).clip(RoundedCornerShape(20.dp))
    ) {
        val textSize = (maxHeight.value / 2.8f + maxWidth.value / 6f + 30).scaledSp / 1.5f
        val topPadding = maxHeight / 10f
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f).background(Color.White.copy(alpha = 0.04f)).pointerInput(Unit) {
                detectTapGestures {
                    onIncrement()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            })
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(1.0f).background(Color.Black.copy(alpha = 0.04f)).pointerInput(Unit) {
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
                modifier = Modifier.wrapContentSize(),
                style = textShadowStyle()
            )
            SettingsButton(
                imageVector = vectorResource(iconResource),
                modifier = Modifier.fillMaxSize(0.35f).aspectRatio(1.0f).padding(bottom = 15.dp),
                mainColor = player.textColor,
                backgroundColor = Color.Transparent,
                shadowEnabled = true,
                enabled = false
            )
        }
    }
}

/**
 * Player Button Background composable
 * @param player The player to display
 * @param state The state of the button
 */
@Composable
fun PlayerButtonBackground(player: Player, state: PlayerButtonState) {
    if (player.imageUri == null) {
        var c = when (state) {
            PlayerButtonState.NORMAL -> player.color
            PlayerButtonState.COMMANDER_RECEIVER -> player.color.saturateColor(0.2f).brightenColor(0.3f)

            PlayerButtonState.COMMANDER_DEALER -> player.color.saturateColor(0.5f).brightenColor(0.6f)

            PlayerButtonState.SETTINGS -> player.color
        }
        if (player.isDead) {
            c = c.ghostify()
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = c
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
        }
        AsyncImage(
            model = player.imageUri!!,
            contentDescription = "Player uploaded image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.colorMatrix(
                colorMatrix = colorMatrix
            )
        )
    }
}

/**
 * State button composable
 * @param modifier Modifier to apply to the layout
 * @param size The size of the button
 * @param visible Whether or not the button is visible
 * @param icon The icon to display
 * @param color The color of the icon
 * @param onPress The callback for when the button is pressed
 */
@Composable
fun PlayerStateButton(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    visible: Boolean,
    iconResource: DrawableResource,
    color: Color,
    onPress: () -> Unit,
) {
    SettingsButton(
        modifier = modifier.size(size),
        backgroundColor = Color.Transparent,
        mainColor = color,
        imageVector = vectorResource(iconResource),
        visible = visible,
        onPress = onPress
    )
}

/**
 * Commander damage number composable
 * @param modifier Modifier to apply to the layout
 * @param player The player to display
 * @param currentDealer The current dealer
 * @param partnerMode Whether or not the player is in partner mode
 */
@Composable
fun CommanderDamageNumber(
    modifier: Modifier = Modifier, player: Player, currentDealer: Player?, partnerMode: Boolean
) {
    BoxWithConstraints(modifier = modifier) {
        val dividerOffset = maxHeight / 12f

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SingleCommanderDamageNumber(
                modifier = Modifier.padding(horizontal = 10.dp).then(if (partnerMode) Modifier.fillMaxWidth(0.5f) else Modifier.fillMaxWidth()),
                player = player,
                currentDealer = currentDealer,
                partner = false
            )
            if (partnerMode) {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(0.6f).width(2.dp).offset(y = dividerOffset),
                    color = player.textColor
                )

                SingleCommanderDamageNumber(
                    modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth(),
                    player = player,
                    currentDealer = currentDealer,
                    partner = true
                )
            }
        }
    }
}

/**
 * Single commander damage number composable
 * @param modifier Modifier to apply to the layout
 * @param player The player to display
 * @param currentDealer The current dealer
 * @param partner Whether or not the player is in partner mode
 */
@Composable
fun SingleCommanderDamageNumber(
    modifier: Modifier = Modifier, player: Player, currentDealer: Player?, partner: Boolean = false
) {
    val iconResource = Res.drawable.commander_solid_icon

    NumericValue(modifier = modifier,
        player = player,
        iconResource = iconResource,
        getValue = { p ->
            p.getCommanderDamage(
                currentDealer!!,
                partner
            ).toString()
        },
        getRecentChangeText = { "" })
}

/**
 * Life number composable
 * @param modifier Modifier to apply to the layout
 * @param player The player to display
 */
@Composable
fun LifeNumber(
    modifier: Modifier = Modifier, player: Player
) {
    val iconResource = Res.drawable.heart_solid_icon

    NumericValue(modifier = modifier,
        player = player,
        iconResource = iconResource,
        getValue = { p -> p.life.toString() },
        getRecentChangeText = { if (player.recentChange == 0) "" else if (player.recentChange > 0) "+${player.recentChange}" else "${player.recentChange}" })
}

/**
 * Generic numeric value composable
 * @param modifier Modifier to apply to the layout
 * @param player The player to display
 * @param iconID The icon to display
 * @param getValue The callback for getting the value
 * @param getRecentChangeText The callback for getting the recent change text
 */
@Composable
fun NumericValue(
    modifier: Modifier = Modifier, player: Player, iconResource: DrawableResource, getValue: (Player) -> String, getRecentChangeText: (Player) -> String
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var largeTextSize = (maxHeight.value / 2.8f + maxWidth.value / 6f + 30)
        val largeTextPadding = (largeTextSize / 6f).dp
        val largeText = getValue(player)

        if (largeText.length >= 3) {
            for (i in 0 until largeText.length - 2) {
                largeTextSize /= 1.15f
            }
        }
        val smallTextSize = maxHeight.value / 14f + 4

        val recentChangeSize = (maxHeight / 7f).value
        val recentChangeText = getRecentChangeText(player)

        val iconSize = maxHeight / 7f

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.padding(top = 5.dp).offset(y = (largeTextSize / 24f).dp),
                text = player.name,
                color = player.textColor,
                fontSize = smallTextSize.scaledSp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = textShadowStyle()
            )
            Row(
                modifier = Modifier.wrapContentSize(unbounded = true).offset(y = (-largeTextSize / 12f).dp).padding(top = largeTextPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier.wrapContentHeight(unbounded = true),
                    text = largeText,
                    color = player.textColor,
                    fontSize = largeTextSize.scaledSp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = textShadowStyle()
                )
                Spacer(modifier = Modifier.weight(0.2f))
                Text(
                    modifier = Modifier.weight(0.8f).padding(start = 20.dp).wrapContentSize(unbounded = true),
                    text = recentChangeText,
                    color = player.textColor,
                    fontSize = recentChangeSize.scaledSp,
                    style = textShadowStyle()
                )
            }
            SettingsButton(
                modifier = Modifier.size(iconSize),
                backgroundColor = Color.Transparent,
                mainColor = player.textColor,
                imageVector = vectorResource(iconResource),
                enabled = false
            )
        }
    }
}

/**
 * Settings menu possible states
 */
private enum class SettingsState { Default, Customize, BackgroundColorPicker, TextColorPicker, ChangeName, LoadPlayer, Counters }

/**
 * Settings menu composable
 * @param modifier Modifier to apply to the layout
 * @param player The player to display
 * @param backStack The back stack for the settings menu
 * @param onMonarchyButtonClick The callback for when the monarchy button is pressed
 * @param onFromCameraRollButtonClick The callback for when the camera roll button is pressed
 * @param onScryfallButtonClick The callback for when the scryfall button is pressed
 * @param closeSettingsMenu The callback for when the settings menu is closed
 * @param onResetPrefsClick The callback for when the reset button is pressed
 * @param setBlurBackground The callback for when the blur background setting is changed
 */
@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    player: Player,
    backStack: SnapshotStateList<() -> Unit>,
    onMonarchyButtonClick: () -> Unit,
    onFromCameraRollButtonClick: () -> Unit,
    onScryfallButtonClick: () -> Unit,
    closeSettingsMenu: () -> Unit,
    onResetPrefsClick: () -> Unit,
    setBlurBackground: (Boolean) -> Unit
) {
    var state by remember { mutableStateOf(SettingsState.Default) }

    BoxWithConstraints(modifier.fillMaxSize()) {
        var settingsButtonSize = if (maxHeight / 2 * 3 < maxWidth) {
            maxHeight / 2
        } else {
            maxWidth / 3
        }
        settingsButtonSize = min(
            115F,
            settingsButtonSize.value
        ).dp
        val smallPadding = settingsButtonSize / 10f
        val smallTextSize = maxHeight.value.scaledSp / 12f

        @Composable
        fun FormattedSettingsButton(imageResource: DrawableResource, text: String, onPress: () -> Unit) {
            SettingsButton(
                Modifier.size(settingsButtonSize),
                imageVector = vectorResource(imageResource),
                text = text,
                onPress = onPress,
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
                            imageResource = Res.drawable.monarchy_icon,
                            text = "Monarchy"
                        ) { onMonarchyButtonClick() }
                    }
                    item {
                        FormattedSettingsButton(
                            imageResource = Res.drawable.transparent,
                            text = ""
                        ) {
//                            state = SettingsState.LoadPlayer
//                            backStack.add { state = SettingsState.Default }
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = Res.drawable.mana_icon,
                            text = "Counters"
                        ) {
                            state = SettingsState.Counters
                            backStack.add { state = SettingsState.Default }
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource =Res.drawable.change_background_icon,
                            text = "Customize"
                        ) {
                            state = SettingsState.Customize
                            backStack.add { state = SettingsState.Default }
                        }
                    }
                    item {
                        FormattedSettingsButton(
                            imageResource =Res.drawable.skull_icon,
                            text = "KO Player"
                        ) {
                            player.setDead = !player.isDead
                            closeSettingsMenu()
                            backStack.clear()
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource =Res.drawable.change_name_icon,
                            text = "Change Name"
                        ) {
                            state = SettingsState.ChangeName
                            backStack.add { state = SettingsState.Default }
                        }
                    }
                }
            }

            SettingsState.Counters -> {
                Counters(
                    modifier = modifier.padding(5.dp),
                    player = player,
                    backStack = backStack
                )
            }

            SettingsState.Customize -> {
                LazyHorizontalGrid(
                    modifier = Modifier.fillMaxSize(),
                    rows = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        FormattedSettingsButton(
                            imageResource =Res.drawable.color_picker_icon,
                            text = "Background Color"
                        ) {
                            state = SettingsState.BackgroundColorPicker
                            backStack.add { state = SettingsState.Customize }
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource =Res.drawable.text_icon,
                            text = "Text Color"
                        ) {
                            state = SettingsState.TextColorPicker
                            backStack.add { state = SettingsState.Customize }
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource =Res.drawable.change_background_icon,
                            text = "Upload Image"
                        ) { onFromCameraRollButtonClick() }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource =Res.drawable.download_icon,
                            text = "Load Profile"
                        ) {
                            state = SettingsState.LoadPlayer
                            backStack.add { state = SettingsState.Customize }
//                            player.imageUri = null
//                            SettingsManager.savePlayerPref(player)
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource =Res.drawable.search_icon,
                            text = "Search Image",
                            onPress = onScryfallButtonClick
                        )
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource =Res.drawable.reset_icon,
                            text = "Reset",
                            onPress = onResetPrefsClick
                        )
                    }
                }
            }

            SettingsState.BackgroundColorPicker -> {
                ColorPicker(Modifier.wrapContentSize().align(Alignment.Center).padding(bottom = maxHeight / 5f),
                    text = "Choose a Background Color",
                    colorList = mutableListOf<Color>().apply {
                        add(Color.Black)
                        add(Color.White)
                        addAll(allPlayerColors)
                    },
                    player = player,
                    initialColor = player.color,
                    setBlurBackground = { setBlurBackground(it) }) { color ->
                    player.imageUri = null
                    player.color = color
                    SettingsManager.savePlayerPref(player)
                }
            }

            SettingsState.TextColorPicker -> {
                ColorPicker(Modifier.wrapContentSize().align(Alignment.Center).padding(bottom = maxHeight / 5f),
                    text = "Choose a Text Color",
                    colorList = mutableListOf<Color>().apply {
                        add(Color.Black)
                        add(Color.White)
                        addAll(allPlayerColors)
                    },
                    player = player,
                    initialColor = player.textColor,
                    setBlurBackground = { setBlurBackground(it) }) { color ->
                    player.textColor = color
                    SettingsManager.savePlayerPref(player)
                }
            }


            SettingsState.LoadPlayer -> {
                val playerList = remember {
                    mutableStateListOf<Player>().apply {
                        addAll(SettingsManager.loadPlayerPrefs())
                    }
                }

                Column(
                    modifier.fillMaxSize().padding(bottom = smallPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(unbounded = true).padding(top = smallPadding),
                        text = "Saved profiles",
                        color = player.textColor,
                        fontSize = smallTextSize,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        modifier = Modifier.wrapContentSize(unbounded = true).padding(bottom = smallPadding),
                        text = "(hold to delete)",
                        color = player.textColor,
                        fontSize = smallTextSize / 2,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        Modifier.fillMaxSize().weight(0.5f)
                    ) {
                        LazyHorizontalGrid(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.15f)).border(
                            0.5.dp,
                            player.textColor.copy(alpha = 0.9f),
                            RoundedCornerShape(20.dp)
                        ).padding(smallPadding),
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
                        if (playerList.isEmpty()) {
                            Column(
                                Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    modifier = Modifier.wrapContentSize().padding(horizontal = 20.dp).padding(bottom = 5.dp),
                                    text = "No saved profiles found",
                                    color = player.textColor,
                                    fontSize = smallTextSize * 0.7f,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    modifier = Modifier.wrapContentSize().padding(horizontal = 20.dp),
                                    text = "Changes to name/customization will be saved automatically",
                                    color = player.textColor,
                                    lineHeight = smallTextSize,
                                    fontSize = smallTextSize * 0.7f,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            SettingsState.ChangeName -> {
                ChangeNameField(
                    modifier = modifier,
                    closeSettingsMenu = closeSettingsMenu,
                    player = player
                )
            }
        }
    }
}

/**
 * Color picker composable
 * @param modifier Modifier to apply to the layout
 * @param text The text to display
 * @param colorList The list of colors to display
 * @param player The player to modify
 * @param initialColor The initial color
 * @param setBlurBackground The callback for when the blur background setting is changed
 * @param onPress The callback for when a color is pressed
 */
@Composable
fun ColorPicker(modifier: Modifier = Modifier, text: String, colorList: List<Color>, player: Player, initialColor: Color, setBlurBackground: (Boolean) -> Unit, onPress: (Color) -> Unit) {
    BoxWithConstraints(modifier) {
        val colorPickerPadding = maxWidth / 200f
        val containerPadding = maxWidth / 50f
        val textPadding = maxHeight / 25f
        val smallTextSize = maxHeight.value.scaledSp / 8f
        Column(
            Modifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.wrapContentSize().padding(
                    bottom = textPadding,
                    top = textPadding * 2
                ),
                text = text,
                color = player.textColor,
                fontSize = smallTextSize / 1.25f,
                textAlign = TextAlign.Center
            )
            LazyHorizontalGrid(modifier = Modifier.wrapContentSize().weight(0.5f).clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.15f)).border(
                0.5.dp,
                player.textColor.copy(alpha = 0.9f),
                RoundedCornerShape(20.dp)
            ).padding(containerPadding * 2),
                rows = GridCells.Fixed(2),
                state = rememberLazyGridState(),
                horizontalArrangement = Arrangement.spacedBy(colorPickerPadding),
                verticalArrangement = Arrangement.spacedBy(colorPickerPadding),
                content = {
                    item {
                        CustomColorPickerButton(modifier = Modifier.padding(colorPickerPadding),
                            player = player,
                            initialColor = initialColor,
                            setBlurBackground = { setBlurBackground(it) },
                            setColor = { color ->
                                onPress(color)
                            })
                    }
                    items(colorList) { color ->
                        ColorPickerButton(
                            modifier = Modifier.padding(colorPickerPadding),
                            onClick = {
                                onPress(color)
                            },
                            color = color
                        )
                    }
                })
        }
    }
}

/**
 * Change name field composable
 * @param modifier Modifier to apply to the layout
 * @param closeSettingsMenu The callback for when the settings menu is closed
 * @param player The player to modify
 */
@Composable
fun ChangeNameField(
    modifier: Modifier = Modifier, closeSettingsMenu: () -> Unit, player: Player
) {
    var newName by remember { mutableStateOf(player.name) }
    val textColor = player.textColor.invert().copy(alpha = 1.0f)
    fun onDone() {
        player.name = newName
        closeSettingsMenu()
        SettingsManager.savePlayerPref(player)
    }
    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.wrapContentSize()
            ) {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = {
                        Text(
                            "New Name",
                            color = player.color,
                            fontSize = 12.scaledSp
                        )
                    },
                    textStyle = TextStyle(fontSize = 15.scaledSp),
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
                        capitalization = KeyboardCapitalization.None,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onDone() }),
                    modifier = Modifier.fillMaxWidth(0.8f).height(80.dp).padding(top = 20.dp).padding(horizontal = 5.dp)
                )
                SettingsButton(Modifier.size(50.dp).align(Alignment.CenterEnd).padding(
                    top = 20.dp,
                    end = 5.dp
                ),
                    imageVector = vectorResource(Res.drawable.enter_icon),
                    shadowEnabled = false,
                    mainColor = player.color,
                    backgroundColor = player.textColor,
                    onPress = { onDone() })
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    player.name = newName
                    closeSettingsMenu()
                    SettingsManager.savePlayerPref(player)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = player.textColor,
                    contentColor = player.color
                ),
                modifier = Modifier.wrapContentHeight().fillMaxWidth(0.7f).padding(horizontal = 10.dp).padding(bottom = 20.dp)

            ) {
                Text("Save Name")
            }
        }

    }
}

/**
 * Mini player button composable
 * @param currPlayer The current player
 * @param player The player to display
 * @param playerList The list of players
 */
@Composable
fun MiniPlayerButton(
    currPlayer: Player, player: Player, playerList: MutableList<Player>
) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(modifier = Modifier.fillMaxHeight().aspectRatio(2.5f).clip(RoundedCornerShape(15.dp)).pointerInput(Unit) {
        detectTapGestures(onTap = {
            currPlayer.copySettings(player)
        },
            onLongPress = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                SettingsManager.deletePlayerPref(player)
                playerList.remove(player)
            })
    }) {
        if (player.imageUri == null) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = player.color
            ) {}
        } else {
            AsyncImage(
                model = player.imageUri!!,
                contentDescription = "Player uploaded image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Text(
            text = player.name,
            color = player.textColor,
            fontSize = maxHeight.value.scaledSp / 2f,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Color picker button composable
 * @param modifier Modifier to apply to the layout
 * @param onClick The callback for when the button is pressed
 * @param color The color to display
 */
@Composable
fun ColorPickerButton(modifier: Modifier = Modifier, onClick: () -> Unit, color: Color) {
    Box(modifier = modifier.fillMaxHeight().aspectRatio(1f).background(color).pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                onClick()
            },
        )
    }) {}
}

/**
 * Custom color picker button composable
 * @param modifier Modifier to apply to the layout
 *  @param player The player to modify
 *  @param initialColor The initial color
 *  @param setBlurBackground The callback for when the blur background setting is changed
 *  @param setColor The callback for when a color is selected
 */
@Composable
fun CustomColorPickerButton(modifier: Modifier = Modifier, player: Player, initialColor: Color, setBlurBackground: (Boolean) -> Unit, setColor: (Color) -> Unit) {
    var showColorDialog by remember { mutableStateOf(false) }

    if (showColorDialog) {
        ColorDialog(modifier = Modifier.fillMaxSize(),
            onDismiss = {
                showColorDialog = false
                setBlurBackground(false)
            },
            initialColor = initialColor,
            setColor = { color ->
                setColor(color)
            })
    }

    Box(
        modifier = modifier.fillMaxHeight().aspectRatio(1f).pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    showColorDialog = true
                    setBlurBackground(true)
                },
            )
        },
    ) {
        Icon(
            imageVector = vectorResource(Res.drawable.custom_color_icon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = player.textColor
        )
    }
}

/**
 * Life change buttons composable
 * @param modifier Modifier to apply to the layout
 * @param onIncrementLife The callback for when the increment button is pressed
 * @param onDecrementLife The callback for when the decrement button is pressed
 */
@Composable
fun LifeChangeButtons(
    modifier: Modifier = Modifier, onIncrementLife: () -> Unit, onDecrementLife: () -> Unit
) {
    Column(modifier = modifier) {
        CustomIncrementButton(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
            onIncrementLife = onIncrementLife,
        )

        CustomIncrementButton(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(1.0f),
            onIncrementLife = onDecrementLife,
        )
    }
}

/**
 * Custom increment button composable
 * @param modifier Modifier to apply to the layout
 * @param onIncrementLife The callback for when the button is pressed
 */
@Composable
private fun CustomIncrementButton(
    modifier: Modifier = Modifier, onIncrementLife: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier.indication(
            interactionSource = interactionSource,
            indication = rememberRipple(color = Color.Black)
        ).repeatingClickable(
            interactionSource = interactionSource,
            enabled = true,
            onPress = onIncrementLife
        )
    )
}

