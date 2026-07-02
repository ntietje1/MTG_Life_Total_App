package ui.lifecounter.playerbutton

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.add_icon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import theme.LocalDimensions
import theme.defaultTextStyle
import theme.scaledSp
import theme.textShadowStyle
import ui.components.SettingsButton
import ui.modifier.bounceClick

@Composable
fun CounterWrapper(
    modifier: Modifier = Modifier,
    textColor: Color,
    text: String,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val smallPadding = maxHeight / 20f
        val smallTextSize = maxHeight.value.scaledSp / 12f
        val dimensions = LocalDimensions.current
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.wrapContentSize(unbounded = true).padding(
                    top = 0.dp,
                    bottom = smallPadding / 4f
                ),
                text = text,
                color = textColor,
                fontSize = smallTextSize,
                textAlign = TextAlign.Center,
                style = defaultTextStyle()
            )
            Box(
                Modifier.fillMaxSize().background(
                    Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12)
                ).border(
                    dimensions.borderThin,
                    textColor.copy(alpha = 0.9f),
                    RoundedCornerShape(12)
                ).clip(RoundedCornerShape(12))
            ) {
                content()
            }
        }
    }
}

@Composable
fun AddCounter(
    modifier: Modifier = Modifier,
    textColor: Color,
    contentDescription: String,
    onTap: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current
    BoxWithConstraints(modifier.bounceClick(0.0125f).background(
        Color.Black.copy(0.2f),
        shape = RoundedCornerShape(15)
    ).border(
        dimensions.borderThin,
        textColor.copy(alpha = 0.9f),
        RoundedCornerShape(15)
    ).pointerInput(Unit) {
        detectTapGestures {
            onTap()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }.semantics { this.contentDescription = contentDescription }) {
        val iconSize = maxHeight / 2.5f
        SettingsButton(
            modifier = Modifier.align(Alignment.Center).size(iconSize),
            imageVector = vectorResource(Res.drawable.add_icon),
            backgroundColor = Color.Transparent,
            mainColor = textColor,
            shadowEnabled = true,
            enabled = false
        )
    }
}

@Composable
fun Counter(
    modifier: Modifier = Modifier,
    textColor: Color,
    iconResource: DrawableResource,
    value: Int,
    valueContentDescription: String,
    incrementContentDescription: String,
    decrementContentDescription: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val dimensions = LocalDimensions.current

    BoxWithConstraints(
        modifier.bounceClick(0.0125f).background(
            Color.Black.copy(0.2f),
            shape = RoundedCornerShape(15)
        ).border(
            dimensions.borderThin,
            textColor.copy(alpha = 0.9f),
            RoundedCornerShape(15)
        ).clip(RoundedCornerShape(15))
    ) {
        val textSize = (maxHeight.value / 2.8f + maxWidth.value / 6f + 30).scaledSp / 1.5f
        val topPadding = maxHeight / 10f
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f).background(Color.White.copy(alpha = 0.04f)).pointerInput(Unit) {
                detectTapGestures {
                    onIncrement()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }.semantics { contentDescription = incrementContentDescription })
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(1.0f).background(Color.Black.copy(alpha = 0.04f)).pointerInput(Unit) {
                detectTapGestures {
                    onDecrement()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }.semantics { contentDescription = decrementContentDescription })
        }

        Column(
            Modifier.fillMaxSize().semantics { contentDescription = valueContentDescription },
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(topPadding))
            Text(
                text = value.toString(),
                color = textColor,
                fontSize = textSize,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.wrapContentSize(),
                style = textShadowStyle()
            )
            SettingsButton(
                imageVector = vectorResource(iconResource),
                modifier = Modifier.fillMaxSize(0.35f).aspectRatio(1.0f).padding(bottom = topPadding * 0.85f),
                mainColor = textColor,
                backgroundColor = Color.Transparent,
                shadowEnabled = true,
                enabled = false
            )
        }
    }
}
