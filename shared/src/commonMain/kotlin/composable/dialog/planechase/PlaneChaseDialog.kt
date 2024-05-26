package composable.dialog.planechase

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CutCornerShape
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
import coil3.compose.AsyncImage
import composable.dialog.ScryfallSearchBar
import composable.dialog.SettingsButton
import data.serializable.Card
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.back_icon_alt
import lifelinked.shared.generated.resources.chaos_icon
import lifelinked.shared.generated.resources.checkmark
import lifelinked.shared.generated.resources.deck_icon
import lifelinked.shared.generated.resources.invisible_icon
import lifelinked.shared.generated.resources.planeswalker_icon
import lifelinked.shared.generated.resources.question_icon
import lifelinked.shared.generated.resources.reset_icon
import lifelinked.shared.generated.resources.visible_icon
import lifelinked.shared.generated.resources.x_icon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.scaledSp

private enum class PlanarDieResult(
    val toString: String,
    val drawableResource: DrawableResource
) {
    PLANESWALK("Planeswalk", Res.drawable.planeswalker_icon), CHAOS("Chaos Ensues", Res.drawable.chaos_icon), NO_EFFECT("No Effect", Res.drawable.x_icon)
}

@Composable
fun PlaneChaseDialogContent(
    modifier: Modifier = Modifier,
    goToChoosePlanes: () -> Unit,
    viewModel: PlaneChaseViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var rotated by remember { mutableStateOf(false) }
    var planarDieResultVisible by remember { mutableStateOf(false) }
    var planarDieResult by remember { mutableStateOf(PlanarDieResult.NO_EFFECT) }

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
                    Spacer(modifier = Modifier.weight(0.65f))
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

    BoxWithConstraints(modifier = modifier) {
        val previewPadding = maxWidth / 20f
        val buttonModifier = Modifier.size(maxWidth / 6f).padding(bottom = maxHeight / 15f, top = 5.dp)
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.CenterHorizontally) {
            PlaneChaseCardPreview(
                modifier = Modifier.graphicsLayer { rotationZ = if (rotated) 180f else 0f }.fillMaxHeight(0.9f).padding(bottom = previewPadding),
                card = state.planarDeck.lastOrNull(),
                allowEnlarge = false
            )
            Row(
                Modifier.fillMaxWidth().wrapContentHeight(), horizontalArrangement = Arrangement.SpaceAround
            ) {
                SettingsButton(modifier = buttonModifier, text = "Previous", shadowEnabled = false, imageVector = vectorResource(Res.drawable.back_icon_alt), onPress = {
                    viewModel.backPlane()
                })
                SettingsButton(modifier = buttonModifier, text = "Flip Image", shadowEnabled = false, imageVector = vectorResource(Res.drawable.reset_icon), onPress = {
                    rotated = !rotated
                })
                SettingsButton(modifier = buttonModifier, text = "Planeswalk", shadowEnabled = false, imageVector = vectorResource(Res.drawable.planeswalker_icon), onPress = {
                    viewModel.planeswalk()
                })
                SettingsButton(modifier = buttonModifier, text = "Planar Die", shadowEnabled = false, imageVector = vectorResource(Res.drawable.chaos_icon), onPress = {
                    rollPlanarDie()
                })
                SettingsButton(modifier = buttonModifier, text = "Planar Deck", shadowEnabled = false, imageVector = vectorResource(Res.drawable.deck_icon), onPress = {
                    goToChoosePlanes()
                })
            }
            Spacer(Modifier.weight(1.0f))
        }
    }
}

@Composable
fun ChoosePlanesDialogContent(
    modifier: Modifier = Modifier,
    addToBackStack: (() -> Unit) -> Unit,
    viewModel: PlaneChaseViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    val filteredPlanes by derivedStateOf {
        state.searchedPlanes.filter { card -> state.planarDeck.contains(card) || !state.hideUnselected }
    }
    var backStackDiff by remember { mutableStateOf(0) }

    BoxWithConstraints(modifier = modifier) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        val buttonModifier = Modifier.size(maxWidth / 6f).padding(bottom = maxHeight / 20f)
        Column(Modifier.fillMaxSize()) {
            ScryfallSearchBar(
                Modifier.padding(top = 10.dp).padding(start = 20.dp, end = 20.dp).clip(RoundedCornerShape(10.dp)),
                query = state.query,
                onQueryChange = { viewModel.setQuery(it) },
                searchInProgress = state.searchInProgress,
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
                modifier = Modifier.fillMaxWidth(),
                text = "${state.planarDeck.intersect(filteredPlanes.toSet()).size}/${filteredPlanes.size} Planes Selected, ${state.allPlanes.size - filteredPlanes.size}/${state.allPlanes.size} Hidden",
                fontSize = 15.scaledSp,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize().padding(bottom = maxWidth / 15f).weight(0.5f).border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)),
                columns = GridCells.Fixed(2),
            ) {
                items(filteredPlanes, key = { card -> card.hashCode() }) { card ->
                    PlaneChaseCardPreview(modifier = Modifier.width(maxWidth / 2),
                        card = card,
                        onTap = {
                            if (!state.planarDeck.contains(card)) {
                                println("Adding ${card.name} to planar deck")
                                viewModel.selectPlane(card)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else {
                                println("Removing ${card.name} from planar deck")
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
                Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = maxWidth / 10f), horizontalArrangement = Arrangement.SpaceAround

            ) {
                SettingsButton(modifier = buttonModifier, text = "Select All", shadowEnabled = false, imageVector = vectorResource(Res.drawable.checkmark), onPress = {
                    viewModel.addAllPlanarDeck(filteredPlanes)
                })
                SettingsButton(modifier = buttonModifier, text = "Unselect All", shadowEnabled = false, imageVector = vectorResource(Res.drawable.x_icon), onPress = {
                    viewModel.removeAllPlanarDeck(filteredPlanes)
                })
                SettingsButton(modifier = buttonModifier,
                    text = if (!state.hideUnselected) "Hide Unselected" else "Show Unselected",
                    shadowEnabled = false,
                    imageVector = if (!state.hideUnselected) vectorResource(Res.drawable.invisible_icon) else vectorResource(Res.drawable.visible_icon),
                    onPress = {
                        viewModel.toggleHideUnselected()
                    })
            }
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
    selected: Boolean = false
) {
    var showLargeImage by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    if (showLargeImage && allowEnlarge) {
        Dialog(onDismissRequest = { showLargeImage = false }, properties = DialogProperties(
            usePlatformDefaultWidth = false
        ), content = {
            Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures(onTap = {
                    showLargeImage = false
                })
            }) {
                AsyncImage(
                    model = card!!.getUris().large, modifier = Modifier.clip(CutCornerShape(125.dp)).fillMaxSize(0.85f).align(Alignment.Center), contentDescription = ""
                )
            }

        })
    }

    if (card != null) {
        BoxWithConstraints(modifier = modifier.aspectRatio(5 / 7f).pointerInput(Unit) {
            detectTapGestures(onLongPress = {
                showLargeImage = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }, onPress = {
                onPress()
            }, onTap = {
                if (allowSelection) {
                    onTap()
                }
            })
        }) {
            val clipSize = maxWidth / 20f
            Box(
                Modifier.fillMaxSize().then(
                    if (selected) {
                        Modifier.background(Color.Green.copy(alpha = 0.2f))
                    } else {
                        Modifier
                    }
                )
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize().then(
                        if (selected) {
                            Modifier.padding(10.dp).clip(CutCornerShape(clipSize + 5.dp))
                        } else {
                            Modifier.padding(1.dp).clip(CutCornerShape(clipSize + 0.5f.dp))
                        }
                    ), model = card.getUris().normal, contentDescription = ""

                )
            }

        }
    } else {
        BoxWithConstraints(modifier = modifier.aspectRatio(5 / 7f).background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.2f), CutCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f), CutCornerShape(10.dp)).pointerInput(Unit) {}) {
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




