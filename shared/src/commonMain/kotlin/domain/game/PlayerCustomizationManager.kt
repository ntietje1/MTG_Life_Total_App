package domain.game

import androidx.compose.ui.graphics.Color
import domain.storage.ISettingsManager
import model.Player
import model.Player.Companion.allPlayerColors
import ui.lifecounter.playerbutton.PlayerButtonViewModel

/**
 * Manages player customization operations
 * Attaches to PlayerButtonViewModels flow to get player color information
 */
class PlayerCustomizationManager(
    private val settingsManager: ISettingsManager
) : AttachableFlowManager<List<PlayerButtonViewModel>>() {

    fun resetPlayerPrefs(player: Player): Player {
        val playerButtonViewModels = requireAttached().value
        val usedColors = playerButtonViewModels.map { it.state.value.player.color }
        val newColor = allPlayerColors.filter { it !in usedColors }.random()

        return player.copy(
            name = "P${player.playerNum}",
            textColor = Color.White,
            imageString = null,
            color = newColor
        )
    }

    fun copyPlayerPrefs(target: Player, source: Player): Player {
        return target.copy(
            imageString = source.imageString,
            color = source.color,
            textColor = source.textColor,
            name = source.name
        )
    }

    fun saveAllPlayerPrefs() {
        val playerButtonViewModels = requireAttached().value
        playerButtonViewModels.forEach {
            savePlayerPrefs(it.state.value.player)
        }
    }

    fun savePlayerPrefs(player: Player) {
        settingsManager.savePlayerPref(player)
    }

    fun resetAllPlayerPrefs() {
        val playerButtonViewModels = requireAttached().value
        playerButtonViewModels.forEach {
            it.resetPlayerPref()
            it.copyPrefs(it.state.value.player)
        }
    }
} 