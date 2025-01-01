package domain.player

import androidx.compose.ui.graphics.Color
import data.Player
import data.Player.Companion.allPlayerColors
import kotlinx.coroutines.flow.StateFlow
import ui.lifecounter.playerbutton.PlayerButtonViewModel

class PlayerCustomizationManager {

    private lateinit var playerViewModelsFlow: StateFlow<List<PlayerButtonViewModel>>

    fun init(playerViewModelsFlow: StateFlow<List<PlayerButtonViewModel>>) {
        if (::playerViewModelsFlow.isInitialized) {
            throw IllegalStateException("PlayerCustomizationManager already initialized")
        }
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