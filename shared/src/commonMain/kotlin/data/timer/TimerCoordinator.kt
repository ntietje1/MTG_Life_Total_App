import data.timer.GameTimer
import data.timer.TurnTimer
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonViewModel

class TimerCoordinator(
    private val gameTimer: GameTimer,
    private val playerViewModels: List<PlayerButtonViewModel>,
) {
    init {
        registerTimerCallbacks()
    }

    private fun registerTimerCallbacks() {
        val callbacks = playerViewModels.mapIndexed { index, viewModel ->
            { targetIndex: Int, timer: TurnTimer? ->
                val shouldShowTimer = (index == targetIndex)
                viewModel.setTimer(if (shouldShowTimer) timer else null)
            }
        }
        gameTimer.registerTimerCallbacks(callbacks)
    }

    fun handleFirstPlayerSelection(index: Int?) {
        clearFirstPlayerSelectionState()
        gameTimer.setFirstPlayer(index)
    }

    private fun clearFirstPlayerSelectionState() {
        playerViewModels.forEach { viewModel ->
            if (viewModel.state.value.buttonState == PBState.SELECT_FIRST_PLAYER) {
                viewModel.popBackStack()
            }
        }
    }

    fun promptForFirstPlayer() {
        playerViewModels.forEach { it.onFirstPlayerPrompt() }
        gameTimer.promptFirstPlayer()
    }

    fun onTimerEnabledChange(timerEnabled: Boolean) {
        gameTimer.setTimerEnabled(timerEnabled)
        
        if (timerEnabled && gameTimer.timerState.value.firstPlayer == null) {
            promptForFirstPlayer()
            return
        }

        if (!timerEnabled) {
            reset()
        }
    }

    fun reset() {
        clearFirstPlayerSelectionState()
        gameTimer.reset()
    }
} 