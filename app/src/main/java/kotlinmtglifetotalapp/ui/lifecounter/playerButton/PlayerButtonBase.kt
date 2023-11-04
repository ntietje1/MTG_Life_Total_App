package kotlinmtglifetotalapp.ui.lifecounter.playerButton


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton
import androidx.core.graphics.ColorUtils
import com.example.kotlinmtglifetotalapp.R

class PlayerButtonBase(context: Context, attrs: AttributeSet?) : AppCompatButton(context, attrs),
    PlayerObserver {

    companion object {
        private val allPlayerButtons = mutableListOf<PlayerButtonBase>()
        private var currDealer: Player? = null
        val currCommanderDamage: ArrayList<Int>
            get() {
                return if (currDealer != null) {
                    currDealer!!.commanderDamage
                } else {
                    arrayListOf()
                }
            }

        fun switchAllStates(mode: PlayerButtonState) {
            currDealer = null
            allPlayerButtons.forEach {
                it.playerButtonCallback.resetState()
                it.state = mode
            }
        }
    }

    lateinit var playerButtonCallback: PlayerButton

    var player: Player? = null
        set(value) {
            field = value
            field?.setObserver(this)
            updateUI()
            field?.zeroRecentChange()
        }

    private lateinit var animator: PlayerButtonAnimator
    private val repeater = PlayerButtonRepeater(this, 500, 100)
    private val drawer = PlayerButtonDrawer(this)

    var state = PlayerButtonState.NORMAL
        private set(value) {
            field = value
            playerButtonCallback.updateButtonVisibility()
            updateUI()
        }

    fun vibrate() {
        animator.vibrate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        allPlayerButtons.add(this)
        animator = PlayerButtonAnimator(this.playerButtonCallback)

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        allPlayerButtons.remove(this)
    }

    override fun onPlayerUpdated() {
        updateUI()
    }

    fun updateUI() {
        drawer.setBackground()
        invalidate()
    }

    fun switchState(state: PlayerButtonState) {
        when (state) {
            PlayerButtonState.NORMAL -> switchToNormal()
            PlayerButtonState.COMMANDER_RECEIVER -> switchToCommanderReceiver()
            PlayerButtonState.COMMANDER_DEALER -> switchToCommanderDealer()
            PlayerButtonState.SETTINGS -> switchToSettings()
        }
    }

    private fun switchToCommanderReceiver() {
        state = PlayerButtonState.COMMANDER_RECEIVER
    }

    private fun switchToCommanderDealer() {
        switchAllStates(PlayerButtonState.COMMANDER_RECEIVER)
        state = PlayerButtonState.COMMANDER_DEALER
        currDealer = player
    }

    private fun switchToNormal() {
        state = PlayerButtonState.NORMAL
    }

    private fun switchToSettings() {
        state = PlayerButtonState.SETTINGS
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val isVertical = rotation == 90f || rotation == 270f
        setMeasuredDimension(
            if (isVertical) measuredHeight else measuredWidth,
            if (isVertical) measuredWidth else measuredHeight
        )
        super.onMeasure(
            if (isVertical) heightMeasureSpec else widthMeasureSpec,
            if (isVertical) widthMeasureSpec else heightMeasureSpec
        )
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        animator.slideIn()
        drawer.draw(canvas)

        if (repeater.isRepeating) {
            animator.jiggleAndVibrate()
        }
    }

    override fun performClick(): Boolean {
        animator.performClick()
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (state == PlayerButtonState.COMMANDER_DEALER) return true

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                repeater.startRepeating(determineChange(event.x))
                animator.lastEventUp = false
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                repeater.stopRepeating()
                animator.lastEventUp = true
            }

            MotionEvent.ACTION_MOVE -> {
                val isInButtonBounds = event.x.toInt() in 0..width && event.y.toInt() in 0..height
                if (repeater.isRepeating && !isInButtonBounds) {
                    repeater.stopRepeating()
                }
            }
        }
        return super.onTouchEvent(event)
    }


    private fun determineChange(x: Float): Int {
        val change: Int = if (rotation == 90f || rotation == 270f) {
            if (x > this.width / 2) 1 else -1
        } else {
            if (x < this.width / 2) 1 else -1
        }
        return change
    }
}
