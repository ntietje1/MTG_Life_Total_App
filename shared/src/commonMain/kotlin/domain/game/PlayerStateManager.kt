package domain.game

import data.GameRepository
import domain.common.NumberWithRecentChange
import domain.common.RecentChangeValue
import domain.storage.ISettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import model.Player
import ui.lifecounter.CounterType
import kotlin.coroutines.coroutineContext

/**
 * Manages player state operations
 */
class PlayerStateManager(
    private val settingsManager: ISettingsManager,
    private val gameRepository: GameRepository
)  {
    private val lifeTotalTrackers = mutableMapOf<Int, RecentChangeValue>()

    fun onClear() {
        lifeTotalTrackers.values.forEach { it.detach() }
        lifeTotalTrackers.clear()
    }

    fun savePlayerState(player: Player) {
        return gameRepository.updatePlayer(player)
    }

    fun generatePlayer(playerNum: Int): Player {
        val startingLife = settingsManager.defaultStartingLife.value
        val name = "P$playerNum"
        return Player(lifeTotal = NumberWithRecentChange(startingLife, 0), name = name, playerNum = playerNum)
    }

    fun resetPlayerState(player: Player): Player {
        val startingLife = settingsManager.defaultStartingLife.value
        lifeTotalTrackers[player.playerNum]?.set(startingLife)
        return player.copy(
            lifeTotal = NumberWithRecentChange(startingLife, 0),
            monarch = false,
            setDead = false,
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

    fun setMonarchy(player: Player, value: Boolean): Player {
        return player.copy(monarch = value)
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

    suspend fun attachLifeTracker(
        getCurrentPlayer: () -> Player,
        onUpdate: (Player) -> Unit
    ) {
        val trackerScope = CoroutineScope(coroutineContext + Job())
        val initialPlayer = getCurrentPlayer()

        lifeTotalTrackers[initialPlayer.playerNum] = RecentChangeValue(
            initialValue = initialPlayer.lifeTotal
        ) { newValue ->
            onUpdate(getCurrentPlayer().copy(lifeTotal = newValue))
        }.apply { attach(trackerScope) }
    }

    fun incrementLife(player: Player, value: Int) {
        lifeTotalTrackers[player.playerNum]?.increment(value)
    }
}