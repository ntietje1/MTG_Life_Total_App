package composable.dialog.coinflip

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import composable.SettingsButton
import composable.dialog.ExpandableCard
import composable.flippable.Flippable
import composable.flippable.FlippableState
import composable.flippable.rememberFlipController
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.question_icon
import lifelinked.shared.generated.resources.thumbsup_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.scaledSp

@Composable
fun CoinFlipDialogContent(
    modifier: Modifier = Modifier,
    goToCoinFlipTutorial: () -> Unit,
    viewModel: CoinFlipViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    val haptic = LocalHapticFeedback.current

    val lastResultString = remember(state.krarksThumbs, state.lastResults.size) { viewModel.buildLastResultString() }
    val historyString = remember(state.history.size) { viewModel.buildHistoryString() }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.repairHistoryString()
            viewModel.resetCoinControllers()
        }
    }

    LaunchedEffect(state.history.size, state.lastResults.size) {
        if ( // hacky way to trigger haptic feedback on a flip
            state.history.lastOrNull() == CoinFace.R_DIVIDER_LIST || state.history.lastOrNull() == CoinFace.R_DIVIDER_SINGLE || state.lastResults.lastOrNull() == CoinFace.COMMA) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    BoxWithConstraints(Modifier.wrapContentSize()) {
        val padding = remember(Unit) { maxWidth / 25f + maxHeight / 30f }
        val counterHeight = remember(Unit) { maxHeight / 20f }
        val textSize = remember(Unit) { (maxWidth / 30f).value }
        val buttonSize = remember(Unit) { maxWidth / 4.5f }

        SettingsButton(
            modifier = Modifier.size(buttonSize * 0.5f).align(Alignment.TopEnd).padding(end = buttonSize * 0.15f, top = buttonSize * 0.15f),
            onPress = goToCoinFlipTutorial,
            hapticEnabled = true,
            textSizeMultiplier = 0.9f,
            shape = RoundedCornerShape(100),
            imageVector = vectorResource(Res.drawable.question_icon),
            backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
            mainColor = MaterialTheme.colorScheme.onPrimary
        )

        Column(modifier = modifier.fillMaxSize()) {
            Spacer(Modifier.weight(0.1f))
            Column(
                Modifier.wrapContentSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.wrapContentSize().padding(top = buttonSize * 0.05f, start = buttonSize * 0.1f, bottom = buttonSize * 0.05f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    SettingsButton(
                        modifier = Modifier.size(buttonSize * 0.7f).rotate(180f).padding(buttonSize * 0.025f),
                        onPress = { if (!viewModel.flipInProgress) viewModel.incrementKrarksThumbs(-1) },
                        hapticEnabled = true,
                        imageVector = vectorResource(Res.drawable.thumbsup_icon),
                        shape = RoundedCornerShape(30),
                        backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                    )
                    SettingsButton(
                        modifier = Modifier.size(buttonSize * 0.7f).padding(buttonSize * 0.025f),
                        onPress = { if (!viewModel.flipInProgress) viewModel.incrementKrarksThumbs(1) },
                        hapticEnabled = true,
                        imageVector = vectorResource(Res.drawable.thumbsup_icon),
                        shape = RoundedCornerShape(30),
                        backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                    )
                }
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = "Krark's Thumbs: ${state.krarksThumbs}",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = textSize.scaledSp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.weight(0.3f))
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().aspectRatio(1.5f), contentAlignment = Alignment.Center
            ) {
                val columns = remember(state.krarksThumbs) {
                    viewModel.columns()
                }

                val rows = remember(state.krarksThumbs) {
                    viewModel.rows()
                }

                val coinSize = remember(columns, rows) {
                    minOf(
                        maxWidth / columns * 0.95f, maxHeight / rows * 0.90f
                    )
                }

                LazyVerticalGrid(
                    modifier = Modifier.width(coinSize * columns).height(coinSize * rows),
                    columns = GridCells.Fixed(columns),
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(items = viewModel.coinControllers, key = { it.hashCode() }) {
                        CoinFlippable(modifier = Modifier.height(coinSize).wrapContentWidth(), coinController = it, skipAnimation = state.krarksThumbs >= 4, onTap = {
                            if (!viewModel.flipInProgress) {
                                viewModel.randomFlip()
                            }
                        })
                    }
                }
            }
            Spacer(Modifier.weight(1.25f))

            Column(
                modifier = Modifier.wrapContentHeight().fillMaxWidth(0.8f).align(Alignment.CenterHorizontally)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), RoundedCornerShape(30)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Flip Until You Lose",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = textSize.scaledSp,
                    modifier = Modifier.padding(top = padding / 6f, bottom = padding / 8f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = padding / 2f),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    SettingsButton(
                        modifier = Modifier.size(buttonSize * 0.7f),
                        onPress = { if (!viewModel.flipInProgress) viewModel.flipUntil(CoinFace.HEADS) },
                        hapticEnabled = false,
                        text = "Call Tails",
                        imageVector = vectorResource(CoinFace.HEADS.drawable)
                    )
                    Spacer(Modifier.width(buttonSize / 4f))
                    SettingsButton(
                        modifier = Modifier.size(buttonSize * 0.7f),
                        onPress = { if (!viewModel.flipInProgress) viewModel.flipUntil(CoinFace.TAILS) },
                        hapticEnabled = false,
                        text = "Call Heads",
                        imageVector = vectorResource(CoinFace.TAILS.drawable)
                    )
                }
                Spacer(Modifier.height(padding / 8f))
            }
            Spacer(Modifier.height(padding / 8f))
            Spacer(Modifier.weight(0.1f))
            FlipCounter(
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().height(counterHeight), headCount = state.headCount, tailCount = state.tailCount, clearHistory = viewModel::reset
            )
            Spacer(Modifier.weight(0.5f))
            Text(
                text = "Last Result",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = textSize.scaledSp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = padding / 4f, bottom = padding / 12f)
            )
            LastResult(
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.8f).height(counterHeight), lastResult = lastResultString
            )
            Spacer(Modifier.weight(0.1f))
            Text(
                text = "Flip History",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = textSize.scaledSp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = padding / 4f, bottom = padding / 12f)
            )
            FlipHistory(
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.8f).height(counterHeight), coinFlipHistory = historyString
            )
            Spacer(Modifier.height(padding / 2f))
            Spacer(Modifier.weight(0.7f))
        }
    }
}


@Composable
fun CoinFlippable(
    modifier: Modifier = Modifier, coinController: CoinController, skipAnimation: Boolean, onTap: () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier, contentAlignment = Alignment.Center
    ) {
        Flippable(modifier = Modifier.fillMaxHeight().aspectRatio(1f),
            flipController = coinController.flipController,
            flipDurationMs = if (skipAnimation) 0 else coinController.duration,
            flipOnTouch = false,
            flipEnabled = true,
            flipAnimationType = coinController.flipAnimationType,
            frontSide = {
                Image(
                    imageVector = vectorResource(CoinFace.HEADS.drawable), contentDescription = CoinFace.HEADS.name, modifier = Modifier.fillMaxSize()
                )
            },
            backSide = {
                Image(
                    imageVector = vectorResource(CoinFace.TAILS.drawable), contentDescription = CoinFace.TAILS.name, modifier = Modifier.fillMaxSize()
                )
            },
            onFlippedListener = { currentSide ->
                if (coinController.continueFlip()) { // continues to flip until no more flips left
                    coinController.onResult(if (currentSide == FlippableState.FRONT) CoinFace.HEADS else CoinFace.TAILS)
                }
            })
        Box(Modifier.fillMaxHeight().aspectRatio(1f).pointerInput(Unit) {
            detectTapGestures(onPress = { _ ->
                onTap()
            })
        })
    }
}

@Composable
fun ResetButton(modifier: Modifier = Modifier, onReset: () -> Unit) {
    BoxWithConstraints(
        modifier = modifier.aspectRatio(2.5f).clip(RoundedCornerShape(5.dp)).pointerInput(Unit) {
            detectTapGestures(onTap = { _ -> onReset() })
        },

        ) {
        val textSize = (maxWidth / 4f).value.scaledSp
        val textPadding = maxHeight / 10f
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
        ) {
            Text(
                text = "Reset",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = textSize),
                modifier = Modifier.align(Alignment.BottomCenter).padding(top = textPadding)
            )
        }
    }
}

@Composable
fun FlipCounter(
    modifier: Modifier = Modifier, headCount: Int, tailCount: Int, clearHistory: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(Modifier.wrapContentSize()) {
        val padding = remember(Unit) { maxWidth / 100f }
        val textSize = remember(Unit) { (maxWidth / 25f).value }
        Row(
            modifier = modifier, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Green, fontWeight = FontWeight.Bold)) {
                        append("Heads   ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Normal
                        )
                    ) {
                        append("$headCount")
                    }
                }, style = TextStyle(fontSize = textSize.scaledSp), modifier = Modifier.padding(vertical = 0.dp, horizontal = padding)
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                        append("Tails   ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Normal
                        )
                    ) {
                        append("$tailCount")
                    }
                }, style = TextStyle(fontSize = textSize.scaledSp), modifier = Modifier.padding(vertical = 0.dp, horizontal = padding)
            )
            ResetButton(modifier = Modifier.fillMaxHeight().padding(start = padding * 5).padding(vertical = padding / 2), onReset = {
                clearHistory()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            })
        }
    }
}

@Composable
fun FlipHistory(modifier: Modifier = Modifier, coinFlipHistory: AnnotatedString) {
    historyBase(modifier, coinFlipHistory, wrapContentSize = false)
}

@Composable
fun LastResult(modifier: Modifier = Modifier, lastResult: AnnotatedString) {
    historyBase(modifier, lastResult, wrapContentSize = true)
}

@Composable
private fun historyBase(modifier: Modifier = Modifier, lastResult: AnnotatedString, wrapContentSize: Boolean) {
    val scrollState = rememberScrollState()
    LaunchedEffect(lastResult) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }
    BoxWithConstraints(
        modifier
    ) {
        val textSize = (maxWidth / 18f).value.scaledSp
        val padding = maxWidth / 50f
        Box(
            Modifier.align(Alignment.Center).clip(RoundedCornerShape(30)).background(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), shape = RoundedCornerShape(30))
                .then(if (wrapContentSize) Modifier.wrapContentSize() else Modifier.fillMaxWidth())
        ) {
            Text(
                text = lastResult,
                maxLines = 1,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = textSize,
                modifier = Modifier.align(Alignment.Center).wrapContentSize().padding(vertical = padding / 2f, horizontal = padding).horizontalScroll(scrollState)
            )
        }
    }
}

@Composable
fun CoinFlipTutorialContent(
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = Modifier.wrapContentSize(),
    ) {
        val padding = remember(Unit) { maxWidth / 25f }
        val textSize = remember(Unit) { (maxWidth / 30f).value }
        LazyColumn(
            modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(padding), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    val flipController = rememberFlipController()
                    Flippable(
                        modifier = Modifier.fillMaxWidth(0.33f),
                        flipController = flipController,
                        frontSide = {
                            ExpandableCard(
                                modifier = Modifier.fillMaxSize(), imageUri = "https://cards.scryfall.io/large/front/9/f/9f63277b-e139-46c8-b9e3-0cfb647f44cc.jpg?1670031752"
                            )
                        },
                        backSide = {
                            ExpandableCard(
                                modifier = Modifier.fillMaxSize(), imageUri = "https://cards.scryfall.io/large/back/9/f/9f63277b-e139-46c8-b9e3-0cfb647f44cc.jpg?1670031752"
                            )
                        },
                    )
                    Text(text = """
                            Krark's Thumb is an artifact with the ability "If you would flip a coin, instead flip two coins and ignore one."
                            
                            This means that if you call heads, any number of heads will win the flip.
                            
                            This ability stacks exponentially, so if you have two thumbs, you flip four coins and ignore three, and so on.
                        """.trimIndent(), color = MaterialTheme.colorScheme.onPrimary, fontSize = textSize.scaledSp, modifier = Modifier.fillMaxWidth().padding(padding / 2f).clickable {
                        flipController.flip()
                    })
                }
            }

            item {
                val flipController = rememberFlipController()
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(padding), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = buildAnnotatedString {
                        append("\"Flip until you lose\" means that you will keep flipping coins until you get don't get the result you called.\n\n")
                        append("With Krark's Thumb, this means you will keep flipping until you don't get ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("any")
                        }
                        append(" of the result you called.\n\n")
                        append("For example, to call Heads, press the \"Flip Until Lose (Tails)\" button. This will flip until you fail to get a single Head among all coins flipped.")
                    }, color = MaterialTheme.colorScheme.onPrimary, fontSize = textSize.scaledSp, modifier = Modifier.fillMaxWidth(0.67f).padding(padding / 2f).clickable {
                        flipController.flip()
                    })
                    Flippable(
                        modifier = Modifier.fillMaxWidth(),
                        flipController = flipController,
                        frontSide = {
                            ExpandableCard(
                                modifier = Modifier.fillMaxSize(), imageUri = "https://cards.scryfall.io/large/front/d/5/d5dfd236-b1da-4552-b94f-ebf6bb9dafdf.jpg?1670031689"
                            )
                        },
                        backSide = {
                            ExpandableCard(
                                modifier = Modifier.fillMaxSize(), imageUri = "https://cards.scryfall.io/large/back/d/5/d5dfd236-b1da-4552-b94f-ebf6bb9dafdf.jpg?1670031689"
                            )
                        },
                    )
                }
            }
        }
    }
}