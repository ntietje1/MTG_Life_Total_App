package ui.lifecounter

import domain.state.game.SeatId
import kotlinx.coroutines.flow.StateFlow
import ui.dialog.customization.CustomizationViewModel
import ui.lifecounter.playerbutton.PlayerButtonAction

interface LifeCounterScreenController {
    val state: StateFlow<LifeCounterState>
    val numPlayers: StateFlow<Int>
    val alt4PlayerLayout: StateFlow<Boolean>
    val darkTheme: StateFlow<Boolean>
    val turnTimerEnabled: StateFlow<Boolean>

    fun onNavigate(firstNavigation: Boolean)
    fun openModal(value: LifeCounterModal)
    fun closeModal()
    fun goBackInModal()
    fun toggleDarkTheme(value: Boolean? = null)
    fun toggleKeepScreenOn(value: Boolean? = null)
    fun setShowButtons(value: Boolean)
    fun setAlt4PlayerLayout(value: Boolean)
    fun setNumPlayers(value: Int)
    fun onCommanderDealerButtonClicked()
    fun onPlayerButtonAction(seatId: SeatId, action: PlayerButtonAction)
    fun customizationViewModelFor(seatId: SeatId): CustomizationViewModel?
    fun savePlayerPrefs()
    fun resetAllPrefs()
    fun resetGameState(startingLife: Int? = null)
    fun incrementCounter(index: Int, value: Int)
    fun resetCounters()
    fun toggleDayNight()
    fun setDayNight(value: DayNightState)
}
