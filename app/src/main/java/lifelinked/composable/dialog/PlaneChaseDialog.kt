package lifelinked.composable.dialog

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hypeapps.lifelinked.R
import kotlinx.coroutines.launch
import lifelinked.composable.SettingsButton
import lifelinked.data.AppViewModel
import lifelinked.data.Card
import lifelinked.data.ScryfallApiRetriever
import lifelinked.data.SharedPreferencesManager
import lifelinked.ui.theme.scaledSp

private enum class PlanarDieResult(val toString: String, val resourceId: Int) {
    PLANESWALK("Planeswalk", R.drawable.planeswalker_icon), CHAOS("Chaos Ensues", R.drawable.chaos_icon), NO_EFFECT("No Effect", R.drawable.x_icon)
}

@Composable
fun PlaneChaseDialogContent(modifier: Modifier = Modifier, goToChoosePlanes: () -> Unit) {
    val viewModel: AppViewModel = viewModel()
    var rotated by remember { mutableStateOf(false) }
    var planarDieResultVisible by remember { mutableStateOf(false) }
    var planarDieResult by remember { mutableStateOf(PlanarDieResult.NO_EFFECT) }

    LaunchedEffect(viewModel.planarDeck) {
        if (viewModel.planarDeck.isNotEmpty()) {
            viewModel.planeBackStack.clear()
            viewModel.planarDeck.shuffle()
            viewModel.savePlanarDeck()
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
                card = viewModel.currentPlane(),
                allowEnlarge = false)
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(), horizontalArrangement = Arrangement.SpaceAround
            ) {
                SettingsButton(modifier = buttonModifier, text = "Back", shadowEnabled = false, imageResource = painterResource(R.drawable.back_icon_alt), onPress = {
                    viewModel.backPlane()
                })
                SettingsButton(modifier = buttonModifier, text = "Flip Image", shadowEnabled = false, imageResource = painterResource(R.drawable.reset_icon), onPress = {
                    rotated = !rotated
                })
                SettingsButton(modifier = buttonModifier, text = "Planeswalk", shadowEnabled = false, imageResource = painterResource(R.drawable.planeswalker_icon), onPress = {
                    viewModel.planeswalk()
                })
                SettingsButton(modifier = buttonModifier, text = "Planar Die", shadowEnabled = false, imageResource = painterResource(R.drawable.chaos_icon), onPress = {
                    rollPlanarDie()
                })
                SettingsButton(modifier = buttonModifier, text = "Planar Deck", shadowEnabled = false, imageResource = painterResource(R.drawable.deck_icon), onPress = {
                    goToChoosePlanes()
                })
            }
            Spacer(Modifier.weight(1.0f))
        }
    }
}

@Composable
fun ChoosePlanesDialogContent(modifier: Modifier = Modifier) {
    val viewModel: AppViewModel = viewModel()
    val query = remember { mutableStateOf("") }
    val allPlanes = remember {
        mutableStateListOf<Card>().apply {
            addAll(SharedPreferencesManager.loadAllPlanes())
        }
    }

    var hideUnselected by remember { mutableStateOf(false) }
    val filteredPlanes by remember { derivedStateOf { allPlanes.filter { card -> viewModel.planarDeck.contains(card) || !hideUnselected } } }

    val scryfallApiRetriever = ScryfallApiRetriever()
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    fun searchPlanes(qry: String) {
        coroutineScope.launch {
            focusManager.clearFocus()
            val newCards = scryfallApiRetriever.parseScryfallResponse<Card>(scryfallApiRetriever.searchScryfall(qry))
            newCards.forEach { card ->
                if (card !in allPlanes) {
                    allPlanes.add(card)
                }
            }
            SharedPreferencesManager.saveAllPlanes(allPlanes)
        }
    }

    LaunchedEffect(Unit) {
        searchPlanes("t:plane")
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
                    searchPlanes("t:plane ${query.value}")
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                })
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "${viewModel.planarDeck.size}/${filteredPlanes.size} Planes Selected",
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
                        modifier = Modifier.width(maxWidth / 2), card = card, allowSelection = true
                    )
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = maxWidth / 10f), horizontalArrangement = Arrangement.SpaceAround

            ) {
                SettingsButton(modifier = buttonModifier, text = "Select All", shadowEnabled = false, imageResource = painterResource(R.drawable.checkmark), onPress = {
                    viewModel.planarDeck.clear()
                    viewModel.planarDeck.addAll(filteredPlanes)
                })
                SettingsButton(modifier = buttonModifier, text = "Unselect All", shadowEnabled = false, imageResource = painterResource(R.drawable.x_icon), onPress = {
                    viewModel.planarDeck.clear()
                })
                SettingsButton(modifier = buttonModifier,
                    text = if (!hideUnselected) "Hide Unselected" else "Show Unselected",
                    shadowEnabled = false,
                    imageResource = if (!hideUnselected) painterResource(R.drawable.invisible_icon) else painterResource(R.drawable.visible_icon),
                    onPress = {
                        hideUnselected = !hideUnselected
                    })
            }
        }
    }
}

@Composable
fun PlaneChaseCardPreview(modifier: Modifier = Modifier, card: Card?, allowSelection: Boolean = false, allowEnlarge: Boolean = true) {
    val viewModel: AppViewModel = viewModel()
    var showLargeImage by remember { mutableStateOf(false) }
    val selected by remember { derivedStateOf { viewModel.planarDeck.contains(card) } }
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
                Image(
                    modifier = Modifier
                        .clip(CutCornerShape(125.dp))
                        .fillMaxSize(0.85f)
                        .align(Alignment.Center), painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current).data(card!!.getUris().large).crossfade(true).build()
                    ), contentDescription = null
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
                        if (!selected) {
                            viewModel.planarDeck.add(card)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else {
                            viewModel.planarDeck.remove(card)
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
                        if (selected && allowSelection) {
                            Modifier.background(Color.Green.copy(alpha = 0.2f))
                        } else {
                            Modifier
                        }
                    )
            ) {
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (selected && allowSelection) {
                                Modifier
                                    .padding(10.dp)
                                    .clip(CutCornerShape(clipSize + 5.dp))
                            } else {
                                Modifier
                                    .padding(1.dp)
                                    .clip(CutCornerShape(clipSize + 0.5f.dp))
                            }
                        ), painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current).data(card.getUris().normal).crossfade(true).build()
                    ), contentDescription = null
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
                    imageResource = painterResource(R.drawable.question_icon),
                    shadowEnabled = false,
                    enabled = false
                )
            }
        }
    }
}




