package mtglifeappcompose.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtglifeappcompose.R

@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    size: Dp = 130.dp,
    backgroundColor: Color = Color.DarkGray,
    imageResource: Painter = painterResource(id = R.drawable.placeholder_icon),
    text: String = "",
    color: Color = Color.White,
    enabled: Boolean = true,
    visible: Boolean = true,
    onPress: () -> Unit = {},
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onDoubleTap: () -> Unit = {}
) {
    val cornerRadius = size / 6
    val margin = size / 12
    val imageSize = if (text.isNotEmpty()) size - margin * 2.5f else size - margin * 1.5f
    val fontSize = (size / 10).value.sp
    val matrix = ColorMatrix().apply { setToScale(color.red, color.green, color.blue, color.alpha) }
    val invisMatrix = ColorMatrix().apply { setToScale(color.red, color.green, color.blue, 0f) }

    Box(modifier = modifier
        .wrapContentSize()
        .pointerInput(enabled, visible) {
            detectTapGestures(
                onPress = {
                    if (enabled && visible) {
                        onPress()
                    }
                },
                onTap = {
                    if (enabled && visible) {
                        onTap()
                    }
                },
                onLongPress = {
                    if (enabled && visible) {
                        onLongPress()
                    }
                },
                onDoubleTap = {
                    if (enabled && visible) {
                        onDoubleTap()
                    }
                }
            )
        }
        .clip(RoundedCornerShape(cornerRadius))
        .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(vertical = margin / 2),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(imageSize),
                painter = imageResource,
                contentDescription = "settings button image",
                colorFilter = if (visible) ColorFilter.colorMatrix(matrix) else ColorFilter.colorMatrix(
                    invisMatrix
                )
            )
            Text(
                text = text,
                color = if (visible) color else Color.Transparent,
                style = TextStyle(fontSize = fontSize)
            )
        }
    }
}

