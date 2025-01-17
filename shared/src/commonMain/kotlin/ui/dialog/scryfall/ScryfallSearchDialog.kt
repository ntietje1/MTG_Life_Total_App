package ui.dialog.scryfall

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.card.Card
import model.card.Ruling
import org.koin.compose.koinInject
import theme.LocalDimensions
import theme.halfAlpha
import theme.scaledSp
import ui.components.EnlargeableCardImage
import ui.components.SearchTextField
import ui.dialog.SettingsDialog

@Composable
fun ScryfallDialogContent(
    modifier: Modifier = Modifier,
    addToBackStack: (String, () -> Unit) -> Unit,
    selectButtonEnabled: Boolean = true,
    printingsButtonEnabled: Boolean = true,
    rulingsButtonEnabled: Boolean = false,
    onImageSelected: (String) -> Unit,
    viewModel: ScryfallSearchViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState(state.scrollPosition)
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current

    LaunchedEffect(listState.firstVisibleItemIndex) {
        viewModel.setScrollPosition(listState.firstVisibleItemIndex)
    }

    LaunchedEffect(Unit) {
        viewModel.setPrintingsButtonEnabled(printingsButtonEnabled)
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        val searchBarHeight = remember(Unit) { maxWidth / 8.5f + 30.dp }
        Column(
            modifier = Modifier.wrapContentSize().align(Alignment.TopCenter)
        ) {
            SearchTextField(
                Modifier
                    .fillMaxWidth()
                    .height(searchBarHeight)
                    .padding(
                        top = dimensions.paddingMedium,
                        start = dimensions.paddingMedium,
                        end = dimensions.paddingMedium
                    )
                    .clip(RoundedCornerShape(15))
                    .border(dimensions.borderThin, MaterialTheme.colorScheme.onPrimary.halfAlpha(), RoundedCornerShape(15)),
                query = state.textFieldValue,
                onQueryChange = viewModel::setTextFieldValue,
                searchInProgress = state.isSearchInProgress
            ) {
                focusManager.clearFocus()
                viewModel.searchCards(state.textFieldValue.text)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }

            AnimatedVisibility(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = dimensions.paddingSmall),
                visible = state.lastSearchWasError
            ) {
                Text("No cards found :(", color = Color.Red, fontSize = dimensions.textSmall.scaledSp)
            }
            if (state.lastSearchWasError) return@BoxWithConstraints
//            AnimatedVisibility(
//                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = dimensions.paddingSmall),
//                visible = (state.backStackDiff == 0 || state.isSearchInProgress)
//            ) {
//                Text(
//                    "${state.cardResults.size + state.rulingsResults.size} results",
//                    color = MaterialTheme.colorScheme.onPrimary,
//                    fontSize = textSize.scaledSp,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.padding(vertical = dimensions.paddingSmall)
//                )
//            }

            LazyColumn(
                Modifier.pointerInput(Unit) {
                    detectTapGestures(onPress = { focusManager.clearFocus() })
                }, horizontalAlignment = Alignment.CenterHorizontally,
                state = listState
            ) {
                if (state.backStackDiff == 0 || state.isSearchInProgress) return@LazyColumn
            item {
                Text(
                    "${state.cardResults.size + state.rulingsResults.size} results",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = dimensions.textSmall.scaledSp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = dimensions.paddingSmall)
                )
            }
                items(state.cardResults) { card ->
                    CardInfoPreview(
                        card = card,
                        selectButtonEnabled = selectButtonEnabled,
                        printingsButtonEnabled = state.printingsButtonEnabled,
                        rulingsButtonEnabled = rulingsButtonEnabled,
                        onRulings = {
                            focusManager.clearFocus()
                            viewModel.searchRulings(card.rulingsUri ?: "")
                            viewModel.setRulingCard(card)
                            val scrollPosition = listState.firstVisibleItemIndex
                            addToBackStack("Search: $state.textFieldValue.text") {
                                viewModel.incrementBackStackDiff(-1)
                                focusManager.clearFocus()
                                viewModel.setRulingCard(null)
                                viewModel.searchCards(state.textFieldValue.text) {
                                    println("scrolling to $scrollPosition")
                                    listState.scrollToItem(scrollPosition)
                                }
                            }
                        }, onSelect = {
                            onImageSelected(card.getUris().artCrop)
                        }, onPrintings = {
                            focusManager.clearFocus()
                            viewModel.searchCards(card.printsSearchUri, disablePrintingsButton = true)
                            val scrollPosition = listState.firstVisibleItemIndex
                            addToBackStack("Search: $state.textFieldValue.text") {
                                viewModel.incrementBackStackDiff(-1)
                                focusManager.clearFocus()
                                viewModel.searchCards(state.textFieldValue.text) {
                                    println("scrolling to $scrollPosition")
                                    listState.scrollToItem(scrollPosition)
                                }
                            }
                        })
                }
                if (state.cardResults.isEmpty() && state.rulingCard != null) {
                    item {
                        CardDetails(state.rulingCard!!)
                    }
                    if (state.rulingsResults.isNotEmpty()) {
                        item {
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = dimensions.paddingSmall),
                                color = MaterialTheme.colorScheme.onPrimary.halfAlpha()
                            )
                        }
                    }
                }
                items(state.rulingsResults) { ruling ->
                    RulingPreview(ruling)
                    Spacer(modifier = Modifier.height(dimensions.paddingSmall))
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
fun ScryfallButton(
    modifier: Modifier = Modifier,
    text: String,
    onTap: () -> Unit
) {
    val originalColor = MaterialTheme.colorScheme.onSurface
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
        val textPadding = remember(Unit) { maxWidth / 13.5f }
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
    val dimensions = LocalDimensions.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(0.9f).border(
            dimensions.borderThin, MaterialTheme.colorScheme.onPrimary.halfAlpha(), RoundedCornerShape(10)
        ).clip(RoundedCornerShape(10)), contentAlignment = Alignment.Center
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
fun CardInfoPreview(
    card: Card, onRulings: () -> Unit = {}, onSelect: () -> Unit = {}, onPrintings: () -> Unit = {}, selectButtonEnabled: Boolean, printingsButtonEnabled: Boolean, rulingsButtonEnabled: Boolean
) {
    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current

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
                    dimensions.borderThin, MaterialTheme.colorScheme.onPrimary.halfAlpha(), RoundedCornerShape(15)
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
                                ScryfallButton(
                                    modifier = Modifier.height(buttonSize).aspectRatio(2.75f).padding(horizontal = dimensions.paddingSmall).clip(RoundedCornerShape(30)),
                                    text = "Rulings",
                                    onTap = {
                                        onRulings()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    })
                            }
                            if (selectButtonEnabled) {
                                ScryfallButton(
                                    modifier = Modifier.height(buttonSize).aspectRatio(2.75f).padding(horizontal = dimensions.paddingSmall).clip(RoundedCornerShape(30)),
                                    text = "Select",
                                    onTap = {
                                        onSelect()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    })
                            }
                            if (printingsButtonEnabled) {
                                ScryfallButton(
                                    modifier = Modifier.height(buttonSize).aspectRatio(2.75f).padding(horizontal = dimensions.paddingSmall).clip(RoundedCornerShape(30)),
                                    text = "Printings",
                                    onTap = {
                                        onPrintings()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    })
                            }

                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxHeight().weight(1.0f).padding(vertical = padding / 10f),
                        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EnlargeableCardImage(
                            modifier = Modifier.fillMaxHeight(),
                            smallImageUri = card.getUris().small,
                            largeImageUri = card.getUris().large,
                        )
                    }

                }
            }
        }
    }
}