package kotlinmtglifetotalapp.utils

import android.graphics.Camera
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView

class Rotate3dAnimation(
    private val imageView: ImageView,
    private var curDrawable: Int,
    private var nextDrawable: Int,
    private val fromXDegrees: Float = 0f,
    private val toXDegrees: Float = 180f,
    private val fromYDegrees: Float = 0f,
    private val toYDegrees: Float = 0f,
    private val fromZDegrees: Float = 0f,
    private val toZDegrees: Float = 0f
) : Animation() {
    private var camera: Camera? = null
    private var width = 0
    private var height = 0
    private var numOfRepetition = 0
    private var repeatCount = 0f
    override fun setRepeatCount(repeatCount: Int) {
        super.setRepeatCount(repeatCount)
        this.repeatCount = (repeatCount + 1).toFloat()
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        this.width = width / 2
        this.height = height / 2
        camera = Camera()
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        var xDegrees = fromXDegrees + (toXDegrees - fromXDegrees) * interpolatedTime
        val yDegrees = fromYDegrees + (toYDegrees - fromYDegrees) * interpolatedTime
        val zDegrees = fromZDegrees + (toZDegrees - fromZDegrees) * interpolatedTime
        val matrix = t.matrix

        // ----------------- ZOOM ----------------- //
        if ((numOfRepetition + interpolatedTime) / (repeatCount / 2) <= 1) {
            imageView.scaleX = 1 + (numOfRepetition + interpolatedTime) / (repeatCount / 2)
            imageView.scaleY = 1 + (numOfRepetition + interpolatedTime) / (repeatCount / 2)
        } else if (numOfRepetition < repeatCount) {
            imageView.scaleX = 3 - (numOfRepetition + interpolatedTime) / (repeatCount / 2)
            imageView.scaleY = 3 - (numOfRepetition + interpolatedTime) / (repeatCount / 2)
        }


        // ----------------- ROTATE ----------------- //
        if (interpolatedTime >= 0.5f) {
            if (interpolatedTime == 1f) {
                val temp = curDrawable
                curDrawable = nextDrawable
                nextDrawable = temp
                numOfRepetition++
            } else {
                imageView.setImageResource(nextDrawable)
            }
            xDegrees -= 180f
        } else if (interpolatedTime == 0f) {
            imageView.setImageResource(curDrawable)
        }
        camera!!.save()
        camera!!.rotateX(-xDegrees)
        camera!!.rotateY(yDegrees)
        camera!!.rotateZ(zDegrees)
        camera!!.getMatrix(matrix)
        camera!!.restore()
        matrix.preTranslate(-width.toFloat(), -height.toFloat())
        matrix.postTranslate(width.toFloat(), height.toFloat())
    }
}