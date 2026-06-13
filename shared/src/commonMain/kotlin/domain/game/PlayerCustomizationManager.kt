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

interface PlayerCustomizationHost {
    val players: List<Player>

    fun replacePlayer(player: Player)
}

/**
 * Manages player customization operations
 */
class PlayerCustomizationManager(
    private val profileRepository: PlayerProfileRepository
) : AttachableManager<PlayerCustomizationHost>() {

    fun resetPlayerPrefs(player: Player): Player {
        val usedColors = requireAttached().players.map { it.color }
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
        requireAttached().players.forEach(::savePlayerPrefs)
    }

    fun savePlayerPrefs(player: Player) {
        profileRepository.saveProfile(player.toProfile())
    }

    fun resetAllPlayerPrefs() {
        val host = requireAttached()
        host.players.forEach { player ->
            host.replacePlayer(resetPlayerPrefs(player))
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
