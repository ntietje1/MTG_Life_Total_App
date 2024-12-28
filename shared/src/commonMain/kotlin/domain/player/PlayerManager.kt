package domain.player

import androidx.compose.ui.graphics.Color
import data.IImageManager
import data.ISettingsManager
import data.Player
import data.Player.Companion.MAX_PLAYERS
import ui.lifecounter.CounterType

class PlayerManager(
    private val settingsManager: ISettingsManager,
    private val imageManager: IImageManager,
) {
    companion object {
        const val RECENT_CHANGE_DELAY = 1500L
    }

    fun generatePlayer(startingLife: Int, playerNum: Int, color: Color): Player {
        val name = "P$playerNum"
        return Player(color = color, life = startingLife, name = name, playerNum = playerNum)
    }

    fun resetPlayerState(player: Player, startingLife: Int): Player {
        return player.copy(
            life = startingLife,
            recentChange = 0,
            monarch = false,
            setDead = false,
            commanderDamage = List(MAX_PLAYERS * 2) { 0 },
            counters = List(CounterType.entries.size) { 0 },
            activeCounters = listOf()
        )
    }

    fun clearRecentChange(player: Player): Player {
        return player.copy(recentChange = 0)
    }

    fun incrementLife(player: Player, value: Int): Player {
        return player.copy(
            life = player.life + value,
            recentChange = player.recentChange + value
        )
    }

    fun isPlayerDead(player: Player, autoKo: Boolean): Boolean {
        return (autoKo && (
            player.life <= 0 || 
            player.commanderDamage.any { it >= 21 }
        )) || player.setDead
    }

    fun toggleSetDead(player: Player, value: Boolean? = null): Player {
        return player.copy(setDead = value ?: !player.setDead)
    }
}