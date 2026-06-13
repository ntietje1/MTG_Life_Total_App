package ui.lifecounter.playerbutton

import model.Player
import domain.game.timer.TurnTimer

data class PlayerButtonState(
    val player: Player,
    val buttonState: PBState = PBState.NORMAL,
    val showCustomizeMenu: Boolean = false,
    val timer: TurnTimer? = null
)

fun PlayerButtonState.showsBackButton(backStackIsEmpty: Boolean): Boolean {
    return buttonState !in setOf(
        PBState.SELECT_FIRST_PLAYER,
        PBState.COMMANDER_RECEIVER,
        PBState.COMMANDER_DEALER
    ) && !backStackIsEmpty
}

enum class PBState {
    NORMAL,
    COMMANDER_DEALER,
    COMMANDER_RECEIVER,
    SETTINGS,
    COUNTERS_VIEW,
    COUNTERS_SELECT,
    SELECT_FIRST_PLAYER
}
