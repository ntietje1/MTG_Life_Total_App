package mtglifeappcompose.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtglifeappcompose.R
import mtglifeappcompose.data.PlayerDataManager

@Composable
fun SettingsButton(
    size: Dp = 130.dp,
    backgroundColor: Color = Color.DarkGray,
    imageResource: Painter = painterResource(id = R.drawable.placeholder_icon),
    text: String = "placeholder",
    onClick: () -> Unit = {}
) {
    val cornerRadius = size/6
    val margin = size/12
    val imageSize = if (text.isNotEmpty()) size - margin * 2.5f else size - margin * 1.5f
    val fontSize = (size / 10).value.sp

    Box(
        modifier = Modifier.wrapContentSize().padding(top = margin/2)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onClick()
                    },
                )
            }
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .graphicsLayer()
    ) {
        Column(
            modifier = Modifier.wrapContentSize().padding(bottom = margin/2),
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

