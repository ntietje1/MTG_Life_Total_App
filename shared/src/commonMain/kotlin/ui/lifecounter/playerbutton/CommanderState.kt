package ui.lifecounter.playerbutton

import model.Player

sealed class CommanderState {
    data object Inactive : CommanderState()

    data class Active(val dealer: Player) : CommanderState() {
        fun getDealerIndex(partner: Boolean): Int {
            return dealer.playerNum - 1 + if (partner) Player.MAX_PLAYERS else 0
        }
    }
}
