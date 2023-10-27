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

    //TODO: NORMALIZE IMAGE SIZE
    private val imageView = ImageView(context).apply {
        val imageSize = size - margin * 2
        this.layoutParams = LayoutParams(imageSize, imageSize).apply {
            gravity = Gravity.CENTER
            setMargins(margin, 0, margin, 0)
        }
        this.setImageResource(imageResource)
        this.setBackgroundColor(Color.TRANSPARENT)
    }

    private val textView = TextView(context).apply {
        this.layoutParams = LayoutParams(size, margin*2).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
        this.gravity = Gravity.TOP
        this.text = this@SettingsButton.text
        this.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        this.textSize = margin / 2f
        this.setTextColor(Color.WHITE)
        this.setBackgroundColor(Color.TRANSPARENT)
        this.isClickable = false
        this.setPadding(0, -margin/2, 0, 0)
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

