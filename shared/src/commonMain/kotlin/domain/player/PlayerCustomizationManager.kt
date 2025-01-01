package domain.player

import androidx.compose.ui.graphics.Color
import data.Player
import data.Player.Companion.allPlayerColors
import kotlinx.coroutines.flow.StateFlow
import ui.lifecounter.playerbutton.PlayerButtonViewModel

/**
 * Manages player customization operations
 * Attaches to PlayerButtonViewModels flow to get player color information
 */
class PlayerCustomizationManager {

    private lateinit var playerViewModelsFlow: StateFlow<List<PlayerButtonViewModel>>

    fun init(playerViewModelsFlow: StateFlow<List<PlayerButtonViewModel>>) {
        this.playerViewModelsFlow = playerViewModelsFlow
    }

    fun resetPlayerPreferences(player: Player): Player {
        val usedColors = playerViewModelsFlow.value.map { it.state.value.player.color }
        val newColor = allPlayerColors.filter { it !in usedColors }.random()
        
        return player.copy(
            name = "P${player.playerNum}",
            textColor = Color.White,
            imageString = null,
            color = newColor
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