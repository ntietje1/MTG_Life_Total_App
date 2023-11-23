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

    Box(modifier = modifier
        .wrapContentSize()
        .pointerInput(Unit) {
            if (enabled && visible) {
                detectTapGestures(
                    onPress = {
                        onPress()
                    },
                    onTap = {
                        onTap()
                    },
                    onLongPress = {
                        onLongPress()
                    },
                    onDoubleTap = {
                        onDoubleTap()
                    }
                )
            }
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
                painter = imageResource,
                contentDescription = "settings button image",
                modifier = Modifier
                    .size(imageSize)
            )
            Text(
                text = text,
                color = Color.White,
                style = TextStyle(fontSize = fontSize)
            )
        }
    }
}

