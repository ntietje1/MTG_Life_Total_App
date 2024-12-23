package ui.lifecounter.playerbutton

import data.Player
import features.timer.TurnTimer

data class PlayerButtonState(
    val player: Player,
    val buttonState: PBState = PBState.NORMAL,
    val showCustomizeMenu: Boolean = false,
    val backStack: List<() -> Unit> = listOf(),
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