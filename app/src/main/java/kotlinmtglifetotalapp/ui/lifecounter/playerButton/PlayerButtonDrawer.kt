package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.withMatrix
import com.example.kotlinmtglifetotalapp.R

class PlayerButtonDrawer(private val playerButtonBase: PlayerButtonBase) {

    private val context = playerButtonBase.context

    private val player get() = playerButtonBase.player!!

    private val icon: Bitmap
        get() {
            val drawableResId = when (playerButtonBase.state) {
                PlayerButtonState.NORMAL -> R.drawable.heart_solid_icon
                PlayerButtonState.COMMANDER_RECEIVER -> R.drawable.commander_solid_icon_small
                PlayerButtonState.COMMANDER_DEALER -> R.drawable.transparent
            }

            val drawable = AppCompatResources.getDrawable(context, drawableResId)
            return drawable?.toBitmap() ?: throw IllegalStateException("Drawable is null")
        }

    private val paintVerySmall: Paint
        get() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = (playerButtonBase.height / 20f)
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
        }

    private val paintSmall: Paint
        get() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = (playerButtonBase.height / 15f) + (playerButtonBase.width / 30f)
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
        }

    private val paintLarge: Paint
        get() = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = (playerButtonBase.height / 5f) + (playerButtonBase.width / 4f)
            textAlign = Paint.Align.CENTER
            color = Color.WHITE
            typeface = context.resources.getFont(R.font.robotobold)
        }

    private val centerX: Float
        get() = playerButtonBase.width / 2f

    private val centerY: Float
        get() = playerButtonBase.height / 2f

    private val topLineY: Float
        get() = centerY - playerButtonBase.height * 0.1f - playerButtonBase.width / 10

    private val midLineY: Float
        get() = centerY * 0.625f + (paintLarge.descent() - paintLarge.ascent()) - playerButtonBase.width / 5.25f

    private val rotatedMatrix
        get(): Matrix {
            return Matrix().apply {
                setRotate(playerButtonBase.rotation - 90, centerX, centerY)
            }
        }

    private val mainText: String
        get(): String {
            return when (playerButtonBase.state) {
                PlayerButtonState.NORMAL -> player.life.toString()
                PlayerButtonState.COMMANDER_RECEIVER -> PlayerButtonBase.currCommanderDamage[player.playerNum - 1].toString()
                PlayerButtonState.COMMANDER_DEALER -> "Deal damage with your commander"
            }
        }

    private val recentChangeText: String
        get(): String {
            return if (player.recentChange > 0) {
                (if (player.recentChange > 0) "+" else "") + player.recentChange.toString()
            } else {
                ""
            }
        }


    // TODO: add text to commander damage dealer
    fun draw(canvas: Canvas) {
        with(canvas) {
            save()
            rotate(playerButtonBase.rotation, centerX, centerY)
            withMatrix(rotatedMatrix) {
                drawText(
                    recentChangeText,
                    centerX + paintLarge.measureText(mainText) / 2 + 100,
                    midLineY - 75,
                    paintSmall
                )
                //drawText(player.toString(), centerX, topLineY, paintSmall)
                if (playerButtonBase.state != PlayerButtonState.COMMANDER_DEALER) {
                    drawText(mainText, centerX, midLineY, paintLarge)
                } else {
                    drawText(mainText, centerX, centerY + (paintVerySmall.descent()), paintVerySmall)
                }

                //drawText(player.commanderDamage.toString(), centerX, topLineY, paintSmall)

                val iconPos = calculateIconTopLeft(icon)

                drawBitmap(icon, iconPos.x, iconPos.y, paintSmall)

            }
            restore()
        }
    }

    fun setBackground() {
        with (playerButtonBase) {
            setBackgroundResource(R.drawable.rounded_corners)
            val rippleDrawable = background as RippleDrawable
            val gradientDrawable =
                rippleDrawable.findDrawableByLayerId(android.R.id.background) as GradientDrawable

            val colorStateListRipple =
                ColorStateList.valueOf(ColorUtils.setAlphaComponent(Color.WHITE, 60))
            rippleDrawable.setColor(colorStateListRipple)

            val colorStateListBackground = when (state) {
                PlayerButtonState.NORMAL -> ColorStateList.valueOf(player!!.playerColor)
                PlayerButtonState.COMMANDER_RECEIVER -> ColorStateList.valueOf(Color.DKGRAY)
                PlayerButtonState.COMMANDER_DEALER -> ColorStateList.valueOf(darkenColor(desaturateColor(player!!.playerColor)))
            }

            gradientDrawable.color = colorStateListBackground
        }
    }

    private fun darkenColor(color: Int): Int {
        val factor = 0.6f
        val red = Color.red(color) * factor
        val green = Color.green(color) * factor
        val blue = Color.blue(color) * factor
        return Color.rgb(red.toInt(), green.toInt(), blue.toInt())
    }

    private fun desaturateColor(color: Int): Int {
        val factor = 0.6f
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(color, hsl)

        // Reduce the saturation
        hsl[1] *= factor

        return ColorUtils.HSLToColor(hsl)
    }

    private fun calculateIconTopLeft(bitmap: Bitmap): PointF {
        val iconLeft = centerX - bitmap.width.toFloat() / 2
        val iconTop = midLineY + paintLarge.descent() - playerButtonBase.height / 20
        return PointF(iconLeft, iconTop)
    }
}