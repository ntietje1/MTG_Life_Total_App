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
class CommanderDamageManager(
    private val notificationManager: NotificationManager
) : AttachableFlowManager<List<PlayerButtonViewModel>>() {
    companion object {
        const val MAX_COMMANDER_DAMAGE = 100
        const val MIN_COMMANDER_DAMAGE = 0
    }

    private val _currentDealer = MutableStateFlow<Player?>(null)
    val currentDealer = _currentDealer.asStateFlow()

    private val commanderDamageTrackers = mutableMapOf<Int, List<RecentChangeValue>>()

    override fun detach() {
        super.detach()
        commanderDamageTrackers.values.forEach { it -> it.forEach { it.cancel() } }
        commanderDamageTrackers.clear()
    }

    fun setCurrentDealer(dealer: Player?) {
        requireAttached()
        _currentDealer.value = dealer
    }

    fun togglePartnerMode(player: Player, value: Boolean): Player {
        requireAttached()
        if (_currentDealer.value?.playerNum == player.playerNum) {
            _currentDealer.value = player.copy(partnerMode = value)
        }
        return player.copy(partnerMode = value)
    }

    fun getCommanderDamage(player: Player, partner: Boolean): NumberWithRecentChange {
        val currentDealer = _currentDealer.value ?: return NumberWithRecentChange(0, 0)
        val index = (currentDealer.playerNum - 1) + (if (partner) Player.MAX_PLAYERS else 0)
        return player.commanderDamage[index]
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
        val currentDealer = _currentDealer.value ?: return
        val index = (currentDealer.playerNum - 1) + (if (partner) Player.MAX_PLAYERS else 0)
        val currentDamage = player.commanderDamage[index].number
        if (checkValidCommanderDamage(value, currentDamage)) {
            commanderDamageTrackers[player.playerNum]?.get(index)?.increment(value)
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