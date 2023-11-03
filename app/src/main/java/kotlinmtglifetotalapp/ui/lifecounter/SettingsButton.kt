package kotlinmtglifetotalapp.ui.lifecounter

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
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
            setMargins(margin, margin, margin, -margin/2)
        }
        this.setImageResource(imageResource)
        this.setBackgroundColor(Color.TRANSPARENT)
        scaleType = ImageView.ScaleType.FIT_CENTER
    }

//    private val myTextSize get() = margin / 2.5f
    private val myTextSize get() = margin / 5f + context.resources.getDimensionPixelSize(R.dimen.oneSp)*2

    private val textView = TextView(context).apply {
        this.layoutParams = LayoutParams(size, margin*3).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            setMargins(0,0,0, margin)
        }
        this.gravity = Gravity.TOP
        this.text = this@SettingsButton.text
        this.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        this.textSize = myTextSize.toFloat()
        this.setTextColor(Color.WHITE)
        this.setBackgroundColor(Color.TRANSPARENT)
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

