package mtglifeappcompose.composable.dialog

import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtglifeappcompose.R
import mtglifeappcompose.composable.repeatingClickable


@Composable
fun CounterDialogContent(
    modifier: Modifier = Modifier,
    counters: ArrayList<MutableIntState>,
    onDismiss: () -> Unit,
    showCounterDialog: MutableState<Boolean>
) {

    val haptic = LocalHapticFeedback.current

    val imageResources = arrayListOf(
        painterResource(id = R.drawable.w_icon),
        painterResource(id = R.drawable.u_icon),
        painterResource(id = R.drawable.b_icon),
        painterResource(id = R.drawable.r_icon),
        painterResource(id = R.drawable.g_icon),
        painterResource(id = R.drawable.c_icon),
        painterResource(id = R.drawable.storm_icon)
    )

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                modifier = Modifier.alpha(0.9f),
                text = "Floating Mana",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp
            )
        }

        items(counters.size - 1) { index ->
            SingleCounter(
                imageResource = imageResources[index], counter = counters[index]
            )
        }

        item {
            Text(
                modifier = Modifier
                    .alpha(0.9f)
                    .padding(top = 2.dp),
                text = "Storm Count",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp
            )
        }

        item {
            SingleCounter(
                imageResource = imageResources.last(), counter = counters.last()
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


@Composable
fun SingleCounter(
    modifier: Modifier = Modifier,
    imageResource: Painter = painterResource(id = R.drawable.placeholder_icon),
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
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .repeatingClickable(interactionSource = interactionSource,
                    enabled = true,
                    onPress = { onDecrement() }),
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize(0.7f)
                    .align(Alignment.CenterEnd),
                painter = painterResource(R.drawable.minus_icon),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.width(25.dp))

        Image(
            painter = imageResource, contentDescription = null, modifier = Modifier.size(55.dp)
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            modifier = Modifier.width(50.dp),
            text = "${counter.value}",
            textAlign = TextAlign.Justify,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(0.dp))

        Box(
            modifier = Modifier
                .size(60.dp)
                .repeatingClickable(interactionSource = interactionSource,
                    enabled = true,
                    onPress = { onIncrement() }),
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize(0.7f)
                    .align(Alignment.CenterStart),
                painter = painterResource(R.drawable.plus_icon),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
        }
    }
}