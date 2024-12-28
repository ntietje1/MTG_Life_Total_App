package domain.player

import androidx.compose.ui.graphics.Color
import data.Player

class PlayerCustomizationManager {
    fun resetPlayerPreferences(player: Player): Player {
        return player.copy(
            name = "P${player.playerNum}",
            textColor = Color.White,
            imageString = null
        )
    }

    fun copyPlayerPreferences(target: Player, source: Player): Player {
        return target.copy(
            imageString = source.imageString,
            color = source.color,
            textColor = source.textColor,
            name = source.name
        )
    }
} 