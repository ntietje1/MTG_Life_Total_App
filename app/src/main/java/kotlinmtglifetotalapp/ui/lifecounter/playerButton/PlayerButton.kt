package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.children
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.example.kotlinmtglifetotalapp.R
import kotlinmtglifetotalapp.ui.lifecounter.SettingsButton
import java.lang.Math.max

/**
 * TODO: implement these features in settings
 * counters
 * city's blessing?
 * save/load player setting buttons
 */
class PlayerButton(context: Context, buttonBase: PlayerButtonBase) : FrameLayout(context) {

    val buttonBase: PlayerButtonBase = buttonBase.apply {
        stateListAnimator = null
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
    }

    private lateinit var commanderButton: ImageButton

    private fun commanderButton() {
        commanderButton = ImageButton(context).apply {
            setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.commander_solid_icon))
            background = ColorDrawable(Color.TRANSPARENT)
            rotation -= 90f
            stateListAnimator = null
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LayoutParams(
                (settingsButtonSize * 0.6f).toInt(),
                (settingsButtonSize * 0.6f).toInt()
            ).apply {
                gravity = Gravity.END or Gravity.BOTTOM
                setMargins(settingsButtonMarginSize * 2)
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
    }

    private lateinit var settingsButton: ImageButton

    private fun settingsButton() {
        settingsButton = ImageButton(context).apply {
            setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.settings_solid_icon))
            background =
                AppCompatResources.getDrawable(context, R.drawable.circular_background).apply {}
            rotation -= 90f
            stateListAnimator = null
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LayoutParams(
                (settingsButtonSize * 0.6f).toInt(),
                (settingsButtonSize * 0.6f).toInt()
            ).apply {
                gravity = Gravity.END or Gravity.TOP
                setMargins(settingsButtonMarginSize)
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
    }

    private lateinit var settingsPicker: GridLayout

    /**
     * Must be called after initializing all child layouts
     */
    private fun settingsPicker() {
        settingsPicker = GridLayout(context).apply {
            columnCount = 2
            rowCount = 3
            orientation = GridLayout.HORIZONTAL
            layoutParams = LayoutParams(
                WRAP_CONTENT,
                WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }

            visibility = GONE
        }

        settingsPicker.apply {
            addView(changeColorButton)
            addView(changeNameButton)
            addView(monarchyButton)
            addView(changeBackgroundButton)
            addView(SettingsButton(context, null, settingsButtonSize))
            addView(SettingsButton(context, null, settingsButtonSize))
        }

        for (child in settingsPicker.children) {
            child.rotation -= 90f
        }
    }

    private val settingsButtonSize
        get() = max(
            (this.width / 3.1f).toInt(),
            (this.height / 5.1f).toInt()
        )

    private val settingsButtonMarginSize = context.resources.getDimensionPixelSize(R.dimen.one) * 6

    private val changeColorButton get() = SettingsButton(context, null, settingsButtonSize).apply {
        imageResource = R.drawable.color_picker_icon
        text = "Set Color"
        setOnClickListener {
            settingsPicker.visibility = GONE
            backgroundPicker.visibility = VISIBLE
        }
    }

    private val changeNameButton get() = SettingsButton(context, null, settingsButtonSize).apply {
        imageResource = R.drawable.change_name_icon
        text = "Change Name"
        setOnClickListener {
            changeNameField.setText(buttonBase.player!!.name)
            settingsPicker.visibility = GONE
            nameChangeLayout.visibility = VISIBLE
        }
    }

    private val monarchyButton get() = SettingsButton(context, null, settingsButtonSize).apply {
        imageResource = R.drawable.monarchy_icon
        text = "Monarch"
        setOnClickListener {
            this@PlayerButton.buttonBase.player!!.monarch = true
            this@PlayerButton.resetState()
        }
    }

    private val changeBackgroundButton get() = SettingsButton(context, null, settingsButtonSize).apply {
        imageResource = R.drawable.change_background_icon
        text = "Set Image"
        setOnClickListener {
            settingsPicker.visibility = GONE
            backgroundPicker.visibility = VISIBLE
        }
    }

    private val colorPickerButtonSize get() = settingsButtonSize / 2

    private lateinit var customColorButton: ImageButton

    private fun customColorButton() {
        customColorButton = ImageButton(context).apply {
            setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.custom_color_icon))
            background = ColorDrawable(Color.TRANSPARENT)
            stateListAnimator = null
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LayoutParams(
                colorPickerButtonSize,
                colorPickerButtonSize
            ).apply {
                gravity = Gravity.CENTER
                setMargins(context.resources.getDimensionPixelSize(R.dimen.one) * 4)
            }
            setOnClickListener {
                //buttonBase.player?.let { it1 -> applyBackground(it1) }
                buttonBase.updateUI()
            }
        }
    }

    private lateinit var backgroundPicker: GridLayout

    private fun backgroundPicker() {
        backgroundPicker = GridLayout(context).apply {
            for (c in Player.allColors) {
                val colorButton = ColorPickerButton(context).apply {
                    color = c
                    layoutParams = LayoutParams(
                        colorPickerButtonSize,
                        colorPickerButtonSize
                    ).apply {
                        gravity = Gravity.CENTER
                        setMargins(context.resources.getDimensionPixelSize(R.dimen.one) * 4)
                    }
                    setOnClickListener {
                        buttonBase.player?.let { it1 -> applyBackground(it1) }
                        buttonBase.updateUI()
                    }
                }
                addView(colorButton)
            }
            customColorButton()
            addView(customColorButton)
            columnCount = 5
            rowCount = 2
            rotation -= 90f
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
                setMargins(-context.resources.getDimensionPixelSize(R.dimen.background_picker_button_size))
            }
            visibility = GONE
        }
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
        val roundedCornerDrawable = RoundedCornerDrawable.create(context)
        roundedCornerDrawable.backgroundColor = Color.LTGRAY
        roundedCornerDrawable.backgroundAlpha = 45
        background = roundedCornerDrawable

        orientation = LinearLayout.HORIZONTAL
        rotation -= 90f
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

    fun resetState() {
        println("reset state")
        switchToNormal()
        buttonBase.switchState(PlayerButtonState.NORMAL)
    }

    private fun switchToNormal() {
        settingsPicker.visibility = GONE
        backgroundPicker.visibility = GONE
        nameChangeLayout.visibility = GONE
    }

    private fun switchToSettings() {
        println("switch to settings")
        settingsPicker.visibility = VISIBLE
        buttonBase.switchState(PlayerButtonState.SETTINGS)
    }

    init {
        buttonBase.playerButtonCallback = this
        setLayoutListener()
    }

    private fun setLayoutListener() {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                initViews()
                addAllViews()
                // Remove the listener to avoid multiple calls
                this@PlayerButton.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun addAllViews() {
        addView(buttonBase)
        addView(commanderButton)
        addView(settingsButton)
        addView(settingsPicker)
        addView(backgroundPicker)
        addView(nameChangeLayout)
    }

    private fun initViews() {
        backgroundPicker()
        commanderButton()
        settingsButton()
        settingsPicker()
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

    inner class MyTextWatcher(editText: EditText) : TextWatcher {
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