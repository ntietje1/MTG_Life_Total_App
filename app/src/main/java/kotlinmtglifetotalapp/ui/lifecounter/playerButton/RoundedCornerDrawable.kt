package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import androidx.appcompat.content.res.AppCompatResources
import com.example.kotlinmtglifetotalapp.R

class RoundedCornerDrawable(val context: Context, val rippleDrawable: RippleDrawable) :
    RippleDrawable(
        ColorStateList.valueOf(Color.DKGRAY),
        rippleDrawable, // Use the provided rippleDrawable
        ShapeDrawable()
    ) {

    // Other constructor code if needed

    // You can also provide a factory method to create instances of RoundedCornerDrawable
    companion object {
        fun create(context: Context): RoundedCornerDrawable {
            val rippleDrawable = AppCompatResources.getDrawable(
                context,
                R.drawable.rounded_corners
            ) as RippleDrawable

            return RoundedCornerDrawable(context, rippleDrawable)
        }
    }

    private val gradientDrawable: GradientDrawable get() = rippleDrawable.findDrawableByLayerId(android.R.id.background) as GradientDrawable

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
        radius = 30
    }


}