package ui.lifecounter

import ui.dialog.COUNTER_DIALOG_ENTRIES
import ui.lifecounter.playerbutton.PlayerButtonViewModel
import data.SettingsManager
import data.TurnTimer

data class LifeCounterState(
    val showButtons: Boolean = false,
    val numPlayers: Int = SettingsManager.instance.numPlayers.apply {
        println("GOT NUM PLAYERS: $this")
    },
    val showLoadingScreen: Boolean = true,
    val currentDealer: PlayerButtonViewModel? = null,
    val blurBackground: Boolean = false,
    val dayNight: DayNightState = DayNightState.NONE,
    val coinFlipHistory: List<String> = emptyList(),
    val counters: List<Int> = List(COUNTER_DIALOG_ENTRIES) { 0 },
    val firstPlayer: Int? = 0,
    val activeTimerIndex: Int? = 0,
    val turnTimer: TurnTimer = TurnTimer(seconds = 0, turn = 1),
)

enum class DayNightState {
    NONE, DAY, NIGHT
}
