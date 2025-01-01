package domain.timer

import domain.game.GameStateManager
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Contains all LifeCounterViewModel timer related logic
 */
class TimerCoordinator(
    private val gameStateManager: GameStateManager,
    private val playerViewModels: List<PlayerButtonViewModel>,
    private val numPlayersFlow: StateFlow<Int>
) {
    init {
        initializeGameTimer()
    }

    private fun initializeGameTimer() {
        gameStateManager.initializeTimer(
            playerCount = numPlayersFlow.value,
            deadCheck = { index -> playerViewModels[index].isDead.value }
        )
    }

    suspend fun setupTimerStateObserver() {
        // Observe timer state changes and update ViewModels accordingly
        gameStateManager.timerState.collect { timerState ->
            updateViewModelsWithTimer(timerState)
        }
    }

    private fun updateViewModelsWithTimer(timerState: GameTimerState) {
        playerViewModels.forEachIndexed { index, viewModel ->
            val shouldShowTimer = (index == timerState.activePlayerIndex)
            viewModel.setTimer(if (shouldShowTimer) timerState.turnTimer else null)
        }
    }

    suspend fun handleFirstPlayerSelection(index: Int?) {
        clearFirstPlayerSelectionState()
        gameStateManager.setFirstPlayer(index)
        gameStateManager.setTimerEnabled(true)
    }

    private fun clearFirstPlayerSelectionState() {
        playerViewModels.forEach { viewModel ->
            if (viewModel.state.value.buttonState == PBState.SELECT_FIRST_PLAYER) {
                viewModel.popBackStack()
            }
        }
    }

    suspend fun promptForFirstPlayer() {
        playerViewModels.forEach { it.onFirstPlayerPrompt() }
        if (numPlayersFlow.value == 1) {
            handleFirstPlayerSelection(0)
        }
        initializeGameTimer()
    }

    suspend fun onTimerEnabledChange(timerEnabled: Boolean) {
        gameStateManager.setTimerEnabled(timerEnabled)
        
        if (timerEnabled && gameStateManager.timerState.value.firstPlayer == null) {
            promptForFirstPlayer()
            return
        }

        if (!timerEnabled) {
            reset()
        }
    }

    fun reset() {
        clearFirstPlayerSelectionState()
        gameStateManager.resetTimer()
        initializeGameTimer()
    }
} 