package domain.game

import androidx.compose.ui.graphics.Color
import data.GameRepository
import domain.storage.ISettingsStore
import model.Player
import model.Player.Companion.allPlayerColors

/**
 * Manages player customization operations
 * Attaches to PlayerButtonViewModels flow to get player color information
 */
class PlayerCustomizationManager(
    private val settingsManager: ISettingsStore,
    private val gameRepository: GameRepository,
) {

    fun resetPlayerPrefs(player: Player, usedColors: Set<Color>): Player {
        val newColor = allPlayerColors.filter { it !in usedColors }.random()

        return player.copy(
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

    fun savePlayerPrefs(player: Player) {
        settingsManager.savePlayerPref(player)
    }
} 