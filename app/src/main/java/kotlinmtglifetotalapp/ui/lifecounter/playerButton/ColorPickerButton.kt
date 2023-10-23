package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet

class ColorPickerButton(context: Context, attrs: AttributeSet? = null) : BackgroundPickerButton(
    context,
    attrs
) {

    var color: Int = Color.WHITE
        set(value) {
            field = value
            setBackgroundColor(value)
        }

    override fun applyBackground(player: Player) {
        player.playerColor = color
    }

}