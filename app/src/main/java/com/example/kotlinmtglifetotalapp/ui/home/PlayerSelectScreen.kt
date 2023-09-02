package com.example.kotlinmtglifetotalapp.ui.home


import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.core.RepeatMode
import androidx.core.graphics.drawable.toBitmap
import androidx.core.util.keyIterator
import com.example.kotlinmtglifetotalapp.R
import kotlin.random.Random


class PlayerSelectScreen(context: Context, attrs: AttributeSet?) :
    View(context, attrs) {
    private val activePointers: SparseArray<PointT> = SparseArray()
    private val touchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 15f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 20f
        color = Color.WHITE
    }

    private val rand = Random(System.currentTimeMillis())

    private var lastNewClick = System.currentTimeMillis()

    private val clickHandler = Handler(Looper.getMainLooper())

    private val selectionRunnable = Runnable {

        val randomIndex = rand.nextInt(activePointers.size())
        val selectedId = activePointers.keyAt(randomIndex)

        for (id in activePointers.keyIterator()) {
            if (id == selectedId) {
                selectionAnimator(id).start()
            } else {
                popInAnimator(id).reverse()
            }
        }
        clickHandler.removeCallbacks(pulseRunnable)

    }

    private val pulseAnimator = ValueAnimator.ofFloat(1.0f, 1.25f, 1.0f).apply {
        addUpdateListener { animation ->
            run {
                for (id in activePointers.keyIterator()) {
                    if (activePointers[id] != null) {
                        activePointers[id].size = animation.animatedValue as Float
                    }
                }
                invalidate()
            }
        }
        duration = 1000
        interpolator = DecelerateInterpolator(0.8f)
    }

    private val pulseRunnable = object : Runnable {
        override fun run() {
            pulseAnimator.start()
            invalidate()
            clickHandler.postDelayed(this, PULSE_FREQ)
        }
    }

    init {

    }


    override fun performClick(): Boolean {
        lastNewClick = System.currentTimeMillis()
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val pointerIndex = event.actionIndex
        val pointerId = event.getPointerId(pointerIndex)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                performClick()
                val p = PointT(event.getX(pointerIndex), event.getY(pointerIndex))
                activePointers.put(pointerId, p)
                popInAnimator(pointerId).start()

                clickHandler.removeCallbacks(selectionRunnable)
                clickHandler.postDelayed(selectionRunnable, SELECTION_DELAY)

                clickHandler.removeCallbacks(pulseRunnable)
                clickHandler.postDelayed(pulseRunnable, PULSE_DELAY)

            }

            MotionEvent.ACTION_MOVE -> {
                // a pointer was moved
                var i = 0
                while (i < event.pointerCount) {
                    with(activePointers[event.getPointerId(i)]) {
                        if (this != null) {
                            x = event.getX(i)
                            y = event.getY(i)
                        }
                    }
                    i++
                }
            }

            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointers.remove(pointerId)

                if (event.actionMasked != MotionEvent.ACTION_POINTER_UP) {
                    clickHandler.removeCallbacks(selectionRunnable)
                    clickHandler.removeCallbacks(pulseRunnable)
                }
            }
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        // draw all pointers
        val size = activePointers.size()
        var i = 0
        while (i < size) {
            val point = activePointers.valueAt(i)
            val id = activePointers.keyAt(i)
            val x = point.nextX
            val y = point.nextY
//            canvas.drawBitmap(circleBitmap, x, y, touchPaint)
            canvas.drawCircle(
                x, y, 125f * activePointers[id].size, touchPaint
            )
            i++
        }
        canvas.drawText("Total pointers: " + activePointers.size(), 10f, 40f, textPaint)
    }

    private fun popInAnimator(id: Int): ValueAnimator {
        return ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            addUpdateListener { animation ->
                if (activePointers[id] != null) {
                    activePointers[id].size = animation.animatedValue as Float
                    invalidate()
                }
            }
            duration = 750
            interpolator = OvershootInterpolator(2.5f)
        }
    }

    private fun selectionAnimator(id: Int): ValueAnimator {
        return ValueAnimator.ofFloat(1.0f, 2.0f).apply {
            addUpdateListener { animation ->
                run {
                    activePointers[id].size = animation.animatedValue as Float
                    invalidate()
                }
            }
            duration = 1000
            interpolator = OvershootInterpolator(1.25f)
        }
    }


    companion object {
        private const val SELECTION_DELAY = 5500L
        private const val PULSE_DELAY = 2000L
        private const val PULSE_FREQ = 1250L

    }
}