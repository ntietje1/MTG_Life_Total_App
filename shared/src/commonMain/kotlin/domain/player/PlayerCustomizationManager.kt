package domain.player

import androidx.compose.ui.graphics.Color
import data.ISettingsManager
import data.Player
import data.Player.Companion.allPlayerColors
import domain.base.AttachableManager
import ui.lifecounter.playerbutton.PlayerButtonViewModel

/**
 * Manages player customization operations
 * Attaches to PlayerButtonViewModels flow to get player color information
 */
class PlayerCustomizationManager(
    private val settingsManager: ISettingsManager
) : AttachableManager<List<PlayerButtonViewModel>>() {

    fun resetPlayerPrefs(player: Player): Player {
        checkAttached()
        val usedColors = attachedFlow!!.value.map { it.state.value.player.color }
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
        attachedFlow!!.value.forEach {
            savePlayerPrefs(it.state.value.player)
        }
    }

    fun savePlayerPrefs(player: Player) {
        settingsManager.savePlayerPref(player)
    }

    fun resetAllPlayerPrefs() {
        attachedFlow!!.value.forEach {
            it.resetPlayerPref()
            it.copyPrefs(it.state.value.player)
        }
    }
} 