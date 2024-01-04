package lifelinked.composable.lifecounter

import android.app.AlertDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hypeapps.lifelinked.R
import lifelinked.composable.SettingsButton
import lifelinked.composable.VerticalRotation
import lifelinked.composable.animatedBorderCard
import lifelinked.composable.bounceClick
import lifelinked.composable.dialog.ColorDialog
import lifelinked.composable.dialog.ScryfallSearchDialog
import lifelinked.composable.repeatingClickable
import lifelinked.composable.rotateVertically
import lifelinked.data.AppViewModel
import lifelinked.data.ImageManager
import lifelinked.data.Player
import lifelinked.data.SharedPreferencesManager
import lifelinked.ui.theme.allPlayerColors
import lifelinked.ui.theme.brightenColor
import lifelinked.ui.theme.deadDealerColorMatrix
import lifelinked.ui.theme.deadNormalColorMatrix
import lifelinked.ui.theme.deadReceiverColorMatrix
import lifelinked.ui.theme.deadSettingsColorMatrix
import lifelinked.ui.theme.dealerColorMatrix
import lifelinked.ui.theme.invert
import lifelinked.ui.theme.normalColorMatrix
import lifelinked.ui.theme.receiverColorMatrix
import lifelinked.ui.theme.saturateColor
import lifelinked.ui.theme.scaledSp
import lifelinked.ui.theme.settingsColorMatrix
import lifelinked.ui.theme.textShadowStyle
import java.lang.Float.min


enum class PlayerButtonState {
    NORMAL, COMMANDER_DEALER, COMMANDER_RECEIVER, SETTINGS
}

@Composable
fun PlayerButton(
    modifier: Modifier = Modifier, player: Player, initialState: PlayerButtonState = PlayerButtonState.NORMAL, rotation: Float = 0f
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val viewModel: AppViewModel = viewModel()
    val imageManager = ImageManager(context, player)
    var showScryfallSearch by remember { mutableStateOf(false) }
    val backStack = remember { mutableStateListOf<() -> Unit>() }
    val scryfallBackStack = remember {
        mutableStateListOf({
            showScryfallSearch = false
            viewModel.blurBackground.value = false
        })
    }
    val state = remember { mutableStateOf(initialState) }
    viewModel.registerButtonState(state)
    val commanderButtonVisible = state.value in listOf(PlayerButtonState.NORMAL, PlayerButtonState.COMMANDER_DEALER)
    val settingsButtonVisible = state.value in listOf(
        PlayerButtonState.NORMAL, PlayerButtonState.SETTINGS
    )

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { selectedUri ->
            val copiedUri = imageManager.copyImageToInternalStorage(selectedUri)
            player.imageUri = copiedUri
            SharedPreferencesManager.savePlayerPref(player)
        }
    }


    fun showWarningDialog() {
        if (viewModel.cameraRollDisabled) {
            val alertDialog = AlertDialog.Builder(context).setTitle("Info").setMessage("Camera roll access is disabled. Enable in settings.").setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }.create()

            alertDialog.show()
        } else {
            val alertDialog = AlertDialog.Builder(context).setTitle("Warning").setMessage("This will open the camera roll. Proceed?").setPositiveButton("Proceed") { _, _ ->
                launcher.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.create()

            alertDialog.show()
        }

    }

    LaunchedEffect(state.value) {
        if (state.value == PlayerButtonState.COMMANDER_RECEIVER) backStack.clear()
    }

    BoxWithConstraints(
        modifier = Modifier
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
        if (showScryfallSearch) {
            ScryfallSearchDialog(modifier = Modifier.onGloballyPositioned { _ ->
                viewModel.blurBackground.value = true
            }, onDismiss = {
                showScryfallSearch = false
                viewModel.blurBackground.value = false
            }, backStack = scryfallBackStack, player = player
            )
        }

        MonarchyIndicator(
            modifier = modifier, monarch = player.monarch
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .clip(RoundedCornerShape(30.dp)), contentAlignment = Alignment.Center
            ) {
                PlayerButtonBackground(player = player, state = state.value)


                val smallButtonSize = (maxWidth / 15f) + (maxHeight / 10f)
                val settingsStateMargin = smallButtonSize / 7f
                val commanderStateMargin = settingsStateMargin * 1.4f

                val wideButton = maxWidth / maxHeight > 1.4

                val playerInfoPadding = if (wideButton) Modifier.padding(bottom = smallButtonSize / 2) else Modifier.padding(
                    top = smallButtonSize / 2
                )

                val settingsPadding = if (wideButton) Modifier.padding(bottom = smallButtonSize / 4, top = smallButtonSize / 8) else Modifier.padding(
                    top = smallButtonSize / 2
                )
                if (!player.setDead) {
                    when (state.value) {
                        PlayerButtonState.NORMAL -> {
                            LifeChangeButtons(Modifier.fillMaxWidth(), onIncrementLife = {
                                player.incrementLife(1)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }, onDecrementLife = {
                                player.incrementLife(-1)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            })
                        }

                        PlayerButtonState.COMMANDER_RECEIVER -> {
                            Row(Modifier.fillMaxSize()) {
                                LifeChangeButtons(Modifier.then(if (viewModel.currentDealerIsPartnered()) Modifier.fillMaxWidth(0.5f) else Modifier.fillMaxWidth()), onIncrementLife = {
                                    viewModel.currentDealer?.incrementCommanderDamage(player, 1)
                                    player.incrementLife(-1)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }, onDecrementLife = {
                                    viewModel.currentDealer?.incrementCommanderDamage(player, -1)
                                    player.incrementLife(1)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                })
                                if (viewModel.currentDealerIsPartnered()) {
                                    LifeChangeButtons(Modifier.fillMaxWidth(), onIncrementLife = {
                                        viewModel.currentDealer?.incrementCommanderDamage(player, 1, true)
                                        player.incrementLife(-1)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }, onDecrementLife = {
                                        viewModel.currentDealer?.incrementCommanderDamage(player, -1, true)
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
                        modifier = playerInfoPadding
                            .align(Alignment.Center)
                            .size(smallButtonSize * 4)
                            .padding(top = maxHeight / 9f),
                        backgroundColor = Color.Transparent,
                        mainColor = player.textColor,
                        imageResource = painterResource(id = R.drawable.skull_icon),
                        enabled = false
                    )
                }

                @Composable
                fun PlayerButtonContent(modifier: Modifier = Modifier) {
                    Box(modifier.fillMaxSize()) {
                        when (state.value) {
                            PlayerButtonState.NORMAL -> {
                                if (player.setDead || (viewModel.autoKo && player.isDead)) {
                                    Skull()
                                } else {
                                    LifeNumber(
                                        modifier = playerInfoPadding.fillMaxSize(), player = player
                                    )
                                }

                            }

                            PlayerButtonState.COMMANDER_RECEIVER -> {
                                if (player.setDead || (viewModel.autoKo && player.isDead)) {
                                    Skull()
                                } else {
                                    CommanderDamageNumber(
                                        modifier = playerInfoPadding.fillMaxSize(), player = player, currentDealer = viewModel.currentDealer, partnerMode = viewModel.currentDealerIsPartnered()
                                    )
                                }

                            }

                            PlayerButtonState.COMMANDER_DEALER -> {
                                val iconId = if (viewModel.currentDealerIsPartnered()) R.drawable.sword_icon_double else R.drawable.sword_icon
                                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Spacer(modifier = Modifier.height(40.dp))
                                    Text(
                                        modifier = Modifier, text = "Deal damage with your commander", color = player.textColor, fontSize = 20.scaledSp, lineHeight = 20.scaledSp, textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    SettingsButton(modifier = Modifier.size(smallButtonSize),
                                        imageResource = painterResource(iconId),
                                        backgroundColor = Color.Transparent,
                                        mainColor = player.textColor,
                                        onPress = {
                                            player.partnerMode = !player.partnerMode
                                        })
                                    Text(
                                        modifier = Modifier.wrapContentSize(unbounded = true), text = "Toggle Partner Mode", color = player.textColor, fontSize = 10.scaledSp, textAlign = TextAlign.Center
                                    )
                                }

                            }

                            PlayerButtonState.SETTINGS -> {
                                SettingsMenu(modifier = settingsPadding,
                                    player = player,
                                    backStack = backStack,
                                    onMonarchyButtonClick = { viewModel.toggleMonarch(player) },
                                    onFromCameraRollButtonClick = { showWarningDialog() },
                                    closeSettingsMenu = { state.value = PlayerButtonState.NORMAL },
                                    onScryfallButtonClick = {
                                        showScryfallSearch = !showScryfallSearch
                                    })
                            }

                            else -> throw Exception("unsupported state")
                        }
                    }
                }

                @Composable
                fun BackButton(modifier: Modifier = Modifier) {
                    SettingsButton(modifier = modifier
                        .size(smallButtonSize * 1.1f)
                        .padding(
                            start = settingsStateMargin, bottom = settingsStateMargin
                        ),
                        backgroundColor = Color.Transparent,
                        mainColor = player.textColor,
                        visible = backStack.isNotEmpty(),
                        imageResource = painterResource(id = R.drawable.back_icon),
                        onPress = { backStack.removeLast().invoke() })
                }

                @Composable
                fun CommanderStateButton(modifier: Modifier = Modifier) {
                    PlayerStateButton(
                        modifier = modifier.padding(
                            start = commanderStateMargin,
                            bottom = commanderStateMargin,
                        ), visible = commanderButtonVisible, icon = painterResource(id = R.drawable.commander_solid_icon), color = player.textColor, size = smallButtonSize
                    ) {
                        state.value = when (state.value) {
                            PlayerButtonState.NORMAL -> {
                                viewModel.currentDealer = player
                                viewModel.updateAllStates(PlayerButtonState.COMMANDER_RECEIVER)
                                PlayerButtonState.COMMANDER_DEALER
                            }

                            PlayerButtonState.COMMANDER_DEALER -> {
                                viewModel.updateAllStates(PlayerButtonState.NORMAL)
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
                            ), visible = false, icon = painterResource(id = R.drawable.commander_solid_icon), color = player.textColor, size = smallButtonSize
                        ) {}
                    }
                }

                @Composable
                fun SettingsStateButton(modifier: Modifier = Modifier) {
                    PlayerStateButton(
                        modifier = modifier.padding(
                            end = settingsStateMargin, bottom = settingsStateMargin
                        ), visible = settingsButtonVisible, icon = painterResource(id = R.drawable.settings_icon), color = player.textColor, size = smallButtonSize
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
                            Modifier
                                .wrapContentHeight()
                                .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
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
    val viewModel: AppViewModel = viewModel()
    val context = LocalContext.current
    val duration = viewModel.correctAnimationDuration(7500, context)
    val colors = if (viewModel.getAnimationScale(context) != 0.0f) {
        listOf(
            Color.Transparent,
            Color(255, 191, 8),
            Color(255, 191, 8),
            Color(255, 191, 8),
        )
    } else {
        listOf(
            Color(255, 191, 8),
            Color(255, 191, 8),
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .then(
                if (monarch) {
                    Modifier
                        .animatedBorderCard(
                            shape = RoundedCornerShape(30.dp), borderWidth = width, colors = colors, animationDuration = duration
                        )
                        .clip(RoundedCornerShape(30.dp))
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
    modifier: Modifier = Modifier, player: Player, backStack: SnapshotStateList<() -> Unit>
) {
    var state by remember { mutableStateOf(CounterMenuState.DEFAULT) }
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(modifier.fillMaxSize()) {
        val smallPadding = maxHeight / 20f
        val smallTextSize = maxHeight.value.scaledSp / 12f
        Column(
            Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize(unbounded = true)
                    .padding(top = smallPadding, bottom = smallPadding / 2f),
                text = if (state == CounterMenuState.DEFAULT) "Counters" else "Select Counters",
                color = player.textColor,
                fontSize = smallTextSize,
                textAlign = TextAlign.Center
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .weight(0.5f)
                    .background(Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(25.dp))
                    .border(0.5.dp, player.textColor.copy(alpha = 0.9f), RoundedCornerShape(25.dp))
                    .clip(RoundedCornerShape(25.dp))
            ) {
                when (state) {
                    CounterMenuState.DEFAULT -> {
                        LazyRow(
                            Modifier
                                .fillMaxSize()
                                .padding(5.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            items(player.activeCounters) { counterType ->
                                Counter(player = player,
                                    icon = painterResource(id = counterType.resId),
                                    value = player.getCounterValue(counterType),
                                    onIncrement = { player.incrementCounterValue(counterType, 1) },
                                    onDecrement = { player.incrementCounterValue(counterType, -1) })
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
                        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                            LazyHorizontalGrid(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(5.dp)
                                    .clip(RoundedCornerShape(25.dp)),
                                rows = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.Center,
                                verticalArrangement = Arrangement.Center
                            ) {
                                items(CounterType.values()) { counterType ->
                                    Box(modifier = Modifier
                                        .fillMaxSize()
                                        .aspectRatio(1.0f)
                                        .padding(0.5.dp)
                                        .background(
                                            if (counterType in player.activeCounters) {
                                                Color.Green.copy(alpha = 0.5f)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .pointerInput(Unit) {
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
                                            imageResource = painterResource(id = counterType.resId),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(5.dp),
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
            .background(Color.Black.copy(0.2f), shape = RoundedCornerShape(20.dp))
            .border(0.5.dp, player.textColor.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
            .pointerInput(Unit) {
                detectTapGestures {
                    onTap()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }) {
        val iconSize = maxHeight / 2.5f
        SettingsButton(
            modifier = Modifier
                .align(Alignment.Center)
                .size(iconSize),
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
    player: Player, icon: Painter, value: Int, onIncrement: () -> Unit, onDecrement: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(
        Modifier
            .fillMaxHeight()
            .aspectRatio(0.70f)
            .padding(5.dp)
            .bounceClick(0.0125f)
            .background(Color.Black.copy(0.2f), shape = RoundedCornerShape(20.dp))
            .border(0.5.dp, player.textColor.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(30.dp))
    ) {
        val textSize = (maxHeight.value / 2.8f + maxWidth.value / 6f + 30).scaledSp / 1.5f
        val topPadding = maxHeight / 10f
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
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
            Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(topPadding))
            Text(
                text = value.toString(),
                color = player.textColor,
                fontSize = textSize,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.wrapContentSize(),
                style = textShadowStyle
            )
            SettingsButton(
                imageResource = icon,
                modifier = Modifier
                    .fillMaxSize(0.35f)
                    .aspectRatio(1.0f)
                    .padding(bottom = 15.dp),
                mainColor = player.textColor,
                backgroundColor = Color.Transparent,
                shadowEnabled = true,
                enabled = false
            )
        }
    }
}

@Composable
fun PlayerButtonBackground(player: Player, state: PlayerButtonState) {
    if (player.imageUri == null) {
        var c = when (state) {
            PlayerButtonState.NORMAL -> player.color
            PlayerButtonState.COMMANDER_RECEIVER -> player.color.saturateColor(0.2f).brightenColor(0.3f)

            PlayerButtonState.COMMANDER_DEALER -> player.color.saturateColor(0.5f).brightenColor(0.6f)

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
fun PlayerStateButton(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    visible: Boolean,
    icon: Painter,
    color: Color,
    onPress: () -> Unit,
) {
    SettingsButton(
        modifier = modifier.size(size), backgroundColor = Color.Transparent, mainColor = color, imageResource = icon, visible = visible, onPress = onPress
    )
}

@Composable
fun CommanderDamageNumber(
    modifier: Modifier = Modifier, player: Player, currentDealer: Player?, partnerMode: Boolean
) {
    BoxWithConstraints(modifier = modifier) {
        val dividerOffset = maxHeight / 12f

        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            SingleCommanderDamageNumber(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .then(if (partnerMode) Modifier.fillMaxWidth(0.5f) else Modifier.fillMaxWidth()),
                player = player,
                currentDealer = currentDealer,
                partner = false
            )
            if (partnerMode) {
                Divider(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .width(2.dp)
                        .offset(y = dividerOffset), color = player.textColor
                )

                SingleCommanderDamageNumber(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth(), player = player, currentDealer = currentDealer, partner = true
                )
            }
        }
    }
}

@Composable
fun SingleCommanderDamageNumber(
    modifier: Modifier = Modifier, player: Player, currentDealer: Player?, partner: Boolean = false
) {
    val iconID = R.drawable.commander_solid_icon

    NumericValue(modifier = modifier, player = player, iconID = iconID, getValue = { p -> p.getCommanderDamage(currentDealer!!, partner).toString() }, getRecentChangeText = { "" })
}


@Composable
fun LifeNumber(
    modifier: Modifier = Modifier, player: Player
) {
    val iconID = R.drawable.heart_solid_icon

    NumericValue(modifier = modifier,
        player = player,
        iconID = iconID,
        getValue = { p -> p.life.toString() },
        getRecentChangeText = { if (player.recentChange == 0) "" else if (player.recentChange > 0) "+${player.recentChange}" else "${player.recentChange}" })
}


@Composable
fun NumericValue(
    modifier: Modifier = Modifier, player: Player, iconID: Int, getValue: (Player) -> String, getRecentChangeText: (Player) -> String
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
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

        val recentChangeSize = (maxHeight/ 12.5f).value
        val recentChangeText = getRecentChangeText(player)

        val iconSize = maxHeight / 7f

        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .offset(y = (largeTextSize/ 32f).dp),
                text = player.name,
                color = player.textColor,
                fontSize = smallTextSize.scaledSp,
                textAlign = TextAlign.Center,
                style = textShadowStyle
            )
            Row(
                modifier = Modifier
                    .wrapContentSize(unbounded = true)
                    .offset(y = (-largeTextSize/7f).dp)
                    .padding(top = largeTextPadding), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier
                        .wrapContentHeight(unbounded = true),
                    text = largeText,
                    color = player.textColor,
                    fontSize = largeTextSize.scaledSp,
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
                    fontSize = recentChangeSize.scaledSp,
                    style = textShadowStyle
                )
            }
            SettingsButton(
                modifier = Modifier
                    .size(iconSize),
                backgroundColor = Color.Transparent,
                mainColor = player.textColor,
                imageResource = painterResource(id = iconID),
                enabled = false
            )
        }





    }
}


enum class SettingsState { Default, Customize, BackgroundColorPicker, TextColorPicker, ChangeName, LoadPlayer, Counters }

@Composable
fun SettingsMenu(
    modifier: Modifier = Modifier,
    player: Player,
    backStack: SnapshotStateList<() -> Unit>,
    onMonarchyButtonClick: () -> Unit,
    onFromCameraRollButtonClick: () -> Unit,
    onScryfallButtonClick: () -> Unit,
    closeSettingsMenu: () -> Unit
) {
    var state by remember { mutableStateOf(SettingsState.Default) }
    val viewModel: AppViewModel = viewModel()
    val context = LocalContext.current

    BoxWithConstraints(modifier.fillMaxSize()) {
        var settingsButtonSize = if (maxHeight / 2 * 3 < maxWidth) {
            maxHeight / 2
        } else {
            maxWidth / 3
        }
        settingsButtonSize = min(115F, settingsButtonSize.value).dp
        val smallPadding = settingsButtonSize / 10f
        val smallTextSize = maxHeight.value.scaledSp / 12f

        @Composable
        fun FormattedSettingsButton(imageResource: Painter, text: String, onPress: () -> Unit) {
            SettingsButton(
                Modifier.size(settingsButtonSize), imageResource = imageResource, text = text, onPress = onPress, mainColor = player.textColor, backgroundColor = Color.Transparent
            )
        }

        when (state) {
            SettingsState.Default -> {
                LazyHorizontalGrid(
                    modifier = Modifier.fillMaxSize(), rows = GridCells.Fixed(2), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.Center
                ) {
                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.monarchy_icon), text = "Monarchy"
                        ) { onMonarchyButtonClick() }
                    }
                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.download_icon), text = "Load Profile"
                        ) {
                            state = SettingsState.LoadPlayer
                            backStack.add { state = SettingsState.Default }
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.mana_icon), text = "Counters"
                        ) {
                            state = SettingsState.Counters
                            backStack.add { state = SettingsState.Default }
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.change_background_icon), text = "Customize"
                        ) {
                            state = SettingsState.Customize
                            backStack.add { state = SettingsState.Default }
                        }
                    }
                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.skull_icon), text = "KO Player"
                        ) {
                            player.setDead = !player.isDead
                            viewModel.toggleMonarch(player, false)
                            closeSettingsMenu()
                            backStack.clear()
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.change_name_icon), text = "Change Name"
                        ) {
                            state = SettingsState.ChangeName
                            backStack.add { state = SettingsState.Default }
                        }
                    }
                }
            }

            SettingsState.Counters -> {
                Counters(
                    modifier = modifier.padding(5.dp), player = player, backStack = backStack
                )
            }

            SettingsState.Customize -> {
                LazyHorizontalGrid(
                    modifier = Modifier.fillMaxSize(), rows = GridCells.Fixed(2), horizontalArrangement = Arrangement.Center, verticalArrangement = Arrangement.Center
                ) {
                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.color_picker_icon), text = "Solid Color"
                        ) {
                            state = SettingsState.BackgroundColorPicker
                            backStack.add { state = SettingsState.Customize }
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.text_icon), text = "Text Color"
                        ) {
                            state = SettingsState.TextColorPicker
                            backStack.add { state = SettingsState.Customize }
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.change_background_icon), text = "Camera Roll"
                        ) { onFromCameraRollButtonClick() }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.x_icon), text = "Remove Image"
                        ) {
                            player.imageUri = null
                            SharedPreferencesManager.savePlayerPref(player)
                        }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.search_icon), text = "Search Image"
                        ) { onScryfallButtonClick() }
                    }

                    item {
                        FormattedSettingsButton(
                            imageResource = painterResource(R.drawable.reset_icon), text = "Reset"
                        ) {
                            viewModel.resetPlayerPrefs(player)
                        }
                    }
                }
            }

            SettingsState.BackgroundColorPicker -> {
                ColorPicker(
                    Modifier
                        .wrapContentSize()
                        .align(Alignment.Center)
                        .padding(bottom = maxHeight / 5f), text = "Choose a Background Color", colorList = mutableListOf<Color>().apply {
                        add(Color.Black)
                        add(Color.White)
                        addAll(allPlayerColors)
                    }, player = player, initialColor = player.color
                ) { color ->
                    player.color = color
                    SharedPreferencesManager.savePlayerPref(player)
                }
            }

            SettingsState.TextColorPicker -> {
                ColorPicker(
                    Modifier
                        .wrapContentSize()
                        .align(Alignment.Center)
                        .padding(bottom = maxHeight / 5f), text = "Choose a Text Color", colorList = mutableListOf<Color>().apply {
                        add(Color.Black)
                        add(Color.White)
                        addAll(allPlayerColors)
                    }, player = player, initialColor = player.textColor
                ) { color ->
                    player.textColor = color
                    SharedPreferencesManager.savePlayerPref(player)
                }
            }


            SettingsState.LoadPlayer -> {
                val playerList = remember { mutableStateListOf<Player>() }

                DisposableEffect(context) {
                    playerList.addAll(SharedPreferencesManager.loadPlayerPrefs())
                    onDispose {}
                }

                Column(
                    modifier
                        .fillMaxSize()
                        .padding(bottom = smallPadding), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .wrapContentSize(unbounded = true)
                            .padding(top = smallPadding),
                        text = "Saved profiles",
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

                    Box(
                        Modifier
                            .fillMaxSize()
                            .weight(0.5f)
                    ) {
                        LazyHorizontalGrid(modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.15f))
                            .border(0.5.dp, player.textColor.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                            .padding(smallPadding),
                            rows = GridCells.Fixed(2),
                            state = rememberLazyGridState(),
                            horizontalArrangement = Arrangement.spacedBy(smallPadding),
                            verticalArrangement = Arrangement.spacedBy(smallPadding),
                            content = {
                                items(playerList) { p ->
                                    MiniPlayerButton(
                                        currPlayer = player, player = p, playerList = playerList
                                    )
                                }
                            })
                        if (playerList.isEmpty()) {
                            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .padding(horizontal = 20.dp)
                                        .padding(bottom = 5.dp),
                                    text = "No saved profiles found",
                                    color = player.textColor,
                                    fontSize = smallTextSize * 0.7f,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    modifier = Modifier
                                        .wrapContentSize()
                                        .padding(horizontal = 20.dp),
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
                    modifier = modifier, closeSettingsMenu = closeSettingsMenu, player = player
                )
            }

            else -> throw Exception("unsupported state")
        }
    }
}

@Composable
fun ColorPicker(modifier: Modifier = Modifier, text: String, colorList: List<Color>, player: Player, initialColor: Color, onPress: (Color) -> Unit) {
    BoxWithConstraints(modifier) {
        val colorPickerPadding = maxWidth / 200f
        val containerPadding = maxWidth / 50f
        val textPadding = maxHeight / 25f
        val smallTextSize = maxHeight.value.scaledSp / 8f
        Column(
            Modifier
                .wrapContentSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(bottom = textPadding, top = textPadding * 2),
                text = text,
                color = player.textColor,
                fontSize = smallTextSize / 1.25f,
                textAlign = TextAlign.Center
            )
            LazyHorizontalGrid(modifier = Modifier
                .wrapContentSize()
                .weight(0.5f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.15f))
                .border(0.5.dp, player.textColor.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                .padding(containerPadding * 2),
                rows = GridCells.Fixed(2),
                state = rememberLazyGridState(),
                horizontalArrangement = Arrangement.spacedBy(colorPickerPadding),
                verticalArrangement = Arrangement.spacedBy(colorPickerPadding),
                content = {
                    item {
                        CustomColorPickerButton(modifier = Modifier.padding(colorPickerPadding), player = player, initialColor = initialColor,
                            setColor = { color ->
                                onPress(color)
                            })
                    }
                    items(colorList) { color ->
                        ColorPickerButton(
                            modifier = Modifier.padding(colorPickerPadding), onClick = {
                                onPress(color)
                            }, color = color
                        )
                    }
                })
        }
    }
}

@Composable
fun ChangeNameField(
    modifier: Modifier = Modifier, closeSettingsMenu: () -> Unit, player: Player
) {
    var newName by remember { mutableStateOf(player.name) }
    val textColor = player.textColor.invert().copy(alpha = 1.0f)
    fun onDone() {
        player.name = newName
        closeSettingsMenu()
        SharedPreferencesManager.savePlayerPref(player)
    }
    BoxWithConstraints(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.wrapContentSize()
            ) {
                TextField(value = newName, onValueChange = { newName = it }, label = {
                    Text(
                        "New Name", color = player.color, fontSize = 12.scaledSp
                    )
                }, textStyle = TextStyle(fontSize = 15.scaledSp), singleLine = true, colors = TextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedContainerColor = player.textColor,
                    unfocusedContainerColor = player.textColor,
                    cursorColor = player.color,
                    unfocusedIndicatorColor = player.textColor,
                    focusedIndicatorColor = player.color
                ), keyboardOptions = KeyboardOptions.Default.copy(
                    autoCorrect = false, capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Done
                ), keyboardActions = KeyboardActions(onDone = { onDone() }), modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(80.dp)
                    .padding(top = 20.dp)
                    .padding(horizontal = 5.dp)
                )
                SettingsButton(
                    Modifier
                        .size(50.dp)
                        .align(Alignment.CenterEnd)
                        .padding(top = 20.dp, end = 5.dp),
                    imageResource = painterResource(id = R.drawable.enter_icon),
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
                    SharedPreferencesManager.savePlayerPref(player)
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = player.textColor, contentColor = player.color
                ), modifier = Modifier
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
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(modifier = Modifier
        .fillMaxHeight()
        .aspectRatio(2.5f)
        .clip(RoundedCornerShape(15.dp))
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                currPlayer.copySettings(player)
            }, onLongPress = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                SharedPreferencesManager.deletePlayerPref(player)
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
                modifier = Modifier.fillMaxSize(), painter = painter, contentScale = ContentScale.Crop, contentDescription = null
            )
        }
        Text(
            text = player.name, color = player.textColor, fontSize = maxHeight.value.scaledSp / 2f, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.align(Alignment.Center)
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
fun CustomColorPickerButton(modifier: Modifier = Modifier, player: Player, initialColor: Color, setColor: (Color) -> Unit) {
    val viewModel: AppViewModel = viewModel()
    var showColorDialog by remember { mutableStateOf(false) }

    if (showColorDialog) {
        ColorDialog(modifier = Modifier.fillMaxSize(), onDismiss = {
            showColorDialog = false
            viewModel.blurBackground.value = false
        }, initialColor = initialColor, setColor = { color ->
            setColor(color)
        })
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showColorDialog = true
                        viewModel.blurBackground.value = true
                    },
                )
            },
    ) {
        Icon(
            painter = painterResource(id = R.drawable.custom_color_icon), contentDescription = null, // provide a localized description if needed
            modifier = Modifier.fillMaxSize(), tint = player.textColor
        )
    }
}

@Composable
fun LifeChangeButtons(
    modifier: Modifier = Modifier, onIncrementLife: () -> Unit, onDecrementLife: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(modifier = modifier) {
        CustomIncrementButton(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f), onIncrementLife = onIncrementLife, interactionSource = interactionSource
        )

        CustomIncrementButton(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1.0f), onIncrementLife = onDecrementLife, interactionSource = interactionSource
        )
    }
}

@Composable
private fun CustomIncrementButton(
    modifier: Modifier = Modifier, onIncrementLife: () -> Unit = {}, interactionSource: MutableInteractionSource
) {
    Box(
        modifier = modifier.repeatingClickable(
            interactionSource = interactionSource, enabled = true, onPress = onIncrementLife
        )
    )

}

