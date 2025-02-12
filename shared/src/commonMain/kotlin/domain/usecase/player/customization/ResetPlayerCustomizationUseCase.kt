package domain.usecase.player.customization

import androidx.compose.ui.graphics.Color
import model.Player

class ResetPlayerCustomizationUseCase {
    operator fun invoke(player: Player, usedColors: Set<Color>): Player {
        val newColor = Player.allPlayerColors.filter { it !in usedColors }.random()
        
        return player.copy(
            textColor = Color.White,
            imageString = null,
            color = newColor
        )
    }
}