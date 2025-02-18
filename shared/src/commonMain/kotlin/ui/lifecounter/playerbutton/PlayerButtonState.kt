package ui.lifecounter.playerbutton

import domain.game.timer.TurnTimer
import model.Player

data class PlayerButtonState(
    val player: Player,
    val buttonState: PBState = PBState.NORMAL,
    val showCustomizeMenu: Boolean = false,
    val timer: TurnTimer? = null
)

enum class PBState {
    NORMAL,
    COMMANDER_DEALER,
    COMMANDER_RECEIVER,
    SETTINGS,
    COUNTERS_VIEW,
    COUNTERS_SELECT,
    SELECT_FIRST_PLAYER
}