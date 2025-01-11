package domain.game

import domain.common.NumberWithRecentChange
import model.Player
import model.Player.Companion.MAX_PLAYERS
import ui.lifecounter.CounterType

/**
 * Manages player state operations
 */
class PlayerStateManager {
    fun generatePlayer(startingLife: Int, playerNum: Int): Player {
        val name = "P$playerNum"
        return Player(lifeTotal = NumberWithRecentChange(startingLife, 0), name = name, playerNum = playerNum)
    }

    fun resetPlayerState(player: Player, startingLife: Int): Player {
        return player.copy(
            lifeTotal = NumberWithRecentChange(startingLife, 0),
            monarch = false,
            setDead = false,
            commanderDamage = List(MAX_PLAYERS * 2) { NumberWithRecentChange(0, 0) },
            counters = List(CounterType.entries.size) { 0 },
            activeCounters = listOf()
        )
    }

    fun isPlayerDead(player: Player, autoKo: Boolean): Boolean {
        return player.setDead || (autoKo && (player.life <= 0 || player.commanderDamage.any { it.number >= 21 }))
    }

    fun toggleSetDead(player: Player, value: Boolean? = null): Player {
        return player.copy(setDead = value ?: !player.setDead)
    }

    fun incrementCounter(player: Player, counterType: CounterType, value: Int): Player {
        return player.copy(counters = player.counters.toMutableList().apply {
            this[counterType.ordinal] += value
        })
    }

    fun setActiveCounters(player: Player, counterType: CounterType, active: Boolean): Player {
        return player.copy(activeCounters = player.activeCounters.toMutableList().apply {
            if (active) {
                add(counterType)
            } else {
                remove(counterType)
            }
        })
    }
}