package composable.lifecounter

import composable.dialog.COUNTER_DIALOG_ENTRIES
import composable.lifecounter.playerbutton.PlayerButtonViewModel
import data.SettingsManager

data class LifeCounterState(
    val showButtons: Boolean = false,
    val numPlayers: Int = SettingsManager.instance.numPlayers,
    val showLoadingScreen: Boolean = true,
    val currentDealer: PlayerButtonViewModel? = null,
    val blurBackground: Boolean = false,
    val dayNight: DayNightState = DayNightState.NONE,
    val coinFlipHistory: List<String> = emptyList(),
    val counters: List<Int> = List(COUNTER_DIALOG_ENTRIES) { 0 }
    )

enum class DayNightState {
    NONE, DAY, NIGHT
}
