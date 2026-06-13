package ui.lifecounter

import domain.game.timer.TurnTimer
import domain.state.game.SeatId
import model.Player
import ui.lifecounter.playerbutton.CommanderState
import ui.lifecounter.playerbutton.PBState

data class PlayerSeatUiState(
    val seatId: SeatId,
    val player: Player,
    val buttonState: PBState = PBState.NORMAL,
    val showCustomizeMenu: Boolean = false,
    val timer: TurnTimer? = null,
    val commanderState: CommanderState = CommanderState.Inactive
)
