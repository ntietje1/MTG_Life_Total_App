package composable.lifecounter.playerbutton


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import composable.SettingsButton
import composable.dialog.ColorDialog
import composable.dialog.ScryfallSearchDialog
import composable.dialog.WarningDialog
import composable.lifecounter.CounterType
import composable.modifier.VerticalRotation
import composable.modifier.animatedBorderCard
import composable.modifier.bounceClick
import composable.modifier.repeatingClickable
import composable.modifier.rotateVertically
import data.Player
import data.Player.Companion.allPlayerColors
import getAnimationCorrectionFactor
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.add_icon
import lifelinked.shared.generated.resources.back_icon
import lifelinked.shared.generated.resources.camera_icon
import lifelinked.shared.generated.resources.change_background_icon
import lifelinked.shared.generated.resources.change_name_icon
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
import lifelinked.shared.generated.resources.star_icon
import lifelinked.shared.generated.resources.sword_icon
import lifelinked.shared.generated.resources.sword_icon_double
import lifelinked.shared.generated.resources.text_icon
import lifelinked.shared.generated.resources.transparent
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import theme.brightenColor
import theme.deadDealerColorMatrix
import theme.deadNormalColorMatrix
import theme.deadReceiverColorMatrix
import theme.deadSettingsColorMatrix
import theme.dealerColorMatrix
import theme.defaultTextStyle
import theme.ghostify
import theme.normalColorMatrix
import theme.receiverColorMatrix
import theme.saturateColor
import theme.scaledSp
import theme.settingsColorMatrix
import theme.textShadowStyle

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

    val scope = rememberCoroutineScope()

    val commanderButtonVisible by remember {
        derivedStateOf {
            state.buttonState in listOf(PBState.NORMAL, PBState.COMMANDER_DEALER)
        }
    }
    val settingsButtonVisible by remember {
        derivedStateOf {
            state.buttonState !in listOf(PBState.COMMANDER_DEALER, PBState.COMMANDER_RECEIVER)
        }
    }

    @Composable
    fun generateSizes(maxWidth: Dp, maxHeight: Dp): Triple<Dp, Dp, TextUnit> {
        val settingsButtonSize = if (maxHeight / 2 * 3 < maxWidth) {
            maxHeight / 2
        } else {
            maxWidth / 3
        }
        val smallPadding = settingsButtonSize / 10f
        val smallTextSize = maxHeight.value.scaledSp / 12f
        return Triple(settingsButtonSize, smallPadding, smallTextSize)
    }

    LaunchedEffect(
        state.showResetPrefsDialog,
        state.showCameraWarning,
        state.showScryfallSearch,
        state.showBackgroundColorPicker,
        state.showTextColorPicker,
        state.showChangeNameField
    ) {
        setBlurBackground(state.showResetPrefsDialog || state.showCameraWarning || state.showScryfallSearch || state.showBackgroundColorPicker || state.showTextColorPicker || state.showChangeNameField)
    }

    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                viewModel.onFileSelected(it)
            }
        }
    )

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
                    viewModel.showCameraWarning(false)
                    singleImagePicker.launch()
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

    if (state.showChangeNameField) {
        Dialog(
            onDismissRequest = { viewModel.showChangeNameField(false) },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false,
            )
        ) {
            Column(
                Modifier.fillMaxSize().clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    viewModel.showChangeNameField(false)
                },
                verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.weight(0.7f))
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .aspectRatio(2f)
                        .clip(RoundedCornerShape(12)),
                ) {
                    val buttonSize = maxWidth / 8f
                    val padding = buttonSize / 5
                    val textSize = (maxWidth / 20f).value.scaledSp
                    val textFieldHeight = maxWidth / 5f
                    PlayerButtonBackground(
                        state = state.buttonState,
                        imageUri = viewModel.locateImage(state.player),
                        color = state.player.color,
                        isDead = viewModel.isDead(autoKo = true),
                    )
                    SettingsButton(
                        modifier = Modifier.size(buttonSize).padding(
                            start = padding,
                            bottom = padding
                        ).align(Alignment.BottomStart),
                        backgroundColor = Color.Transparent,
                        mainColor = state.player.textColor,
                        visible = true,
                        imageVector = vectorResource(Res.drawable.back_icon),
                        onPress = {
                            viewModel.showChangeNameField(false)
                            viewModel.closeSettingsMenu()
                        }
                    )
                    Text(
                        modifier = Modifier.wrapContentHeight().fillMaxWidth().padding(top = padding * 2).padding(horizontal = padding),
                        text = "Previous name: ${state.player.name}",
                        color = state.player.textColor,
                        fontSize = textSize,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = textShadowStyle()
                    )

                    ChangeNameField(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.8f)
                            .height(textFieldHeight)
                            .padding(bottom = padding),
                        name = state.changeNameTextField,
                        showChangeNameField = state.showChangeNameField,
                        onChangeName = viewModel::setChangeNameField,
                        backgroundColor = state.player.color,
                        playerTextColor = state.player.textColor,
                        onDone = {
                            viewModel.setName(state.changeNameTextField.text)
                            viewModel.showChangeNameField(false)
                            viewModel.closeSettingsMenu()
                        }
                    )
                }
                Spacer(Modifier.weight(0.6f))
            }
        }
    }

    if (state.showScryfallSearch) {
        val scryfallBackStack = remember { mutableStateListOf({ viewModel.showScryfallSearch(false) }) }
        ScryfallSearchDialog(
            onDismiss = {
                viewModel.showScryfallSearch(false)
            },
            addToBackStack = {
                scryfallBackStack.add(it)
            },
            onImageSelected = {
                viewModel.setImageUri(it)
            }
        )
    }

    LaunchedEffect(state.buttonState) {
        if (state.buttonState == PBState.COMMANDER_RECEIVER) viewModel.clearBackStack()
    }

    val rotationModifier = remember(rotation) {
        when (rotation) {
            90f -> Modifier.rotateVertically(rotation = VerticalRotation.CLOCKWISE)
            270f -> Modifier.rotateVertically(rotation = VerticalRotation.COUNTER_CLOCKWISE)
            180f -> Modifier.rotate(180f)
            else -> Modifier
        }
    }
    BoxWithConstraints(
        modifier = Modifier.wrapContentSize().then(rotationModifier).then(
            if (state.buttonState == PBState.NORMAL || state.buttonState == PBState.COMMANDER_RECEIVER) {
                Modifier.bounceClick(
                    bounceAmount = 0.01f,
                    bounceDuration = 60L,
                    repeatEnabled = true
                )
            } else {
                Modifier
            }
        )
    ) {

        MonarchyIndicator(
            modifier = modifier,
            monarch = state.player.monarch
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().background(Color.Transparent).clip(RoundedCornerShape(12)),
                contentAlignment = Alignment.Center
            ) {
                PlayerButtonBackground(
                    state = state.buttonState,
                    imageUri = viewModel.locateImage(state.player),
                    color = state.player.color,
                    isDead = viewModel.isDead(autoKo = true),
                )

                val smallButtonSize = (maxWidth / 15f) + (maxHeight / 10f)
                val settingsStateMargin = smallButtonSize / 7f
                val commanderStateMargin = settingsStateMargin * 1.4f

                val wideButton = maxWidth / maxHeight > 1.4

                val playerInfoPadding = if (wideButton) {
                    Modifier.padding(bottom = smallButtonSize / 2f).offset(y = -smallButtonSize / 8f)
                } else Modifier.offset(y = smallButtonSize / 4f)

                val settingsPadding = if (wideButton) Modifier.padding(
                    bottom = smallButtonSize / 4,
                    top = smallButtonSize / 8
                ) else Modifier.padding(
                    top = smallButtonSize / 4
                )
                if (!state.player.setDead) {
                    when (state.buttonState) {
                        PBState.NORMAL -> {
                            LifeChangeButtons(Modifier.fillMaxWidth(),
                                onIncrementLife = {
                                    viewModel.incrementLife(1)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                onDecrementLife = {
                                    viewModel.incrementLife(-1)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    onDecrementLife = {
                                        viewModel.incrementCommanderDamage(
                                            value = -1
                                        )
                                        viewModel.incrementLife(1)
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    })
                                if (currentDealerIsPartnered) {
                                    LifeChangeButtons(Modifier.fillMaxWidth(),
                                        onIncrementLife = {
                                            viewModel.incrementCommanderDamage(
                                                value = 1,
                                                partner = true
                                            )
                                            viewModel.incrementLife(-1)
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        },
                                        onDecrementLife = {
                                            viewModel.incrementCommanderDamage(
                                                viewModel,
                                                value = -1,
                                                partner = true
                                            )
                                            viewModel.incrementLife(1)
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                fun FormattedSettingsButton(modifier: Modifier, imageResource: DrawableResource, text: String, onPress: () -> Unit) {
                    SettingsButton(
                        modifier = modifier,
                        imageVector = vectorResource(imageResource),
                        text = text,
                        onPress = onPress,
                        mainColor = state.player.textColor,
                        backgroundColor = Color.Transparent
                    )
                }

                @Composable
                fun PlayerButtonContent(modifier: Modifier = Modifier) {
                    BoxWithConstraints(modifier.fillMaxSize()) {
                        val textSize = (maxWidth / 15f).value.scaledSp
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
                                    Text(
                                        modifier = Modifier,
                                        text = "Deal damage with your commander",
                                        color = state.player.textColor,
                                        fontSize = textSize,
                                        lineHeight = textSize,
                                        textAlign = TextAlign.Center,
                                        style = defaultTextStyle()
                                    )
                                    Spacer(modifier = Modifier.height(smallButtonSize/4f))
                                    SettingsButton(modifier = Modifier.size(smallButtonSize*1.5f),
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
                                        fontSize = textSize*0.6f,
                                        textAlign = TextAlign.Center,
                                        style = defaultTextStyle()
                                    )
                                }
                            }

                            PBState.SETTINGS_DEFAULT -> {
                                BoxWithConstraints(settingsPadding.fillMaxSize()) {
                                    val (settingsButtonSize, _, _) = generateSizes(maxWidth, maxHeight)
                                    LazyHorizontalGrid(
                                        modifier = Modifier.fillMaxSize(),
                                        rows = GridCells.Fixed(2),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        item {
                                            FormattedSettingsButton(
                                                modifier = Modifier.size(settingsButtonSize),
                                                imageResource = Res.drawable.monarchy_icon,
                                                text = "Monarchy"
                                            ) { viewModel.onMonarchyButtonClicked() }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = Modifier.size(settingsButtonSize),
                                                imageResource = Res.drawable.transparent,
                                                text = ""
                                            ) { }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = Modifier.size(settingsButtonSize),
                                                imageResource = Res.drawable.mana_icon,
                                                text = "Counters"
                                            ) {
                                                viewModel.setPlayerButtonState(PBState.COUNTERS_VIEW)
                                                viewModel.pushBackStack { viewModel.setPlayerButtonState(PBState.SETTINGS_DEFAULT) }
                                            }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = Modifier.size(settingsButtonSize),
                                                imageResource = Res.drawable.star_icon,
                                                text = "Customize"
                                            ) {
                                                viewModel.setPlayerButtonState(PBState.SETTINGS_CUSTOMIZE)
                                                viewModel.pushBackStack { viewModel.setPlayerButtonState(PBState.SETTINGS_DEFAULT) }
                                            }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = Modifier.size(settingsButtonSize),
                                                imageResource = Res.drawable.skull_icon,
                                                text = "KO Player"
                                            ) {
                                                viewModel.toggleSetDead()
                                                viewModel.closeSettingsMenu()
                                                viewModel.clearBackStack()
                                            }
                                        }

                                        item {
                                            FormattedSettingsButton(
                                                modifier = Modifier.size(settingsButtonSize),
                                                imageResource = Res.drawable.change_name_icon,
                                                text = "Change Name"
                                            ) {
                                                viewModel.showChangeNameField(true)
                                            }
                                        }
                                    }
                                }
                            }

                            PBState.SETTINGS_CUSTOMIZE -> {
                                BoxWithConstraints(settingsPadding.fillMaxSize()) {
                                    val (settingsButtonSize, _, _) = generateSizes(maxWidth, maxHeight)
                                    LazyHorizontalGrid(
                                        modifier = Modifier.fillMaxSize(),
                                        rows = GridCells.Fixed(2),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        item {
                                            FormattedSettingsButton(
                                                modifier = Modifier.size(settingsButtonSize),
                                                imageResource = Res.drawable.change_background_icon,
                                                text = "Background Color"
                                            ) {
                                                viewModel.setPlayerButtonState(PBState.SETTINGS_BACKGROUND_COLOR_PICKER)
                                                viewModel.pushBackStack { viewModel.setPlayerButtonState(PBState.SETTINGS_CUSTOMIZE) }
                                            }
                                        }

                                        item {
                                            FormattedSettingsButton(
                                                modifier = Modifier.size(settingsButtonSize),
                                                imageResource = Res.drawable.text_icon,
                                                text = "Text Color"
                                            ) {
                                                viewModel.setPlayerButtonState(PBState.SETTINGS_TEXT_COLOR_PICKER)
                                                viewModel.pushBackStack { viewModel.setPlayerButtonState(PBState.SETTINGS_CUSTOMIZE) }
                                            }
                                        }

                                        item {
                                            FormattedSettingsButton(
                                                modifier = Modifier.size(settingsButtonSize),
                                                imageResource = Res.drawable.camera_icon,
                                                text = "Upload Image"
                                            ) {
                                                viewModel.showCameraWarning(true)
                                            }
                                        }

                                        item {
                                            FormattedSettingsButton(
                                                modifier = Modifier.size(settingsButtonSize),
                                                imageResource = Res.drawable.download_icon,
                                                text = "Load Profile"
                                            ) {
                                                viewModel.setPlayerButtonState(PBState.SETTINGS_LOAD_PLAYER)
                                                viewModel.pushBackStack { viewModel.setPlayerButtonState(PBState.SETTINGS_CUSTOMIZE) }
                                            }
                                        }

                                        item {
                                            FormattedSettingsButton(
                                                modifier = Modifier.size(settingsButtonSize),
                                                imageResource = Res.drawable.search_icon,
                                                text = "Search Image"
                                            ) {
                                                viewModel.showScryfallSearch(!state.showScryfallSearch)
                                            }
                                        }

                                        item {
                                            FormattedSettingsButton(
                                                modifier = Modifier.size(settingsButtonSize),
                                                imageResource = Res.drawable.reset_icon,
                                                text = "Reset",
                                            ) {
                                                viewModel.showResetPrefsDialog(true)
                                            }
                                        }
                                    }
                                }
                            }

                            PBState.SETTINGS_LOAD_PLAYER -> {
                                val playerList = remember {
                                    mutableStateListOf<Player>().apply {
                                        addAll(viewModel.settingsManager.loadPlayerPrefs().filter { !it.isDefaultOrEmptyName() })
                                    }
                                }
                                BoxWithConstraints(settingsPadding.fillMaxSize()) {
                                    val (_, smallPadding, smallTextSize) = generateSizes(maxWidth, maxHeight)
                                    Column(
                                        Modifier.fillMaxSize().padding(bottom = smallPadding/2f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            Modifier.fillMaxWidth().wrapContentHeight().padding(bottom = smallPadding),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                modifier = Modifier.wrapContentSize(unbounded = true),
                                                text = "Saved profiles",
                                                color = state.player.textColor,
                                                fontSize = smallTextSize,
                                                textAlign = TextAlign.Center,
                                                style = defaultTextStyle()
                                            )
                                            Text(
                                                modifier = Modifier.wrapContentSize(unbounded = true).offset(y = smallPadding*1.5f),
                                                text = "(hold to delete)",
                                                color = state.player.textColor,
                                                fontSize = smallTextSize / 2,
                                                textAlign = TextAlign.Center,
                                                style = defaultTextStyle()
                                            )
                                        }
                                        Box(
                                            Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = smallPadding)
                                                .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(10))
                                                .border(0.5.dp, state.player.textColor.copy(alpha = 0.9f), RoundedCornerShape(10))
                                        ) {
                                                LazyHorizontalGrid(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(smallPadding)
                                                        .clip(RoundedCornerShape(7)),
                                                    rows = GridCells.Fixed(if (wideButton) 3 else 2),
                                                    state = rememberLazyGridState(),
                                                    horizontalArrangement = Arrangement.spacedBy(smallPadding),
                                                    verticalArrangement = Arrangement.spacedBy(smallPadding),
                                                    content = {
                                                        items(items = playerList, key = { p -> p.hashCode() }) { pInfo ->
                                                            MiniPlayerButton(
                                                                imageUri = viewModel.locateImage(pInfo),
                                                                name = pInfo.name,
                                                                backgroundColor = pInfo.color,
                                                                textColor = pInfo.textColor,
                                                                copyPrefsToCurrentPlayer = {
                                                                    viewModel.copySettings(pInfo)
                                                                    viewModel.closeSettingsMenu()
                                                                },
                                                                removePlayerProfile = {
                                                                    println("Removing player profile: ${pInfo.name}")
                                                                    playerList.remove(pInfo)
                                                                    viewModel.settingsManager.deletePlayerPref(pInfo)
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
                                                            color = state.player.textColor,
                                                            fontSize = smallTextSize * 0.7f,
                                                            textAlign = TextAlign.Center,
                                                            style = defaultTextStyle()
                                                        )
                                                        Text(
                                                            modifier = Modifier.wrapContentSize().padding(horizontal = 20.dp),
                                                            text = "Changes to name/customization will be saved automatically",
                                                            color = state.player.textColor,
                                                            lineHeight = smallTextSize,
                                                            fontSize = smallTextSize * 0.7f,
                                                            textAlign = TextAlign.Center,
                                                            style = defaultTextStyle()
                                                        )
                                                    }
                                                }
                                        }
                                    }
                                }
                            }

                            PBState.SETTINGS_BACKGROUND_COLOR_PICKER -> {
                                BoxWithConstraints(settingsPadding.fillMaxSize()) {
                                    val (_, smallPadding, _) = generateSizes(maxWidth, maxHeight)
                                    ColorPicker(
                                        Modifier
                                            .wrapContentSize()
                                            .align(Alignment.Center)
                                            .padding(bottom = if (wideButton) smallPadding*2 else smallPadding / 4f),
                                        text = "Choose a Background Color",
                                        colorList = mutableListOf<Color>().apply {
                                            add(Color.Black)
                                            add(Color.White)
                                            addAll(allPlayerColors)
                                        },
                                        textColor = state.player.textColor,
                                        showColorPicker = viewModel::showBackgroundColorPicker,
                                        onPress = viewModel::onChangeBackgroundColor
                                    )
                                }
                            }

                            PBState.SETTINGS_TEXT_COLOR_PICKER -> {
                                BoxWithConstraints(settingsPadding.fillMaxSize()) {
                                    val (_, smallPadding, _) = generateSizes(maxWidth, maxHeight)
                                    ColorPicker(
                                        Modifier
                                            .wrapContentSize()
                                            .align(Alignment.Center)
                                            .padding(bottom = if (wideButton) smallPadding*2 else smallPadding / 4f),
                                        text = "Choose a Text Color",
                                        colorList = mutableListOf<Color>().apply {
                                            add(Color.Black)
                                            add(Color.White)
                                            addAll(allPlayerColors)
                                        },
                                        textColor = state.player.textColor,
                                        showColorPicker = viewModel::showTextColorPicker,
                                        onPress = viewModel::onChangeTextColor
                                    )
                                }
                            }

                            PBState.COUNTERS_VIEW -> {
                                CounterWrapper(
                                    modifier = settingsPadding.fillMaxSize(),
                                    textColor = state.player.textColor,
                                    text = "Counters"
                                ) {
                                    LazyRow(
                                        Modifier.fillMaxSize().padding(5.dp),
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        items(state.player.activeCounters) { counterType ->
                                            Counter(
                                                textColor = state.player.textColor,
                                                iconResource = counterType.resource,
                                                value = viewModel.getCounterValue(counterType),
                                                onIncrement = {
                                                    viewModel.incrementCounterValue(
                                                        counterType,
                                                        1
                                                    )
                                                },
                                                onDecrement = {
                                                    viewModel.incrementCounterValue(
                                                        counterType,
                                                        -1
                                                    )
                                                })
                                        }
                                        item {
                                            AddCounter(
                                                textColor = state.player.textColor,
                                                onTap = {
                                                    viewModel.setPlayerButtonState(PBState.COUNTERS_SELECT)
                                                    viewModel.pushBackStack { viewModel.setPlayerButtonState(PBState.COUNTERS_VIEW) }
                                                },
                                            )
                                        }
                                    }
                                }
//                                Counters(
//                                    modifier = settingsPadding.padding(horizontal = 5.dp).padding(bottom = 5.dp),
//                                    textColor = state.player.textColor,
//                                    activeCounters = state.player.activeCounters,
//                                    getCounterValue = viewModel::getCounterValue,
//                                    incrementCounterValue = viewModel::incrementCounterValue,
//                                    setActiveCounter = viewModel::setActiveCounter,
//                                    addToBackStack = viewModel::pushBackStack
//                                )
                            }

                            PBState.COUNTERS_SELECT -> {
                                CounterWrapper(
                                    modifier = settingsPadding.fillMaxSize(),
                                    textColor = state.player.textColor,
                                    text = "Select Counters"
                                ) {
                                    Column(
                                        Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        LazyHorizontalGrid(
                                            modifier = Modifier.fillMaxSize().padding(5.dp).clip(RoundedCornerShape(12)),
                                            rows = GridCells.Fixed(3),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            items(CounterType.entries.toTypedArray()) { counterType ->
                                                var selected by remember { mutableStateOf(counterType in state.player.activeCounters) }
                                                Box(modifier = Modifier.fillMaxSize().aspectRatio(1.0f).padding(0.5.dp).background(
                                                    if (selected) {
                                                        Color.Green.copy(alpha = 0.5f)
                                                    } else {
                                                        Color.Transparent
                                                    }
                                                ).pointerInput(Unit) {
                                                    detectTapGestures {
                                                        selected = viewModel.setActiveCounter(counterType)
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    }
                                                }) {
                                                    SettingsButton(
                                                        imageVector = vectorResource(counterType.resource),
                                                        modifier = Modifier.fillMaxSize().padding(5.dp),
                                                        mainColor = state.player.textColor,
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

                            else -> {
                                Text("Oopsies")
                            }
                        }
                    }
                }

                @Composable
                fun BackButton(modifier: Modifier = Modifier) {
                    SettingsButton(
                        modifier = modifier.size(smallButtonSize * 1.1f).padding(
                            start = settingsStateMargin,
                            bottom = settingsStateMargin
                        ),
                        backgroundColor = Color.Transparent,
                        mainColor = state.player.textColor,
                        visible = state.backStack.isNotEmpty(),
                        imageVector = vectorResource(Res.drawable.back_icon),
                        onPress = viewModel::popBackStack
                    )
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
                    } else if (state.backStack.isNotEmpty()) {
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
                    }
                }

                if (wideButton) {
                    Row(
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
        modifier = modifier.clip(RoundedCornerShape(12)).then(
            if (monarch) {
                Modifier.animatedBorderCard(
                    shape = RoundedCornerShape(12),
                    borderWidth = width,
                    colors = colors,
                    animationDuration = duration
                ).clip(RoundedCornerShape(12))
            } else {
                Modifier.padding(width)
            }
        )
    ) {
        content()
    }
}

@Composable
fun CounterWrapper(
    modifier: Modifier = Modifier,
    textColor: Color,
    text: String,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val smallPadding = maxHeight / 20f
        val smallTextSize = maxHeight.value.scaledSp / 12f
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.wrapContentSize(unbounded = true).padding(
                    top = 0.dp,
                    bottom = smallPadding / 4f
                ),
                text = text,
                color = textColor,
                fontSize = smallTextSize,
                textAlign = TextAlign.Center,
                style = defaultTextStyle()
            )
            Box(
                Modifier.fillMaxSize().background(
                    Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12)
                ).border(
                    0.5.dp,
                    textColor.copy(alpha = 0.9f),
                    RoundedCornerShape(12)
                ).clip(RoundedCornerShape(12))
            ) {
                content()
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
        shape = RoundedCornerShape(10)
    ).border(
        0.5.dp,
        textColor.copy(alpha = 0.9f),
        RoundedCornerShape(10)
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
            shadowEnabled = true,
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
            shape = RoundedCornerShape(10)
        ).border(
            0.5.dp,
            textColor.copy(alpha = 0.9f),
            RoundedCornerShape(10)
        ).clip(RoundedCornerShape(10))
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

            else -> color
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

            else -> {
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
        val smallTextPadding = (smallTextSize / 4f).dp
        val recentChangeSize = (maxHeight / 7f).value

        val iconSize = maxHeight / 7f

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.padding(top = smallTextPadding, bottom = smallTextPadding * 3).offset(y = smallTextPadding*4),
                text = name,
                color = textColor,
                fontSize = smallTextSize.scaledSp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
                    modifier = Modifier.weight(0.8f).padding(start = 25.dp).wrapContentSize(unbounded = true),
                    text = recentChangeText,
                    color = textColor,
                    fontSize = recentChangeSize.scaledSp,
                    style = textShadowStyle()
                )
            }
            SettingsButton(
                modifier = Modifier.size(iconSize).padding(top = 2.dp).offset(y = (largeTextSize / 48f).dp),
                backgroundColor = Color.Transparent,
                mainColor = textColor,
                imageVector = vectorResource(iconResource),
                enabled = false
            )
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
                modifier = Modifier
                    .wrapContentSize(unbounded = true)
                    .padding(
                    bottom = textPadding,
                    top = textPadding * 2),
                text = text,
                color = textColor,
                fontSize = smallTextSize / 1.25f,
                textAlign = TextAlign.Center,
                style = defaultTextStyle()
            )
            Box(
                Modifier
                    .wrapContentSize()
                    .weight(0.5f)
                    .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(10))
                    .border(0.5.dp, textColor.copy(alpha = 0.9f), RoundedCornerShape(10))
            ) {
                LazyHorizontalGrid(
                    modifier = Modifier
                        .padding(containerPadding * 2)
                        .clip(RoundedCornerShape(5)),
                    rows = GridCells.Fixed(2),
                    state = rememberLazyGridState(),
                    horizontalArrangement = Arrangement.spacedBy(colorPickerPadding),
                    verticalArrangement = Arrangement.spacedBy(colorPickerPadding),
                    content = {
                        item {
                            CustomColorPickerButton(
                                modifier = Modifier.padding(colorPickerPadding),
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
}

@Composable
fun ChangeNameField(
    modifier: Modifier = Modifier,
    name: TextFieldValue,
    showChangeNameField: Boolean,
    onChangeName: (TextFieldValue) -> Unit,
    backgroundColor: Color,
    playerTextColor: Color,
    onDone: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequester) {
        if (showChangeNameField) {
            focusRequester.requestFocus()
        }
    }

    BoxWithConstraints(
        modifier = modifier.focusRequester(focusRequester),
        contentAlignment = Alignment.Center
    ) {
        val textSize = (maxHeight / 3.5f).value.scaledSp
        TextField(
            value = name,
            onValueChange = onChangeName,
            label = {
                Text(
                    "New Name",
                    color = backgroundColor,
                    fontSize = textSize * 0.8f,
                    style = defaultTextStyle()
                )
            },
            textStyle = TextStyle(fontSize = textSize),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedTextColor = backgroundColor,
                unfocusedTextColor = backgroundColor,
                focusedContainerColor = playerTextColor,
                unfocusedContainerColor = playerTextColor,
                cursorColor = backgroundColor,
                errorCursorColor = backgroundColor,
                unfocusedIndicatorColor = backgroundColor,
                focusedIndicatorColor = backgroundColor,
                disabledIndicatorColor = backgroundColor,
                errorIndicatorColor = backgroundColor,
            ),
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier.fillMaxSize()
        )
        SettingsButton(Modifier.align(Alignment.CenterEnd)
            .fillMaxHeight()
            .aspectRatio(1.0f),
            imageVector = vectorResource(Res.drawable.enter_icon),
            shadowEnabled = false,
            mainColor = backgroundColor,
            onPress = { onDone() })
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
    BoxWithConstraints(modifier = Modifier.fillMaxHeight().aspectRatio(2.5f).clip(RoundedCornerShape(10)).pointerInput(Unit) {
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
            modifier = Modifier.align(Alignment.Center),
            style = textShadowStyle()
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

@Composable
private fun CustomIncrementButton(
    modifier: Modifier = Modifier, onIncrementLife: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val ripple = rememberRipple(color = Color.Black)
    Box(
        modifier = modifier
            .repeatingClickable(
                interactionSource = interactionSource,
                indication = ripple,
                enabled = true,
                onPress = onIncrementLife
            )
//            .clickable(
//                onClick = onIncrementLife,
//                interactionSource = interactionSource,
//                indication = ripple
//            )
//            .indication(
//                interactionSource = interactionSource,
//                indication = ripple
//            )
    )
}

