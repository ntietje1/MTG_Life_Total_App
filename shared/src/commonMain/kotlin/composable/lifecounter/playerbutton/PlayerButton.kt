package composable.lifecounter.playerbutton


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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import composable.lifecounter.CounterType
import composable.modifier.VerticalRotation
import composable.modifier.animatedBorderCard
import composable.modifier.bounceClick
import composable.modifier.repeatingClickable
import composable.modifier.rotateVertically
import data.Player
import getAnimationCorrectionFactor
import kotlinx.coroutines.launch
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.add_icon
import lifelinked.shared.generated.resources.back_icon
import lifelinked.shared.generated.resources.camera_icon
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
import theme.normalColorMatrix
import theme.receiverColorMatrix
import theme.saturateColor
import theme.scaledSp
import theme.settingsColorMatrix
import theme.textShadowStyle
import kotlin.math.min

@Composable
fun PlayerButton(
    modifier: Modifier = Modifier,
    viewModel: PlayerButtonViewModel,
    rotation: Float = 0f,
    setBlurBackground: (Boolean) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val currentDealerIsPartnered by viewModel.currentDealerIsPartnered.collectAsState()

    val haptic = LocalHapticFeedback.current
    val backStack = remember { mutableStateListOf<() -> Unit>() }

    val scope = rememberCoroutineScope()

    val commanderButtonVisible by remember { derivedStateOf {
        state.buttonState in listOf(PBState.NORMAL, PBState.COMMANDER_DEALER)
    }}
    val settingsButtonVisible by remember { derivedStateOf {
        state.buttonState in listOf(PBState.NORMAL, PBState.SETTINGS)
    }}

    val scryfallBackStack = remember {
        mutableStateListOf({
            viewModel.showScryfallSearch(false)
        })
    }

    LaunchedEffect(state.showResetPrefsDialog, state.showCameraWarning, state.showFilePicker, state.showScryfallSearch, state.showBackgroundColorPicker, state.showTextColorPicker) {
        setBlurBackground(state.showResetPrefsDialog || state.showCameraWarning || state.showFilePicker || state.showScryfallSearch || state.showBackgroundColorPicker || state.showTextColorPicker)
    }

    val fileType = listOf("jpg", "png")
    FilePicker(show = state.showFilePicker, fileExtensions = fileType) { file ->
        viewModel.showFilePicker(false)
        if (file != null) {
            scope.launch { //TODO: move to viewmodel
                val copiedUri = viewModel.imageManager.copyImageToLocalStorage(file.path, state.player.name)
                viewModel.setImageUri(copiedUri)
                viewModel.savePlayerPref()
            }
        }
    }

    if (state.showResetPrefsDialog) {
        WarningDialog(
            title = "Reset Preferences",
            message = "Are you sure you want to reset your customizations?",
            optionOneEnabled = true,
            optionTwoEnabled = true,
            optionOneMessage = "Reset",
            optionTwoMessage = "Cancel",
            onOptionOne = {
                viewModel.resetPlayerPref()
                viewModel.showResetPrefsDialog(false)
            },
            onDismiss = {
                viewModel.showResetPrefsDialog(false)
            }
        )
    }

    if (state.showCameraWarning) {
        if (viewModel.settingsManager.cameraRollDisabled) {
            WarningDialog(
                title = "Info",
                message = "Camera roll access is disabled. Enable in settings.",
                optionOneEnabled = false,
                optionTwoEnabled = true,
                onDismiss = {
                    viewModel.showCameraWarning(false)
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
                    viewModel.showFilePicker(true)
                    viewModel.showCameraWarning(false)
                },
                onDismiss = {
                    viewModel.showCameraWarning(false)
                }
            )
        }
    }

    if (state.showBackgroundColorPicker) {
        ColorDialog(modifier = Modifier.fillMaxSize(),
            onDismiss = {
                viewModel.showBackgroundColorPicker(false)
            },
            initialColor = state.player.color,
            setColor = { color ->
                viewModel.onChangeBackgroundColor(color)
            })
    }

    if (state.showTextColorPicker) {
        ColorDialog(modifier = Modifier.fillMaxSize(),
            onDismiss = {
                viewModel.showTextColorPicker(false)
            },
            initialColor = state.player.textColor,
            setColor = { color ->
                viewModel.onChangeTextColor(color)
            })
    }

    LaunchedEffect(state.buttonState) {
        if (state.buttonState == PBState.COMMANDER_RECEIVER) backStack.clear()
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
            if (state.buttonState == PBState.NORMAL || state.buttonState == PBState.COMMANDER_RECEIVER) {
                Modifier.bounceClick()
            } else {
                Modifier
            }
        )
    ) {
        if (state.showScryfallSearch) {
            ScryfallSearchDialog(
                onDismiss = {
                    viewModel.showScryfallSearch(false)
                },
                addToBackStack = {
                    scryfallBackStack.add(it)
                },
                onImageSelected = {
                    viewModel.setImageUri(it)
                    viewModel.savePlayerPref()
                }
            )
        }

        MonarchyIndicator(
            modifier = modifier,
            monarch = state.player.monarch
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().background(Color.Transparent).clip(RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                PlayerButtonBackground(
                    state = state.buttonState,
                    imageUri = state.player.imageUri,
                    color = state.player.color,
                    isDead = viewModel.isDead(),
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
                if (!state.player.setDead) {
                    when (state.buttonState) {
                        PBState.NORMAL -> {
                            LifeChangeButtons(Modifier.fillMaxWidth(),
                                onIncrementLife = {
                                    viewModel.incrementLife(1)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDecrementLife = {
                                    viewModel.incrementLife(-1)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                })
                        }

                        PBState.COMMANDER_RECEIVER -> {
                            Row(Modifier.fillMaxSize()) {
                                LifeChangeButtons(Modifier.then(if (currentDealerIsPartnered) Modifier.fillMaxWidth(0.5f) else Modifier.fillMaxWidth()),
                                    onIncrementLife = {
                                        viewModel.incrementCommanderDamage(
                                            value = 1
                                        )
                                        viewModel.incrementLife(-1)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDecrementLife = {
                                        viewModel.incrementCommanderDamage(
                                            value = -1
                                        )
                                        viewModel.incrementLife(1)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    })
                                if (currentDealerIsPartnered) {
                                    LifeChangeButtons(Modifier.fillMaxWidth(),
                                        onIncrementLife = {
                                            viewModel.incrementCommanderDamage(
                                                value = 1,
                                                partner = true
                                            )
                                            viewModel.incrementLife(-1)
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDecrementLife = {
                                            viewModel.incrementCommanderDamage(
                                                viewModel,
                                                value = -1,
                                                partner = true
                                            )
                                            viewModel.incrementLife(1)
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
                        mainColor = state.player.textColor,
                        imageVector = vectorResource(Res.drawable.skull_icon),
                        enabled = false
                    )
                }

                @Composable
                fun PlayerButtonContent(modifier: Modifier = Modifier) {
                    Box(modifier.fillMaxSize()) {
                        when (state.buttonState) {
                            PBState.NORMAL -> {
                                if (viewModel.isDead()) {
                                    Skull()
                                } else {
                                    LifeNumber(
                                        modifier = playerInfoPadding.fillMaxSize(),
                                        name = state.player.name,
                                        textColor = state.player.textColor,
                                        largeText = state.player.life.toString(),
                                        recentChangeText = if (state.player.recentChange == 0) "" else if (state.player.recentChange > 0) "+${state.player.recentChange}" else "${state.player.recentChange}",
                                    )
                                }
                            }

                            PBState.COMMANDER_RECEIVER -> {
                                if (viewModel.isDead()) {
                                    Skull()
                                } else {
                                    CommanderDamageNumber(
                                        modifier = playerInfoPadding.fillMaxSize(),
                                        name = state.player.name,
                                        textColor = state.player.textColor,
                                        firstValue = viewModel.getCommanderDamage(
                                            partner = false
                                        ).toString(),
                                        secondValue = if (currentDealerIsPartnered) viewModel.getCommanderDamage(
                                            partner = true
                                        ).toString() else null,
                                    )
                                }
                            }

                            PBState.COMMANDER_DEALER -> {
                                val iconResource = if (currentDealerIsPartnered) Res.drawable.sword_icon_double else Res.drawable.sword_icon
                                Column(
                                    Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Spacer(modifier = Modifier.height(40.dp))
                                    Text(
                                        modifier = Modifier,
                                        text = "Deal damage with your commander",
                                        color = state.player.textColor,
                                        fontSize = 20.scaledSp,
                                        lineHeight = 20.scaledSp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    SettingsButton(modifier = Modifier.size(smallButtonSize),
                                        imageVector = vectorResource(iconResource),
                                        backgroundColor = Color.Transparent,
                                        mainColor = state.player.textColor,
                                        onPress = {
                                            viewModel.togglePartnerMode()
                                        })
                                    Text(
                                        modifier = Modifier.wrapContentSize(unbounded = true),
                                        text = "Toggle Partner Mode",
                                        color = state.player.textColor,
                                        fontSize = 10.scaledSp,
                                        textAlign = TextAlign.Center
                                    )
                                }

                            }

                            PBState.SETTINGS -> {
                                SettingsMenu(
                                    modifier = settingsPadding,
                                    player = state.player,
                                    addToBackStack = backStack::add,
                                    clearBackStack = backStack::clear,
                                    onMonarchyButtonClick = { viewModel.onMonarchyButtonClicked(null) },
                                    onFromCameraRollButtonClick = { viewModel.showCameraWarning(true) },
                                    closeSettingsMenu = { viewModel.setPlayerButtonState(PBState.NORMAL) },
                                    onScryfallButtonClick = { viewModel.showScryfallSearch(!state.showScryfallSearch) },
                                    onResetPrefsClick = { viewModel.showResetPrefsDialog(true) },
                                    savePlayerPref = viewModel::savePlayerPref,
                                    loadPlayerPrefs = viewModel.settingsManager::loadPlayerPrefs,
                                    deletePlayerPref = viewModel.settingsManager::deletePlayerPref,
                                    getCounterValue = viewModel::getCounterValue,
                                    incrementCounterValue = viewModel::incrementCounterValue,
                                    setActiveCounter = viewModel::setActiveCounter,
                                    toggleSetDead = viewModel::toggleSetDead,
                                    copyPlayerPrefs = viewModel::copySettings,
                                    onChangeName = viewModel::setName,
                                    onChangeTextColor = viewModel::onChangeTextColor,
                                    onChangeBackgroundColor = viewModel::onChangeBackgroundColor,
                                    showBackgroundColorPicker = viewModel::showBackgroundColorPicker,
                                    showTextColorPicker = viewModel::showTextColorPicker,
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
                        mainColor = state.player.textColor,
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
                        color = state.player.textColor,
                        size = smallButtonSize
                    ) {
                        viewModel.onCommanderButtonClicked()
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
                            color = state.player.textColor,
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
                        color = state.player.textColor,
                        size = smallButtonSize
                    ) {
                        viewModel.onSettingsButtonClicked()
                        when (state.buttonState) { //TODO: move this to viewmodel
                            PBState.SETTINGS -> {
                                backStack.clear()
                            }
                            PBState.NORMAL -> {
                                backStack.add { viewModel.setPlayerButtonState(PBState.NORMAL) }
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

private enum class CounterMenuState {
    DEFAULT, ADD_COUNTER
}
@Composable
fun Counters(
    modifier: Modifier = Modifier,
    textColor: Color,
    activeCounters: List<CounterType>,
    getCounterValue: (CounterType) -> Int,
    incrementCounterValue: (CounterType, Int) -> Unit,
    setActiveCounter: (CounterType, Boolean) -> Unit,
    addToBackStack: (() -> Unit) -> Unit
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
                color = textColor,
                fontSize = smallTextSize,
                textAlign = TextAlign.Center
            )
            Box(
                Modifier.fillMaxSize().weight(0.5f).background(
                    Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(25.dp)
                ).border(
                    0.5.dp,
                    textColor.copy(alpha = 0.9f),
                    RoundedCornerShape(25.dp)
                ).clip(RoundedCornerShape(25.dp))
            ) {
                when (state) {
                    CounterMenuState.DEFAULT -> {
                        LazyRow(
                            Modifier.fillMaxSize().padding(5.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            items(activeCounters) { counterType ->
                                Counter(
                                    textColor = textColor,
                                    iconResource = counterType.resource,
                                    value = getCounterValue(counterType),
                                    onIncrement = {
                                        incrementCounterValue(
                                            counterType,
                                            1
                                        )
                                    },
                                    onDecrement = {
                                        incrementCounterValue(
                                            counterType,
                                            -1
                                        )
                                    })
                            }
                            item {
                                AddCounter(
                                    textColor = textColor,
                                    onTap = {
                                        state = CounterMenuState.ADD_COUNTER
                                        addToBackStack { state = CounterMenuState.DEFAULT }
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
                                items(CounterType.entries.toTypedArray()) { counterType ->
                                    Box(modifier = Modifier.fillMaxSize().aspectRatio(1.0f).padding(0.5.dp).background(
                                        if (counterType in activeCounters) {
                                            Color.Green.copy(alpha = 0.5f)
                                        } else {
                                            Color.Transparent
                                        }
                                    ).pointerInput(Unit) {
                                        detectTapGestures {
                                            if (counterType in activeCounters) {
                                                setActiveCounter(counterType, false)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            } else {
                                                setActiveCounter(counterType, true)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                    }) {
                                        SettingsButton(
                                            imageVector = vectorResource(counterType.resource),
                                            modifier = Modifier.fillMaxSize().padding(5.dp),
                                            mainColor = textColor,
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

@Composable
fun AddCounter(
    textColor: Color,
    onTap: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(Modifier.fillMaxHeight().aspectRatio(0.70f).padding(5.dp).bounceClick(0.0125f).background(
        Color.Black.copy(0.2f),
        shape = RoundedCornerShape(20.dp)
    ).border(
        0.5.dp,
        textColor.copy(alpha = 0.9f),
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
            mainColor = textColor,
            shadowEnabled = false,
            enabled = false
        )
    }
}

@Composable
fun Counter(
    textColor: Color,
    iconResource: DrawableResource,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(
        Modifier.fillMaxHeight().aspectRatio(0.70f).padding(5.dp).bounceClick(0.0125f).background(
            Color.Black.copy(0.2f),
            shape = RoundedCornerShape(20.dp)
        ).border(
            0.5.dp,
            textColor.copy(alpha = 0.9f),
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
                color = textColor,
                fontSize = textSize,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.wrapContentSize(),
                style = textShadowStyle()
            )
            SettingsButton(
                imageVector = vectorResource(iconResource),
                modifier = Modifier.fillMaxSize(0.35f).aspectRatio(1.0f).padding(bottom = 15.dp),
                mainColor = textColor,
                backgroundColor = Color.Transparent,
                shadowEnabled = true,
                enabled = false
            )
        }
    }
}

@Composable
fun PlayerButtonBackground(
    state: PBState,
    imageUri: String?,
    color: Color,
    isDead: Boolean
) {
    if (imageUri == null) {
        var c = when (state) {
            PBState.NORMAL -> color
            PBState.COMMANDER_RECEIVER -> color.saturateColor(0.2f).brightenColor(0.3f)

            PBState.COMMANDER_DEALER -> color.saturateColor(0.5f).brightenColor(0.6f)

            PBState.SETTINGS -> color
        }
        if (isDead) {
            c = c.ghostify()
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = c
        ) {}
    } else {
        val colorMatrix = when (state) {
            PBState.NORMAL -> {
                if (isDead) deadNormalColorMatrix else normalColorMatrix
            }

            PBState.COMMANDER_RECEIVER -> {
                if (isDead) deadReceiverColorMatrix else receiverColorMatrix
            }

            PBState.COMMANDER_DEALER -> {
                if (isDead) deadDealerColorMatrix else dealerColorMatrix
            }

            PBState.SETTINGS -> {
                if (isDead) deadSettingsColorMatrix else settingsColorMatrix
            }
        }
        AsyncImage(
            model = imageUri,
            contentDescription = "Player uploaded image",
            modifier = Modifier.fillMaxSize(),
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

@Composable
fun CommanderDamageNumber(
    modifier: Modifier = Modifier,
    name: String,
    textColor: Color,
    firstValue: String,
    secondValue: String?,
) {
    BoxWithConstraints(modifier = modifier) {
        val dividerOffset = maxHeight / 12f

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SingleCommanderDamageNumber(
                modifier = Modifier.padding(horizontal = 10.dp).then(if (secondValue != null) Modifier.fillMaxWidth(0.5f) else Modifier.fillMaxWidth()),
                name = name,
                textColor = textColor,
                value = firstValue,
                recentChange = 0 //TODO: add this
            )
            if (secondValue != null) {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight(0.6f).width(2.dp).offset(y = dividerOffset),
                    color = textColor
                )

                SingleCommanderDamageNumber(
                    modifier = Modifier.padding(horizontal = 10.dp).fillMaxWidth(),
                    name = name,
                    textColor = textColor,
                    value = secondValue,
                    recentChange = 0
                )
            }
        }
    }
}

@Composable
fun SingleCommanderDamageNumber(
    modifier: Modifier = Modifier,
    name: String,
    textColor: Color,
    value: String,
    recentChange: Int
) {
    val iconResource = Res.drawable.commander_solid_icon

    NumericValue(
        modifier = modifier,
        iconResource = iconResource,
        name = name,
        textColor = textColor,
        largeText = value,
        recentChangeText = if (recentChange == 0) "" else if (recentChange > 0) "+$recentChange" else "$recentChange"
    )
}

@Composable
fun LifeNumber(
    modifier: Modifier = Modifier,
    textColor: Color,
    name: String,
    largeText: String,
    recentChangeText: String,

    ) {
    val iconResource = Res.drawable.heart_solid_icon

    NumericValue(
        modifier = modifier,
        textColor = textColor,
        name = name,
        largeText = largeText,
        recentChangeText = recentChangeText,
        iconResource = iconResource
    )
}
@Composable
fun NumericValue(
    modifier: Modifier = Modifier,
    textColor: Color,
    name: String,
    largeText: String,
    recentChangeText: String,
    iconResource: DrawableResource,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var largeTextSize = (maxHeight.value / 2.8f + maxWidth.value / 6f + 30)
        val largeTextPadding = (largeTextSize / 6f).dp

        if (largeText.length >= 3) {
            for (i in 0 until largeText.length - 2) {
                largeTextSize /= 1.15f
            }
        }
        val smallTextSize = maxHeight.value / 14f + 4

        val recentChangeSize = (maxHeight / 7f).value

        val iconSize = maxHeight / 7f

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.padding(top = 5.dp).offset(y = (largeTextSize / 24f).dp),
                text = name,
                color = textColor,
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
                    color = textColor,
                    fontSize = largeTextSize.scaledSp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = textShadowStyle()
                )
                Spacer(modifier = Modifier.weight(0.2f))
                Text(
                    modifier = Modifier.weight(0.8f).padding(start = 20.dp).wrapContentSize(unbounded = true),
                    text = recentChangeText,
                    color = textColor,
                    fontSize = recentChangeSize.scaledSp,
                    style = textShadowStyle()
                )
            }
            SettingsButton(
                modifier = Modifier.size(iconSize),
                backgroundColor = Color.Transparent,
                mainColor = textColor,
                imageVector = vectorResource(iconResource),
                enabled = false
            )
        }
    }
}
private enum class SettingsState { Default, Customize, BackgroundColorPicker, TextColorPicker, ChangeName, LoadPlayer, Counters }

@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    player: Player,
    addToBackStack: (() -> Unit) -> Unit,
    clearBackStack: () -> Unit,
    onMonarchyButtonClick: () -> Unit,
    onFromCameraRollButtonClick: () -> Unit,
    onScryfallButtonClick: () -> Unit,
    closeSettingsMenu: () -> Unit,
    onResetPrefsClick: () -> Unit,
    copyPlayerPrefs: (Player) -> Unit,
    savePlayerPref: () -> Unit,
    loadPlayerPrefs: () -> List<Player>,
    deletePlayerPref: (Player) -> Unit,
    getCounterValue: (CounterType) -> Int,
    incrementCounterValue: (CounterType, Int) -> Unit,
    setActiveCounter: (CounterType, Boolean) -> Unit,
    toggleSetDead: () -> Unit,
    onChangeName: (String) -> Unit,
    onChangeBackgroundColor: (Color) -> Unit,
    onChangeTextColor: (Color) -> Unit,
    showBackgroundColorPicker: (Boolean) -> Unit,
    showTextColorPicker: (Boolean) -> Unit,
) {
    var state by remember { mutableStateOf(SettingsState.Default) }
    val textColor = player.textColor

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
                        ) { }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = Res.drawable.mana_icon,
                            text = "Counters"
                        ) {
                            state = SettingsState.Counters
                            addToBackStack { state = SettingsState.Default }
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = Res.drawable.change_background_icon,
                            text = "Customize"
                        ) {
                            state = SettingsState.Customize
                            addToBackStack { state = SettingsState.Default }
                        }
                    }
                    item {
                        FormattedSettingsButton(
                            imageResource = Res.drawable.skull_icon,
                            text = "KO Player"
                        ) {
                            toggleSetDead()
                            closeSettingsMenu()
                            clearBackStack()
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = Res.drawable.change_name_icon,
                            text = "Change Name"
                        ) {
                            state = SettingsState.ChangeName
                            addToBackStack { state = SettingsState.Default }
                        }
                    }
                }
            }

            SettingsState.Counters -> {
                Counters(
                    modifier = modifier.padding(5.dp),
                    textColor = player.textColor,
                    activeCounters = player.activeCounters,
                    getCounterValue = getCounterValue,
                    incrementCounterValue = incrementCounterValue,
                    setActiveCounter = setActiveCounter,
                    addToBackStack = addToBackStack
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
                            imageResource = Res.drawable.color_picker_icon,
                            text = "Background Color"
                        ) {
                            state = SettingsState.BackgroundColorPicker
                            addToBackStack { state = SettingsState.Customize }
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = Res.drawable.text_icon,
                            text = "Text Color"
                        ) {
                            state = SettingsState.TextColorPicker
                            addToBackStack { state = SettingsState.Customize }
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = Res.drawable.camera_icon, //TODO: change to camera icon
                            text = "Upload Image"
                        ) { onFromCameraRollButtonClick() }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = Res.drawable.download_icon,
                            text = "Load Profile"
                        ) {
                            state = SettingsState.LoadPlayer
                            addToBackStack { state = SettingsState.Customize }
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = Res.drawable.search_icon,
                            text = "Search Image",
                            onPress = onScryfallButtonClick
                        )
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = Res.drawable.reset_icon,
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
                    textColor = player.textColor,
                    showColorPicker = showBackgroundColorPicker,
                    onPress = onChangeBackgroundColor
                )
            }

            SettingsState.TextColorPicker -> {
                ColorPicker(Modifier.wrapContentSize().align(Alignment.Center).padding(bottom = maxHeight / 5f),
                    text = "Choose a Text Color",
                    colorList = mutableListOf<Color>().apply {
                        add(Color.Black)
                        add(Color.White)
                        addAll(allPlayerColors)
                    },
                    textColor = player.textColor,
                    showColorPicker = showTextColorPicker,
                    onPress = onChangeTextColor
                )
            }


            SettingsState.LoadPlayer -> {
                val playerList = remember {
                    mutableStateListOf<Player>().apply {
                        addAll(loadPlayerPrefs())
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
                            textColor.copy(alpha = 0.9f),
                            RoundedCornerShape(20.dp)
                        ).padding(smallPadding),
                            rows = GridCells.Fixed(2),
                            state = rememberLazyGridState(),
                            horizontalArrangement = Arrangement.spacedBy(smallPadding),
                            verticalArrangement = Arrangement.spacedBy(smallPadding),
                            content = {
                                items(playerList) { pInfo ->
                                    MiniPlayerButton(
                                        imageUri = pInfo.imageUri,
                                        name = pInfo.name,
                                        backgroundColor = pInfo.color,
                                        textColor = pInfo.textColor,
                                        copyPrefsToCurrentPlayer = {
                                            copyPlayerPrefs(pInfo)
                                            savePlayerPref()
                                            closeSettingsMenu()
                                        },
                                        removePlayerProfile = {
                                            deletePlayerPref(pInfo)
                                            playerList.remove(pInfo)
                                        },
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
                    name = player.name,
                    onChangeName = onChangeName,
                    backgroundColor = player.color,
                    playerTextColor = player.textColor,
                    closeSettingsMenu = closeSettingsMenu,
                    savePlayerPref = savePlayerPref
                )
            }
        }
    }
}

@Composable
fun ColorPicker(
    modifier: Modifier = Modifier,
    text: String,
    colorList: List<Color>,
    textColor: Color,
    showColorPicker: (Boolean) -> Unit,
    onPress: (Color) -> Unit
) {
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
                color = textColor,
                fontSize = smallTextSize / 1.25f,
                textAlign = TextAlign.Center
            )
            LazyHorizontalGrid(modifier = Modifier.wrapContentSize().weight(0.5f).clip(RoundedCornerShape(20.dp)).background(Color.Black.copy(alpha = 0.15f)).border(
                0.5.dp,
                textColor.copy(alpha = 0.9f),
                RoundedCornerShape(20.dp)
            ).padding(containerPadding * 2),
                rows = GridCells.Fixed(2),
                state = rememberLazyGridState(),
                horizontalArrangement = Arrangement.spacedBy(colorPickerPadding),
                verticalArrangement = Arrangement.spacedBy(colorPickerPadding),
                content = {
                    item {
                        CustomColorPickerButton(modifier = Modifier.padding(colorPickerPadding),
                            textColor = textColor,
                            showColorPicker = showColorPicker,
                        )
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

@Composable
fun ChangeNameField(
    modifier: Modifier = Modifier,
    closeSettingsMenu: () -> Unit,
    name: String,
    onChangeName: (String) -> Unit,
    backgroundColor: Color,
    playerTextColor: Color,
    savePlayerPref: () -> Unit
) {
    fun onDone() {
        closeSettingsMenu()
        savePlayerPref()
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
                    value = name,
                    onValueChange = onChangeName,
                    label = {
                        Text(
                            "New Name",
                            color = backgroundColor,
                            fontSize = 12.scaledSp
                        )
                    },
                    textStyle = TextStyle(fontSize = 15.scaledSp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = backgroundColor,
                        unfocusedTextColor = backgroundColor,
                        focusedContainerColor = playerTextColor,
                        unfocusedContainerColor = playerTextColor,
                        cursorColor = backgroundColor,
                        unfocusedIndicatorColor = backgroundColor,
                        focusedIndicatorColor = backgroundColor
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
                    mainColor = backgroundColor,
                    backgroundColor = playerTextColor,
                    onPress = { onDone() })
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    closeSettingsMenu()
                    savePlayerPref()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = playerTextColor,
                    contentColor = backgroundColor
                ),
                modifier = Modifier.wrapContentHeight().fillMaxWidth(0.7f).padding(horizontal = 10.dp).padding(bottom = 20.dp)

            ) {
                Text("Save Name")
            }
        }

    }
}

@Composable
fun MiniPlayerButton(
    imageUri: String?,
    backgroundColor: Color,
    name: String,
    textColor: Color,
    copyPrefsToCurrentPlayer: () -> Unit,
    removePlayerProfile: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(modifier = Modifier.fillMaxHeight().aspectRatio(2.5f).clip(RoundedCornerShape(15.dp)).pointerInput(Unit) {
        detectTapGestures(onTap = {
            copyPrefsToCurrentPlayer()
        },
            onLongPress = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                removePlayerProfile()
            })
    }) {
        if (imageUri == null) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = backgroundColor
            ) {}
        } else {
            AsyncImage(
                model = imageUri,
                contentDescription = "Player uploaded image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        Text(
            text = name,
            color = textColor,
            fontSize = maxHeight.value.scaledSp / 2f,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

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

@Composable
fun CustomColorPickerButton(
    modifier: Modifier = Modifier,
    textColor: Color,
    showColorPicker: (Boolean) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxHeight().aspectRatio(1f).pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    showColorPicker(true)
                },
            )
        },
    ) {
        Icon(
            imageVector = vectorResource(Res.drawable.custom_color_icon),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = textColor
        )
    }
}

@Composable
fun LifeChangeButtons(
    modifier: Modifier = Modifier,
    onIncrementLife: () -> Unit,
    onDecrementLife: () -> Unit
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

