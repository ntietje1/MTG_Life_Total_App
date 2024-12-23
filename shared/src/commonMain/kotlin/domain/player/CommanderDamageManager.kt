package domain.player

import data.Player
import di.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CommanderDamageManager(
    private val notificationManager: NotificationManager
) {
    private val _currentDealer = MutableStateFlow<Player?>(null)
    val currentDealer = _currentDealer.asStateFlow()

    fun setCurrentDealer(dealer: Player?) {
        _currentDealer.value = dealer
    }

    fun togglePartnerMode(player: Player, value: Boolean): Player {
        if (_currentDealer.value?.playerNum == player.playerNum) {
            _currentDealer.value = player.copy(partnerMode = value)
        }
        return player.copy(partnerMode = value)
    }
} 