package ui.lifecounter.playerbutton


import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import domain.common.NumberWithRecentChange
import domain.game.CommanderState
import domain.game.timer.TurnTimer
import domain.system.SystemManager
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.add_icon
import lifelinked.shared.generated.resources.back_icon
import lifelinked.shared.generated.resources.commander_solid_icon
import lifelinked.shared.generated.resources.heart_solid_icon
import lifelinked.shared.generated.resources.image_error_icon
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
import theme.brightenColor
import theme.defaultTextStyle
import theme.ghostify
import theme.saturateColor
import theme.scaledSp
import theme.textShadowStyle
import ui.components.SettingsButton
import ui.dialog.customization.PlayerCustomizationDialog
import ui.lifecounter.CounterType
import ui.modifier.VerticalRotation
import ui.modifier.animatedBorderCard
import ui.modifier.bounceClick
import ui.modifier.repeatingClickable
import ui.modifier.rotateVertically
import kotlin.math.pow

@Composable
fun PlayerButton(
    modifier: Modifier = Modifier,
    viewModel: PlayerButtonViewModel,
    rotation: Float = 0f,
    turnTimerModifier: Modifier,
    setBlurBackground: (Boolean) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val isDead by viewModel.isDead.collectAsState()
    val commanderState by viewModel.commanderState.collectAsState()
    val currentDealerIsPartnered = (commanderState as? CommanderState.Active)?.dealer?.partnerMode == true
    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current

    val commanderButtonVisible by remember {
        derivedStateOf {
            state.buttonState in listOf(PBState.NORMAL, PBState.COMMANDER_DEALER)
        }
    }
    val settingsButtonVisible by remember {
        derivedStateOf {
            state.buttonState !in listOf(PBState.COMMANDER_DEALER, PBState.COMMANDER_RECEIVER, PBState.SELECT_FIRST_PLAYER)
        }
    }

    val backButtonVisible by viewModel.showBackButton.collectAsState()

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

    if (state.showCustomizeMenu && viewModel.customizationViewmodel != null) {
        PlayerCustomizationDialog(
            modifier = Modifier.fillMaxSize(), onDismiss = {
                viewModel.onShowCustomizeMenu(false)
            }, viewModel = viewModel.customizationViewmodel!!
        )
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
                    if ((state.buttonState == PBState.NORMAL || state.buttonState == PBState.COMMANDER_RECEIVER) && !timerJustClicked && !isDead) {
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
                    isDead = isDead,
                )

                val smallButtonSize = remember(Unit) { (maxWidth / 15f) + (maxHeight / 10f) }

                val wideButton = remember(Unit) { maxWidth / maxHeight > 1.4 }

                if (!state.player.setDead) {
                    when (state.buttonState) {
                        PBState.NORMAL -> {
                            LifeChangeButtons(Modifier.fillMaxWidth(), onIncrementLife = {
                                viewModel.incrementLife(1)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }, onDecrementLife = {
                                viewModel.incrementLife(-1)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            })
                        }

                        PBState.COMMANDER_RECEIVER -> {
                            Row(Modifier.fillMaxSize()) {
                                LifeChangeButtons(Modifier.then(if (currentDealerIsPartnered) Modifier.fillMaxWidth(0.5f) else Modifier.fillMaxWidth()), onIncrementLife = {
                                    viewModel.incrementCommanderDamage(
                                        value = 1,
                                        partner = false
                                    )
                                    viewModel.incrementLife(-1)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }, onDecrementLife = {
                                    viewModel.incrementCommanderDamage(
                                        value = -1,
                                        partner = false
                                    )
                                    viewModel.incrementLife(1)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                })
                                if (currentDealerIsPartnered) {
                                    LifeChangeButtons(Modifier.fillMaxWidth(), onIncrementLife = {
                                        viewModel.incrementCommanderDamage(
                                            value = 1,
                                            partner = true
                                        )
                                        viewModel.incrementLife(-1)
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }, onDecrementLife = {
                                        viewModel.incrementCommanderDamage(
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
                fun FormattedSettingsButton(modifier: Modifier, imageResource: DrawableResource, text: String, onPress: () -> Unit) {
                    SettingsButton(
                        modifier = modifier, imageVector = vectorResource(imageResource), text = text, onPress = onPress, mainColor = state.player.textColor, backgroundColor = Color.Transparent
                    )
                }

                @Composable
                fun Skull(modifier: Modifier = Modifier) {
                    SettingsButton(
                        modifier = modifier.align(Alignment.Center).size(smallButtonSize * 4).padding(top = maxHeight / 9f),
                        backgroundColor = Color.Transparent,
                        mainColor = state.player.textColor,
                        imageVector = vectorResource(Res.drawable.skull_icon),
                        enabled = false
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
                                if (isDead) {
                                    Skull(playerInfoModifier)
                                } else {
                                    LifeNumber(
                                        modifier = playerInfoModifier.fillMaxSize(),
                                        name = state.player.name,
                                        textColor = state.player.textColor,
                                        value = state.player.lifeTotal
                                    )
                                }
                            }

                            PBState.SELECT_FIRST_PLAYER -> {
                                Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                                    detectTapGestures(onPress = {
                                        viewModel.setFirstPlayer()
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
                                if (isDead) {
                                    Skull(playerInfoModifier)
                                } else {
                                    CommanderDamageNumber(
                                        modifier = playerInfoModifier.fillMaxSize(),
                                        name = state.player.name,
                                        textColor = state.player.textColor,
                                        firstValue = viewModel.getCommanderDamage(partner = false),
                                        secondValue = if (currentDealerIsPartnered) viewModel.getCommanderDamage(
                                            partner = true
                                        ) else null,
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
                                        onPress = {
                                            viewModel.togglePartnerMode(!state.player.partnerMode)
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
                                    val (settingsButtonSize, smallPadding, _) = remember { generateSizes(maxWidth, maxHeight) }
                                    val settingsButtonModifier = remember { Modifier.size(settingsButtonSize).padding(smallPadding / 2f) }
                                    LazyHorizontalGrid(
                                        modifier = Modifier.fillMaxSize(), rows = GridCells.Fixed(2), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.Center
                                    ) {
                                        item {
                                            FormattedSettingsButton(
                                                modifier = settingsButtonModifier, imageResource = Res.drawable.monarchy_icon, text = "Monarchy"
                                            ) { viewModel.onMonarchyButtonClicked(!state.player.monarch) }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = settingsButtonModifier, imageResource = Res.drawable.transparent, text = ""
                                            ) { }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = settingsButtonModifier, imageResource = Res.drawable.mana_icon, text = "Counters"
                                            ) {
                                                viewModel.onCountersButtonClicked()
                                            }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = settingsButtonModifier, imageResource = Res.drawable.pencil_icon, text = "Customize"
                                            ) {
                                                viewModel.onShowCustomizeMenu(true)
                                            }
                                        }
                                        item {
                                            FormattedSettingsButton(
                                                modifier = settingsButtonModifier, imageResource = Res.drawable.skull_icon, text = "KO Player"
                                            ) {
                                                viewModel.onKOButtonClicked()
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
                                                Counter(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .aspectRatio(0.70f)
                                                        .then(if (index == 0) Modifier.padding(start = padding) else Modifier),
                                                    textColor = state.player.textColor, iconResource = counterType.resource, value = viewModel.getCounterValue(counterType), onIncrement = {
                                                        viewModel.incrementCounterValue(
                                                            counterType, 1
                                                        )
                                                    }, onDecrement = {
                                                        viewModel.incrementCounterValue(
                                                            counterType, -1
                                                        )
                                                    })
                                            }
                                            item {
                                                AddCounter(
                                                    modifier = Modifier.fillMaxHeight().aspectRatio(0.70f).padding(
                                                        top = dimensions.paddingMedium,
                                                        bottom = dimensions.paddingMedium,
                                                        end = padding
                                                    ),
                                                    textColor = state.player.textColor,
                                                ) {
                                                    viewModel.onAddCounterButtonClicked()
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            PBState.COUNTERS_SELECT -> {
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
                                                    Box(modifier = Modifier.fillMaxSize().aspectRatio(1.0f).background(
                                                        if (selected) {
                                                            Color.Green.copy(alpha = 0.5f)
                                                        } else {
                                                            Color.Transparent
                                                        }
                                                    ).pointerInput(Unit) {
                                                        detectTapGestures {
                                                            selected = viewModel.setActiveCounter(counterType, true)
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                        visible = backButtonVisible,
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
                        ), visible = commanderButtonVisible, iconResource = Res.drawable.commander_solid_icon, color = state.player.textColor, size = smallButtonSize
                    ) {
                        viewModel.onCommanderButtonClicked()
                    }
                }

                @Composable
                fun BackButtonOrCommanderButton(modifier: Modifier = Modifier) {
                    if (commanderButtonVisible) {
                        CommanderStateButton(modifier)
                    } else if (backButtonVisible) {
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
                        ), visible = settingsButtonVisible, iconResource = Res.drawable.settings_icon, color = state.player.textColor, size = smallButtonSize
                    ) {
                        viewModel.onSettingsButtonClicked()
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
                if (state.timer != null && state.buttonState == PBState.NORMAL) {
                    Timer(modifier = turnTimerModifier.then(Modifier.pointerInput(Unit) {
                        detectTapGestures(onPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            timerJustClicked = true
                        })
                    }), timer = state.timer!!)
                }
            }
        }
    }
}

@Composable
fun MonarchyIndicator(
    modifier: Modifier = Modifier, monarch: Boolean = false, borderWidth: Dp, content: @Composable () -> Unit = {}
) {
    val duration = (7500 / SystemManager.getAnimationCorrectionFactor()).toInt()
    val colors = listOf(
        Color.Unspecified,
        Color(
            255, 191, 8
        ),
        Color(
            255, 191, 8
        ),
        Color(
            255, 191, 8
        ),
    )
    Box(
        modifier = modifier.then(
            if (monarch) {
                Modifier.animatedBorderCard(
                    shape = RoundedCornerShape(12), borderWidth = borderWidth, colors = colors, animationDuration = duration
                )
            } else {
                Modifier.padding(borderWidth)
            }
        )
    ) {
        content()
    }
}

@Composable
fun CounterWrapper(
    modifier: Modifier = Modifier, textColor: Color, text: String, content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val smallPadding = maxHeight / 20f
        val smallTextSize = maxHeight.value.scaledSp / 12f
        val dimensions = LocalDimensions.current
        Column(
            Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.wrapContentSize(unbounded = true).padding(
                    top = 0.dp, bottom = smallPadding / 4f
                ), text = text, color = textColor, fontSize = smallTextSize, textAlign = TextAlign.Center, style = defaultTextStyle()
            )
            Box(
                Modifier.fillMaxSize().background(
                    Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(12)
                ).border(
                    dimensions.borderThin, textColor.copy(alpha = 0.9f), RoundedCornerShape(12)
                ).clip(RoundedCornerShape(12))
            ) {
                content()
            }
        }
    }
}

@Composable
fun AddCounter(
    modifier: Modifier = Modifier,
    textColor: Color,
    onTap: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current
    BoxWithConstraints(modifier.bounceClick(0.0125f).background(
        Color.Black.copy(0.2f), shape = RoundedCornerShape(15)
    ).border(
        dimensions.borderThin, textColor.copy(alpha = 0.9f), RoundedCornerShape(15)
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
    modifier: Modifier = Modifier,
    textColor: Color, iconResource: DrawableResource, value: Int, onIncrement: () -> Unit, onDecrement: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current

    BoxWithConstraints(
        modifier.bounceClick(0.0125f).background(
            Color.Black.copy(0.2f), shape = RoundedCornerShape(15)
        ).border(
            dimensions.borderThin, textColor.copy(alpha = 0.9f), RoundedCornerShape(15)
        ).clip(RoundedCornerShape(15))
    ) {
        val textSize = (maxHeight.value / 2.8f + maxWidth.value / 6f + 30).scaledSp / 1.5f
        val topPadding = maxHeight / 10f
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
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
            Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally
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
                modifier = Modifier.fillMaxSize(0.35f).aspectRatio(1.0f).padding(bottom = topPadding * 0.85f),
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
    modifier: Modifier = Modifier, state: PBState, imageUri: String?, color: Color, isDead: Boolean, showError: Boolean = false
) {
    val dimensions = LocalDimensions.current

    var errored = false
    val c = remember(color, isDead, state) {
        val ghostify = isDead && state != PBState.SELECT_FIRST_PLAYER
        when {
            state == PBState.COMMANDER_RECEIVER && !ghostify -> color.saturateColor(0.2f).brightenColor(0.3f)
            state == PBState.COMMANDER_RECEIVER && ghostify -> color.saturateColor(0.2f).brightenColor(0.3f).ghostify()
            state == PBState.COMMANDER_DEALER && !ghostify -> color.saturateColor(0.5f).brightenColor(0.6f)
            state == PBState.COMMANDER_DEALER && ghostify -> color.saturateColor(0.5f).brightenColor(0.6f).ghostify()
            ghostify -> color.ghostify()
            else -> color
        }
    }
    Surface(
        modifier = modifier.fillMaxSize(), color = c
    ) {}
    if (imageUri != null) {
        KamelImage(
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            resource = { asyncPainterResource(data = imageUri) },
            contentDescription = "Player uploaded image",
            onLoading = { progress ->
                if (progress == 0.0f) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = dimensions.borderSmall,
                    )
                } else {
                    CircularProgressIndicator(
                        progress = { progress },
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = dimensions.borderSmall,
                    )
                }
            },
            onFailure = { error ->
                errored = true
                if (showError) {
                    Box(
                        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
                    ) {
                        SettingsButton(
                            modifier = Modifier.fillMaxHeight(0.4f),
                            backgroundColor = Color.Transparent,
                            mainColor = MaterialTheme.colorScheme.onPrimary,
                            imageVector = vectorResource(Res.drawable.image_error_icon),
                            visible = true,
                            onPress = {}
                        )
                    }
                }
            }
        )
        val b = remember(isDead, state) {
            val ghostify = isDead && state != PBState.SELECT_FIRST_PLAYER
            when {
                state == PBState.COMMANDER_RECEIVER && !ghostify -> Color.hsl(0f, 0f, 0.1f, 0.7f)
                state == PBState.COMMANDER_RECEIVER && ghostify -> Color.hsl(0f, 0f, 0.2f, 0.9f)
                state == PBState.COMMANDER_DEALER && !ghostify -> Color.hsl(0f, 0f, 0.0f, 0.7f)
                state == PBState.COMMANDER_DEALER && ghostify -> Color.hsl(0f, 0f, 0.1f, 0.9f)
                ghostify -> Color.Gray.copy(alpha = 0.7f)
                else -> Color.Transparent
            }
        }
        if (!errored) {
            Box(
                modifier = modifier.fillMaxSize().background(color = b), contentAlignment = Alignment.Center
            ) {}
        }
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
        modifier = modifier.size(size), backgroundColor = Color.Transparent, mainColor = color, imageVector = vectorResource(iconResource), visible = visible, onPress = onPress
    )
}

@Composable
fun CommanderDamageNumber(
    modifier: Modifier = Modifier,
    name: String,
    textColor: Color,
    firstValue: NumberWithRecentChange,
    secondValue: NumberWithRecentChange?,
) {
    BoxWithConstraints(modifier = modifier) {
        val dividerOffset = remember { maxHeight / 12f }
        val dimensions = LocalDimensions.current
        val numberWidth = remember { maxWidth * 0.4f }
        val padding = remember { maxWidth * 0.05f }

        if (secondValue != null) {
            VerticalDivider(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight(0.6f)
                    .width(dimensions.borderThin)
                    .offset(y = dividerOffset)
                    .alpha(0.4f),
                color = textColor
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.fillMaxHeight()
                    .then(
                        if (secondValue != null) {
                            Modifier.width(numberWidth).padding(end = padding * 2, start = padding)
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                SingleCommanderDamageNumber(
                    modifier = Modifier.then(
                        if (secondValue != null) {
                            Modifier.fillMaxSize().padding(padding)
                        } else {
                            Modifier.fillMaxSize()
                        }
                    ),
                    name = name,
                    textColor = textColor,
                    value = firstValue
                )
            }
            if (secondValue == null) return@BoxWithConstraints
            Box(
                modifier = Modifier.fillMaxHeight().width(numberWidth).padding(start = padding * 2, end = padding),
                contentAlignment = Alignment.Center
            ) {
                SingleCommanderDamageNumber(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    name = name,
                    textColor = textColor,
                    value = secondValue
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
    value: NumberWithRecentChange,
) {
    val iconResource = remember { Res.drawable.commander_solid_icon }

    NumericValue(
        modifier = modifier,
        iconResource = iconResource,
        name = name,
        textColor = textColor,
        value = value
    )
}

@Composable
fun LifeNumber(
    modifier: Modifier = Modifier,
    textColor: Color,
    name: String,
    value: NumberWithRecentChange
) {
    val iconResource = remember { Res.drawable.heart_solid_icon }

    NumericValue(
        modifier = modifier,
        textColor = textColor,
        name = name,
        value = value,
        iconResource = iconResource,
    )
}

@Composable
fun NumericValue(
    modifier: Modifier = Modifier,
    textColor: Color,
    name: String,
    value: NumberWithRecentChange,
    iconResource: DrawableResource,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val largeText = value.number.toString()
        val recentChangeText = if (value.recentChange == 0) ""
        else if (value.recentChange > 0) "+${value.recentChange}"
        else "${value.recentChange}"

        val aspectRatio = maxWidth / maxHeight
        val heightWeight = (1f / (1f + aspectRatio)).pow(1.3f)
        val widthWeight = (1f - heightWeight).pow(1.3f)

        val largeTextSize = (
                maxHeight.value / 3.5f * heightWeight +
                        maxWidth.value / 9f * widthWeight +
                        (maxHeight.value * maxWidth.value).pow(0.525f) / 4.15f +
                        10f
                )

        val smallTextSize = largeTextSize / 12f + 10f
        val smallTextPadding = largeTextSize.dp / 20f
        val recentChangeSize = 5f + largeTextSize / 6f
        val iconSize = (largeTextSize / 6f + 10f).dp

        Text(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = smallTextPadding * (3 - heightWeight.pow(2) * 2)),
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
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = smallTextPadding * 4)
                .wrapContentSize(unbounded = true),
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
                maxLines = 1,
                style = textShadowStyle()
            )
            Spacer(modifier = Modifier.weight(0.2f))
            Text(
                modifier = Modifier.weight(0.8f).padding(start = recentChangeSize.dp).wrapContentSize(unbounded = true),
                text = recentChangeText,
                color = textColor,
                fontSize = recentChangeSize.scaledSp,
                maxLines = 1,
                style = textShadowStyle()
            )
        }
        SettingsButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(iconSize)
                .offset(y = smallTextPadding * (1.5f + heightWeight.pow(2) * 2)),
            backgroundColor = Color.Transparent,
            mainColor = textColor,
            imageVector = vectorResource(iconResource),
            enabled = false
        )
    }
}

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

@Composable
private fun CustomIncrementButton(
    modifier: Modifier = Modifier, onIncrementLife: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val ripple = remember { ripple(color = Color.Black) }
    Box(
        modifier = modifier.repeatingClickable(
            interactionSource = interactionSource, indication = ripple, enabled = true, onPress = onIncrementLife
        )
    )
}


