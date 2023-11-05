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
import java.lang.Integer.min

class PlayerButtonDrawer(private val playerButtonBase: PlayerButtonBase) {

    private val context = playerButtonBase.context

    private val player get() = playerButtonBase.player!!

    private val rippleDrawable get() = playerButtonBase.background as RippleDrawable
    private val background get() = rippleDrawable.findDrawableByLayerId(android.R.id.background) as GradientDrawable

    private var colorList: ColorStateList
        get() = background.color!!
        set(value) {
            background.color = value
        }

    private var color: Int
        get() = colorList.defaultColor
        set(value) {
            colorList = ColorStateList.valueOf(value)
        }

    private val icon: Bitmap
        get() {
            val drawableResId = when (playerButtonBase.state) {
                PlayerButtonState.NORMAL -> R.drawable.heart_solid_icon
                PlayerButtonState.COMMANDER_RECEIVER -> R.drawable.commander_solid_icon_small
                else -> R.drawable.transparent
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
            textSize = paintLarge.textSize / 5f
//            textSize = (playerButtonBase.height / 15f) + (playerButtonBase.width / 30f)
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
        get() = midLineY + paintLarge.ascent()*0.865f
//    get() = centerY - playerButtonBase.height * 0.1f - playerButtonBase.width / 10

    private val midLineY: Float
        get() = centerY - paintLarge.ascent()/2.5f
    //get() = centerY * 0.625f + (paintLarge.descent() - paintLarge.ascent()) - playerButtonBase.width / 5.25f

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
                else -> ""
            }
        }

    private val recentChangeText: String
        get(): String {
            return if (player.recentChange != 0) {
                (if (player.recentChange > 0) "+" else "") + player.recentChange.toString()
            } else {
                ""
            }
        }

    init {
        playerButtonBase.setBackgroundResource(R.drawable.rounded_corners)
        val colorStateListRipple =
            ColorStateList.valueOf(ColorUtils.setAlphaComponent(Color.WHITE, 60))
        rippleDrawable.setColor(colorStateListRipple)
        //rippleDrawable.alpha = 100
    }

    fun draw(canvas: Canvas) {
        with(canvas) {
            save()
            rotate(playerButtonBase.rotation, centerX, centerY)
            withMatrix(rotatedMatrix) {
                if (playerButtonBase.state == PlayerButtonState.NORMAL || playerButtonBase.state == PlayerButtonState.COMMANDER_RECEIVER) {
                    drawText(
                        recentChangeText,
                        centerX + paintLarge.measureText(mainText) / 2 + 100,
                        midLineY - 75,
                        paintSmall
                    )
                }
                if (playerButtonBase.state != PlayerButtonState.SETTINGS) {
                    drawText(player.name, centerX, topLineY, paintSmall)
                }
                if (playerButtonBase.state != PlayerButtonState.COMMANDER_DEALER) {
                    drawText(mainText, centerX, midLineY, paintLarge)
                } else {
                    drawText(mainText, centerX, centerY + (paintVerySmall.descent()), paintVerySmall)
                }

                val iconPos = calculateIconTopLeft(icon)

                drawBitmap(icon, iconPos.x, iconPos.y, paintSmall)

            }
            restore()
        }
    }

    private val strokeWidth
        get() = if (playerButtonBase.player!!.monarch) {
            0
        } else {
            0
        }

    fun setBackground() {
        with (playerButtonBase) {
            color = when (state) {
                PlayerButtonState.NORMAL -> {
                    if (player!!.isDead) {
                        player!!.playerColor.desaturateColor(0.9f)
                    } else {
                        player!!.playerColor
                    }
                }
                PlayerButtonState.COMMANDER_RECEIVER -> Color.DKGRAY
                PlayerButtonState.COMMANDER_DEALER -> player!!.playerColor.desaturateColor().darkenColor()
                else -> player!!.playerColor.desaturateColor(0.8f).darkenColor(0.8f)
            }

            // Modify the stroke width and color
            val newStrokeColor = Color.parseColor("#f2d100")
            this@PlayerButtonDrawer.background.setStroke(strokeWidth, newStrokeColor)
            playerButtonCallback.updateMonarchy()
        }

    }

    private fun calculateIconTopLeft(bitmap: Bitmap): PointF {
        val iconLeft = centerX - bitmap.width.toFloat() / 2
        val iconTop = midLineY + paintLarge.descent() - playerButtonBase.height / 20
        return PointF(iconLeft, iconTop)
    }
}

fun Int.darkenColor(factor: Float = 0.6f): Int {
    val red = Color.red(this) * factor
    val green = Color.green(this) * factor
    val blue = Color.blue(this) * factor
    return Color.rgb(red.toInt(), green.toInt(), blue.toInt())
}

fun Int.desaturateColor(factor: Float = 0.6f): Int {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this, hsl)
    hsl[1] *= factor
    return ColorUtils.HSLToColor(hsl)
}