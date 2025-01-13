package domain.game

import domain.common.NumberWithRecentChange
import domain.system.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.Player
import ui.lifecounter.playerbutton.PlayerButtonViewModel

/**
 * Manages commander damage and commander mode for players
 * 1 instance per game
 */
class CommanderDamageManager(
    private val notificationManager: NotificationManager
) : AttachableFlowManager<List<PlayerButtonViewModel>>() {
    private val _currentDealer = MutableStateFlow<Player?>(null)
    val currentDealer = _currentDealer.asStateFlow()

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

    fun incrementCommanderDamage(player: Player, value: Int, partner: Boolean): Player {
        requireAttached()
        val currentDealer = _currentDealer.value ?: return player
        val index = (currentDealer.playerNum - 1) + (if (partner) Player.MAX_PLAYERS else 0)
        return receiveCommanderDamage(player, index, value)
    }

    private fun receiveCommanderDamage(player: Player, index: Int, value: Int): Player {
        val currentDamage = player.commanderDamage[index].number
        if (value < 0 && currentDamage + value < 0) {
            notificationManager.showNotification("Commander damage cannot be negative")
            return player
        } else if (value > 0 && currentDamage + value >= 99) {
            notificationManager.showNotification("Commander damage limit reached")
            return player
        }

        return player.copy(
            commanderDamage = player.commanderDamage.toMutableList().apply {
                this[index] = NumberWithRecentChange(currentDamage + value, value)
            }
        )
    }
} 