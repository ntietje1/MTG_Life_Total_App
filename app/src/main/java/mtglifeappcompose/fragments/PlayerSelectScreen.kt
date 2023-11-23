package mtglifeappcompose.fragments


import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.compose.runtime.MutableState
import androidx.core.util.keyIterator

import kotlin.random.Random


class PlayerSelectScreen(context: Context, attrs: AttributeSet?, numPlayers: MutableState<Int>, private val goToLifeCounter: () -> Unit) : View(context, attrs) {

    private val activePointers: SparseArray<PointT> = SparseArray()
    private val touchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 15f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 100f
        color = Color.WHITE
    }

    private val helperText = "Hold to select player"

    private val helperTextBounds = Rect()

    private val helperTextX get() = width/2f - helperTextBounds.width()/2.1f
    private val helperTextY get() = height/2f
    private var showHelperText = true

    private val rand = Random(System.currentTimeMillis())

    private var lastNewClick = System.currentTimeMillis()

    private val clickHandler = Handler(Looper.getMainLooper())

    private var selectedId = -1

    private var numPlayers = -1

    private val helperTextHandler = Handler(Looper.getMainLooper())

    private val showHelperTextRunnable = Runnable {
        showHelperText = true
        invalidate()
    }

    private val selectionRunnable = Runnable {
        val randomIndex = rand.nextInt(activePointers.size())
        selectedId = activePointers.keyAt(randomIndex)
        numPlayers.value = activePointers.size()

        for (id in activePointers.keyIterator()) {
            if (id == selectedId) {
                selectionAnimator(id).start()
            } else {
                popInAnimator(id).reverse()
            }
        }
        clickHandler.removeCallbacks(pulseRunnable)
    }

    private val pulseRunnable = object : Runnable {
        override fun run() {
            pulseAnimator.start()
            invalidate()
            clickHandler.postDelayed(this, PULSE_FREQ)
        }
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
                if (selectedId == -1 && activePointers.size() < 6) {
                    performClick()
                    val p = PointT(event.getX(pointerIndex), event.getY(pointerIndex))
                    activePointers.put(pointerId, p)
                    popInAnimator(pointerId).start()
                    showHelperText = false
                    helperTextHandler.removeCallbacks(showHelperTextRunnable)

                    if (activePointers.size() > 1) {
                        removeCallbacks()
                        postCallbacks()
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
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
                removeCallbacks()
                helperTextHandler.postDelayed(showHelperTextRunnable, SHOW_HELPER_TEXT_DELAY)

                if (activePointers.size() > 1 && selectedId == -1) {
                    postCallbacks()
                } else if (activePointers.size() == 0 && selectedId != -1) {
                    sendToLifeCounter()
                }
            }
        }
        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = activePointers.size()
        var i = 0
        while (i < size) {
            val point = activePointers.valueAt(i)
            val id = activePointers.keyAt(i)
            val x = point.nextX
            val y = point.nextY

            canvas.drawCircle(x, y, 125f * activePointers[id].size, touchPaint)
            i++
        }
        if (showHelperText && activePointers.size() == 0) {
            with (canvas) {
                textPaint.getTextBounds(helperText, 0, helperText.length, helperTextBounds)
                save()
                rotate(90f, width/2f, height/2f)
                canvas.drawText(helperText, helperTextX, helperTextY,  textPaint)
                restore()
            }
        }

    }

    private fun popInAnimator(id: Int): ValueAnimator {
        return ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            addUpdateListener { animation ->
                if (activePointers[id] != null) {
                    activePointers[id].sizeMultiplier = animation.animatedValue as Float
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
                    if (activePointers[id] != null) {
                        activePointers[id].sizeMultiplier = animation.animatedValue as Float
                        invalidate()
                    }
                }
            }
            duration = 1000
            interpolator = OvershootInterpolator(1.25f)
        }
    }

    private val pulseAnimator = ValueAnimator.ofFloat(1.0f, 1.25f, 1.0f).apply {
        addUpdateListener { animation ->
            run {
                for (id in activePointers.keyIterator()) {
                    if (activePointers[id] != null) {
                        activePointers[id].sizeMultiplier = animation.animatedValue as Float
                    }
                }
                invalidate()
            }
        }
        duration = 1000
        interpolator = DecelerateInterpolator(0.8f)
    }

    private fun sendToLifeCounter() {
        goToLifeCounter()
//        val bundle = Bundle()
//        if (Player.currentPlayers.size != 0) {
//            Player.packBundle(bundle)
//        } else {
//            bundle.putInt("numPlayers", numPlayers)
//        }
//        Navigation.findNavController(this).navigate(R.id.navigation_life_counter, bundle)
    }

    private fun postCallbacks() {
        clickHandler.postDelayed(selectionRunnable, SELECTION_DELAY)
        clickHandler.postDelayed(pulseRunnable, PULSE_DELAY)
    }

    private fun removeCallbacks() {
        clickHandler.removeCallbacks(selectionRunnable)
        clickHandler.removeCallbacks(pulseRunnable)
    }

    companion object {
        private const val PULSE_DELAY = 200L
        private const val PULSE_FREQ = 100L

//        private const val PULSE_DELAY = 1000L
//        private const val PULSE_FREQ = 750L

        private const val SHOW_HELPER_TEXT_DELAY = 5000L
        private const val SELECTION_DELAY = PULSE_DELAY + PULSE_FREQ * 3 - 10
    }
}