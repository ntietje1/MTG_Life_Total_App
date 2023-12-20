package mtglifeappcompose.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
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
    size: Dp = 140.dp,
    shape: Shape = RectangleShape,
    backgroundColor: Color = Color.Transparent,
    imageResource: Painter = painterResource(id = R.drawable.placeholder_icon),
    text: String = "",
    mainColor: Color = Color.White,
    enabled: Boolean = true,
    visible: Boolean = true,
    onPress: () -> Unit = {},
    onTap: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onDoubleTap: () -> Unit = {},
    overlay: @Composable () -> Unit = {}
) {
    val margin = size / 9f
    val imageSize = if (text.isNotEmpty()) size - margin * 2 else size - margin
    val fontSize = margin.value.sp
    val matrix = ColorMatrix().apply {
        setToScale(
            mainColor.red, mainColor.green, mainColor.blue, mainColor.alpha
        )
    }
    val invisMatrix =
        ColorMatrix().apply { setToScale(mainColor.red, mainColor.green, mainColor.blue, 0f) }

    Box(modifier = modifier
        .size(size)
        .pointerInput(enabled, visible) {
            detectTapGestures(onPress = {
                if (enabled && visible) {
                    onPress()
                }
            }, onTap = {
                if (enabled && visible) {
                    onTap()
                }
            }, onLongPress = {
                if (enabled && visible) {
                    onLongPress()
                }
            }, onDoubleTap = {
                if (enabled && visible) {
                    onDoubleTap()
                }
            })
        }
        .clip(shape)
        .background(backgroundColor)) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .padding(top = margin)
            ) {
                Image(
                    modifier = Modifier.size(imageSize),
                    painter = imageResource,
                    contentDescription = "settings button image",
                    colorFilter = if (visible) ColorFilter.colorMatrix(matrix) else ColorFilter.colorMatrix(
                        invisMatrix
                    )
                )
                overlay()
            }
            Text(
                text = text,
                color = if (visible) mainColor else Color.Transparent,
                style = if (visible) TextStyle(fontSize = fontSize) else TextStyle(fontSize = 0.sp)
            )

        }
    }
}

