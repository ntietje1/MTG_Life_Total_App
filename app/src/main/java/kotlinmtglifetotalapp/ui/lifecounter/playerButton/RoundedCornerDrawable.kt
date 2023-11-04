package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.content.Context
import android.content.res.ColorStateList

import android.graphics.Color

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable

import androidx.appcompat.content.res.AppCompatResources

import androidx.core.graphics.ColorUtils
import com.example.kotlinmtglifetotalapp.R

class RoundedCornerDrawable(val context: Context, val rippleDrawable: RippleDrawable) :
    RippleDrawable(
        ColorStateList.valueOf(Color.DKGRAY),
        rippleDrawable, // Use the provided rippleDrawable
        null
    ) {

    // Other constructor code if needed

    companion object {
        fun create(context: Context): RoundedCornerDrawable {
            val rippleDrawable = AppCompatResources.getDrawable(
                context,
                R.drawable.rounded_corners
            ) as RippleDrawable

            val res = RoundedCornerDrawable(context, rippleDrawable)
            res.backgroundColor = Color.DKGRAY
            return res
        }
    }

    private val gradientDrawable: GradientDrawable = GradientDrawable()

    var rippleColor: Int = Color.WHITE
        set(v) = run {
            rippleDrawable.setColor(ColorStateList.valueOf(v))
            invalidateSelf()
        }

    var backgroundColor: Int = Color.DKGRAY
        set(v) = run {
            gradientDrawable.color = ColorStateList.valueOf(v)
            invalidateSelf()
        }

    var backgroundAlpha: Int = 255
        set(v) = run {
            gradientDrawable.alpha = v
            invalidateSelf()
        }

    var backgroundRadius: Int = 30
        set(v) = run {
            gradientDrawable.cornerRadius =
                context.resources.getDimensionPixelSize(R.dimen.one) * v.toFloat()
            invalidateSelf()

        }

    fun setOutline(width: Int, color: Int) {
        gradientDrawable.setStroke(width, color)
        invalidateSelf()
    }

    init {
        rippleDrawable.setDrawableByLayerId(android.R.id.background, gradientDrawable)
        radius = 30
        rippleDrawable.setColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(Color.WHITE, 0)))
        setOutline(0, 0)
    }




}