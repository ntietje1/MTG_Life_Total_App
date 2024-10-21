package ui.dialog.customization

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.Player
import kotlinx.coroutines.delay
import theme.scaledSp
import theme.textShadowStyle
import ui.dialog.GridDialogContent
import ui.dialog.WarningDialog
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonBackground
import ui.modifier.routePointerChangesTo

@Composable
fun LoadPlayerDialogContent(
    modifier: Modifier = Modifier, playerList: List<Player>,
    onPlayerSelected: (Player) -> Unit, onPlayerDeleted: (Player) -> Unit
) {
    var showDeletePlayerWarning by remember { mutableStateOf(false) }
    var toBeDeletedPlayer by remember { mutableStateOf<Player?>(null) }
    var highlightedPlayer by remember { mutableStateOf<Player?>(null) }
    val haptic = LocalHapticFeedback.current

    if (showDeletePlayerWarning) {
        WarningDialog(title = "Warning",
            message = "This will delete the player profile. Proceed?",
            optionOneEnabled = true,
            optionTwoEnabled = true,
            optionOneMessage = "Delete",
            optionTwoMessage = "Cancel",
            onOptionOne = {
                onPlayerDeleted(toBeDeletedPlayer!!)
                toBeDeletedPlayer = null
                highlightedPlayer = null
                showDeletePlayerWarning = false
                haptic.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
            },
            onDismiss = {
                showDeletePlayerWarning = false
                highlightedPlayer = null
            })
    }



    BoxWithConstraints(
        Modifier.wrapContentSize()
    ) {
        val buttonWidth = remember { (maxWidth / 2f) - 16.dp }
        GridDialogContent(
            modifier = modifier, title = "Load Profile", columns = 2
        ) {
            items(items = playerList, key = { player -> player.hashCode() }) { player ->
                var isLongPressed by remember { mutableStateOf(false) }
                SmallPlayerButtonPreview(
                    name = player.name,
                    state = PBState.NORMAL,
                    isDead = false,
                    imageUri = player.imageString,
                    backgroundColor = player.color,
                    accentColor = player.textColor,
                    overlayColor = if (isLongPressed || highlightedPlayer == player) Color.Red.copy(alpha = 0.4f) else Color.Transparent,
                    modifier = Modifier.padding(8.dp).width(buttonWidth).aspectRatio(1.75f).graphicsLayer(
                        alpha = if (isLongPressed) 0.6f else 1f
                    ).pointerInput(Unit) {
                        routePointerChangesTo(
                            onDown = {},
                            onLongPress = {
                                delay(500)
                                isLongPressed = true
                                haptic.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                            }, onUp = {
                                if (isLongPressed) {
                                    toBeDeletedPlayer = player
                                    highlightedPlayer = player
                                    showDeletePlayerWarning = true
                                } else {
                                    onPlayerSelected(player)
                                }
                                isLongPressed = false
                            }, onMove = { pointerInputChange ->
                                if (isLongPressed && pointerInputChange.isOutOfBounds(size = size, extendedTouchPadding = extendedTouchPadding)) {
                                    isLongPressed = false
                                }
                            }
                        )
                    },
                )
            }
        }
    }
}

@Composable
fun SmallPlayerButtonPreview(
    modifier: Modifier = Modifier, name: String, state: PBState, isDead: Boolean, imageUri: String?, backgroundColor: Color, accentColor: Color, overlayColor: Color = Color.Transparent
) {
    val animatedOverlayColor by animateColorAsState(targetValue = overlayColor)

    BoxWithConstraints(
        modifier = modifier.clip(RoundedCornerShape(12))
    ) {
        val largeTextSize = remember { (maxHeight.value / 2.8f + maxWidth.value / 6f + 30) * 0.45f }
        val largeTextPadding = remember { (largeTextSize / 10f).dp }
        PlayerButtonBackground(
            state = state,
            imageUri = imageUri,
            color = backgroundColor,
            isDead = isDead,
        )
        Box(
            modifier = Modifier.matchParentSize().background(animatedOverlayColor)
        ) {
            Text(
                modifier = Modifier.wrapContentHeight().fillMaxWidth().padding(horizontal = largeTextPadding).align(Alignment.Center),
                text = name,
                color = accentColor,
                fontSize = largeTextSize.scaledSp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                style = textShadowStyle()
            )
        }
    }
}