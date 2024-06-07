package composable.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import data.ScryfallApiRetriever
import data.serializable.Card
import data.serializable.Ruling
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.search_icon
import org.jetbrains.compose.resources.vectorResource
import theme.scaledSp

@Composable
fun ScryfallSearchDialog(
    modifier: Modifier = Modifier,
    addToBackStack: (() -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onImageSelected: (String) -> Unit
) {
    SettingsDialog(modifier = modifier, backButtonEnabled = false, onDismiss = onDismiss) {
        ScryfallDialogContent(
            modifier = Modifier.fillMaxSize(),
            addToBackStack = addToBackStack,
            onImageSelected = onImageSelected
        )
    }
}

@Composable
fun ScryfallDialogContent(
    modifier: Modifier = Modifier,
    addToBackStack: (() -> Unit) -> Unit,
    selectButtonEnabled: Boolean = true,
    printingsButtonEnabled: Boolean = true,
    rulingsButtonEnabled: Boolean = false,
    onImageSelected: (String) -> Unit
) {
    val query = remember { mutableStateOf("") }
    var cardResults by remember { mutableStateOf(listOf<Card>()) }
    var rulingsResults by remember { mutableStateOf(listOf<Ruling>()) }
    val scryfallApiRetriever = ScryfallApiRetriever()
    val coroutineScope = rememberCoroutineScope()
    var lastSearchWasError by remember { mutableStateOf(false) }
    var rulingCard: Card? by remember { mutableStateOf(null) }
    var backStackDiff by remember { mutableStateOf(0) }
    var _printingsButtonEnabled by remember { mutableStateOf(printingsButtonEnabled) }

    var isSearchInProgress by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    fun clearResults() {
        cardResults = listOf()
        rulingsResults = listOf()
    }

    fun searchCards(qry: String, disablePrintingsButton: Boolean = false) {
        coroutineScope.launch {
            clearResults()
            focusManager.clearFocus()
            isSearchInProgress = true
            cardResults = scryfallApiRetriever.parseScryfallResponse<Card>(scryfallApiRetriever.searchScryfall(qry))
            lastSearchWasError = cardResults.isEmpty()
            _printingsButtonEnabled = !disablePrintingsButton
            isSearchInProgress = false

            if (backStackDiff == 0) {
                backStackDiff += 1
                addToBackStack {
                    backStackDiff -= 1
                    query.value = ""
                    clearResults()
                }
            }
        }
    }

    fun searchRulings(qry: String) {
        coroutineScope.launch {
            clearResults()
            isSearchInProgress = true
            rulingsResults = scryfallApiRetriever.parseScryfallResponse<Ruling>(scryfallApiRetriever.searchScryfall(qry))
            lastSearchWasError = false
            isSearchInProgress = false
            if (backStackDiff == 0) {
                backStackDiff += 1
                addToBackStack {
                    backStackDiff -= 1
                    query.value = ""
                    clearResults()
                }
            }
        }
    }

    BoxWithConstraints(Modifier.wrapContentSize()) {
        val searchBarHeight = maxWidth / 9f + 30.dp
        val padding = searchBarHeight / 10f
        val textSize = (maxHeight / 50f).value.scaledSp
//        val textSize = maxWidth / 20f
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ScryfallSearchBar(
                Modifier
                    .fillMaxWidth(0.9f)
                    .height(searchBarHeight)
                    .padding(top = padding)
                    .clip(RoundedCornerShape(15))
                    .border(
                        1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(15)
                    )
                ,
                query = query.value,
                onQueryChange = { query.value = it },
                searchInProgress = isSearchInProgress
            ) {
                searchCards(query.value)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = padding), visible = lastSearchWasError
            ) {
                Text("No cards found :(", color = Color.Red, fontSize = textSize)
            }
            if (lastSearchWasError) return@Column
            LazyColumn(
                Modifier.pointerInput(Unit) {
                    detectTapGestures(onPress = { focusManager.clearFocus() })
                }, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (backStackDiff == 0 || isSearchInProgress) return@LazyColumn
                item {
                    Text(
                        "${cardResults.size + rulingsResults.size} results",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = textSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = padding).align(Alignment.CenterHorizontally)
                    )
                }
                items(cardResults) { card ->
                    CardPreview(
                        card = card,
                        selectButtonEnabled = selectButtonEnabled,
                        printingsButtonEnabled = _printingsButtonEnabled,
                        rulingsButtonEnabled = rulingsButtonEnabled,
                        onRulings = {
                        searchRulings(card.rulingsUri ?: "")
                        rulingCard = card
                        backStackDiff += 1
                        addToBackStack {
                            backStackDiff -= 1
                            searchCards(query.value)
                            rulingCard = null
                        }
                    }, onSelect = {
                        onImageSelected(card.getUris().artCrop)
                    }, onPrintings = {
                        searchCards(card.printsSearchUri, disablePrintingsButton = true)
                        backStackDiff += 1
                        addToBackStack {
                            backStackDiff -= 1
                            searchCards(query.value)
                        }
                    })
                }
                if (cardResults.isEmpty() && rulingCard != null) {
                    item {
                        CardDetails(rulingCard!!)
                    }
                    if (rulingsResults.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = padding).align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                            )
                        }
                    }
                }
                items(rulingsResults) { ruling ->
                    RulingPreview(ruling)
                    Spacer(modifier = Modifier.height(padding))
                }
//            item {
//                AnimatedVisibility(
//                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp), visible = rulingsResults.isEmpty() && !isSearchInProgress
//                ) {
//                    Text("No rulings found :(", color = Color.Red, fontSize = 15.scaledSp)
//                }
//            }
            }
        }
    }
}

@Composable
fun ScryfallSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    searchInProgress: Boolean = false,
    onSearch: () -> Unit
) {
    BoxWithConstraints(Modifier.wrapContentSize()) {
        val padding = remember(Unit) { maxHeight / 100f }
        TextFieldWithButton(
            modifier = modifier,
            value = query,
            onValueChange = onQueryChange,
            label = "Search Scryfall",
            keyboardType = KeyboardType.Text,
            onDone = {
                onSearch()
            }
        ) {
            IconButton(
                onClick = {
                    onSearch()
                },
                modifier = Modifier.fillMaxSize()
            ) {
                if (searchInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding * 1.5f),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = vectorResource(Res.drawable.search_icon),
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
fun ScryfallButton(
    modifier: Modifier = Modifier,
    text: String,
    onTap: () -> Unit
) {
    val originalColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
    val pressedColor = Color.Green.copy(alpha = 0.5f)
    var color by remember { mutableStateOf(originalColor) }
    val coroutineScope = rememberCoroutineScope()
    val animatedColor by animateColorAsState(targetValue = color, animationSpec = tween(durationMillis = 500), finishedListener = {
        coroutineScope.launch {
            delay(1500)
            color = originalColor
        }
    })
    BoxWithConstraints(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { _ ->
                color = pressedColor
                onTap()
            }
        },
        ) {
        val textSize = remember(Unit) { (maxWidth / 4.5f).value }
        val textPadding = remember(Unit) { maxWidth / 12f }
        Surface(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(30)), color = animatedColor
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                fontSize = textSize.scaledSp,
                modifier = Modifier.padding(top = textPadding).align(Alignment.Center)
            )
        }
    }
}

@Composable
fun CardDetails(
    card: Card
) {
    TextPreview(
        largeText = "Oracle Text",
        bodyText = card.oracleText ?: ""
    )
}

@Composable
fun RulingPreview(
    ruling: Ruling
) {
    TextPreview(
        largeText = "Ruling (${ruling.publishedAt})",
        bodyText = ruling.comment
    )
}

@Composable
private fun TextPreview(
    largeText: String,
    bodyText: String,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(0.9f).border(
            1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(30.dp)
        ).clip(RoundedCornerShape(30.dp)), contentAlignment = Alignment.Center
    ) {
        val textSize = remember(Unit) { (maxWidth / 30f).value }
        val padding = remember(Unit) { maxWidth / 60f }
        Column(Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(horizontal = padding * 2, vertical = padding),
                text = largeText, color = MaterialTheme.colorScheme.onPrimary, textAlign = TextAlign.Center,
                fontSize = textSize.scaledSp,
            )
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.4f).align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
            )
            Text(
                modifier = Modifier.fillMaxWidth().padding(horizontal = padding * 2, vertical = padding),
                text = bodyText,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Start,
                fontSize = textSize.scaledSp,
                lineHeight = textSize.scaledSp
            )
        }
    }
}

@Composable
fun CardPreview(
    card: Card, onRulings: () -> Unit = {}, onSelect: () -> Unit = {}, onPrintings: () -> Unit = {}, selectButtonEnabled: Boolean, printingsButtonEnabled: Boolean, rulingsButtonEnabled: Boolean
) {
    var showLargeImage by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    if (showLargeImage) {
        Dialog(onDismissRequest = { showLargeImage = false }, properties = DialogProperties(
            usePlatformDefaultWidth = false
        ), content = {
            Box(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures(onTap = {
                    showLargeImage = false
                })
            }) {
                SubcomposeAsyncImage(
                    model = card.getUris().large, modifier = Modifier.clip(CutCornerShape(32)).fillMaxSize(0.85f).align(Alignment.Center), contentDescription = "",
                    loading = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
                )
            }
        })
    }

    BoxWithConstraints(
        modifier = Modifier.wrapContentSize()
    ) {
        val cardHeight = remember(Unit) { (maxWidth / 3.5f) + 50.dp }
        val padding = remember(Unit) { cardHeight / 11f }
        val buttonSize = remember(Unit) { (cardHeight / 6f) }
        val fontSize = remember(Unit) { (cardHeight / 9f).value }
        Box(Modifier.padding(horizontal = padding, vertical = padding / 2f)) {
            Surface(
                modifier = Modifier.fillMaxSize().border(
                    1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(15)
                ).clip(RoundedCornerShape(15)), color = MaterialTheme.colorScheme.background.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = padding / 2f, horizontal = padding).height(cardHeight).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.fillMaxHeight().weight(2.0f), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            card.name, color = MaterialTheme.colorScheme.onPrimary, fontSize = fontSize.scaledSp, lineHeight = fontSize.scaledSp, fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            card.setName, color = MaterialTheme.colorScheme.onPrimary, fontSize = (fontSize * 0.8f).scaledSp, lineHeight = (fontSize * 0.8f).scaledSp, fontWeight = FontWeight.Light
                        )
                        Text(
                            card.artist, color = MaterialTheme.colorScheme.onPrimary, fontSize = (fontSize * 0.8f).scaledSp, lineHeight = (fontSize * 0.8f).scaledSp, fontWeight = FontWeight.Light
                        )
                        Text(
                            "© Wizards of the Coast",
                            fontSize = (fontSize * 0.6f).scaledSp,
                            lineHeight = (fontSize * 0.6f).scaledSp,
                            fontWeight = FontWeight.ExtraLight,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Row {
                            if (rulingsButtonEnabled) {
                                ScryfallButton(modifier = Modifier.height(buttonSize).aspectRatio(2.75f).padding(horizontal = 5.dp).clip(RoundedCornerShape(30)), text = "Rulings", onTap = {
                                    onRulings()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                })
                            }
                            if (selectButtonEnabled) {
                                ScryfallButton(modifier = Modifier.height(buttonSize).aspectRatio(2.75f).padding(horizontal = 5.dp).clip(RoundedCornerShape(30)), text = "Select", onTap = {
                                    onSelect()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                })
                            }
                            if (printingsButtonEnabled) {
                                ScryfallButton(modifier = Modifier.height(buttonSize).aspectRatio(2.75f).padding(horizontal = 5.dp).clip(RoundedCornerShape(30)), text = "Printings", onTap = {
                                    onPrintings()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                })
                            }

                        }
                    }
                    Box(Modifier.fillMaxHeight().weight(1.0f).clip(CutCornerShape(6.dp)).pointerInput(Unit) {
                        detectTapGestures(onLongPress = {
                            showLargeImage = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        })
                    }) {
                        AsyncImage(
                            model = card.getUris().large, modifier = Modifier.fillMaxSize(), contentDescription = ""
                        )
                    }
                }
            }
        }
    }
}