package ui.tutorial.pages

import data.Player
import data.TurnTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ui.dialog.customization.CustomizationViewModel
import ui.lifecounter.CounterType
import ui.lifecounter.DayNightState
import ui.lifecounter.ILifeCounterViewModel
import ui.lifecounter.LifeCounterState
import ui.lifecounter.playerbutton.AbstractPlayerButtonViewModel
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonState

abstract class MockPlayerButtonViewModel(state: PlayerButtonState) : AbstractPlayerButtonViewModel(state) {
    override val customizationViewmodel: CustomizationViewModel? = null

    override fun setTimer(timer: TurnTimer?) {}
    override fun setPlayerButtonState(buttonState: PBState) {}
    override fun onMoveTimer() {}
    override fun onMonarchyButtonClicked(value: Boolean) {}
    override fun onCommanderButtonClicked() {}
    override fun onSettingsButtonClicked() {}
    override fun onKOButtonClicked() {}
    override fun popBackStack() {}
    override fun pushBackStack(back: () -> Unit) {}
    override fun resetPlayerPref() {}
    override fun savePlayerPref() {}
    override fun isDead(): Boolean { return false }
    override fun getCounterValue(counterType: CounterType): Int { return 0 }
    override fun showCustomizeMenu(value: Boolean) {}
    override fun toggleMonarch(value: Boolean) {}
    override fun togglePartnerMode(value: Boolean) {}
    override fun incrementCounterValue(counterType: CounterType, value: Int) {}
    override fun setActiveCounter(counterType: CounterType, active: Boolean): Boolean { return false }
    override fun getCommanderDamage(partner: Boolean): Int { return 0 }
    override fun incrementCommanderDamage(value: Int, partner: Boolean) {}
    override fun copyPrefs(other: Player) {}
    override fun resetState(startingLife: Int) {}
}

abstract class MockLifeCounterViewModel(
    lifeCounterState: LifeCounterState = LifeCounterState(showButtons = true, showLoadingScreen = false),
    override val playerButtonViewModels: List<AbstractPlayerButtonViewModel>
) : ILifeCounterViewModel {
    override val state: StateFlow<LifeCounterState> = MutableStateFlow(lifeCounterState)

    override fun promptFirstPlayer() {}
    override fun setFirstPlayer(index: Int?) {}
    override fun setTimerEnabled(value: Boolean) {}
    override fun killTimer() {}
    override fun onNavigate(firstNavigation: Boolean) {}
    override fun onCommanderDealerButtonClicked(playerButtonViewModel: AbstractPlayerButtonViewModel) {}
    override fun savePlayerPrefs() {}
    override fun savePlayerStates() {}
    override fun resetAllPrefs() {}
    override fun setFirstPlayerSelectionActive(value: Boolean) {}
    override fun openMiddleButtonDialog(value: Boolean) {}
    override fun setNumPlayers(value: Int) {}
    override fun resetGameState() {}
    override fun setShowButtons(value: Boolean) {}
    override fun setBlurBackground(value: Boolean) {}
    override fun setDayNight(value: DayNightState) {}
    override fun incrementCounter(index: Int, value: Int) {}
    override fun resetCounters() {}
    override fun toggleDayNight() {}
}