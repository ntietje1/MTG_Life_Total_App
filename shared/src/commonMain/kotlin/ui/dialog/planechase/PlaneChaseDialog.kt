package ui.dialog.planechase

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import domain.system.NotificationManager
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.delay
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.back_icon_alt
import lifelinked.shared.generated.resources.card_back
import lifelinked.shared.generated.resources.chaos_icon
import lifelinked.shared.generated.resources.checkmark
import lifelinked.shared.generated.resources.deck_icon
import lifelinked.shared.generated.resources.die_icon
import lifelinked.shared.generated.resources.invisible_icon
import lifelinked.shared.generated.resources.planeswalker_icon
import lifelinked.shared.generated.resources.question_icon
import lifelinked.shared.generated.resources.reset_icon
import lifelinked.shared.generated.resources.visible_icon
import lifelinked.shared.generated.resources.x_icon
import model.card.Card
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.LocalDimensions
import theme.scaledSp
import ui.components.CARD_CORNER_PERCENT
import ui.components.CardImage
import ui.components.InfoButton
import ui.components.SearchTextField
import ui.components.SettingsButton
import ui.modifier.routePointerChangesTo

private enum class PlanarDieResult(
    val toString: String,
    val drawableResource: DrawableResource
) {
    PLANESWALK("Planeswalk", Res.drawable.planeswalker_icon), CHAOS("Chaos Ensues", Res.drawable.chaos_icon), NO_EFFECT("No Effect", Res.drawable.x_icon)
}

@Composable
fun PlaneChaseDialogContent( //TODO: add animations
    modifier: Modifier = Modifier,
    goToChoosePlanes: () -> Unit,
    goToPlanechaseTutorial: () -> Unit,
    notificationManager: NotificationManager = koinInject(),
    viewModel: PlaneChaseViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var rotated by remember { mutableStateOf(false) }
    var planarDieResultVisible by remember { mutableStateOf(false) }
    var planarDieResult by remember { mutableStateOf(PlanarDieResult.NO_EFFECT) }
    val dimensions = LocalDimensions.current
    val haptic = LocalHapticFeedback.current

    if (planarDieResultVisible) {
        Dialog(onDismissRequest = { planarDieResultVisible = false }, properties = DialogProperties(
            usePlatformDefaultWidth = false
        ), content = {
            Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures(onTap = {
                    planarDieResultVisible = false
                })
            }) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(0.8f))
                    Column(
                        modifier = Modifier.wrapContentSize().pointerInput(Unit) {
                            detectTapGestures {
                                if (planarDieResult == PlanarDieResult.PLANESWALK) {
                                    viewModel.planeswalk()
                                    planarDieResultVisible = false
                                }
                            }
                        },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SettingsButton(
                            modifier = Modifier.size(100.dp).padding(bottom = 20.dp),
                            textSizeMultiplier = 0.8f,
                            imageVector = vectorResource(planarDieResult.drawableResource),
                            shadowEnabled = false,
                            enabled = false
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                            text = planarDieResult.toString,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center,
                            fontSize = 35.scaledSp
                        )
                        if (planarDieResult == PlanarDieResult.PLANESWALK) {
                            Text(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                                text = "(Tap to planeswalk)",
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center,
                                fontSize = 15.scaledSp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1.0f))
                }
            }
        })
    }

    fun rollPlanarDie() {
        when ((1..6).random()) {
            1 -> {
                planarDieResult = PlanarDieResult.PLANESWALK
            }

            2 -> {
                planarDieResult = PlanarDieResult.CHAOS
            }

            3, 4, 5, 6 -> {
                planarDieResult = PlanarDieResult.NO_EFFECT
            }
        }
        planarDieResultVisible = true
    }

    BoxWithConstraints(modifier = modifier.padding(bottom = 20.dp)) {
        val buttonSize = remember(Unit) { maxWidth / 6f }
        val textSize = remember(Unit) { (maxWidth / 35f).value }
        InfoButton(
            modifier = Modifier.size(dimensions.infoButtonSize).align(Alignment.TopEnd).padding(end = dimensions.paddingLarge, top = dimensions.paddingLarge),
            onPress = goToPlanechaseTutorial
        )
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(dimensions.paddingTiny).weight(0.05f))
            Text(
                modifier = Modifier.fillMaxWidth().padding(top = dimensions.paddingSmall),
                text = "Planar deck size: ${state.planarDeck.size}",
                fontSize = textSize.scaledSp,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(dimensions.paddingTiny).weight(0.01f))
            PlaneChaseCardPreview(
                modifier = Modifier
                    .graphicsLayer { rotationZ = if (rotated) 180f else 0f }
                    .fillMaxHeight()
                    .weight(0.99f)
                    .padding(dimensions.paddingLarge)
                    .pointerInput(Unit) {
                        routePointerChangesTo(onLongPress = {
                            delay(500)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            rotated = !rotated
                        })
                    },
                card = state.planarDeck.lastOrNull(),
                allowEnlarge = false,
                showSelectedBackground = false
            )
            Spacer(Modifier.height(buttonSize / 2f))
            Row(
                Modifier.fillMaxWidth().height(buttonSize * 0.6f), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsButton(
                    modifier = Modifier.size(buttonSize).padding(bottom = buttonSize / 2f),
                    text = "Previous",
                    shadowEnabled = false,
                    imageVector = vectorResource(Res.drawable.back_icon_alt),
                    onPress = {
                        if (state.planarDeck.size == 0) {
                            notificationManager.showNotification("Select your planar deck first")
                        } else if (state.planarBackStack.size == 0) {
                            notificationManager.showNotification("Can't go back")
                        } else {
                            viewModel.backPlane()
                        }
                    },
                    mainColor = if (state.planarBackStack.size > 0 && state.planarDeck.size > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
                SettingsButton(
                    modifier = Modifier.size(buttonSize).padding(bottom = buttonSize / 2f),
                    text = "Flip Image",
                    shadowEnabled = false,
                    imageVector = vectorResource(Res.drawable.reset_icon),
                    onPress = {
                        if (state.planarDeck.size > 0) {
                            rotated = !rotated
                        } else {
                            notificationManager.showNotification("Select your planar deck first")
                        }
                    },
                    mainColor = if (state.planarDeck.size > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
                SettingsButton(
                    modifier = Modifier.size(buttonSize).padding(bottom = buttonSize / 2f),
                    text = "Planeswalk",
                    shadowEnabled = false,
                    imageVector = vectorResource(Res.drawable.planeswalker_icon),
                    onPress = {
                        if (state.planarDeck.size > 0) {
                            viewModel.planeswalk()
                        } else {
                            notificationManager.showNotification("Select your planar deck first")
                        }
                    },
                    mainColor = if (state.planarDeck.size > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
                SettingsButton(
                    modifier = Modifier.size(buttonSize).padding(bottom = buttonSize / 2f),
                    text = "Planar Die",
                    shadowEnabled = false,
                    imageVector = vectorResource(Res.drawable.die_icon),
                    onPress = {
                        if (state.planarDeck.size > 0) {
                            rollPlanarDie()
                        } else {
                            notificationManager.showNotification("Select your planar deck first")
                        }
                    },
                    mainColor = if (state.planarDeck.size > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
                SettingsButton(
                    modifier = Modifier.size(buttonSize).padding(bottom = buttonSize / 2f),
                    text = "Planar Deck",
                    shadowEnabled = false,
                    imageVector = vectorResource(Res.drawable.deck_icon),
                    onPress = {
                        goToChoosePlanes()
                    })
            }
            Spacer(Modifier.height(buttonSize / 5f))
        }
    }
}

@Composable
fun ChoosePlanesDialogContent(
    modifier: Modifier = Modifier,
    addToBackStack: (() -> Unit) -> Unit,
    popBackStack: () -> Unit,
    viewModel: PlaneChaseViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val dimensions = LocalDimensions.current

    val filteredPlanes by derivedStateOf {
        state.searchedPlanes.filter { card -> state.planarDeck.map { it.name }.contains(card.name) || !state.hideUnselected }
    }
    var backStackDiff by remember { mutableStateOf(0) }

    BoxWithConstraints(modifier = modifier) {
        val maxWidth = remember(Unit) { maxWidth }
        val maxHeight = remember(Unit) { maxHeight }
        val buttonSize = remember(Unit) { maxWidth / 6f }

        val searchBarHeight = remember(Unit) { maxWidth / 9f + 30.dp }
        val padding = remember(Unit) { searchBarHeight / 10f }
        val columnCount = remember(Unit) { if (maxWidth / 3 > maxHeight / 4) 3 else 2 }
        val textSize = remember(Unit) { (maxHeight / 50f).value }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SearchTextField(
                Modifier
                    .fillMaxWidth(0.9f)
                    .height(searchBarHeight)
                    .padding(top = padding)
                    .clip(RoundedCornerShape(15))
                    .border(
                        dimensions.borderThin, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(15)
                    ),
                query = state.query,
                onQueryChange = { viewModel.setQuery(it) },
                searchInProgress = state.searchInProgress
            ) {
                viewModel.searchPlanes {
                    focusManager.clearFocus()
                    if (backStackDiff == 0) {
                        backStackDiff += 1
                        addToBackStack {
                            backStackDiff -= 1
                            viewModel.onBackPress()
                        }
                    }
                }
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            Text(
                modifier = Modifier.fillMaxWidth().padding(vertical = dimensions.paddingTiny),
                text = "${state.planarDeck.intersect(filteredPlanes.toSet()).size}/${filteredPlanes.size} Planes Selected, ${state.allPlanes.size - filteredPlanes.size}/${state.allPlanes.size} Hidden",
                fontSize = textSize.scaledSp,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = maxWidth / 15f)
                    .weight(0.5f)
                    .border(dimensions.borderThin, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)),
                columns = GridCells.Fixed(columnCount),
                verticalArrangement = Arrangement.SpaceAround,
            ) {
                items(filteredPlanes, key = { card -> card.hashCode() }) { card ->
                    PlaneChaseCardPreview(
                        modifier = Modifier.width(maxWidth / 2),
                        card = card,
                        onTap = {
                            if (!state.planarDeck.contains(card)) {
                                viewModel.selectPlane(card)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else {
                                viewModel.deselectPlane(card)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        },
                        onPress = focusManager::clearFocus,
                        allowSelection = true,
                        selected = card in state.planarDeck
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().height(buttonSize * 0.6f).padding(horizontal = maxWidth / 10f),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically

            ) {
                SettingsButton(modifier = Modifier.size(buttonSize).padding(bottom = buttonSize / 4f),
                    text = "Select All", shadowEnabled = false, imageVector = vectorResource(Res.drawable.deck_icon), onPress = {
                        viewModel.addAllPlanarDeck(filteredPlanes)
                    })
                SettingsButton(modifier = Modifier.size(buttonSize).padding(bottom = buttonSize / 4f),
                    text = "Unselect All", shadowEnabled = false, imageVector = vectorResource(Res.drawable.x_icon), onPress = {
                        viewModel.removeAllPlanarDeck(filteredPlanes)
                    })
                SettingsButton(modifier = Modifier.size(buttonSize).padding(bottom = buttonSize / 4f),
                    text = if (!state.hideUnselected) "Hide Unselected" else "Show Unselected",
                    shadowEnabled = false,
                    imageVector = if (!state.hideUnselected) vectorResource(Res.drawable.invisible_icon) else vectorResource(Res.drawable.visible_icon),
                    onPress = {
                        viewModel.toggleHideUnselected()
                    })
                SettingsButton(modifier = Modifier.size(buttonSize).padding(bottom = buttonSize / 4f),
                    text = "Done",
                    shadowEnabled = false,
                    imageVector = vectorResource(Res.drawable.checkmark),
                    onPress = {
                        if (backStackDiff != 0) {
                            popBackStack()
                        }
                        popBackStack()
                    }
                )
            }
            Spacer(Modifier.height(buttonSize / 5f))
        }
    }
}

@Composable
fun PlaneChaseCardPreview(
    modifier: Modifier = Modifier,
    card: Card?,
    allowSelection: Boolean = false,
    onPress: () -> Unit = {},
    onTap: () -> Unit = {},
    allowEnlarge: Boolean = true,
    selected: Boolean = false,
    showSelectedBackground: Boolean = true
) {
    var showLargeImage by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current

    var longPressed by remember { mutableStateOf(false) }
    var rotated by remember { mutableStateOf(false) }

    if (showLargeImage && allowEnlarge) {
        Dialog(onDismissRequest = { showLargeImage = false }, properties = DialogProperties(
            usePlatformDefaultWidth = false
        ), content = {
            Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                routePointerChangesTo(
                    onUp = {
                        if (!longPressed) {
                            showLargeImage = false
                        }
                        longPressed = false
                    },
                    onLongPress = {
                        delay(500)
                        longPressed = true
                        rotated = !rotated
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                )
            }) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize().padding(dimensions.paddingLarge)
                ) {
                    val cardAspectRatio = 5f / 7f // Standard card ratio
                    val screenAspectRatio = maxWidth / maxHeight

                    val largeImageModifier = if (screenAspectRatio > cardAspectRatio) {
                        // Screen is wider than card - constrain by height
                        Modifier.fillMaxHeight().aspectRatio(cardAspectRatio)
                    } else {
                        // Screen is taller than card - constrain by width
                        Modifier.fillMaxWidth().aspectRatio(cardAspectRatio)
                    }

                    CardImage(
                        modifier = largeImageModifier
                            .clip(RoundedCornerShape(CARD_CORNER_PERCENT))
                            .align(Alignment.Center)
                            .graphicsLayer { rotationZ = if (rotated) 180f else 0f },
                        painter = asyncPainterResource(card!!.getUris().large),
                        placeholderPainter = painterResource(Res.drawable.card_back)
                    )
                }
            }
        })
    }

    if (card != null) {
        BoxWithConstraints(modifier = modifier.aspectRatio(5 / 7f).pointerInput(Unit) {
            detectTapGestures(onLongPress = {
                if (allowEnlarge) {
                    showLargeImage = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }, onPress = {
                onPress()
            }, onTap = {
                if (allowSelection) {
                    onTap()
                }
            })
        }) {
            Box(
                Modifier.fillMaxSize().then(
                    if (!showSelectedBackground) {
                        Modifier
                    } else if (selected) {
                        Modifier.background(Color.Green.copy(alpha = 0.2f))
                    } else {
                        Modifier.background(Color.Red.copy(alpha = 0.1f))
                    }
                )
            ) {
                CardImage(
                    modifier = Modifier.fillMaxSize().then(
                        if (selected) {
                            Modifier.padding(dimensions.paddingLarge).clip(RoundedCornerShape(CARD_CORNER_PERCENT))
                        } else {
                            Modifier.padding(dimensions.paddingSmall).clip(RoundedCornerShape(CARD_CORNER_PERCENT))
                        }
                    ), imageUri = card.getUris().normal
                )
            }

        }
    } else {
        BoxWithConstraints(modifier = modifier.aspectRatio(5 / 7f).background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.2f), RoundedCornerShape(CARD_CORNER_PERCENT))
            .border(dimensions.borderThin, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f), RoundedCornerShape(CARD_CORNER_PERCENT)).pointerInput(Unit) {}) {
            val iconPadding = maxWidth / 4f
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                SettingsButton(
                    modifier = Modifier.fillMaxSize().padding(iconPadding),
                    text = "No Planes Selected",
                    textSizeMultiplier = 0.8f,
                    imageVector = vectorResource(Res.drawable.question_icon),
                    shadowEnabled = false,
                    enabled = false
                )
            }
        }
    }
}




