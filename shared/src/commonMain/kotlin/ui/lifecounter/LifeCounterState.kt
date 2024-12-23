package ui.lifecounter

import ui.dialog.COUNTER_DIALOG_ENTRIES
import ui.dialog.MiddleButtonDialogState
import ui.lifecounter.playerbutton.PlayerButtonViewModel

data class LifeCounterState(
    val showButtons: Boolean = false,
    val showLoadingScreen: Boolean = true,
    val currentDealer: PlayerButtonViewModel? = null,
    val blurBackground: Boolean = false,
    val dayNight: DayNightState = DayNightState.NONE,
    val coinFlipHistory: List<String> = emptyList(),
    val counters: List<Int> = List(COUNTER_DIALOG_ENTRIES) { 0 },
    val firstPlayer: Int? = null,
    val activeTimerIndex: Int? = null,
    val middleButtonDialogState: MiddleButtonDialogState? = null,
    val firstPlayerSelectionActive: Boolean = false,
    val currentDealerIsPartnered: Boolean = false,
)

enum class DayNightState {
    NONE, DAY, NIGHT
}
