package ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.b_icon
import lifelinked.shared.generated.resources.c_icon
import lifelinked.shared.generated.resources.g_icon
import lifelinked.shared.generated.resources.minus_icon
import lifelinked.shared.generated.resources.placeholder_icon
import lifelinked.shared.generated.resources.plus_icon
import lifelinked.shared.generated.resources.r_icon
import lifelinked.shared.generated.resources.storm_icon
import lifelinked.shared.generated.resources.u_icon
import lifelinked.shared.generated.resources.w_icon
import org.jetbrains.compose.resources.vectorResource
import theme.LocalDimensions
import theme.scaledSp
import ui.components.ResetButton
import ui.modifier.bounceClick
import ui.modifier.repeatingClickable


const val COUNTER_DIALOG_ENTRIES = 7

@Composable
fun CounterDialogContent(
    modifier: Modifier = Modifier,
    counters: List<Int>,
    incrementCounter: (Int, Int) -> Unit,
    resetCounters: () -> Unit
) {

    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current

    val counterResources = arrayListOf(
        Triple(vectorResource(Res.drawable.w_icon), Color(0xFFfffbd5), Color(0xFF211d15)),
        Triple(vectorResource(Res.drawable.u_icon), Color(0xFFaae0fa), Color(0xFF061922)),
        Triple(vectorResource(Res.drawable.b_icon), Color(0xFFcbc2bf), Color(0xFF130c0e)),
        Triple(vectorResource(Res.drawable.r_icon), Color(0xFFf9aa8f), Color(0xFF200000)),
        Triple(vectorResource(Res.drawable.g_icon), Color(0xFF9bd3ae), Color(0xFF00160b)),
        Triple(vectorResource(Res.drawable.c_icon), Color(0xFFccc2c0), Color(0xFF130c0e)),
        Triple(vectorResource(Res.drawable.storm_icon), Color(0xFFffd84c), Color(0xFF2b2515))
    )

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val counterSize = remember(Unit) { maxHeight / 11f }
        val padding = remember(Unit) { (counterSize / 5f) }
        val textSize = remember(Unit) { (maxWidth / 25f).value }
        LazyColumn(
            modifier = Modifier, verticalArrangement = Arrangement.spacedBy(padding, Alignment.CenterVertically), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(0.dp))
            }
            item {
                Text(
                    modifier = Modifier.alpha(0.9f), text = "Floating Mana", color = MaterialTheme.colorScheme.onPrimary, fontSize = textSize.scaledSp
                )
            }

            items(counters.size - 1) { index ->
                SingleCounter(
                    modifier = Modifier.height(counterSize).fillMaxWidth(0.8f),
                    imageVector = counterResources[index].first,
                    backgroundColor = counterResources[index].second,
                    buttonColor = counterResources[index].third,
                    counter = counters[index],
                    incrementCounter = { incrementCounter(index, it) }
                )
            }

            item {
                Text(
                    modifier = Modifier.alpha(0.9f).padding(top = 2.dp), text = "Storm Count", color = MaterialTheme.colorScheme.onPrimary, fontSize = textSize.scaledSp
                )
            }

            item {
                SingleCounter(
                    modifier = Modifier.height(counterSize).fillMaxWidth(0.8f),
                    imageVector = counterResources.last().first,
                    backgroundColor = counterResources.last().second,
                    buttonColor = counterResources.last().third,
                    counter = counters.last(),
                    incrementCounter = { incrementCounter(counters.lastIndex, it) }
                )
            }

            item {
                ResetButton(modifier = Modifier.height(counterSize / 2f).padding(top = dimensions.paddingSmall), onReset = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    resetCounters()
                })
            }
        }
    }
}

@Composable
fun SingleCounter(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Transparent,
    buttonColor: Color = Color.White,
    imageVector: ImageVector = vectorResource(Res.drawable.placeholder_icon),
    counter: Int,
    incrementCounter: (Int) -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    fun onIncrement() {
        incrementCounter(1)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    fun onDecrement() {
        incrementCounter(-1)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    BoxWithConstraints(
        Modifier
            .wrapContentSize()
            .bounceClick(
                bounceAmount = 0.01f,
                interactionSource = interactionSource,
                repeatEnabled = true
            )
    ) {
        val textSize = remember(Unit) { (maxWidth / 15f).value }
        val padding = remember(Unit) { (maxWidth / 30f) }
        Row(
            modifier = modifier.background(backgroundColor, RoundedCornerShape(20)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.aspectRatio(1.0f).fillMaxSize().repeatingClickable(interactionSource = interactionSource, enabled = true, onPress = { onDecrement() }),
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(0.7f).align(Alignment.CenterEnd),
                    imageVector = vectorResource(Res.drawable.minus_icon),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(buttonColor)
                )
            }

            Row(
                modifier = Modifier.fillMaxSize().weight(0.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    imageVector = imageVector, contentDescription = null, modifier = Modifier.aspectRatio(1.0f).fillMaxHeight().padding(vertical = padding / 2f)
                )

                Box(
                    modifier = Modifier.aspectRatio(1.0f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(unbounded = true),
                        text = "$counter",
                        textAlign = TextAlign.Justify,
                        color = buttonColor,
                        fontSize = textSize.scaledSp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier.aspectRatio(1.0f).fillMaxSize().repeatingClickable(interactionSource = interactionSource, enabled = true, onPress = { onIncrement() }),
            ) {
                Image(
                    modifier = Modifier.fillMaxSize(0.7f).align(Alignment.CenterStart),
                    imageVector = vectorResource(Res.drawable.plus_icon),
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(buttonColor)
                )
            }
        }
    }
}