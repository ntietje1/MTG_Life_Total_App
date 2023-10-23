package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.content.Context
import android.util.AttributeSet

abstract class BackgroundPickerButton(context: Context, attrs: AttributeSet? = null) : androidx.appcompat.widget.AppCompatButton(context, attrs) {

    abstract fun applyBackground(player: Player)

    init {
    }

}
