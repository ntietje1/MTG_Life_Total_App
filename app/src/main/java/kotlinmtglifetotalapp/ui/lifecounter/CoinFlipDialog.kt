package kotlinmtglifetotalapp.ui.lifecounter

import SettingsDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinmtglifetotalapp.R
import com.wajahatkarim.flippable.FlipAnimationType
import com.wajahatkarim.flippable.Flippable
import com.wajahatkarim.flippable.FlippableState
import com.wajahatkarim.flippable.rememberFlipController
import kotlin.random.Random

@Composable
fun CoinFlipDialogBox() {
    val history = remember { mutableStateListOf<String>() }

            Box(modifier = Modifier.fillMaxSize()) {
                CoinFlippable(history)
                FlipHistory(
                    history,
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 0.dp)
                )
            }
}

@Composable
fun CoinFlipDialog(onDismiss: () -> Unit = {}) {
    val history = remember { mutableStateListOf<String>() }
    SettingsDialog(
        content = {
            Box(modifier = Modifier.fillMaxSize()) {
                CoinFlippable(history)
                FlipHistory(
                    history,
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 0.dp)
                )
            }
        },
        onDismiss = onDismiss
    )
}

@Composable
fun CoinFlippable(history: MutableList<String>) {
    val flipEnabled by remember { mutableStateOf(true) }
    val initialDuration = 300
    var duration by remember { mutableIntStateOf(initialDuration) }
    var flipAnimationType by remember { mutableStateOf(FlipAnimationType.VERTICAL_ANTI_CLOCKWISE) }
    val totalFlips = 2 // + 1
    var flipCount by remember { mutableIntStateOf(totalFlips) }
    val flipController = rememberFlipController()

    fun addToHistory(v: String) {
        if (history.size > 17) {
            history.removeAt(0)
        }
        history.add(v)
    }

    fun decrementFlipCount() {
        flipCount--
        duration += 90
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
            flipAnimationType =
                if (flipAnimationType == FlipAnimationType.VERTICAL_ANTI_CLOCKWISE) {
                    FlipAnimationType.VERTICAL_CLOCKWISE
                } else {
                    FlipAnimationType.VERTICAL_ANTI_CLOCKWISE
                }
            flipController.flip()
            decrementFlipCount()
        } else {
            addToHistory(if (currentSide == FlippableState.FRONT) "H" else "T")
            resetCount()
        }
    }

    Flippable(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp, top = 0.dp, start = 30.dp, end = 30.dp),
        flipController = flipController,
        flipDurationMs = duration,
        flipEnabled = flipEnabled,
        flipAnimationType = flipAnimationType,
        frontSide = {
            Image(
                painter = painterResource(id = R.drawable.heads),
                contentDescription = "Front Side",
                modifier = Modifier.fillMaxSize()
            )
        },
        backSide = {
            Image(
                painter = painterResource(id = R.drawable.tails),
                contentDescription = "Back Side",
                modifier = Modifier.fillMaxSize()
            )
        },
        onFlippedListener = { currentSide ->
            continueFlip(currentSide)
        }
    )

}

@Composable
fun FlipHistory(coinFlipHistory: MutableList<String>, modifier: Modifier = Modifier) {
    val hPadding = 10.dp
    val vPadding = 5.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            text = "Flip History",
            color = Color.White, // Set the text color to white or another contrasting color
            fontWeight = FontWeight.Bold,
            style = TextStyle(fontSize = 20.sp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = vPadding)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(35.dp)
                .clip(RoundedCornerShape(0.dp)),
            color = Color(0x60, 0x60, 0x60)
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