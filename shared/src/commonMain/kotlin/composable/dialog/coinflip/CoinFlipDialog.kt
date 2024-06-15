package composable.dialog.coinflip

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import composable.flippable.Flippable
import composable.flippable.FlippableState
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.scaledSp

/**
 * TODO:
 * - add krarks thumb toggle, with hold down function to change to number
 *  - modify flipping behavior when krark thumb is toggled
 * - "flip till lose" button
 * - popup to show results
 *        single flip: "WIN by X heads, Y tails in Z flips" HT
 *        flip till lose: "X wins by flipping Y heads, Z tails in M flips" HH | HT | TH | TT
 *
 * - "set winning side" button
 * -
 */

@Composable
fun CoinFlipDialogContent(
    modifier: Modifier = Modifier,
    viewModel: CoinFlipViewModel = koinInject(),
) {
    val state by viewModel.state.collectAsState()

    BoxWithConstraints(Modifier.wrapContentSize()) {
        val padding = remember(Unit) { maxWidth / 25f + maxHeight / 30f }
        val counterHeight = remember(Unit) { maxHeight / 20f }
        val textSize = remember(Unit) { (maxWidth / 30f).value }
        val buttonSize = remember(Unit) { maxWidth / 4f }

        Column(modifier = modifier.fillMaxSize()) {
            Spacer(Modifier.weight(0.8f))
            CoinFlippable(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = padding / 2f, start = padding, end = padding)
                    .fillMaxWidth(0.9f),
                viewModel = viewModel
            )
            Spacer(Modifier.weight(0.1f))
            Text(
                text = "Tap to flip",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = textSize.scaledSp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = padding / 10f, bottom = padding / 8f)
            )
            Spacer(Modifier.weight(1.25f))
            Row(
                modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(horizontal = padding / 2f),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SettingsButton(
                    modifier = Modifier.size(buttonSize),
                    onPress = { if (!state.flipInProgress) viewModel.flipUntil(CoinFace.TAILS) },
                    hapticEnabled = false,
                    text = "Flip Until Tails",
                    imageVector = vectorResource(CoinFace.TAILS.drawable)
                )
                SettingsButton(
                    modifier = Modifier.size(buttonSize),
                    onPress = { if (!state.flipInProgress) viewModel.flipUntil(CoinFace.HEADS) },
                    hapticEnabled = false,
                    text = "Flip Until Heads",
                    imageVector = vectorResource(CoinFace.HEADS.drawable)
                )
            }
            FlipCounter(
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().height(counterHeight),
                headCount = state.headCount,
                tailCount = state.tailCount,
                clearHistory = viewModel::reset
            )
            Spacer(Modifier.weight(0.5f))
            Text(
                text = "Flip History",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = textSize.scaledSp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = padding / 4f, bottom = padding / 12f)
            )
            FlipHistory(
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .fillMaxWidth(0.8f)
                    .height(counterHeight),
                coinFlipHistory = state.historyString
            )
            Spacer(Modifier.height(padding / 2f))
            Spacer(Modifier.weight(0.7f))
        }
    }
}


@Composable
fun CoinFlippable(
    modifier: Modifier = Modifier,
    viewModel: CoinFlipViewModel,
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current

    BoxWithConstraints(modifier = modifier
    ) {
        Flippable(modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
            flipController = viewModel.flipController,
            flipDurationMs = state.duration,
            flipOnTouch = false,
            flipEnabled = true,
            flipAnimationType = state.flipAnimationType,
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
                if (viewModel.continueFlip()) { // continues to flip until no more flips left
                    viewModel.onResult(if (currentSide == FlippableState.FRONT) CoinFace.HEADS else CoinFace.TAILS)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        )
        Box(Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .pointerInput(Unit) {
            detectTapGestures(onPress = { _ ->
                if (!state.flipInProgress) viewModel.randomFlip()
            })
        })
    }
}

@Composable
fun ResetButton(modifier: Modifier = Modifier, onReset: () -> Unit) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(2.5f)
            .clip(RoundedCornerShape(5.dp))
            .pointerInput(Unit) {
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
    modifier: Modifier = Modifier,
    headCount: Int,
    tailCount: Int,
    clearHistory: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    BoxWithConstraints(Modifier.wrapContentSize()) {
        val padding = remember(Unit) { maxWidth / 100f }
        val textSize = remember(Unit) { (maxWidth / 25f).value }
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
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
            ResetButton(
                modifier = Modifier.fillMaxHeight().padding(start = padding * 5).padding(vertical = padding / 2),
                onReset = {
                    clearHistory()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                })
        }
    }
}

@Composable
fun FlipHistory(modifier: Modifier = Modifier, coinFlipHistory: AnnotatedString) {
    BoxWithConstraints(
        modifier
            .clip(RoundedCornerShape(30))
            .background(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), shape = RoundedCornerShape(30))
    ) {
        val textSize = (maxWidth / 18f).value.scaledSp
        val hPadding = maxWidth / 50f
        Text(
            text = coinFlipHistory,
            maxLines = 1,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = textSize,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 0.dp, horizontal = hPadding)
        )
    }
}