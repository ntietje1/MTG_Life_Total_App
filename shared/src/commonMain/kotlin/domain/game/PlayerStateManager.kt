package domain.game

import domain.common.NumberWithRecentChange
import domain.common.RecentChangeValue
import domain.storage.ISettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import model.Player
import ui.lifecounter.CounterType
import ui.lifecounter.playerbutton.PlayerButtonViewModel
import kotlin.coroutines.coroutineContext

/**
 * Manages player state operations
 */
class PlayerStateManager(
    private val settingsManager: ISettingsManager
) : AttachableFlowManager<List<PlayerButtonViewModel>>() {
    private val lifeTotalTrackers = mutableMapOf<Int, RecentChangeValue>()

    override fun detach() {
        super.detach()
        lifeTotalTrackers.values.forEach { it.detach() }
        lifeTotalTrackers.clear()
    }

    fun generatePlayer(playerNum: Int): Player {
        val startingLife = settingsManager.startingLife.value
        val name = "P$playerNum"
        return Player(lifeTotal = NumberWithRecentChange(startingLife, 0), name = name, playerNum = playerNum)
    }

    fun resetPlayerState(player: Player): Player {
        val startingLife = settingsManager.startingLife.value
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
        initialPlayer: Player,
        onUpdate: (Player) -> Unit
    ) {
        val trackerScope = CoroutineScope(coroutineContext + Job())

        lifeTotalTrackers[initialPlayer.playerNum] = RecentChangeValue(
            initialValue = initialPlayer.lifeTotal
        ) { newValue ->
            val currentPlayer = requireAttached().value.getPlayer(initialPlayer.playerNum)
            onUpdate(currentPlayer.copy(lifeTotal = newValue))
        }.apply { attach(trackerScope) }
    }

    fun incrementLife(player: Player, value: Int) {
        lifeTotalTrackers[player.playerNum]?.increment(value)
    }
}

fun List<PlayerButtonViewModel>.getPlayer(playerNum: Int): Player {
    return find { it.state.value.player.playerNum == playerNum }?.state?.value?.player ?: throw IllegalArgumentException("Player not found")
}