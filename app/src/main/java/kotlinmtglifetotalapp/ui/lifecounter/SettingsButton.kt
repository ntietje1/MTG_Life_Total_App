package com.example.kotlinmtglifetotalapp.ui.lifecounter

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.example.kotlinmtglifetotalapp.R

class SettingsButton(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

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


    private val imageView = ImageView(context).apply {
        val imageSize =
            context.resources.getDimensionPixelSize(R.dimen.settings_button_size) - context.resources.getDimensionPixelSize(
                R.dimen.button_image_margin_horiz) * 2
        this.layoutParams = LayoutParams(imageSize, imageSize).apply {
            gravity = Gravity.CENTER
            setMargins(
                context.resources.getDimensionPixelSize(R.dimen.button_image_margin_horiz),
                0,
                context.resources.getDimensionPixelSize(R.dimen.button_image_margin_horiz),
                0
            )
        }
        this.setImageResource(imageResource)
        this.setBackgroundColor(Color.TRANSPARENT)
        this.setPadding(
//            context.resources.getDimensionPixelSize(R.dimen.button_image_padding)
            0
        )
    }

    private val textView = TextView(context).apply {
        this.layoutParams = LayoutParams(
            context.resources.getDimensionPixelSize(R.dimen.settings_button_size),
            context.resources.getDimensionPixelSize(R.dimen.text_view_height)
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
        this.gravity = Gravity.TOP
        this.text = this@SettingsButton.text
        this.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        this.textSize = context.resources.getDimensionPixelSize(R.dimen.text_size)
            .toFloat() // Set text size as needed
        this.setTextColor(Color.WHITE)
        this.setBackgroundColor(Color.TRANSPARENT)
        this.isClickable = false // Disable text view's click behavior
        this.setPadding(0,context.resources.getDimensionPixelSize(R.dimen.text_reverse_padding),0,0)
    }

    init {
        // Set the FrameLayout clickable
        isClickable = true

        layoutParams = LayoutParams(
            context.resources.getDimensionPixelSize(R.dimen.settings_button_size),
            context.resources.getDimensionPixelSize(R.dimen.settings_button_size)
        ).apply {
            setMargins(0, 0, 0, 0)
        }
        setPadding(0, 0, 0, 0) //5dp

        orientation = VERTICAL
        setBackgroundResource(R.drawable.transparent)
        gravity = Gravity.CENTER
        focusable = FOCUSABLE

        // Add the ImageView and TextView to the LinearLayout
        addView(imageView)
        addView(textView)
        //this.addView(button)
    }

}

