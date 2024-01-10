package composable.dialog

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import composable.lifecounter.LifeCounterComponent
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.http.Url
import kotlinx.coroutines.launch
import data.ScryfallApiRetriever
import data.SettingsManager
import data.SettingsManager.savePlanarDeck
import data.serializable.Card
import theme.scaledSp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

/**
 * The possible outcomes of a planar die roll
 * @param toString the string representation of the outcome
 * @param resourceId the resource id of the icon for the outcome
 */
private enum class PlanarDieResult(val toString: String, val resourceId: String) {
    PLANESWALK("Planeswalk", "planeswalker_icon.xml"), CHAOS("Chaos Ensues", "chaos_icon.xml"), NO_EFFECT("No Effect", "x_icon.xml")
}

/**
 * A dialog that allows the user to view and interact with the planar deck
 * @param modifier the modifier for this composable
 * @param component the LifeCounterComponent that this dialog is for
 * @param goToChoosePlanes the callback to switch to the choose planes dialog
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun PlaneChaseDialogContent(modifier: Modifier = Modifier, component: LifeCounterComponent, goToChoosePlanes: () -> Unit) {
    var rotated by remember { mutableStateOf(false) }
    var planarDieResultVisible by remember { mutableStateOf(false) }
    var planarDieResult by remember { mutableStateOf(PlanarDieResult.NO_EFFECT) }

    LaunchedEffect(component.planarDeck) {
        if (component.planarDeck.isNotEmpty()) {
            component.planeBackStack.clear()
            component.planarDeck.shuffle()
            savePlanarDeck(component.planarDeck)
        }
    }

    if (planarDieResultVisible) {
        Dialog(onDismissRequest = { planarDieResultVisible = false }, properties = DialogProperties(
            usePlatformDefaultWidth = false
        ), content = {
            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        planarDieResultVisible = false
                    })
                }) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(0.65f))
                    SettingsButton(
                        modifier = Modifier
                            .size(100.dp)
                            .padding(bottom = 20.dp),
                        textSizeMultiplier = 0.8f,
                        imageResource = painterResource(planarDieResult.resourceId),
                        shadowEnabled = false,
                        enabled = false
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
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
        val buttonModifier = Modifier
            .size(maxWidth / 6f)
            .padding(bottom = maxHeight / 15f, top = 5.dp)
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.CenterHorizontally) {
            PlaneChaseCardPreview(modifier = Modifier
                .graphicsLayer { rotationZ = if (rotated) 180f else 0f }
                .fillMaxHeight(0.9f)
                .padding(bottom = previewPadding),
                card = component.currentPlane(),
                allowEnlarge = false)
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(), horizontalArrangement = Arrangement.SpaceAround
            ) {
                SettingsButton(modifier = buttonModifier, text = "Previous", shadowEnabled = false, imageResource = painterResource("back_icon_alt.xml"), onPress = {
                    component.backPlane()
                })
                SettingsButton(modifier = buttonModifier, text = "Flip Image", shadowEnabled = false, imageResource = painterResource("reset_icon.xml"), onPress = {
                    rotated = !rotated
                })
                SettingsButton(modifier = buttonModifier, text = "Planeswalk", shadowEnabled = false, imageResource = painterResource("planeswalker_icon.xml"), onPress = {
                    component.planeswalk()
                })
                SettingsButton(modifier = buttonModifier, text = "Planar Die", shadowEnabled = false, imageResource = painterResource("chaos_icon.xml"), onPress = {
                    rollPlanarDie()
                })
                SettingsButton(modifier = buttonModifier, text = "Planar Deck", shadowEnabled = false, imageResource = painterResource("deck_icon.xml"), onPress = {
                    goToChoosePlanes()
                })
            }
            Spacer(Modifier.weight(1.0f))
        }
    }
}

/**
 * A dialog that allows the user to choose which planes are in the planar deck
 * @param modifier the modifier for this composable
 * @param planarDeck the list of planes in the planar deck
 * @param backStack the back stack of the dialog
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun ChoosePlanesDialogContent(modifier: Modifier = Modifier, planarDeck: SnapshotStateList<Card>, backStack: SnapshotStateList<() -> Unit>) {
    val initialBackStackSize by remember { mutableStateOf(backStack.size) }
    val query = remember { mutableStateOf("") }
    val allPlanes = remember {
        mutableStateListOf<Card>().apply {
            addAll(SettingsManager.loadAllPlanes())
        }
    }
    val searchedPlanes = remember {
        mutableStateListOf<Card>()
    }

    var hideUnselected by remember { mutableStateOf(false) }
    val filteredPlanes by remember { derivedStateOf {
        if (searchedPlanes.isNotEmpty()) {
            searchedPlanes.filter { card -> planarDeck.contains(card) || !hideUnselected }
        } else {
            allPlanes.filter { card -> planarDeck.contains(card) || !hideUnselected }
        }
    }
    }

    val scryfallApiRetriever = ScryfallApiRetriever()
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    fun searchPlanes(qry: String, onResult: (List<Card>) -> Unit) {
        coroutineScope.launch {
            focusManager.clearFocus()
            val newCards = scryfallApiRetriever.parseScryfallResponse<Card>(scryfallApiRetriever.searchScryfall(qry))
            onResult(newCards)
        }
    }

    LaunchedEffect(Unit) {
        searchPlanes("t:plane") { result ->
            result.forEach { card ->
                if (card !in allPlanes) {
                    allPlanes.add(card)
                }
            }
            SettingsManager.saveAllPlanes(allPlanes)
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        val buttonModifier = Modifier
            .size(maxWidth / 6f)
            .padding(bottom = maxHeight / 20f)
        Column(Modifier.fillMaxSize()) {
            ScryfallSearchBar(
                Modifier
                    .padding(top = 10.dp)
                    .padding(start = 20.dp, end = 20.dp)
                    .clip(RoundedCornerShape(10.dp)), query = query, onSearch = {
                    searchPlanes("t:plane ${query.value}") { result ->
                        searchedPlanes.clear()
                        searchedPlanes.addAll(result)
                        if (initialBackStackSize == backStack.size) {
                            backStack.add {
                                query.value = ""
                                searchedPlanes.clear()
                            }
                        }
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                })
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "${planarDeck.size}/${filteredPlanes.size} Planes Selected (${allPlanes.size - filteredPlanes.size} Hidden)",
                fontSize = 15.scaledSp,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = maxWidth / 15f)
                    .weight(0.5f)
                    .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)), columns = GridCells.Fixed(2)
            ) {
                items(filteredPlanes) { card ->
                    PlaneChaseCardPreview(
                        modifier = Modifier.width(maxWidth / 2),
                        card = card,
                        addToPlanarDeck = { planarDeck.add(it) },
                        removeFromPlanarDeck = { planarDeck.remove(it) },
                        allowSelection = true,
                        selected ={ planarDeck.contains(card) }
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = maxWidth / 10f), horizontalArrangement = Arrangement.SpaceAround

            ) {
                SettingsButton(modifier = buttonModifier, text = "Select All", shadowEnabled = false, imageResource = painterResource("checkmark.xml"), onPress = {
                    planarDeck.clear()
                    planarDeck.addAll(filteredPlanes)
                })
                SettingsButton(modifier = buttonModifier, text = "Unselect All", shadowEnabled = false, imageResource = painterResource("x_icon.xml"), onPress = {
                    planarDeck.clear()
                })
                SettingsButton(modifier = buttonModifier,
                    text = if (!hideUnselected) "Hide Unselected" else "Show Unselected",
                    shadowEnabled = false,
                    imageResource = if (!hideUnselected) painterResource("invisible_icon.xml") else painterResource("visible_icon.xml"),
                    onPress = {
                        hideUnselected = !hideUnselected
                    })
            }
        }
    }
}

/**
 * A composable for showing a plane card (or lack thereof)
 * @param modifier the modifier for this composable
 * @param card the card to show
 * @param allowSelection whether the card can be selected
 * @param addToPlanarDeck the action to perform when the card is selected
 * @param removeFromPlanarDeck the action to perform when the card is unselected
 * @param allowEnlarge whether the card can be enlarged to preview the full image
 * @param selected callback to check whether the card is selected
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun PlaneChaseCardPreview(
    modifier: Modifier = Modifier,
    card: Card?,
    allowSelection: Boolean = false,
    addToPlanarDeck: (Card) -> Unit = {},
    removeFromPlanarDeck: (Card) -> Unit = {},
    allowEnlarge: Boolean = true,
    selected: () -> Boolean = { false }
) {
    var showLargeImage by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    if (showLargeImage && allowEnlarge) {
        Dialog(onDismissRequest = { showLargeImage = false }, properties = DialogProperties(
            usePlatformDefaultWidth = false
        ), content = {
            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        showLargeImage = false
                    })
                }) {
                KamelImage(
                    modifier = Modifier
                        .clip(CutCornerShape(125.dp))
                        .fillMaxSize(0.85f)
                        .align(Alignment.Center),
                    onLoading = { progress -> CircularProgressIndicator(progress, color = MaterialTheme.colorScheme.onPrimary) },
                    resource = asyncPainterResource(Url(card!!.getUris().large)),
                    contentDescription = null
                )
            }

        })
    }

    if (card != null) {
        BoxWithConstraints(modifier = modifier
            .aspectRatio(5 / 7f)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                    showLargeImage = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }, onTap = {
                    if (allowSelection) {
                        if (!selected.invoke()) {
                            addToPlanarDeck(card)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else {
                            removeFromPlanarDeck(card)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                })
            }) {
            val clipSize = maxWidth / 20f
            Box(
                Modifier
                    .fillMaxSize()
                    .then(
                        if (selected.invoke() && allowSelection) {
                            Modifier.background(Color.Green.copy(alpha = 0.2f))
                        } else {
                            Modifier
                        }
                    )
            ) {
                KamelImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (selected.invoke() && allowSelection) {
                                Modifier
                                    .padding(10.dp)
                                    .clip(CutCornerShape(clipSize + 5.dp))
                            } else {
                                Modifier
                                    .padding(1.dp)
                                    .clip(CutCornerShape(clipSize + 0.5f.dp))
                            }
                        ),
                    onLoading = { progress -> CircularProgressIndicator(progress, color = MaterialTheme.colorScheme.onPrimary) },
                    resource = asyncPainterResource(Url(card.getUris().normal)),
                    contentDescription = null
                )
            }

        }
    } else {
        BoxWithConstraints(modifier = modifier
            .aspectRatio(5 / 7f)
            .background(color = MaterialTheme.colorScheme.background.copy(alpha = 0.2f), CutCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f), CutCornerShape(10.dp))
            .pointerInput(Unit) {}) {
            val iconPadding = maxWidth / 4f
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                SettingsButton(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(iconPadding),
                    text = "No Planes Selected",
                    textSizeMultiplier = 0.8f,
                    imageResource = painterResource("question_icon.xml"),
                    shadowEnabled = false,
                    enabled = false
                )
            }
        }
    }
}




