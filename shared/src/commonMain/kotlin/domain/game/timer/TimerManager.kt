package domain.game.timer

import domain.storage.ISettingsManager
import domain.game.AttachableManager
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
) : AttachableManager<List<PlayerButtonViewModel>>() {

    private var observerJob: Job? = null
    private val observerAttached: Boolean
        get() = observerJob != null

    private val gameTimer: GameTimer = GameTimer(settingsManager.savedTimerState.value ?: GameTimerState())
    private var timerJob: Job? = null

    override fun attach(flow: StateFlow<List<PlayerButtonViewModel>>) {
        super.attach(flow)
        initializeGameTimer()
    }

    override fun checkAttached() {
        super.checkAttached()
        if (!observerAttached) {
            throw IllegalStateException("TimerManager observer must be attached before use")
        }
    }

    override fun detach() {
        super.detach()
        stopTimerLoop()
        stopObserver()
    }

    fun setupTimerStateObserver(scope: CoroutineScope) {
        // Observe timer enabled changes
        super.checkAttached()
        observerJob = scope.launch {
            settingsManager.turnTimer.collect { turnTimerEnabled ->
                onTimerEnabledChange(turnTimerEnabled)
            }
        }
    }

    fun moveTimer() {
        checkAttached()
        gameTimer.moveTimer()
        updateViewModelsWithTimer()
        saveTimerState()
    }

    fun handleFirstPlayerSelection(index: Int?) {
        checkAttached()
        clearFirstPlayerSelectionState()
        gameTimer.setFirstPlayer(index)
        gameTimer.setTimerEnabled(true)
        updateViewModelsWithTimer()
        saveTimerState()
    }

    private fun clearFirstPlayerSelectionState() {
        attachedFlow!!.value.forEach { viewModel ->
            if (viewModel.state.value.buttonState == PBState.SELECT_FIRST_PLAYER) {
                viewModel.popBackStack()
            }
        }
    }

    suspend fun onTimerEnabledChange(timerEnabled: Boolean) {
        gameTimer.setTimerEnabled(timerEnabled)
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
        checkAttached()
        if (gameTimer.timerState.value.firstPlayer != null) {
            println("WARNING: First player already selected")
            return
        }
        attachedFlow!!.value.forEach { it.onFirstPlayerPrompt() }
        if (settingsManager.numPlayers.value == 1) {
            handleFirstPlayerSelection(0)
        }
        initializeGameTimer()
    }

    private fun initializeGameTimer() {
        gameTimer.initialize(
            playerCount = settingsManager.numPlayers.value,
            deadCheck = { index -> attachedFlow!!.value[index].isDead.value }
        )
    }

    private fun updateViewModelsWithTimer() {
        val timerState = gameTimer.timerState.value
        attachedFlow!!.value.forEachIndexed { index, viewModel ->
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