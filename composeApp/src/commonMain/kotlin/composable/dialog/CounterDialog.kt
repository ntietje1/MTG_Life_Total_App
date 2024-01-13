package composable.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import composable.modifier.bounceClick
import composable.modifier.repeatingClickable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import theme.scaledSp


/**
 * A dialog that allows the user to keep track of floating mana and storm count
 * @param modifier the modifier for this composable
 * @param counters the list of counters to display
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun CounterDialogContent(
    modifier: Modifier = Modifier, counters: List<MutableIntState>
) {

    val haptic = LocalHapticFeedback.current

    val counterResources = arrayListOf(
        Triple(painterResource("w_icon.xml"), Color(0xFFfffbd5), Color(0xFF211d15)),
        Triple(painterResource("u_icon.xml"), Color(0xFFaae0fa), Color(0xFF061922)),
        Triple(painterResource("b_icon.xml"), Color(0xFFcbc2bf), Color(0xFF130c0e)),
        Triple(painterResource("r_icon.xml"), Color(0xFFf9aa8f), Color(0xFF200000)),
        Triple(painterResource("g_icon.xml"), Color(0xFF9bd3ae), Color(0xFF00160b)),
        Triple(painterResource("c_icon.xml"), Color(0xFFccc2c0), Color(0xFF130c0e)),
        Triple(painterResource("storm_icon.xml"), Color(0xFFffd84c), Color(0xFF2b2515))
    )

    LazyColumn(
        modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                modifier = Modifier.alpha(0.9f), text = "Floating Mana", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.scaledSp
            )
        }

        items(counters.size - 1) { index ->
            SingleCounter(
                imageResource = counterResources[index].first, backgroundColor = counterResources[index].second, buttonColor = counterResources[index].third, counter = counters[index]
            )
        }

        item {
            Text(
                modifier = Modifier.alpha(0.9f).padding(top = 2.dp), text = "Storm Count", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.scaledSp
            )
        }

        item {
            SingleCounter(
                imageResource = counterResources.last().first, backgroundColor = counterResources.last().second, buttonColor = counterResources.last().third, counter = counters.last()
            )
        }

        item {
            ResetButton(modifier = Modifier.padding(top = 20.dp), onReset = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                for (value in counters) {
                    value.value = 0
                }
            })
        }
    }
}

/**
 * A single counter that can be incremented
 * @param modifier the modifier for this composable
 * @param imageResource the image resource of the counter
 * @param counter the counter to display
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun SingleCounter(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    buttonColor: Color = Color.White,
    imageResource: Painter = painterResource("placeholder_icon.xml"),
    counter: MutableIntState
) {

    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    fun onIncrement() {
        counter.value++
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun onDecrement() {
        counter.value--
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Row(
        modifier = modifier.bounceClick(0.01f).background(backgroundColor, RoundedCornerShape(10.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(60.dp).repeatingClickable(interactionSource = interactionSource, enabled = true, onPress = { onDecrement() }),
        ) {
            Image(
                modifier = Modifier.fillMaxSize(0.7f).align(Alignment.CenterEnd),
                painter = painterResource("minus_icon.xml"),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                colorFilter = ColorFilter.tint(buttonColor)
            )
        }

        Spacer(modifier = Modifier.width(25.dp))

        Image(
            painter = imageResource, contentDescription = null, modifier = Modifier.size(55.dp)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            modifier = Modifier.width(50.dp), text = "${counter.value}", textAlign = TextAlign.Justify, color = buttonColor, fontSize = 28.scaledSp, fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(0.dp))

        Box(
            modifier = Modifier.size(60.dp).repeatingClickable(interactionSource = interactionSource, enabled = true, onPress = { onIncrement() }),
        ) {
            Image(
                modifier = Modifier.fillMaxSize(0.7f).align(Alignment.CenterStart),
                painter = painterResource("plus_icon.xml"),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                colorFilter = ColorFilter.tint(buttonColor)
            )
        }
    }
}