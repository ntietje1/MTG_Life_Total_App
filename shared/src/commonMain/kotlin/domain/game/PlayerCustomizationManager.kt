package domain.game

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import domain.state.game.PlayerProfileId
import domain.state.profile.PlayerBackground
import domain.state.profile.PlayerColors
import domain.state.profile.PlayerProfile
import domain.state.profile.PlayerProfileRepository
import model.Player
import model.Player.Companion.allPlayerColors
import ui.lifecounter.playerbutton.PlayerButtonViewModel

/**
 * Manages player customization operations
 * Attaches to PlayerButtonViewModels flow to get player color information
 */
class PlayerCustomizationManager(
    private val profileRepository: PlayerProfileRepository
) : AttachableFlowManager<List<PlayerButtonViewModel>>() {

    fun resetPlayerPrefs(player: Player): Player {
        val playerButtonViewModels = requireAttached().value
        val usedColors = playerButtonViewModels.map { it.state.value.player.color }
        val newColor = allPlayerColors.filter { it !in usedColors }.random()

        return player.copy(
            name = "P${player.playerNum}",
            textColor = Color.White,
            imageString = null,
            background = PlayerBackground.None,
            color = newColor
        )
    }

    fun copyPlayerPrefs(target: Player, source: Player): Player {
        return target.copy(
            imageString = source.imageString,
            background = source.background,
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
        profileRepository.saveProfile(player.toProfile())
    }

    fun resetAllPlayerPrefs() {
        val playerButtonViewModels = requireAttached().value
        playerButtonViewModels.forEach {
            it.resetPlayerPref()
            it.copyPrefs(it.state.value.player)
        }
    }
}

private fun Player.toProfile(): PlayerProfile {
    return PlayerProfile(
        id = PlayerProfileId(name),
        displayName = name,
        colors = PlayerColors(
            backgroundArgb = color.toArgb(),
            textArgb = textColor.toArgb()
        ),
        background = if (background == PlayerBackground.None) {
            imageString?.let(PlayerBackground::LocalImage) ?: PlayerBackground.None
        } else {
            background
        }
    )
}
