package domain.state.game

import domain.common.NumberWithRecentChange
import domain.common.RecentChangeValue
import domain.system.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.Player
import kotlin.collections.set
import kotlin.coroutines.coroutineContext

/**
 * Manages commander damage and commander mode for players
 */
sealed class CommanderDealerState {
    data object Inactive : CommanderDealerState()
    data class Active(val dealer: Player) : CommanderDealerState() {
        fun getDealerIndex(partner: Boolean) = dealer.playerNum - 1 + if (partner) Player.MAX_PLAYERS else 0
    }
}

class CommanderDamageState(
    private val notificationManager: NotificationManager
)  {
    companion object {
        const val MAX_COMMANDER_DAMAGE = 100
        const val MIN_COMMANDER_DAMAGE = 0
    }

    private val _commanderDealerState = MutableStateFlow<CommanderDealerState>(CommanderDealerState.Inactive)
    val commanderState = _commanderDealerState.asStateFlow()

    private val commanderDamageTrackers = mutableMapOf<Int, List<RecentChangeValue>>()

    fun onClear() {
        commanderDamageTrackers.values.forEach { it -> it.forEach { it.detach() } }
        commanderDamageTrackers.clear()
    }

    fun setCurrentDealer(dealer: Player?) {
        _commanderDealerState.value = dealer?.let { CommanderDealerState.Active(it) } ?: CommanderDealerState.Inactive
    }

    fun togglePartnerMode(player: Player, value: Boolean): Player {
        if (_commanderDealerState.value is CommanderDealerState.Active) {
            _commanderDealerState.value = CommanderDealerState.Active(player.copy(partnerMode = value))
        }
        return player.copy(partnerMode = value)
    }

    fun getCommanderDamage(player: Player, partner: Boolean): NumberWithRecentChange {
        return when (val state = _commanderDealerState.value) {
            is CommanderDealerState.Active -> player.commanderDamage[state.getDealerIndex(partner)]
            is CommanderDealerState.Inactive -> NumberWithRecentChange(0, 0)
        }
    }

    suspend fun attachCommanderTrackers(
        getCurrentPlayer: () -> Player,
        onUpdate: (Player) -> Unit
    ) {
        val trackerScope = CoroutineScope(coroutineContext + Job())
        val initialPlayer = getCurrentPlayer()

        commanderDamageTrackers[initialPlayer.playerNum] = List(Player.MAX_PLAYERS * 2) { index ->
            RecentChangeValue(initialValue = initialPlayer.commanderDamage[index]) { updatedValue ->
                val currentPlayer = getCurrentPlayer()
                onUpdate(currentPlayer.copy(
                    commanderDamage = currentPlayer.commanderDamage.toMutableList().apply {
                        this[index] = updatedValue
                    }
                ))
            }.apply { attach(trackerScope) }
        }
    }

    fun resetCommanderDamage(player: Player): Player {
        val commanderDamageTracker = requireNotNull(commanderDamageTrackers[player.playerNum])
        commanderDamageTracker.forEach { it.set(0) }
        return player.copy(
            commanderDamage = List(Player.MAX_PLAYERS * 2) { NumberWithRecentChange(0, 0) }
        )
    }

    fun incrementCommanderDamage(player: Player, value: Int, partner: Boolean) {
        val currentDealerIndex = when (val state = _commanderDealerState.value) {
            is CommanderDealerState.Active -> state.getDealerIndex(partner)
            is CommanderDealerState.Inactive -> return
        }
        val currentDamage = player.commanderDamage[currentDealerIndex].number
        if (checkValidCommanderDamage(value, currentDamage)) {
            commanderDamageTrackers[player.playerNum]?.get(currentDealerIndex)?.increment(value)
        }
    }

    private fun checkValidCommanderDamage(value: Int, currentDamage: Int): Boolean {
        if (value < 0 && currentDamage + value < MIN_COMMANDER_DAMAGE) {
            notificationManager.showNotification("Commander damage cannot be negative")
            return false
        } else if (value > 0 && currentDamage + value >= MAX_COMMANDER_DAMAGE) {
            notificationManager.showNotification("Commander damage limit reached")
            return false
        }
        return true
    }
}