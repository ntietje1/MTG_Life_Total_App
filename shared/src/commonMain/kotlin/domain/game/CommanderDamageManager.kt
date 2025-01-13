package domain.game

import domain.common.NumberWithRecentChange
import domain.common.RecentChangeValue
import domain.system.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.Player
import ui.lifecounter.playerbutton.PlayerButtonViewModel
import kotlin.collections.List
import kotlin.collections.forEach
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.toMutableList
import kotlin.coroutines.coroutineContext

/**
 * Manages commander damage and commander mode for players
 * 1 instance per game
 */
sealed class CommanderState {
    data object Inactive : CommanderState()
    data class Active(val dealer: Player) : CommanderState() {
        fun getDealerIndex(partner: Boolean) = dealer.playerNum - 1 + if (partner) Player.MAX_PLAYERS else 0
    }
}

class CommanderDamageManager(
    private val notificationManager: NotificationManager
) : AttachableFlowManager<List<PlayerButtonViewModel>>() {
    companion object {
        const val MAX_COMMANDER_DAMAGE = 100
        const val MIN_COMMANDER_DAMAGE = 0
    }

    private val _commanderState = MutableStateFlow<CommanderState>(CommanderState.Inactive)
    val commanderState = _commanderState.asStateFlow()

    private val commanderDamageTrackers = mutableMapOf<Int, List<RecentChangeValue>>()

    override fun detach() {
        super.detach()
        commanderDamageTrackers.values.forEach { it -> it.forEach { it.detach() } }
        commanderDamageTrackers.clear()
    }

    fun setCurrentDealer(dealer: Player?) {
        requireAttached()
        _commanderState.value = dealer?.let { CommanderState.Active(it) } ?: CommanderState.Inactive
    }

    fun togglePartnerMode(player: Player, value: Boolean): Player {
        requireAttached()
        if (_commanderState.value is CommanderState.Active) {
            _commanderState.value = CommanderState.Active(player.copy(partnerMode = value))
        }
        return player.copy(partnerMode = value)
    }

    fun getCommanderDamage(player: Player, partner: Boolean): NumberWithRecentChange {
        return when (val state = _commanderState.value) {
            is CommanderState.Active -> player.commanderDamage[state.getDealerIndex(partner)]
            is CommanderState.Inactive -> NumberWithRecentChange(0, 0)
        }
    }

    suspend fun attachCommanderTrackers(
        initialPlayer: Player,
        onUpdate: (Player) -> Unit
    ) {
        val trackerScope = CoroutineScope(coroutineContext + Job())

        commanderDamageTrackers[initialPlayer.playerNum] = List(Player.MAX_PLAYERS * 2) { index ->
            RecentChangeValue(initialValue = initialPlayer.commanderDamage[index]) { updatedValue ->
                val currentPlayer = requireAttached().value.getPlayer(initialPlayer.playerNum)
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
        val currentDealerIndex = when (val state = _commanderState.value) {
            is CommanderState.Active -> state.getDealerIndex(partner)
            is CommanderState.Inactive -> return
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