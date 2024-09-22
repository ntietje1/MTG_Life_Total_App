package ui.lifecounter

import data.SettingsManager
import data.TurnTimer
import ui.dialog.COUNTER_DIALOG_ENTRIES
import ui.lifecounter.playerbutton.PlayerButtonViewModel

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
    val firstPlayer: Int? = null,
    val activeTimerIndex: Int? = null,
    val turnTimer: TurnTimer? = null,
)

enum class DayNightState {
    NONE, DAY, NIGHT
}
