package kotlinmtglifetotalapp.ui.lifecounter

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinmtglifetotalapp.R

class SettingsButton(
    context: Context,
    attrs: AttributeSet? = null,
    var size: Int = context.resources.getDimensionPixelSize(R.dimen.settings_button_size)
) : LinearLayout(context, attrs) {

    var imageResource = R.drawable.placeholder_icon
        set(value) {
            field = value
            imageView.setImageResource(value)
        }

    var text = "placeholder"
        set(value) {
            field = value
            textView.text = value
        }

    private val margin = size / 130 * 15

    private val imageView = ImageView(context).apply {
        val imageSize = size - margin * 2
        this.layoutParams = LayoutParams(imageSize, imageSize).apply {
            gravity = Gravity.CENTER
            setMargins(margin, margin, margin, -margin / 2)
        }
        this.setImageResource(imageResource)
        this.setBackgroundColor(Color.Transparent.toArgb())
        scaleType = ImageView.ScaleType.FIT_CENTER
    }

    //    private val myTextSize get() = margin / 2.5f
    private val myTextSize get() = margin / 5f + context.resources.getDimensionPixelSize(R.dimen.oneSp) * 2

    private val textView = TextView(context).apply {
        this.layoutParams = LayoutParams(size, margin * 3).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            setMargins(0, 0, 0, margin)
        }
        this.gravity = Gravity.TOP
        this.text = this@SettingsButton.text
        this.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        this.textSize = myTextSize.toFloat()
        this.setTextColor(Color.White.toArgb())
        this.setBackgroundColor(Color.Transparent.toArgb())
        this.isClickable = false
        //this.setPadding(0, myTextSize.toInt(), 0, 0)

    }

    init {
        isClickable = true
        layoutParams = LayoutParams(size, size)
        orientation = VERTICAL
        setBackgroundResource(R.drawable.transparent)
        gravity = Gravity.CENTER

        addView(imageView)
        addView(textView)
    }
}

@Composable
fun SettingsButton(
    size: Dp = 130.dp,
    color: Color = Color.DarkGray,
    imageResource: Painter = painterResource(id = R.drawable.placeholder_icon),
    text: String = "placeholder",
    onClick: () -> Unit = {}
) {
    val cornerRadius = 30.dp
    val margin = 15.dp
    val imageSize = size - margin * 2
    val bottomPadding = margin / 2
    val fontSize = (size / 10).value.sp

    // Utilize the lambda version of Modifier and graphicsLayer for optimization
    Box(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(cornerRadius))
            .background(color)
            .graphicsLayer() // Use graphicsLayer for optimization
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
                style = TextStyle(fontSize = fontSize),
                modifier = Modifier.padding(bottom = bottomPadding)
            )
        }
    }
}

