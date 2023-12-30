package mtglifeappcompose.composable.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mtglifeappcompose.R
import com.wajahatkarim.flippable.FlipAnimationType
import com.wajahatkarim.flippable.Flippable
import com.wajahatkarim.flippable.FlippableState
import com.wajahatkarim.flippable.rememberFlipController
import mtglifeappcompose.data.AppViewModel
import kotlin.random.Random

@Composable
fun CoinFlipDialogContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    history: SnapshotStateList<String>,
) {
    CoinFlipDialogBox(modifier, history)
}

@Composable
fun CoinFlipDialogBox(modifier: Modifier = Modifier, history: SnapshotStateList<String>) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(Modifier.weight(0.5f))
        CoinFlippable(Modifier.align(Alignment.CenterHorizontally), history = history)
        Text(
            text = "Tap to Flip Coin",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontSize = 20.sp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 15.dp)
        )
        FlipCounter(Modifier.align(Alignment.CenterHorizontally), history)
        Spacer(Modifier.weight(2.0f))
        FlipHistory(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 0.dp), coinFlipHistory = history
        )
        Spacer(Modifier.weight(0.7f))
    }
}

@Composable
fun CoinFlippable(
    modifier: Modifier = Modifier, history: MutableList<String>, maxHistoryLength: Int = 18
) {
    val viewModel: AppViewModel = viewModel()
    val flipEnabled by remember { mutableStateOf(true) }
    val initialDuration = if (viewModel.fastCoinFlip) 150 else 300
    val additionalDuration = if (viewModel.fastCoinFlip) 35 else 75
    var duration by remember { mutableIntStateOf(initialDuration) }
    var flipAnimationType by remember { mutableStateOf(FlipAnimationType.VERTICAL_ANTI_CLOCKWISE) }
    val totalFlips = 2 // + 1
    var flipCount by remember { mutableIntStateOf(totalFlips) }
    val flipController = rememberFlipController()
    val haptic = LocalHapticFeedback.current

    fun addToHistory(v: String) {
        if (history.size > maxHistoryLength) {
            history.removeAt(0)
        }
        history.add(v)
    }

    fun decrementFlipCount() {
        flipCount--
        duration += additionalDuration
    }

    fun resetCount() {
        flipCount = totalFlips
        duration = initialDuration
    }

    fun continueFlip(currentSide: FlippableState) {
        if (flipCount == totalFlips) {
            if (Random.nextBoolean()) {
                decrementFlipCount()
            }
        }
        if (flipCount > 0) {
            flipAnimationType = if (flipAnimationType == FlipAnimationType.VERTICAL_ANTI_CLOCKWISE) {
                FlipAnimationType.VERTICAL_CLOCKWISE
            } else {
                FlipAnimationType.VERTICAL_ANTI_CLOCKWISE
            }
            flipController.flip()
            decrementFlipCount()
        } else {
            addToHistory(if (currentSide == FlippableState.FRONT) "H" else "T")
            resetCount()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    Flippable(modifier = modifier
        .size(screenWidth)
        .padding(bottom = 50.dp, top = 0.dp, start = 30.dp, end = 30.dp),
        flipController = flipController,
        flipDurationMs = duration,
        flipEnabled = flipEnabled,
        flipAnimationType = flipAnimationType,
        frontSide = {
            Image(
                painter = painterResource(id = R.drawable.heads), contentDescription = "Front Side", modifier = Modifier.fillMaxSize()
            )
        },
        backSide = {
            Image(
                painter = painterResource(id = R.drawable.tails), contentDescription = "Back Side", modifier = Modifier.fillMaxSize()
            )
        },
        onFlippedListener = { currentSide ->
            continueFlip(currentSide)
        })

}

@Composable
fun ResetButton(modifier: Modifier = Modifier, onReset: () -> Unit) {
    Box(
        modifier = modifier
            .width(50.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(0.dp))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { _ -> onReset() })
            },

        ) {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.35f)
        ) {
            Text(
                text = "Reset",
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 16.sp),
                modifier = Modifier.padding(top = 2.5.dp)
            )
        }
    }
}

@Composable
fun FlipCounter(modifier: Modifier = Modifier, history: MutableList<String>) {
    val hPadding = 10.dp
    val textSize = 20.sp

    val numberOfHeads = history.count { it == "H" }
    val numberOfTails = history.count { it == "T" }
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(), horizontalArrangement = Arrangement.Center
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
                    append("$numberOfHeads")
                }
            }, style = TextStyle(fontSize = textSize), modifier = Modifier.padding(vertical = 0.dp, horizontal = hPadding)
        )
        Spacer(modifier.width(10.dp))
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
                    append("$numberOfTails")
                }
            }, style = TextStyle(fontSize = textSize), modifier = Modifier.padding(vertical = 0.dp, horizontal = hPadding)
        )
        Spacer(modifier.width(10.dp))
        ResetButton(onReset = {
            history.clear()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        })
    }
}


@Composable
fun FlipHistory(modifier: Modifier = Modifier, coinFlipHistory: MutableList<String>) {
    val hPadding = 10.dp
    val vPadding = 5.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            text = "Flip History",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontSize = 20.sp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = vPadding)
        )

        Surface(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.8f)
                .height(35.dp)
                .clip(RoundedCornerShape(30.dp)), color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = vPadding)
            ) {

                Text(
                    text = buildAnnotatedString {
                        coinFlipHistory.forEach { result ->
                            withStyle(style = SpanStyle(color = if (result == "H") Color.Green else Color.Red)) {
                                append("$result ")
                            }
                        }
                    },
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontSize = 16.sp),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 0.dp, horizontal = hPadding)
                )

            }
        }
    }
}