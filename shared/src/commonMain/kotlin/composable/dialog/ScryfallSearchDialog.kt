package composable.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
//    playerButtonViewModel: PlayerButtonViewModel,
//    backStack: List<() -> Unit> = listOf(),
    addToBackStack: (() -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onImageSelected: (String) -> Unit
) {
    SettingsDialog(modifier = modifier, backButtonEnabled = false, onDismiss = onDismiss) {
        ScryfallDialogContent(
//            backStack = backStack,
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
            rulingsResults = scryfallApiRetriever.parseScryfallResponse<Ruling>(scryfallApiRetriever.searchScryfall(qry))
            lastSearchWasError = false
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

    Column(modifier) {
        ScryfallSearchBar(
            Modifier.padding(top = 10.dp).padding(start = 20.dp, end = 20.dp).clip(RoundedCornerShape(10.dp)),
            query = query.value,
            onQueryChange = { query.value = it },
            searchInProgress = isSearchInProgress
        ) {
            searchCards(query.value)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp), visible = lastSearchWasError
        ) {
            Text("No cards found :(", color = Color.Red, fontSize = 15.scaledSp)
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
                    fontSize = 15.scaledSp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp).align(Alignment.CenterHorizontally)
                )
            }
            items(cardResults) { card ->
                CardPreview(card, selectButtonEnabled = selectButtonEnabled, printingsButtonEnabled = _printingsButtonEnabled, rulingsButtonEnabled = rulingsButtonEnabled, onRulings = {
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
            }
            items(rulingsResults) { ruling ->
                RulingPreview(ruling)
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
    Box(
        modifier = modifier.wrapContentSize()
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search Scryfall", fontSize = 15.scaledSp) },
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.scaledSp, color = MaterialTheme.colorScheme.onPrimary),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None, autoCorrect = false, keyboardType = KeyboardType.Text, imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(onSearch = {
                onSearch()
            }),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                disabledTextColor = MaterialTheme.colorScheme.onPrimary,
                focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                disabledLabelColor = MaterialTheme.colorScheme.onPrimary,
                cursorColor = MaterialTheme.colorScheme.onPrimary,
                selectionColors = TextSelectionColors(
                    handleColor = MaterialTheme.colorScheme.onPrimary,
                    backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                ),
                focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.1f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.height(60.dp).fillMaxWidth().border(
                1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(10.dp)
            ).align(Alignment.CenterStart)
        )
        Box(
            Modifier.wrapContentSize().padding(end = 10.dp).align(Alignment.CenterEnd)
        ) {
            IconButton(
                onClick = {
                    onSearch()
                }, modifier = Modifier.size(50.dp)
            ) {
                if (searchInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize().padding(5.dp), color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = vectorResource(Res.drawable.search_icon),
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxSize().padding(5.dp)
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
    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { _ ->
                color = pressedColor
                onTap()
            }
        },

        ) {
        Surface(
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(30)), color = animatedColor
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 13.scaledSp),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp).align(Alignment.Center)
            )
        }
    }
}

@Composable
fun CardDetails(
    card: Card
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp).border(
            1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(30.dp)
        ).clip(RoundedCornerShape(30.dp)), contentAlignment = Alignment.Center
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                text = "Oracle Text", color = MaterialTheme.colorScheme.onPrimary, textAlign = TextAlign.Center,
                fontSize = 15.scaledSp,
            )
            Divider(
                modifier = Modifier.fillMaxWidth(0.4f).align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
            )
            Text(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                text = card.oracleText ?: "",
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                fontSize = 15.scaledSp,
                lineHeight = 15.scaledSp
            )
        }
    }
}

@Composable
fun RulingPreview(
    ruling: Ruling
) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp).border(
            1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(30.dp)
        ).clip(RoundedCornerShape(30.dp)), contentAlignment = Alignment.Center
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                text = ruling.publishedAt,
                fontSize = 15.scaledSp,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
            Divider(
                modifier = Modifier.fillMaxWidth(0.4f).align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
            )
            Text(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp),
                text = ruling.comment,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                fontSize = 15.scaledSp,
                lineHeight = 15.scaledSp
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
                    model = card.getUris().large, modifier = Modifier.clip(CutCornerShape(125.dp)).fillMaxSize(0.85f).align(Alignment.Center), contentDescription = "",
                    loading = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }
                )
            }

        })
    }

    Box(
        modifier = Modifier.wrapContentSize().padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().border(
                1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(30.dp)
            ).clip(RoundedCornerShape(30.dp)), color = MaterialTheme.colorScheme.background.copy(alpha = 0.2f)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 15.dp).height(150.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().weight(2.0f), verticalArrangement = Arrangement.SpaceEvenly, horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        card.name, color = MaterialTheme.colorScheme.onPrimary, fontSize = 15.scaledSp, lineHeight = 15.scaledSp, fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        card.setName, color = MaterialTheme.colorScheme.onPrimary, fontSize = 13.scaledSp, lineHeight = 13.scaledSp, fontWeight = FontWeight.Light
                    )
                    Text(
                        card.artist, color = MaterialTheme.colorScheme.onPrimary, fontSize = 13.scaledSp, lineHeight = 15.scaledSp, fontWeight = FontWeight.Light
                    )
                    Text(
                        "© Wizards of the Coast",
                        fontSize = 10.scaledSp,
                        lineHeight = 10.scaledSp,
                        fontWeight = FontWeight.ExtraLight,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Row() {
                        if (rulingsButtonEnabled) {
                            ScryfallButton(modifier = Modifier.width(85.dp).height(25.dp).padding(horizontal = 5.dp).clip(RoundedCornerShape(30)), text = "Rulings", onTap = {
                                onRulings()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            })
                        }
                        if (selectButtonEnabled) {
                            ScryfallButton(modifier = Modifier.width(85.dp).height(25.dp).padding(horizontal = 5.dp).clip(RoundedCornerShape(30)), text = "Select", onTap = {
                                onSelect()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            })
                        }
                        if (printingsButtonEnabled) {
                            ScryfallButton(modifier = Modifier.width(85.dp).height(25.dp).padding(horizontal = 5.dp).clip(RoundedCornerShape(30)), text = "Printings", onTap = {
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