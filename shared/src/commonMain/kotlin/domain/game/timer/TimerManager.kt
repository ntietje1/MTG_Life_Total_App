package domain.game.timer

import domain.game.AttachableFlowManager
import domain.storage.ISettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonViewModel
import kotlin.coroutines.coroutineContext

/**
 * Contains all LifeCounterViewModel timer related logic
 */
class TimerManager(
    private val settingsManager: ISettingsManager
) : AttachableFlowManager<List<PlayerButtonViewModel>>() {

    private var observerJob: Job? = null
    private val observerRegistered: Boolean
        get() = observerJob != null

    private val gameTimer: GameTimer = GameTimer(settingsManager.savedTimerState.value ?: GameTimerState())
    private var timerJob: Job? = null

    override fun attach(source: StateFlow<List<PlayerButtonViewModel>>): TimerManager {
        super.attach(source)
        initializeGameTimer()
        return this
    }

    override fun detach() {
        super.detach()
        stopTimerLoop()
        stopObserver()
    }

    fun moveTimer() {
        requireAttached()
        gameTimer.moveTimer()
        updateViewModelsWithTimer()
        saveTimerState()
    }

    fun handleFirstPlayerSelection(index: Int?) {
        requireAttached()
        clearFirstPlayerSelectionState()
        gameTimer.setFirstPlayer(index)
        gameTimer.setTimerEnabled(true)
        updateViewModelsWithTimer()
        saveTimerState()
    }

    private fun clearFirstPlayerSelectionState() {
        val playerButtonViewModels = requireAttached().value
        playerButtonViewModels.forEach { viewModel ->
            if (viewModel.state.value.buttonState == PBState.SELECT_FIRST_PLAYER) {
                viewModel.popBackStack()
            }
        }
    }

    suspend fun onTimerEnabledChange(timerEnabled: Boolean) {
        gameTimer.setTimerEnabled(timerEnabled)
        if (!observerRegistered) {
            registerTimerStateObserver()
        }
        if (timerEnabled) {
            if (gameTimer.timerState.value.firstPlayer == null) {
                promptForFirstPlayer()
            }
            startTimerLoop()
        } else {
            stopTimerLoop()
            reset()
        }
        updateViewModelsWithTimer()
    }

    fun reset() {
        clearFirstPlayerSelectionState()
        gameTimer.reset()
        initializeGameTimer()
        if (settingsManager.turnTimer.value) {
            promptForFirstPlayer()
        }
    }

    private fun promptForFirstPlayer() {
        if (gameTimer.timerState.value.firstPlayer != null) {
            println("WARNING: First player already selected")
            return
        }
        val playerButtonViewModels = requireAttached().value
        playerButtonViewModels.forEach { it.onFirstPlayerPrompt() }
        if (settingsManager.numPlayers.value == 1) {
            handleFirstPlayerSelection(0)
        }
        initializeGameTimer()
    }

    private fun initializeGameTimer() {
        val playerButtonViewModels = requireAttached().value
        gameTimer.initialize(
            playerCount = settingsManager.numPlayers.value,
            deadCheck = { index -> playerButtonViewModels[index].isDead.value }
        )
    }

    private fun updateViewModelsWithTimer() {
        val playerButtonViewModels = requireAttached().value
        val timerState = gameTimer.timerState.value
        playerButtonViewModels.forEachIndexed { index, viewModel ->
            val shouldShowTimer = (index == timerState.activePlayerIndex)
            viewModel.setTimer(if (shouldShowTimer) timerState.turnTimer else null)
        }
    }

    private suspend fun startTimerLoop() {
        stopTimerLoop()
        timerJob = CoroutineScope(coroutineContext).launch {
            while (true) {
                gameTimer.tick()
                updateViewModelsWithTimer()
                delay(1000L)
                saveTimerState()
            }
        }
    }

    private suspend fun registerTimerStateObserver() {
        // Observe timer enabled changes
        requireAttached()
        observerJob = CoroutineScope(coroutineContext).launch {
            settingsManager.turnTimer.collect { turnTimerEnabled ->
                onTimerEnabledChange(turnTimerEnabled)
            }
        }
    }

    private fun stopObserver() {
        observerJob?.cancel()
        observerJob = null
    }

    private fun stopTimerLoop() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun saveTimerState() {
        settingsManager.setSavedTimerState(gameTimer.timerState.value)
    }
} 