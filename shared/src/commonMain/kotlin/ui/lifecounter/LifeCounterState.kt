package ui.lifecounter

import domain.game.timer.TurnTimer
import ui.dialog.COUNTER_DIALOG_ENTRIES

data class LifeCounterState(
    val showButtons: Boolean = false,
    val showLoadingScreen: Boolean = true,
    val blurBackground: Boolean = false,
    val dayNight: DayNightState = DayNightState.NONE,
    val coinFlipHistory: List<String> = emptyList(),
    val counters: List<Int> = List(COUNTER_DIALOG_ENTRIES) { 0 },
    val players: List<PlayerSeatUiState> = emptyList(),
    val modalStack: LifeCounterModalStack = LifeCounterModalStack.Empty,
    val middleButtonState: MiddleButtonState = MiddleButtonState.DEFAULT,
) {
    val currentModal: LifeCounterModal? get() = modalStack.current
    val isModalOpen: Boolean get() = currentModal != null

    // Temporary bridge for tutorial pages while Phase 6 removes child view-model mocks.
    val middleButtonDialogState: LifeCounterModal? get() = currentModal
}

enum class MiddleButtonState {
    DEFAULT, COMMANDER_EXIT
}

enum class DayNightState {
    NONE, DAY, NIGHT
}

fun LifeCounterState.showTimer(activePlayerIndex: Int?, timer: TurnTimer?): LifeCounterState {
    return copy(
        players = players.mapIndexed { index, player ->
            player.copy(timer = if (index == activePlayerIndex) timer else null)
        }
    )
}
