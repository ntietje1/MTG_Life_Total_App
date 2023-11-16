package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.Gravity.CENTER
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.children
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import com.example.kotlinmtglifetotalapp.R
import kotlinmtglifetotalapp.ui.lifecounter.AnimatedBorderCard
import kotlinmtglifetotalapp.ui.lifecounter.RoundedCornerDrawable
import kotlinmtglifetotalapp.ui.lifecounter.SettingsButton
import kotlinmtglifetotalapp.utils.RotateLayout
import yuku.ambilwarna.AmbilWarnaDialog
import java.lang.Math.max


/**
 * TODO: implement these features in settings
 * counters
 * city's blessing?
 * save/load player setting buttons
 */
class PlayerButton(context: Context, player: Player?) : FrameLayout(context) {

    val buttonBase: PlayerButtonBase = PlayerButtonBase(context, null).apply {
        stateListAnimator = null
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
    }

    var player: Player
        get() = buttonBase.player!!
        set(p) = run {
            buttonBase.player = p
        }

    private val composeView = ComposeView(context).apply {
//        setViewCompositionStrategy(
//            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
//        )
        setContent {
            AnimatedBorderCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 0.dp),
                gradient = Brush.sweepGradient(
                    listOf(
                        Color.White.copy(alpha = 0.1f),
                        Color(255, 191, 8),
                        Color(255, 191, 8),
                        Color(255, 191, 8),
                    )
                ),
                borderWidth = borderWidth,
                animationDuration = 6500
            ) {
                AndroidView(
                    factory = {
                        buttonBase
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    }

    private var borderWidth: MutableState<Dp> = mutableStateOf(0.dp)

    private fun toggleMonarchy() {
        player.monarch = !player.monarch
    }

    fun updateMonarchy() {
        if (player.monarch) {
            borderWidth.value = 4.dp
        } else {
            borderWidth.value = 0.dp
        }
    }

    private lateinit var commanderButton: ImageButton

    private fun initCommanderButton() {
        commanderButton = ImageButton(context).apply {
            setImageDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.commander_solid_icon
                )
            )
            background = AppCompatResources.getDrawable(context, R.drawable.transparent)
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

    private fun initSettingsButton() {
        settingsButton = ImageButton(context).apply {
            setImageDrawable(
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.settings_solid_icon
                )
            )
            background = AppCompatResources.getDrawable(context, R.drawable.circular_background)
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
    private fun initSettingsPicker() {
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
            addView(changeBackgroundButton)
            addView(monarchyButton)
            addView(changeNameButton)
            addView(savePlayerButton)
            addView(loadPlayerButton)
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

    private val changeColorButton
        get() = SettingsButton(context, null, settingsButtonSize).apply {
            imageResource = R.drawable.color_picker_icon
            text = "Set Color"
            setOnClickListener {
                settingsPicker.visibility = GONE
                backgroundPicker.visibility = VISIBLE
            }
        }

    private val changeNameButton
        get() = SettingsButton(context, null, settingsButtonSize).apply {
            imageResource = R.drawable.change_name_icon
            text = "Change Name"
            setOnClickListener {
                changeNameField.setText(buttonBase.player!!.name)
                settingsPicker.visibility = GONE
                nameChangeLayout.visibility = VISIBLE
            }
        }

    private val monarchyButton
        get() = SettingsButton(context, null, settingsButtonSize).apply {
            imageResource = R.drawable.monarchy_icon
            text = "Monarch"
            setOnClickListener {
                toggleMonarchy()
                this@PlayerButton.resetState()
            }
        }

    private val savePlayerButton
        get() = SettingsButton(context, null, settingsButtonSize).apply {
            imageResource = R.drawable.upload_icon
            text = "Save Player"
            setOnClickListener {
                PlayerDataManager(context).savePlayer(player)
                with(loadPlayerPicker) {
                    this@with.removeAllViews()
                    for (b in generateMiniPlayerButtons()) {
                        addView(b)
                    }
                }
                resetState()
            }
        }

    private val loadPlayerButton
        get() = SettingsButton(context, null, settingsButtonSize).apply {
            imageResource = R.drawable.download_icon
            text = "Load Player"
            setOnClickListener {
                refreshLoadPlayerPicker()
                settingsPicker.visibility = GONE
                loadPlayerPicker.visibility = VISIBLE
            }
        }

    //private lateinit var loadPlayerLayout: ScrollView

    private fun refreshLoadPlayerPicker() {
        with(loadPlayerPicker) {
            removeAllViews()
            for (b in generateMiniPlayerButtons()) {
                addView(b)
            }
        }
    }

    private lateinit var loadPlayerPicker: GridLayout

    private fun initLoadPlayerPicker() {
        if (this::loadPlayerPicker.isInitialized) {
            return
        }

        loadPlayerPicker = GridLayout(context).apply {
            columnCount = 2
            rowCount = 3
            orientation = GridLayout.HORIZONTAL

            layoutParams = LayoutParams(
                WRAP_CONTENT,
                WRAP_CONTENT
            ).apply {
                gravity = CENTER
            }
            visibility = GONE
        }

        refreshLoadPlayerPicker()

    }

    private fun generateMiniPlayerButtons(): ArrayList<FrameLayout> {
        val res = arrayListOf<FrameLayout>()
        val playerList = PlayerDataManager(context).loadPlayers()
        println(playerList)
        println("LOADED " + playerList.size + " PLAYERS")

        for (p in playerList) {
            if (p.name == "#Placeholder") {
                continue
            }
            val button = Button(context).apply {
                background = RoundedCornerDrawable.create(context).apply {
                    backgroundColor = p.playerColor
                    backgroundRadius = (backgroundRadius * 0.75f).toInt()
                    rippleColor = Color.Red.toArgb()
                }
                layoutParams = LayoutParams(
                    settingsButtonSize,
                    (settingsButtonSize * 0.6f).toInt()
                )
                text = p.name
                setOnClickListener {
                    player.fromString(p.toString())
                    resetState()
                }
                setOnLongClickListener {
                    println("LONG CLICK")
                    buttonBase.vibrate()
                    PlayerDataManager(context).deletePlayer(p)
                    loadPlayerPicker.removeAllViews()
                    for (b in generateMiniPlayerButtons()) {
                        loadPlayerPicker.addView(b)
                    }
                    true
                }

            }

            val rotateLayout = RotateLayout(context).apply {
                this.angle += 90
                addView(button)
            }
            val frameLayout = FrameLayout(context).apply {
                addView(rotateLayout)
                layoutParams = LayoutParams(
                    WRAP_CONTENT,
                    WRAP_CONTENT
                ).apply {
                    setMargins(settingsButtonMarginSize / 5)
                }
            }

            res.add(frameLayout)
        }

        return res
    }

    private val changeBackgroundButton
        get() = SettingsButton(context, null, settingsButtonSize).apply {
            imageResource = R.drawable.change_background_icon
            text = "Set Image"
            setOnClickListener {
                settingsPicker.visibility = GONE
                backgroundPicker.visibility = VISIBLE
            }
        }

    private val colorPickerButtonSize get() = settingsButtonSize / 2

    private lateinit var customColorButton: ImageButton

    private fun initCustomColorButton() {
        customColorButton = ImageButton(context).apply {
            setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.custom_color_icon))
            background = AppCompatResources.getDrawable(context, R.drawable.transparent)
            stateListAnimator = null
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LayoutParams(
                colorPickerButtonSize,
                colorPickerButtonSize
            ).apply {
                gravity = Gravity.CENTER
                setMargins(context.resources.getDimensionPixelSize(R.dimen.one) * 4)
            }
            setPadding(0)
            setOnClickListener {
                val initialColor = player.playerColor

                val colorPickerDialog = AmbilWarnaDialog(
                    context,
                    initialColor,
                    object : AmbilWarnaDialog.OnAmbilWarnaListener {
                        override fun onCancel(dialog: AmbilWarnaDialog?) {
                            // User canceled the color picker dialog
                        }

                        override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                            player.playerColor = color
                            resetState()
                        }
                    })
                colorPickerDialog.dialog?.window?.setBackgroundDrawable(
                    RoundedCornerDrawable.create(
                        context
                    )
                )
                colorPickerDialog.show()
            }
        }
    }

    private lateinit var backgroundPicker: GridLayout

    private fun initBackgroundPicker() {
        backgroundPicker = GridLayout(context).apply {
            for (c in Player.allColors) {
                val colorButton = Button(context).apply {
                    setBackgroundColor(c)
                    layoutParams = LayoutParams(
                        colorPickerButtonSize,
                        colorPickerButtonSize
                    ).apply {
                        gravity = Gravity.CENTER
                        setMargins(context.resources.getDimensionPixelSize(R.dimen.one) * 4)
                    }
                    setOnClickListener {
                        player.playerColor = c
                        buttonBase.updateUI()
                    }
                }
                addView(colorButton)
            }
            initCustomColorButton()
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
        roundedCornerDrawable.backgroundColor = Color.LightGray.toArgb()
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
        loadPlayerPicker.visibility = GONE
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
//        addView(buttonBase)
        addView(composeView)
        addView(commanderButton)
        addView(settingsButton)
        addView(settingsPicker)
        addView(backgroundPicker)
        addView(nameChangeLayout)
        addView(loadPlayerPicker)
    }

    private fun initViews() {
        initBackgroundPicker()
        initCommanderButton()
        initSettingsButton()
        initSettingsPicker()
        initLoadPlayerPicker()
    }

    fun updateUI() {
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