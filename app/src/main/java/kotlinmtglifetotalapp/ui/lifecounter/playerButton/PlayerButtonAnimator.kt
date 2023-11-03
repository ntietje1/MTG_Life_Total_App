package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.VibratorManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator

class PlayerButtonAnimator (private val playerButton: PlayerButton){

    private var firstJiggle = false
    private var secondJiggle = false

    private var firstDraw = true

    var lastEventUp = false

    private val context = playerButton.context

    private val dpHeight: Float
        get() = context.resources.displayMetrics.density / playerButton.height

    private val dpWidth: Float
        get() = context.resources.displayMetrics.density / playerButton.width

    private var vibration = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

    private lateinit var objectAnimator: ObjectAnimator

    private val jiggleAnimator: ObjectAnimator
        get() = ObjectAnimator.ofPropertyValuesHolder(
            playerButton,
            PropertyValuesHolder.ofFloat("scaleX", 1.0075f - dpWidth * 4, 1f - dpWidth * 4),
            PropertyValuesHolder.ofFloat("scaleY", 1.0075f - dpHeight * 4, 1f - dpHeight * 4)
        ).apply {
            interpolator = AccelerateInterpolator()
            duration = 50
        }

    fun slideIn() {
        if (firstDraw) {
            jiggle()
            val translationXValue = if (playerButton.rotation < 180f) playerButton.width.toFloat() else -playerButton.width.toFloat()
            playerButton.translationX = translationXValue
            objectAnimator = ObjectAnimator.ofFloat(playerButton, "translationX", translationXValue, 0f)

            objectAnimator.duration = 1000
            objectAnimator.interpolator = AccelerateDecelerateInterpolator()
            objectAnimator.start()
            firstDraw = false
        }
    }

    fun jiggleAndVibrate() {
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

        vibrate()
        jiggle()
    }


    fun jiggle() {
        jiggleAnimator.start()
    }

    fun vibrate() {
        vibration.vibrate(
            CombinedVibration.createParallel(
                VibrationEffect.createPredefined(
                    VibrationEffect.EFFECT_HEAVY_CLICK
                )
            ))
    }

    fun performClick() {
        firstJiggle = true
        secondJiggle = false
    }

}