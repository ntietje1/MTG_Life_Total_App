package ui.lifecounter.playerbutton


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import domain.common.NumberWithRecentChange
import domain.game.timer.TurnTimer
import domain.system.SystemManager
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.back_icon
import lifelinked.shared.generated.resources.commander_solid_icon
import lifelinked.shared.generated.resources.mana_icon
import lifelinked.shared.generated.resources.monarchy_icon
import lifelinked.shared.generated.resources.one_finger_tap
import lifelinked.shared.generated.resources.pencil_icon
import lifelinked.shared.generated.resources.settings_icon
import lifelinked.shared.generated.resources.skull_icon
import lifelinked.shared.generated.resources.sword_icon
import lifelinked.shared.generated.resources.sword_icon_double
import lifelinked.shared.generated.resources.transparent
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import theme.LocalDimensions
import theme.defaultTextStyle
import theme.scaledSp
import theme.textShadowStyle
import ui.components.SettingsButton
import ui.dialog.customization.CustomizationViewModel
import ui.dialog.customization.PlayerCustomizationDialog
import ui.lifecounter.CounterType
import ui.lifecounter.PlayerSeatUiState
import ui.modifier.VerticalRotation
import ui.modifier.bounceClick
import ui.modifier.rotateVertically

@Composable
fun PlayerButton(
    modifier: Modifier = Modifier,
    state: PlayerSeatUiState,
    customizationViewModel: CustomizationViewModel?,
    onAction: (PlayerButtonAction) -> Unit,
    rotation: Float = 0f,
    turnTimerModifier: Modifier,
    setBlurBackground: (Boolean) -> Unit,
) {
    val currentDealerIsPartnered = (state.commanderState as? CommanderState.Active)?.dealer?.partnerMode == true
    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current
    val commanderButtonVisible = state.buttonState.showsCommanderButton()
    val settingsButtonVisible = state.buttonState.showsSettingsButton()

    fun generateSizes(maxWidth: Dp, maxHeight: Dp): Triple<Dp, Dp, Float> {
        val settingsButtonSize = if (maxHeight / 2 * 3 < maxWidth) {
            maxHeight / 2
        } else {
            maxWidth / 3
        }
        val smallPadding = settingsButtonSize / 10f
        val smallTextSize = maxHeight.value / 12f
        return Triple(settingsButtonSize, smallPadding, smallTextSize)
    }

    LaunchedEffect(
        state.showCustomizeMenu
    ) {
        val dialogStates = listOf(
            state.showCustomizeMenu
        )
        setBlurBackground(dialogStates.any { it })
    }

    if (state.showCustomizeMenu) {
        customizationViewModel?.let { customizationViewModel ->
            PlayerCustomizationDialog(
                modifier = Modifier.fillMaxSize(),
                onDismiss = {
                    onAction(PlayerButtonAction.CloseCustomization)
                },
                viewModel = customizationViewModel
            )
        }
    }

    var timerTextSize by remember(Unit) { mutableStateOf(15) }
    var timerPadding by remember(Unit) { mutableStateOf(5) }

    @Composable
    fun Timer(modifier: Modifier = Modifier, timer: TurnTimer) {
        val textSize = timerTextSize.scaledSp
        val padding = timerPadding.dp
        Column(
            modifier = modifier.wrapContentSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(padding)
        ) {
            Text(
                text = timer.getTimeString(),
                color = state.player.textColor,
                fontSize = textSize,
                lineHeight = textSize,
                textAlign = TextAlign.Center,
                style = defaultTextStyle(),
                modifier = Modifier.padding(horizontal = padding * 3.5f).padding(top = padding * 1.5f)
            )
            Text(
                text = "Turn ${timer.turn}",
                color = state.player.textColor,
                fontSize = textSize,
                lineHeight = textSize,
                textAlign = TextAlign.Center,
                style = defaultTextStyle(),
                modifier = Modifier.padding(horizontal = padding * 3.5f).padding(bottom = padding * 1.5f)
            )
        }
    }

    // Jank way of stopping the repeating bounce if long pressing on timer
    var timerJustClicked by remember { mutableStateOf(false) }

    LaunchedEffect(timerJustClicked) {
        if (timerJustClicked) {
            timerJustClicked = false
        }
    }

    val rotationModifier = remember(rotation) {
        when (rotation) {
            90f -> Modifier.rotateVertically(rotation = VerticalRotation.CLOCKWISE)
            270f -> Modifier.rotateVertically(rotation = VerticalRotation.COUNTER_CLOCKWISE)
            180f -> Modifier.rotate(180f)
            else -> Modifier
        }
    }

    Box(
        modifier = Modifier
            .wrapContentSize()
            .then(rotationModifier)
    ) {
        MonarchyIndicator(
            modifier = Modifier.wrapContentSize(),
            monarch = state.player.monarch,
            borderWidth = dimensions.paddingTiny,
        ) {
            BoxWithConstraints(
                modifier = modifier.then(
                    if ((state.buttonState == PBState.NORMAL || state.buttonState == PBState.COMMANDER_RECEIVER) && !timerJustClicked && !state.isDead) {
                        Modifier.bounceClick(
                            initialBounceFactor = 3.5f, bounceAmount = 0.005f, bounceDuration = 60L, repeatEnabled = true
                        )
                    } else {
                        Modifier
                    }
                ), contentAlignment = Alignment.Center
            ) {
                timerTextSize = remember(Unit) { (4.dp + maxWidth / 35f + maxHeight / 55f).value.toInt() }
                timerPadding = remember(Unit) { timerTextSize / 3 }

                PlayerButtonBackground(
                    modifier = Modifier.clip(RoundedCornerShape(12)),
                    state = state.buttonState,
                    imageUri = state.player.imageString,
                    color = state.player.color,
                    isDead = state.isDead,
                )

                val smallButtonSize = remember(Unit) { (maxWidth / 15f) + (maxHeight / 10f) }

                val wideButton = remember(Unit) { maxWidth / maxHeight > 1.4 }

                if (!state.player.setDead) {
                    when (state.buttonState) {
                        PBState.NORMAL -> {
                            val playerNumber = state.player.playerNum
                            LifeChangeButtons(
                                modifier = Modifier.fillMaxWidth(),
                                incrementContentDescription = "P$playerNumber increase life",
                                decrementContentDescription = "P$playerNumber decrease life",
                                onIncrementLife = {
                                    onAction(PlayerButtonAction.IncrementLife)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                onDecrementLife = {
                                    onAction(PlayerButtonAction.DecrementLife)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                            )
                        }

                        PBState.COMMANDER_RECEIVER -> {
                            val playerNumber = state.player.playerNum
                            Row(Modifier.fillMaxSize()) {
                                LifeChangeButtons(
                                    modifier = Modifier.then(if (currentDealerIsPartnered) Modifier.fillMaxWidth(0.5f) else Modifier.fillMaxWidth()),
                                    incrementContentDescription = "P$playerNumber primary commander damage increase",
                                    decrementContentDescription = "P$playerNumber primary commander damage decrease",
                                    onIncrementLife = {
                                        onAction(PlayerButtonAction.IncrementCommanderDamage(partner = false))
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                    onDecrementLife = {
                                        onAction(PlayerButtonAction.DecrementCommanderDamage(partner = false))
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    },
                                )
                                if (currentDealerIsPartnered) {
                                    LifeChangeButtons(
                                        modifier = Modifier.fillMaxWidth(),
                                        incrementContentDescription = "P$playerNumber partner commander damage increase",
                                        decrementContentDescription = "P$playerNumber partner commander damage decrease",
                                        onIncrementLife = {
                                            onAction(PlayerButtonAction.IncrementCommanderDamage(partner = true))
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        },
                                        onDecrementLife = {
                                            onAction(PlayerButtonAction.DecrementCommanderDamage(partner = true))
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        },
                                    )
                                }
                            }
                        }

                        else -> {
                        }
                    }
                }

                @Composable
                fun FormattedSettingsButton(
                    modifier: Modifier,
                    imageResource: DrawableResource,
                    text: String,
                    contentDescription: String? = null,
                    onPress: () -> Unit
                ) {
                    SettingsButton(
                        modifier = modifier,
                        imageVector = vectorResource(imageResource),
                        text = text,
                        contentDescription = contentDescription,
                        onPress = onPress,
                        mainColor = state.player.textColor,
                        backgroundColor = Color.Transparent
                    )
                }

                @Composable
                fun Skull(modifier: Modifier = Modifier) {
                    SettingsButton(
                        modifier = modifier.align(Alignment.Center).size(smallButtonSize * 4).padding(top = maxHeight / 9f),
                        backgroundColor = Color.Transparent,
                        mainColor = state.player.textColor,
                        imageVector = vectorResource(Res.drawable.skull_icon),
                        enabled = false,
                    )
                }

                @Composable
                fun PlayerButtonContent(modifier: Modifier = Modifier) {
                    BoxWithConstraints(modifier.fillMaxSize()) {
                        val textSize = remember { (maxWidth / 15f).value }

                        val playerInfoModifier = remember(Unit) {
                            if (wideButton) {
                                Modifier.padding(bottom = smallButtonSize / 2f).offset(y = -smallButtonSize / 8f)
                            } else {
                                Modifier.offset(y = maxHeight / 20f)
                            }
                        }

                        val settingsModifier = remember(Unit) {
                            if (wideButton) Modifier.padding(
                                bottom = smallButtonSize / 4, top = smallButtonSize / 8
                            ) else Modifier.padding(
                                top = smallButtonSize / 4
                            )
                        }

                        when (state.buttonState) {
                            PBState.NORMAL -> {
                                if (state.isDead) {
                                    Skull(playerInfoModifier)
                                } else {
                                    LifeNumber(
                                        modifier = playerInfoModifier.fillMaxSize(),
                                        name = state.player.name,
                                        textColor = state.player.textColor,
                                        value = state.player.lifeTotal,
                                        contentDescription = "P${state.player.playerNum} life total ${state.player.lifeTotal.number}",
                                    )
                                }
                            }

                            PBState.SELECT_FIRST_PLAYER -> {
                                Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                                    detectTapGestures(onPress = {
                                        onAction(PlayerButtonAction.SelectFirstPlayer)
                                    })
                                }) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                                    ) {
                                        if (!wideButton) {
                                            Spacer(modifier = Modifier.height(smallButtonSize))
                                        }
                                        Text(
                                            text = "Select first player", color = state.player.textColor, fontSize = textSize.scaledSp * 1.2f, textAlign = TextAlign.Center, style = textShadowStyle()
                                        )
                                        Spacer(modifier = Modifier.height(smallButtonSize / 3f))
                                        SettingsButton(
                                            modifier = Modifier.size(smallButtonSize * 1.2f).rotate(20f),
                                            backgroundColor = Color.Transparent,
                                            mainColor = state.player.textColor,
                                            enabled = false,
                                            imageVector = vectorResource(Res.drawable.one_finger_tap),
                                        )
                                    }
                                }
                            }

                            PBState.COMMANDER_RECEIVER -> {
                                if (state.isDead) {
                                    Skull(playerInfoModifier)
                                } else {
                                    val playerNumber = state.player.playerNum
                                    val primaryCommanderDamage = commanderDamageValue(state, partner = false)
                                    val partnerCommanderDamage = if (currentDealerIsPartnered) {
                                        commanderDamageValue(state, partner = true)
                                    } else {
                                        null
                                    }
                                    CommanderDamageNumber(
                                        modifier = playerInfoModifier.fillMaxSize(),
                                        name = state.player.name,
                                        textColor = state.player.textColor,
                                        firstValue = primaryCommanderDamage,
                                        secondValue = partnerCommanderDamage,
                                        firstContentDescription = "P$playerNumber primary commander damage ${primaryCommanderDamage.number}",
                                        secondContentDescription = if (partnerCommanderDamage == null) {
                                            null
                                        } else {
                                            "P$playerNumber partner commander damage ${partnerCommanderDamage.number}"
                                        },
                                    )
                                }
                            }

                            PBState.COMMANDER_DEALER -> {
                                val commanderDealerModifier = remember(Unit) {
                                    if (!wideButton) Modifier.offset(y = smallButtonSize / 4f) else Modifier
                                }

                                Column(
                                    commanderDealerModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        modifier = Modifier,
                                        text = "Deal damage with your commander",
                                        color = state.player.textColor,
                                        fontSize = textSize.scaledSp * 0.8f,
                                        lineHeight = textSize.scaledSp * 0.8f,
                                        textAlign = TextAlign.Center,
                                        style = textShadowStyle()
                                    )
                                    Spacer(modifier = Modifier.height(smallButtonSize / 4f))
                                    SettingsButton(modifier = Modifier.size(smallButtonSize * 1.5f),
                                        imageVector = vectorResource(if (currentDealerIsPartnered) Res.drawable.sword_icon_double else Res.drawable.sword_icon),
                                        backgroundColor = Color.Transparent,
                                        mainColor = state.player.textColor,
                                        contentDescription = if (currentDealerIsPartnered) {
                                            "Disable partner commander damage"
                                        } else {
                                            "Enable partner commander damage"
                                        },
                                        onPress = {
                                            onAction(PlayerButtonAction.ToggleCommanderPartnerMode)
                                        })
                                    Text(
                                        modifier = Modifier.wrapContentSize(unbounded = true),
                                        text = "Toggle Partner Mode",
                                        color = state.player.textColor,
                                        fontSize = textSize.scaledSp * 0.6f,
                                        textAlign = TextAlign.Center,
                                        style = textShadowStyle()
                                    )
                                }
                            }

                            PBState.SETTINGS -> {
                                BoxWithConstraints(settingsModifier.fillMaxSize()) {
                                    val playerNumber = state.player.playerNum
                                    val (settingsButtonSize, smallPadding, _) = remember { generateSizes(maxWidth, maxHeight) }
                                    val settingsButtonModifier = remember { Modifier.size(settingsButtonSize).padding(smallPadding / 2f) }
                                    LazyHorizontalGrid(
                                        modifier = Modifier.fillMaxSize(), rows = GridCells.Fixed(2), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.Center
                                    ) {
                                        item {
                                            FormattedSettingsButton(
                                                modifier = settingsButtonModifier,
                                                imageResource = Res.drawable.monarchy_icon,
                                                text = "Monarchy",
                                                contentDescription = if (state.player.monarch) {
                                                    "Clear P$playerNumber as monarch"
                                                } else {
                                                    "Make P$playerNumber the monarch"
                                                }
                                            ) { onAction(PlayerButtonAction.SetMonarch(!state.player.monarch)) }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = settingsButtonModifier, imageResource = Res.drawable.transparent, text = ""
                                            ) { }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = settingsButtonModifier,
                                                imageResource = Res.drawable.mana_icon,
                                                text = "Counters",
                                                contentDescription = "Open P$playerNumber counters"
                                            ) {
                                                onAction(PlayerButtonAction.OpenCounters)
                                            }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = settingsButtonModifier,
                                                imageResource = Res.drawable.pencil_icon,
                                                text = "Customize",
                                                contentDescription = "Customize P$playerNumber"
                                            ) {
                                                onAction(PlayerButtonAction.OpenCustomization)
                                            }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = settingsButtonModifier,
                                                imageResource = Res.drawable.skull_icon,
                                                text = "KO Player",
                                                contentDescription = "KO P$playerNumber"
                                            ) {
                                                onAction(PlayerButtonAction.SetManualDeath(!state.player.setDead))
                                            }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = settingsButtonModifier, imageResource = Res.drawable.transparent, text = ""
                                            ) { }
                                        }
                                    }
                                }
                            }

                            PBState.COUNTERS_VIEW -> {
                                val playerNumber = state.player.playerNum
                                CounterWrapper(
                                    modifier = settingsModifier.fillMaxSize(), textColor = state.player.textColor, text = "Counters"
                                ) {
                                    BoxWithConstraints(Modifier.wrapContentSize()) {
                                        val padding = maxWidth / 30f
                                        LazyRow(
                                            modifier = Modifier.fillMaxSize().padding(vertical = padding),
                                            horizontalArrangement = Arrangement.spacedBy(padding),
                                        ) {
                                            itemsIndexed(state.player.activeCounters) { index, counterType ->
                                                val counterName = counterType.name
                                                Counter(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .aspectRatio(0.70f)
                                                        .then(if (index == 0) Modifier.padding(start = padding) else Modifier),
                                                    textColor = state.player.textColor,
                                                    iconResource = counterType.resource,
                                                    value = state.player.counters[counterType.ordinal],
                                                    valueContentDescription = "P$playerNumber $counterName counter ${state.player.counters[counterType.ordinal]}",
                                                    incrementContentDescription = "P$playerNumber increase $counterName counter",
                                                    decrementContentDescription = "P$playerNumber decrease $counterName counter",
                                                    onIncrement = {
                                                        onAction(PlayerButtonAction.ChangeCounter(counterType, 1))
                                                    },
                                                    onDecrement = {
                                                        onAction(PlayerButtonAction.ChangeCounter(counterType, -1))
                                                    },
                                                )
                                            }
                                            item {
                                                AddCounter(
                                                    modifier = Modifier.fillMaxHeight().aspectRatio(0.70f).padding(
                                                        top = dimensions.paddingMedium,
                                                        bottom = dimensions.paddingMedium,
                                                        end = padding
                                                    ),
                                                    textColor = state.player.textColor,
                                                    contentDescription = "Add P$playerNumber counter",
                                                ) {
                                                    onAction(PlayerButtonAction.OpenCounterSelection)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            PBState.COUNTERS_SELECT -> {
                                val playerNumber = state.player.playerNum
                                CounterWrapper(
                                    modifier = settingsModifier.fillMaxSize(), textColor = state.player.textColor, text = "Select Counters"
                                ) {
                                    BoxWithConstraints(Modifier.wrapContentSize()) {
                                        val padding = maxWidth / 50f + maxHeight / 60f
                                        Column(
                                            Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            LazyHorizontalGrid(
                                                modifier = Modifier.fillMaxSize().padding(padding).clip(RoundedCornerShape(8)),
                                                rows = GridCells.Fixed(3),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                items(CounterType.entries.toTypedArray()) { counterType ->
                                                    var selected by remember { mutableStateOf(counterType in state.player.activeCounters) }
                                                    val counterName = counterType.name
                                                    Box(modifier = Modifier.fillMaxSize().aspectRatio(1.0f).background(
                                                        if (selected) {
                                                            Color.Green.copy(alpha = 0.5f)
                                                        } else {
                                                            Color.Transparent
                                                        }
                                                    ).pointerInput(Unit) {
                                                        detectTapGestures {
                                                            selected = true
                                                            onAction(PlayerButtonAction.SetCounterActive(counterType, true))
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        }
                                                    }.semantics {
                                                        contentDescription = if (selected) {
                                                            "P$playerNumber $counterName counter selected"
                                                        } else {
                                                            "Add P$playerNumber $counterName counter"
                                                        }
                                                    }) {
                                                        SettingsButton(
                                                            imageVector = vectorResource(counterType.resource),
                                                            modifier = Modifier.fillMaxSize().padding(padding),
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
                            }
                        }
                    }
                }

                val settingsStateMargin = remember(Unit) { smallButtonSize / 7f }
                val commanderStateMargin = remember(Unit) { settingsStateMargin * 1.4f }

                @Composable
                fun BackButton(modifier: Modifier = Modifier) {
                    SettingsButton(
                        modifier = modifier.size(smallButtonSize * 1.1f).padding(
                            start = settingsStateMargin, bottom = settingsStateMargin
                        ),
                        backgroundColor = Color.Transparent,
                        mainColor = state.player.textColor,
                        visible = state.backButtonVisible,
                        imageVector = vectorResource(Res.drawable.back_icon),
                        contentDescription = "P${state.player.playerNum} back",
                        onPress = { onAction(PlayerButtonAction.PopBackStack) }
                    )
                }

                @Composable
                fun CommanderStateButton(modifier: Modifier = Modifier) {
                    val description = if (state.buttonState == PBState.COMMANDER_DEALER) {
                        "P${state.player.playerNum} is commander dealer"
                    } else {
                        "P${state.player.playerNum} commander mode"
                    }
                    PlayerStateButton(
                        modifier = modifier.padding(
                            start = commanderStateMargin,
                            bottom = commanderStateMargin,
                        ),
                        visible = commanderButtonVisible,
                        iconResource = Res.drawable.commander_solid_icon,
                        color = state.player.textColor,
                        size = smallButtonSize,
                        contentDescription = description,
                    ) {
                        onAction(PlayerButtonAction.ToggleCommanderDealer)
                    }
                }

                @Composable
                fun BackButtonOrCommanderButton(modifier: Modifier = Modifier) {
                    if (commanderButtonVisible) {
                        CommanderStateButton(modifier)
                    } else if (state.backButtonVisible) {
                        BackButton(modifier)
                    } else {
                        PlayerStateButton(
                            modifier = modifier.padding(
                                start = commanderStateMargin,
                                bottom = commanderStateMargin,
                            ), visible = false, iconResource = Res.drawable.commander_solid_icon, color = state.player.textColor, size = smallButtonSize
                        ) {}
                    }
                }

                @Composable
                fun SettingsStateButton(modifier: Modifier = Modifier) {
                    PlayerStateButton(
                        modifier = modifier.padding(
                            end = settingsStateMargin, bottom = settingsStateMargin
                        ),
                        visible = settingsButtonVisible,
                        iconResource = Res.drawable.settings_icon,
                        color = state.player.textColor,
                        size = smallButtonSize,
                        contentDescription = "P${state.player.playerNum} settings",
                    ) {
                        onAction(PlayerButtonAction.ToggleSettings)
                    }
                }

                if (wideButton) {
                    Row(
                        Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
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
                            Modifier.wrapContentHeight().fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                        ) {
                            BackButtonOrCommanderButton(Modifier.align(Alignment.Bottom))
                            SettingsStateButton(Modifier.align(Alignment.Bottom))
                        }
                    }
                }
                val timer = state.timer
                if (timer != null && state.buttonState == PBState.NORMAL) {
                    Timer(modifier = turnTimerModifier.then(Modifier.pointerInput(Unit) {
                        detectTapGestures(onPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            timerJustClicked = true
                        })
                    }), timer = timer)
                }
            }
        }
    }
}

private fun commanderDamageValue(
    state: PlayerSeatUiState,
    partner: Boolean
): NumberWithRecentChange {
    return when (val commanderState = state.commanderState) {
        is CommanderState.Active -> state.player.commanderDamage[commanderState.getDealerIndex(partner)]
        CommanderState.Inactive -> NumberWithRecentChange(0, 0)
    }
}

