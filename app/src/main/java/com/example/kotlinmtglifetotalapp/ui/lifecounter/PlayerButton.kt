package com.example.kotlinmtglifetotalapp.ui.lifecounter

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.CombinedVibration
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.VibrationEffect.EFFECT_HEAVY_CLICK
import android.os.VibrationEffect.createOneShot
import android.os.VibrationEffect.createPredefined
import android.os.Vibrator
import android.os.VibratorManager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnticipateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withMatrix
import com.example.kotlinmtglifetotalapp.R
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

class PlayerButton(context: Context, attrs: AttributeSet?) : AppCompatButton(context, attrs) {

    var player: Player? = null
        set(value) {
            field = value
            setBackground()
        }

    private var vibration = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

    private var isRepeating = false
    private var lastEventUp = false
    private var firstJiggle = false
    private var secondJiggle = false
    private val initialDelay: Long = 500
    private val repeatDelay: Long = 100
    private var recentChange: Int = 0
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var disposable: Disposable? = null
    private val resetRecentChangeRunnable = Runnable {
        recentChange = 0
        invalidate()
    }

    private val paintSmall: Paint
        get() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = (height / 15f) + (width / 30f)
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
        }

    private val paintLarge: Paint
        get() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = (height / 5f) + (width / 4f)
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
            typeface = resources.getFont(R.font.robotobold)
        }

    private val rotatedMatrix
        get(): Matrix {
            return Matrix().apply {
                setRotate(rotation - 90, centerX, centerY)
            }
        }

    private val heartMap = AppCompatResources.getDrawable(context, R.drawable.heart_solid_icon)?.toBitmap()

    private val centerX: Float
        get() = width / 2f

    private val centerY: Float
        get() = height / 2f

    private val topLineY: Float
        get() = centerY - height * 0.1f - width / 10

    private val midLineY: Float
        get() = centerY * 0.675f + (paintLarge.descent() - paintLarge.ascent()) - width / 5.25f

    private val dpHeight: Float
        get() = context.resources.displayMetrics.density / height

    private val dpWidth: Float
        get() = context.resources.displayMetrics.density / width

    private var firstDraw = true

    private lateinit var objectAnimator: ObjectAnimator

    private val jiggleAnimator: ObjectAnimator
        get() = ObjectAnimator.ofPropertyValuesHolder(
    this,
    PropertyValuesHolder.ofFloat("scaleX", 1.0075f - dpWidth * 4, 1f - dpWidth * 4),
    PropertyValuesHolder.ofFloat("scaleY", 1.0075f - dpHeight * 4, 1f - dpHeight * 4)
    ).apply {
        interpolator = AccelerateInterpolator()
        duration = 50
    }


    private fun setBackground() {
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

    init {
    }

    private fun slideIn() {
        if (firstDraw) {
            jiggle()
            val translationXValue = if (rotation < 180f) width.toFloat() else -width.toFloat()
            this.translationX = translationXValue
            objectAnimator = ObjectAnimator.ofFloat(this, "translationX", translationXValue, 0f)

            objectAnimator.duration = 1000
            objectAnimator.interpolator = AccelerateDecelerateInterpolator()
            objectAnimator.start()
            firstDraw = false
        }
    }

    private fun jiggle() {
        if (firstJiggle) {
            firstJiggle = false
            secondJiggle = true
        }
        else if (secondJiggle) {
            secondJiggle = false
            return
        }
        else if (lastEventUp) {
            return
        }

        jiggleAnimator.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val isVertical = rotation == 90f || rotation == 270f
        setMeasuredDimension(if (isVertical) measuredHeight else measuredWidth, if (isVertical) measuredWidth else measuredHeight)
        super.onMeasure(if (isVertical) heightMeasureSpec else widthMeasureSpec, if (isVertical) widthMeasureSpec else heightMeasureSpec)
    }

    override fun draw(canvas: Canvas) {
        slideIn()
        with(canvas) {
            super.draw(this)
            save()
            rotate(rotation, centerX, centerY)
            withMatrix(rotatedMatrix) {
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

                val heartIconLeft = centerX - heartMap!!.width.toFloat() / 2
                val heartIconTop = midLineY + paintLarge.descent() - height / 20

                drawBitmap(heartMap, heartIconLeft, heartIconTop, paint)
            }
            restore()
        }

        if (isRepeating) {
            jiggle()
        }
    }

    private fun vibrate() {
        vibration.vibrate(CombinedVibration.createParallel(createPredefined(EFFECT_HEAVY_CLICK)))
    }

    override fun performClick(): Boolean {
        firstJiggle = true
        secondJiggle = false
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val initialX = event.x

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                performClick()
                increment(initialX)
                startRepeating(initialX)
                lastEventUp = false
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopRepeating()
                resetRecentChange()
                lastEventUp = true
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
                increment(initialX)
            }, {
                it.printStackTrace()
            })
        isRepeating = true
    }

    private fun stopRepeating() {
        disposable?.dispose()
        disposable = null
        isRepeating = false
    }

    private fun increment(initialX: Float) {
        println("increment")
        vibrate()
        val change: Int = if (rotation == 90f || rotation == 270f) {
            if (initialX > this.width / 2) 1 else -1
        } else {
            if (initialX < this.width / 2) 1 else -1
        }

        player!!.increment(change)
        recentChange += change
        resetRecentChange()
        setBackground()
    }

    private fun resetRecentChange() {
        handler.removeCallbacks(resetRecentChangeRunnable)
        handler.postDelayed(resetRecentChangeRunnable, 1500)
    }
}
