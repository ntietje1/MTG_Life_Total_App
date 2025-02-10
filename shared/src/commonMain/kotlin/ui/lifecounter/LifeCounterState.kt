package ui.lifecounter

import model.Game
import ui.dialog.COUNTER_DIALOG_ENTRIES
import ui.dialog.MiddleButtonDialogState

data class LifeCounterState(
    val game: Game = Game(numPlayers = 4),
    val showButtons: Boolean = false,
    val showLoadingScreen: Boolean = true,
    val blurBackground: Boolean = false,
    val coinFlipHistory: List<String> = emptyList(),
    val counters: List<Int> = List(COUNTER_DIALOG_ENTRIES) { 0 },
    val middleButtonDialogState: MiddleButtonDialogState? = null,
    val middleButtonState: MiddleButtonState = MiddleButtonState.DEFAULT,
)

enum class MiddleButtonState {
    DEFAULT, COMMANDER_EXIT
}
