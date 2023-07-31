package com.example.kotlinmtglifetotalapp.ui.lifecounter


import android.animation.AnimatorInflater
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Interpolator
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator

import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment


import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withMatrix
import com.example.kotlinmtglifetotalapp.R

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit


/**
 * Custom Button class that has extended functionality and multiple text views
 * TODO: add commander damage
 * TODO: add settings
 * TODO: add selector at beginning
 */

class PlayerButton(context: Context, attrs: AttributeSet?) : AppCompatButton(context, attrs) {

    var player: Player? = null
        set(value) {
            field = value
            setBackground()
        }

    private var disposable: Disposable? = null

    private var isRepeating = false

    private val initialDelay: Long = 500

    private val repeatDelay: Long = 100

    private var recentChange: Int = 0
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val resetRecentChangeRunnable = Runnable {
        recentChange = 0
        invalidate()
    }

    private val paintSmall: Paint
        get() {
            return Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = (height / 10f)
                textAlign = Paint.Align.CENTER
                color = Color.WHITE
            }
        }

    private val paintLarge: Paint
        get() {
            return Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = (height / 3.25f)
                textAlign = Paint.Align.CENTER
                color = Color.WHITE
                typeface = resources.getFont(R.font.robotobold)
            }
        }

    private val heartMap =
        AppCompatResources.getDrawable(context, R.drawable.heart_solid_icon)?.toBitmap()

    private val rotatedMatrix
        get(): Matrix {
            return Matrix().apply {
                setRotate(rotation - 90, centerX, centerY)
            }
        }

    private val centerX
        get(): Float {
            return width / 2f
        }
    private val centerY
        get(): Float {
            return height / 2f
        }
    private val topLineY
        get(): Float {
            return (paintSmall.descent() - paintSmall.ascent()) * 2.8f
        }
    private val midLineY
        get(): Float {
            return centerY + (paintLarge.descent() - paintLarge.ascent()) * 0.3f
        }
    private val botLineY
        get(): Float {
            return height * 0.75f
        }

    private val heartX
        get() : Float {
            return centerX - heartMap!!.width / 2
        }

    private val heartY
        get() : Float {
            return botLineY - heartMap!!.height
        }

    private var firstDraw = true

    private lateinit var objectAnimator: ObjectAnimator

    private fun setBackground() {
        val rippleDrawable = background as RippleDrawable
        val gradientDrawable =
            rippleDrawable.findDrawableByLayerId(android.R.id.background) as GradientDrawable

        // can swap color with array of colors
        val colorStateListRipple =
            ColorStateList.valueOf(ColorUtils.setAlphaComponent(Color.WHITE, 70))
        rippleDrawable.setColor(colorStateListRipple)

        val colorStateListBackground = ColorStateList.valueOf(player!!.playerColor)
        gradientDrawable.color = colorStateListBackground
    }

    private fun init() {

    }

    private fun slideIn() {
        if (firstDraw) {
            if (rotation == 0f) {
                this.translationX = width.toFloat()
                objectAnimator = ObjectAnimator.ofFloat(this, "translationX", width.toFloat(), 0f)
            } else {
                this.translationX = -width.toFloat()
                objectAnimator = ObjectAnimator.ofFloat(this, "translationX", -width.toFloat(), 0f)
            }

            objectAnimator.duration = 1000
            objectAnimator.interpolator = DecelerateInterpolator(1.5f)
            objectAnimator.start()
            firstDraw = false
        }
    }

    private fun jiggle() {
        objectAnimator = AnimatorInflater.loadAnimator(context, R.animator.jiggle) as ObjectAnimator
        objectAnimator.target = this
        objectAnimator.start()
    }

    override fun onDraw(canvas: Canvas) {
        slideIn()

        with(canvas) {
            super.onDraw(this)
            save()
            rotate(rotation, centerX, centerY)
            withMatrix(rotatedMatrix) {
                // Display recent change if it's not zero
                if (recentChange != 0) {
                    var recentChangeString = if (recentChange > 0) "+" else ""
                    recentChangeString += recentChange.toString()
                    drawText(
                        recentChangeString,
                        centerX + paintLarge.measureText(player!!.life.toString()) / 2 + 100,
                        midLineY - 75,
                        paintSmall
                    )
                }
                drawText(player.toString(), centerX, topLineY, paintSmall)
                drawText(player!!.life.toString(), centerX, midLineY, paintLarge)
                drawBitmap(heartMap!!, heartX, heartY, paint)
            }
            restore()
        }

        if (isRepeating) {
            jiggle()
        }
    }

    override fun performClick(): Boolean {
        jiggle()
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val initialX = event.x

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                increment(if (initialX < this.width / 2) 1 else -1)
                startRepeating(initialX)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopRepeating()
                resetRecentChange()
            }

            MotionEvent.ACTION_MOVE -> {
                val isInButtonBounds = event.x.toInt() in 0..width && event.y.toInt() in 0..height

                if (isRepeating && !isInButtonBounds) {
                    stopRepeating()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun startRepeating(initialX: Float) {
        disposable?.dispose()
        disposable = Observable.interval(initialDelay, repeatDelay, TimeUnit.MILLISECONDS)
            .takeWhile { isRepeating }.subscribe({
                increment(if (initialX < this.width / 2) 1 else -1)
            }, {
                // onError case
                it.printStackTrace()
            })
        isRepeating = true
    }

    private fun stopRepeating() {
        disposable?.dispose()
        disposable = null
        isRepeating = false
    }

    private fun increment(change: Int) {
        player!!.increment(change)
        recentChange += change
        resetRecentChange()
        invalidate()
    }

    private fun resetRecentChange() {
        handler.removeCallbacks(resetRecentChangeRunnable)
        handler.postDelayed(resetRecentChangeRunnable, 1500)
    }
}