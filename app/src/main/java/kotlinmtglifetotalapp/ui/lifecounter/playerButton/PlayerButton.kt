package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.setPadding
import com.example.kotlinmtglifetotalapp.R
import kotlinmtglifetotalapp.ui.lifecounter.SettingsButton

/**
 * TODO: implement these features in settings
 * monarchy
 * change color/name
 * change color of playerbutton ui
 * counters
 * city's blessing?
 */
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
        setOnClickListener {
            when (buttonBase.state) {
                PlayerButtonState.NORMAL -> buttonBase.switchState(PlayerButtonState.COMMANDER_DEALER)
                PlayerButtonState.COMMANDER_DEALER -> PlayerButtonBase.switchAllStates(PlayerButtonState.NORMAL)
                else -> throw IllegalStateException()
            }
        }
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
        setOnClickListener {
            when (buttonBase.state) {
                PlayerButtonState.NORMAL -> this@PlayerButton.switchToSettings()
                PlayerButtonState.SETTINGS -> this@PlayerButton.resetState()
                else -> this@PlayerButton.switchToSettings()
            }
        }
    }

    private val changeColorButton get() = SettingsButton(context).apply {
        //imageResource = R.drawable.six_icon
        text = "Change Color"
        setOnClickListener {
            resetView()
            addView(backgroundPicker)
        }
    }

    private fun resetView() {
        removeAllViews() // EXCEPT BUTTON BASE
        //addView(buttonBase)
        addView(commanderButton)
        addView(settingsButton)
    }

    private val settingsPicker = GridLayout(context).apply {
        addView(changeColorButton)
        addView(SettingsButton(context))
        addView(SettingsButton(context))
        addView(SettingsButton(context))
    }

    private val backgroundPicker = GridLayout(context).apply {
        for (c in Player.allColors) {
            val colorButton = ColorPickerButton(context).apply{
                color = c
                layoutParams = LayoutParams(
                    context.resources.getDimensionPixelSize(R.dimen.background_picker_button_size),
                    context.resources.getDimensionPixelSize(R.dimen.background_picker_button_size)
                ).apply {
                    gravity = Gravity.CENTER
                    setMargins(20, 20, 20, 20)
                }
                setOnClickListener {
                    buttonBase.player?.let { it1 -> applyBackground(it1) }
                    buttonBase.updateUI()
                }
            }
            addView(colorButton)
        }
        columnCount = 2
        rowCount = 10
        setPadding(40,40,40,40)
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            //setMargins(20, 20, 20, 20)
        }

    }

    fun resetState() {
        println("reset state")
        resetView()
        buttonBase.switchState(PlayerButtonState.NORMAL)

    }

    private fun switchToSettings() {
        println("switch to settings")
        addView(backgroundPicker)
        buttonBase.switchState(PlayerButtonState.SETTINGS)
    }


    init {
        resetView()
        buttonBase.playerButtonCallback = this
        addView(buttonBase)

    }

    fun updateButtonVisibility() {
        when (buttonBase.state) {
            PlayerButtonState.NORMAL -> {
                commanderButton.visibility = VISIBLE
                settingsButton.visibility = VISIBLE
            }
            PlayerButtonState.COMMANDER_DEALER -> {
                commanderButton.visibility = VISIBLE
                settingsButton.visibility = GONE
            }
            PlayerButtonState.COMMANDER_RECEIVER -> {
                commanderButton.visibility = GONE
                settingsButton.visibility = GONE
            }
            PlayerButtonState.SETTINGS -> {
                commanderButton.visibility = GONE
                settingsButton.visibility = VISIBLE
            }
        }
    }




}