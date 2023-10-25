package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.setMargins
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
class PlayerButton(context: Context, buttonBase: PlayerButtonBase) : FrameLayout(context) {

    val buttonBase: PlayerButtonBase = buttonBase.apply {
        stateListAnimator = null
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
    }

    private val commanderButton: ImageButton = ImageButton(context).apply {
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
                PlayerButtonState.COMMANDER_DEALER -> PlayerButtonBase.switchAllStates(
                    PlayerButtonState.NORMAL
                )

                else -> throw IllegalStateException()
            }
        }
    }

    private val settingsButton: ImageButton = ImageButton(context).apply {
        setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.settings_solid_icon))
        background =
            AppCompatResources.getDrawable(context, R.drawable.circular_background).apply {}
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

    private val changeColorButton: SettingsButton
        get() = SettingsButton(context).apply {
            //imageResource = R.drawable.six_icon
            text = "Change Color"
            setOnClickListener {

                settingsPicker.visibility = GONE
                println("change color button clicked")
                backgroundPicker.visibility = VISIBLE
            }
        }

    private val changeNameButton: SettingsButton
        get() = SettingsButton(context).apply {
            //imageResource = R.drawable.six_icon
            text = "Change Name"
            setOnClickListener {
                settingsPicker.visibility = GONE
                println("change name button clicked")
                changeNameField.setText(buttonBase.player!!.name)
                nameChangeLayout.visibility = VISIBLE
            }
        }

    private val settingsPicker: GridLayout = GridLayout(context).apply {
        columnCount = 2
        rowCount = 2
        setPadding(40, 40, 40, 40)
        rotation = 270f
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        addView(changeColorButton)
        addView(changeNameButton)
        addView(SettingsButton(context))
        addView(SettingsButton(context))
        visibility = GONE
    }

    private val backgroundPicker: GridLayout = GridLayout(context).apply {
        for (c in Player.allColors) {
            val colorButton = ColorPickerButton(context).apply {
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
        columnCount = 5
        rowCount = 2
        rotation = 270f
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
            setMargins(
                -context.resources.getDimensionPixelSize(R.dimen.background_picker_button_size),
                -context.resources.getDimensionPixelSize(R.dimen.background_picker_button_size),
                -context.resources.getDimensionPixelSize(R.dimen.background_picker_button_size),
                -context.resources.getDimensionPixelSize(R.dimen.background_picker_button_size)
            )
        }
        visibility = GONE
    }

    private val changeNameField: EditText = EditText(context).apply {
        layoutParams = LayoutParams(
            context.resources.getDimensionPixelSize(R.dimen.background_picker_button_size) * 4,
            WRAP_CONTENT
        )
        //inputType = InputType.TYPE_TEXT_VARIATION_NORMAL
        maxLines = 1
        gravity = Gravity.CENTER
        background = AppCompatResources.getDrawable(context, R.drawable.transparent)

        textSize = 30f
        addTextChangedListener(MyTextWatcher(this))

        onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                text = Editable.Factory.getInstance().newEditable("")
            }
        }
    }

    private val enterButton: ImageButton = ImageButton(context).apply {
        setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.enter_icon))
        background = AppCompatResources.getDrawable(context, R.drawable.transparent)

        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.MATCH_PARENT
        )
        setOnClickListener {
            resetState()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this@PlayerButton.windowToken, 0)

        }
    }

    private val placeholderButton: ImageButton = ImageButton(context).apply {
        setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.enter_icon))
        background = AppCompatResources.getDrawable(context, R.drawable.transparent)
        visibility = INVISIBLE

        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.MATCH_PARENT
        )

    }

    private val nameChangeLayout: LinearLayout = LinearLayout(context).apply {
        background = AppCompatResources.getDrawable(context, R.drawable.rounded_corners)
        val rippleDrawable = this.background as RippleDrawable
        val grad = rippleDrawable.findDrawableByLayerId(android.R.id.background) as GradientDrawable
        grad.color = ColorStateList.valueOf(Color.LTGRAY)
        grad.alpha = 25

        orientation = LinearLayout.HORIZONTAL
        rotation = 270f
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }
        addView(placeholderButton)
        addView(changeNameField)
        addView(enterButton)
        visibility = GONE
    }

    private fun switchToNormal() {
        settingsPicker.visibility = GONE
        backgroundPicker.visibility = GONE
        nameChangeLayout.visibility = GONE
    }

    fun resetState() {
        println("reset state")
        switchToNormal()
        buttonBase.switchState(PlayerButtonState.NORMAL)
    }

    private fun switchToSettings() {
        println("switch to settings")
        settingsPicker.visibility = VISIBLE
        buttonBase.switchState(PlayerButtonState.SETTINGS)
    }


    init {
        buttonBase.playerButtonCallback = this
        switchToNormal()
        addView(buttonBase)
        addView(commanderButton)
        addView(settingsButton)
        addView(settingsPicker)
        addView(backgroundPicker)
        addView(nameChangeLayout)
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

    inner class MyTextWatcher(editText: EditText): TextWatcher {
        val editText = editText
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // This method is called before the text changes.

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // This method is called when the text changes.
            // Update the player's name with the new text entered by the user
            val newName = s.toString()
            buttonBase.player!!.name = newName
        }

        override fun afterTextChanged(s: Editable?) {
            // This method is called after the text changes.
        }
    }


}