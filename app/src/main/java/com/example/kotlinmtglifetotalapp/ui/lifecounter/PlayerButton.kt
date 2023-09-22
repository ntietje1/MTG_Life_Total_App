package com.example.kotlinmtglifetotalapp.ui.lifecounter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.marginBottom
import androidx.core.view.setPadding
import com.example.kotlinmtglifetotalapp.R

class PlayerButton (context: Context, buttonBase: PlayerButtonBase) : FrameLayout(context) {

    val buttonBase = buttonBase.apply{

        stateListAnimator = null
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
    }

    private val commanderButton = ImageButton(context).apply {
        setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.commander_solid_icon))
        background = ColorDrawable(Color.TRANSPARENT)
        rotation -= 90f
        stateListAnimator = null
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.END or Gravity.BOTTOM
            setMargins(40, 40, 40, 40)
        }
        setPadding(5)

    }

    private val settingsButton = ImageButton(context).apply {
        setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.settings_solid_icon))
        background = AppCompatResources.getDrawable(context, R.drawable.circular_background).apply {}
        rotation -= 90f
        stateListAnimator = null
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.END or Gravity.TOP
            setMargins(20, 20, 20, 20)
        }
        setPadding(5)

    }

    init {
        addView(buttonBase)
        addView(commanderButton)
        addView(settingsButton)
    }


}