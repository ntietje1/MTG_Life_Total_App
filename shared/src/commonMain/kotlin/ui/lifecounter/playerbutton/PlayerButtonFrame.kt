package ui.lifecounter.playerbutton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import domain.system.SystemManager
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.image_error_icon
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import theme.LocalDimensions
import theme.brightenColor
import theme.ghostify
import theme.saturateColor
import ui.components.SettingsButton
import ui.modifier.animatedBorderCard

@Composable
fun MonarchyIndicator(
    modifier: Modifier = Modifier,
    monarch: Boolean = false,
    borderWidth: Dp,
    content: @Composable () -> Unit = {}
) {
    val duration = (7500 / SystemManager.getAnimationCorrectionFactor()).toInt()
    val colors = listOf(
        Color.Unspecified,
        Color(255, 191, 8),
        Color(255, 191, 8),
        Color(255, 191, 8),
    )
    Box(
        modifier = modifier.then(
            if (monarch) {
                Modifier.animatedBorderCard(
                    shape = RoundedCornerShape(12),
                    borderWidth = borderWidth,
                    colors = colors,
                    animationDuration = duration
                )
            } else {
                Modifier.padding(borderWidth)
            }
        )
    ) {
        content()
    }
}

@Composable
fun PlayerButtonBackground(
    modifier: Modifier = Modifier,
    state: PBState,
    imageUri: String?,
    color: Color,
    isDead: Boolean,
    showError: Boolean = false
) {
    val dimensions = LocalDimensions.current

    var errored = false
    val c = remember(color, isDead, state) {
        val ghostify = isDead && state != PBState.SELECT_FIRST_PLAYER
        when {
            state == PBState.COMMANDER_RECEIVER && !ghostify -> color.saturateColor(0.2f).brightenColor(0.3f)
            state == PBState.COMMANDER_RECEIVER && ghostify -> color.saturateColor(0.2f).brightenColor(0.3f).ghostify()
            state == PBState.COMMANDER_DEALER && !ghostify -> color.saturateColor(0.5f).brightenColor(0.6f)
            state == PBState.COMMANDER_DEALER && ghostify -> color.saturateColor(0.5f).brightenColor(0.6f).ghostify()
            ghostify -> color.ghostify()
            else -> color
        }
    }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = c
    ) {}
    if (imageUri != null) {
        KamelImage(
            modifier = modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            resource = { asyncPainterResource(data = imageUri) },
            contentDescription = "Player uploaded image",
            onLoading = { progress ->
                if (progress == 0.0f) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = dimensions.borderSmall,
                    )
                } else {
                    CircularProgressIndicator(
                        progress = { progress },
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = dimensions.borderSmall,
                    )
                }
            },
            onFailure = {
                errored = true
                if (showError) {
                    Box(
                        modifier = modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        SettingsButton(
                            modifier = Modifier.fillMaxHeight(0.4f),
                            backgroundColor = Color.Transparent,
                            mainColor = MaterialTheme.colorScheme.onPrimary,
                            imageVector = vectorResource(Res.drawable.image_error_icon),
                            visible = true,
                            onPress = {}
                        )
                    }
                }
            }
        )
        val b = remember(isDead, state) {
            val ghostify = isDead && state != PBState.SELECT_FIRST_PLAYER
            when {
                state == PBState.COMMANDER_RECEIVER && !ghostify -> Color.hsl(0f, 0f, 0.1f, 0.7f)
                state == PBState.COMMANDER_RECEIVER && ghostify -> Color.hsl(0f, 0f, 0.2f, 0.9f)
                state == PBState.COMMANDER_DEALER && !ghostify -> Color.hsl(0f, 0f, 0.0f, 0.7f)
                state == PBState.COMMANDER_DEALER && ghostify -> Color.hsl(0f, 0f, 0.1f, 0.9f)
                ghostify -> Color.Gray.copy(alpha = 0.7f)
                else -> Color.Transparent
            }
        }
        if (!errored) {
            Box(
                modifier = modifier.fillMaxSize().background(color = b),
                contentAlignment = Alignment.Center
            ) {}
        }
    }
}

@Composable
fun PlayerStateButton(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    visible: Boolean,
    iconResource: DrawableResource,
    color: Color,
    onPress: () -> Unit,
) {
    SettingsButton(
        modifier = modifier.size(size),
        backgroundColor = Color.Transparent,
        mainColor = color,
        imageVector = vectorResource(iconResource),
        visible = visible,
        onPress = onPress
    )
}
