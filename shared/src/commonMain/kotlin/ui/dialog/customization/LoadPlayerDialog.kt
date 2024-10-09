package ui.dialog.customization

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.Player
import theme.scaledSp
import theme.textShadowStyle
import ui.dialog.GridDialogContent
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonBackground

@Composable
fun LoadPlayerDialogContent(modifier: Modifier = Modifier, playerList: List<Player>, onPlayerSelected: (Player) -> Unit, onPlayerDeleted: (Player) -> Unit) {
    BoxWithConstraints(
        Modifier.wrapContentSize()
    ) {
        val buttonWidth = remember { (maxWidth / 2f) - 16.dp }
        GridDialogContent(
            modifier = modifier,
            title = "Load Profile",
            columns = 2
        ) {
            items(
                items = playerList,
                key = { player -> player.hashCode() }
            ) { player ->
                SmallPlayerButtonPreview(
                    name = player.name,
                    state = PBState.NORMAL,
                    isDead = false,
                    imageUri = player.imageString,
                    backgroundColor = player.color,
                    accentColor = player.textColor,
                    modifier = Modifier.padding(8.dp).width(buttonWidth).aspectRatio(1.75f).pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                onPlayerDeleted(player)
                            },
                            onTap = {
                                onPlayerSelected(player)
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
    modifier: Modifier = Modifier,
    name: String,
    state: PBState,
    isDead: Boolean,
    imageUri: String?,
    backgroundColor: Color,
    accentColor: Color,
    ) {
    BoxWithConstraints(
        modifier = modifier.clip(RoundedCornerShape(12)),
    ) {
        val largeTextSize = remember { (maxHeight.value / 2.8f + maxWidth.value / 6f + 30) * 0.45f }
        val largeTextPadding = remember { (largeTextSize / 10f).dp }
        PlayerButtonBackground(
            state = state,
            imageUri = imageUri,
            color = backgroundColor,
            isDead = isDead,
        )
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