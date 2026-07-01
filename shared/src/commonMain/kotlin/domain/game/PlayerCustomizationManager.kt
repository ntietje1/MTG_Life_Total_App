package domain.game

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import domain.state.game.PlayerProfileId
import domain.state.game.SeatAppearance
import domain.state.profile.PlayerBackground
import domain.state.profile.PlayerColors
import domain.state.profile.PlayerProfile
import domain.state.profile.PlayerProfileRepository
import model.Player
import model.Player.Companion.allPlayerColors

interface PlayerCustomizationHost {
    val players: List<Player>
}

/**
 * Manages player customization operations
 */
class PlayerCustomizationManager(
    private val profileRepository: PlayerProfileRepository,
    private val defaultColorOrder: (List<Color>) -> List<Color> = { colors -> colors.shuffled() }
) : AttachableManager<PlayerCustomizationHost>() {

    fun resetPlayerPrefs(player: Player): Player {
        val usedColors = requireAttached().players.map { it.color }
        val newColor = allPlayerColors.filter { it !in usedColors }.random()

        return player.defaultAppearance(newColor)
    }

    fun resetAllPlayerPrefs(): List<Player> {
        val players = requireAttached().players
        val colors = freshDefaultColors(players)
        return players.mapIndexed { index, player ->
            player.defaultAppearance(colors[index])
        }
    }

    private fun freshDefaultColors(players: List<Player>): List<Color> {
        val colors = defaultColorOrder(allPlayerColors)
        val selectedColors = mutableListOf<Color>()
        players.forEach { player ->
            selectedColors += colors.first { color -> color !in selectedColors && color != player.color }
        }
        return selectedColors
    }

    private fun Player.defaultAppearance(color: Color): Player {
        return copy(
            name = "P$playerNum",
            textColor = Color.White,
            imageString = null,
            background = PlayerBackground.None,
            color = color
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

    fun seatAppearanceFor(player: Player): SeatAppearance {
        return player.toProfile().toSeatAppearance()
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
