package ui.lifecounter.playerbutton

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import domain.common.NumberWithRecentChange
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.commander_solid_icon
import lifelinked.shared.generated.resources.heart_solid_icon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import theme.LocalDimensions
import theme.scaledSp
import theme.textShadowStyle
import ui.components.SettingsButton
import ui.modifier.repeatingClickable
import kotlin.math.pow

@Composable
fun CommanderDamageNumber(
    modifier: Modifier = Modifier,
    name: String,
    textColor: Color,
    firstValue: NumberWithRecentChange,
    secondValue: NumberWithRecentChange?,
) {
    BoxWithConstraints(modifier = modifier) {
        val dividerOffset = remember { maxHeight / 12f }
        val dimensions = LocalDimensions.current
        val numberWidth = remember { maxWidth * 0.4f }
        val padding = remember { maxWidth * 0.05f }

        if (secondValue != null) {
            VerticalDivider(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight(0.6f)
                    .width(dimensions.borderThin)
                    .offset(y = dividerOffset)
                    .alpha(0.4f),
                color = textColor
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.fillMaxHeight()
                    .then(
                        if (secondValue != null) {
                            Modifier.width(numberWidth).padding(end = padding * 2, start = padding)
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                SingleCommanderDamageNumber(
                    modifier = Modifier.then(
                        if (secondValue != null) {
                            Modifier.fillMaxSize().padding(padding)
                        } else {
                            Modifier.fillMaxSize()
                        }
                    ),
                    name = name,
                    textColor = textColor,
                    value = firstValue
                )
            }
            if (secondValue == null) return@BoxWithConstraints
            Box(
                modifier = Modifier.fillMaxHeight().width(numberWidth).padding(start = padding * 2, end = padding),
                contentAlignment = Alignment.Center
            ) {
                SingleCommanderDamageNumber(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    name = name,
                    textColor = textColor,
                    value = secondValue
                )
            }
        }
    }
}

@Composable
fun SingleCommanderDamageNumber(
    modifier: Modifier = Modifier,
    name: String,
    textColor: Color,
    value: NumberWithRecentChange,
) {
    val iconResource = remember { Res.drawable.commander_solid_icon }

    NumericValue(
        modifier = modifier,
        iconResource = iconResource,
        name = name,
        textColor = textColor,
        value = value
    )
}

@Composable
fun LifeNumber(
    modifier: Modifier = Modifier,
    textColor: Color,
    name: String,
    value: NumberWithRecentChange
) {
    val iconResource = remember { Res.drawable.heart_solid_icon }

    NumericValue(
        modifier = modifier,
        textColor = textColor,
        name = name,
        value = value,
        iconResource = iconResource,
    )
}

@Composable
fun NumericValue(
    modifier: Modifier = Modifier,
    textColor: Color,
    name: String,
    value: NumberWithRecentChange,
    iconResource: DrawableResource,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val largeText = value.number.toString()
        val recentChangeText = if (value.recentChange == 0) ""
        else if (value.recentChange > 0) "+${value.recentChange}"
        else "${value.recentChange}"

        val aspectRatio = maxWidth / maxHeight
        val heightWeight = (1f / (1f + aspectRatio)).pow(1.3f)
        val widthWeight = (1f - heightWeight).pow(1.3f)

        val largeTextSize = (
                maxHeight.value / 3.5f * heightWeight +
                        maxWidth.value / 9f * widthWeight +
                        (maxHeight.value * maxWidth.value).pow(0.525f) / 4.15f +
                        10f
                )

        val smallTextSize = largeTextSize / 12f + 10f
        val smallTextPadding = largeTextSize.dp / 20f
        val recentChangeSize = 5f + largeTextSize / 6f
        val iconSize = (largeTextSize / 6f + 10f).dp

        Text(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = smallTextPadding * (3 - heightWeight.pow(2) * 2)),
            text = name,
            color = textColor,
            fontSize = smallTextSize.scaledSp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = textShadowStyle()
        )
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = smallTextPadding * 4)
                .wrapContentSize(unbounded = true),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier = Modifier.wrapContentHeight(unbounded = true),
                text = largeText,
                color = textColor,
                fontSize = largeTextSize.scaledSp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = textShadowStyle()
            )
            Spacer(modifier = Modifier.weight(0.2f))
            Text(
                modifier = Modifier.weight(0.8f).padding(start = recentChangeSize.dp).wrapContentSize(unbounded = true),
                text = recentChangeText,
                color = textColor,
                fontSize = recentChangeSize.scaledSp,
                maxLines = 1,
                style = textShadowStyle()
            )
        }
        SettingsButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(iconSize)
                .offset(y = smallTextPadding * (1.5f + heightWeight.pow(2) * 2)),
            backgroundColor = Color.Transparent,
            mainColor = textColor,
            imageVector = vectorResource(iconResource),
            enabled = false
        )
    }
}

@Composable
fun LifeChangeButtons(
    modifier: Modifier = Modifier,
    onIncrementLife: () -> Unit,
    onDecrementLife: () -> Unit
) {
    Column(modifier = modifier) {
        CustomIncrementButton(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.5f),
            onIncrementLife = onIncrementLife,
        )

        CustomIncrementButton(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(1.0f),
            onIncrementLife = onDecrementLife,
        )
    }
}

@Composable
private fun CustomIncrementButton(
    modifier: Modifier = Modifier,
    onIncrementLife: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val ripple = remember { ripple(color = Color.Black) }
    Box(
        modifier = modifier.repeatingClickable(
            interactionSource = interactionSource,
            indication = ripple,
            enabled = true,
            onPress = onIncrementLife
        )
    )
}
