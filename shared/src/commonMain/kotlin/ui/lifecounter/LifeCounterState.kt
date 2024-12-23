package ui.lifecounter

import ui.dialog.COUNTER_DIALOG_ENTRIES
import ui.dialog.MiddleButtonDialogState
import ui.lifecounter.playerbutton.PlayerButtonViewModel

data class LifeCounterState(
    val showButtons: Boolean = false,
    val showLoadingScreen: Boolean = true,
    val blurBackground: Boolean = false,
    val dayNight: DayNightState = DayNightState.NONE,
    val coinFlipHistory: List<String> = emptyList(),
    val counters: List<Int> = List(COUNTER_DIALOG_ENTRIES) { 0 },
    val middleButtonDialogState: MiddleButtonDialogState? = null,
    val middleButtonState: MiddleButtonState = MiddleButtonState.DEFAULT,
)

enum class MiddleButtonState {
    DEFAULT, COMMANDER_EXIT
}

enum class DayNightState {
    NONE, DAY, NIGHT
}
