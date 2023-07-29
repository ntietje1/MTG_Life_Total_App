package com.example.kotlinmtglifetotalapp.ui.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.concurrent.ConcurrentHashMap

class PlayerSelectScreen(context: Context?, attrs: AttributeSet?) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    private val surfaceHolder: SurfaceHolder = holder
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val points: ConcurrentHashMap<Int, MotionEvent> = ConcurrentHashMap()
    private var renderingThread: Thread? = null // Custom thread for rendering
    private var isRunning: Boolean = false // Flag to indicate whether the app is running or paused

    private var availableColors: MutableList<Int> = mutableListOf(
        Color.parseColor("#F75FA8"),
        Color.parseColor("#F75F5F"),
        Color.parseColor("#F7C45F"),
        Color.parseColor("#92F75F"),
        Color.parseColor("#5FEAF7"),
        Color.parseColor("#625FF7"),
        Color.parseColor("#C25FF7"),
    )

    init {
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        surfaceHolder.addCallback(this)


    }

    override fun performClick(): Boolean {
        paint.color = availableColors.random()

        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        synchronized(points) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    performClick()
                    // Update the points data with new touch events
                    for (i in 0 until event.pointerCount) {
                        val pointerId = event.getPointerId(i)
                        points[pointerId] = MotionEvent.obtain(event)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    // Update the points data for moved touch events
                    for (i in 0 until event.pointerCount) {
                        val pointerId = event.getPointerId(i)
                        points[pointerId] = MotionEvent.obtain(event)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    // Remove lifted pointers from the points data
                    val pointerId = event.getPointerId(event.actionIndex)
                    points.remove(pointerId)
                }

                else -> {}
            }
        }
        return true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isRunning = true // App is running, set the flag to true
        renderingThread = Thread(RenderRunnable())
        renderingThread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // Surface properties changed, if necessary, handle changes here.
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isRunning = false // App is paused or destroyed, set the flag to false
        var retry = true
        renderingThread?.interrupt()
        while (retry) {
            try {
                renderingThread?.join()
                retry = false
            } catch (e: InterruptedException) {
                // Retry until the thread is properly shut down.
            }
        }
    }

    inner class RenderRunnable : Runnable {
        override fun run() {
            while (isRunning && !Thread.currentThread().isInterrupted) {
                val canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(points) {
                        canvas.drawColor(Color.BLACK)
                        // Draw the circles based on the points data
                        for (pointerId in points.keys) {
                            val event = points[pointerId]
                            if (event != null) {
                                canvas.drawCircle(
                                    event.getX(event.findPointerIndex(pointerId)),
                                    event.getY(event.findPointerIndex(pointerId)),
                                    100f,
                                    paint
                                )
                            }
                        }
                    }
                    surfaceHolder.unlockCanvasAndPost(canvas)
                }
            }
        }
    }
}
