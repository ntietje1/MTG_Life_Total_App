package com.example.kotlinmtglifetotalapp.ui.lifecounter


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

class PlayerButtonBase(context: Context, attrs: AttributeSet?) : AppCompatButton(context, attrs), PlayerObserver {

    var player: Player? = null
        set(value) {
            field = value
            field?.setObserver(this)
            updateUI()
        }

    private lateinit var animator: PlayerButtonAnimator
    private val repeater = PlayerButtonRepeater(this, 500, 100)
    private val drawer = PlayerButtonDrawer(this)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator = PlayerButtonAnimator(this.parent as PlayerButton)
    }

    override fun onPlayerUpdated(player: Player) {
        updateUI()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val isVertical = rotation == 90f || rotation == 270f
        setMeasuredDimension(if (isVertical) measuredHeight else measuredWidth, if (isVertical) measuredWidth else measuredHeight)
        super.onMeasure(if (isVertical) heightMeasureSpec else widthMeasureSpec, if (isVertical) widthMeasureSpec else heightMeasureSpec)
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

    private fun updateUI() {
        val rippleDrawable = background as RippleDrawable
        val gradientDrawable =
            rippleDrawable.findDrawableByLayerId(android.R.id.background) as GradientDrawable

        val colorStateListRipple =
            ColorStateList.valueOf(ColorUtils.setAlphaComponent(Color.WHITE, 60))
        rippleDrawable.setColor(colorStateListRipple)

        val colorStateListBackground = ColorStateList.valueOf(player!!.playerColor)
        gradientDrawable.color = colorStateListBackground
        invalidate()
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
