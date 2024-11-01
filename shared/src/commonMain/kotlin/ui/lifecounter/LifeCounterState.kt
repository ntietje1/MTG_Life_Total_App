package ui.lifecounter

import data.TurnTimer
import ui.dialog.COUNTER_DIALOG_ENTRIES
import ui.lifecounter.playerbutton.AbstractPlayerButtonViewModel

data class LifeCounterState(
    val showButtons: Boolean = false,
    val showLoadingScreen: Boolean = true,
    val currentDealer: AbstractPlayerButtonViewModel? = null,
    val blurBackground: Boolean = false,
    val dayNight: DayNightState = DayNightState.NONE,
    val coinFlipHistory: List<String> = emptyList(),
    val counters: List<Int> = List(COUNTER_DIALOG_ENTRIES) { 0 },
    val firstPlayer: Int? = null,
    val activeTimerIndex: Int? = null,
    val turnTimer: TurnTimer? = null,
    val showMiddleButtonDialog: Boolean = false,
    val firstPlayerSelectionActive: Boolean = false,
    val currentDealerIsPartnered: Boolean = false,
)

enum class DayNightState {
    NONE, DAY, NIGHT
}
