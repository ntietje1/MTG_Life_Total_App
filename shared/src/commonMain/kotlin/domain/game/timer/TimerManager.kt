package domain.game.timer

import domain.game.AttachableManager
import domain.storage.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

interface TimerManagerHost {
    val playerCount: Int

    fun isPlayerDead(index: Int): Boolean
    fun promptForFirstPlayer()
    fun clearFirstPlayerPrompt()
    fun showTimer(activePlayerIndex: Int?, timer: TurnTimer?)
}

/**
 * Contains all LifeCounterViewModel timer related logic
 */
class TimerManager(
    private val timerStateRepository: TimerStateRepository,
    private val preferencesRepository: PreferencesRepository
) : AttachableManager<TimerManagerHost>() {

    private var observerJob: Job? = null
    private val gameTimer: GameTimer = GameTimer(timerStateRepository.load() ?: GameTimerState())
    private var timerJob: Job? = null

    override fun detach() {
        super.detach()
        stopTimerLoop()
        stopObserver()
    }

    fun moveTimer() {
        requireAttached()
        gameTimer.moveTimer()
        updateHostWithTimer()
        saveTimerState()
    }

    fun handleFirstPlayerSelection(index: Int?) {
        requireAttached()
        clearFirstPlayerSelectionState()
        gameTimer.setFirstPlayer(index)
        gameTimer.setTimerEnabled(true)
        updateHostWithTimer()
        saveTimerState()
    }

    private fun clearFirstPlayerSelectionState() {
        requireAttached().clearFirstPlayerPrompt()
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
        updateHostWithTimer()
    }

    fun reset() {
        clearFirstPlayerSelectionState()
        gameTimer.reset()
        initializeGameTimer()
        if (preferencesRepository.turnTimer.value) {
            promptForFirstPlayer()
        }
    }

    private fun promptForFirstPlayer() {
        if (gameTimer.timerState.value.firstPlayer != null) {
            println("WARNING: First player already selected")
            return
        }
        val host = requireAttached()
        host.promptForFirstPlayer()
        if (preferencesRepository.numPlayers.value == 1) {
            handleFirstPlayerSelection(0)
        }
        initializeGameTimer()
    }

    private fun initializeGameTimer() {
        val host = requireAttached()
        gameTimer.initialize(
            playerCount = host.playerCount,
            deadCheck = host::isPlayerDead
        )
    }

    private fun updateHostWithTimer() {
        val host = requireAttached()
        val timerState = gameTimer.timerState.value
        host.showTimer(
            activePlayerIndex = timerState.activePlayerIndex,
            timer = timerState.turnTimer
        )
    }

    private suspend fun startTimerLoop() {
        stopTimerLoop()
        timerJob = CoroutineScope(coroutineContext).launch {
            while (true) {
                gameTimer.tick()
                updateHostWithTimer()
                delay(1000L)
                saveTimerState()
            }
        }
    }

    suspend fun registerTimerStateObserver() {
        // Observe timer enabled changes
        requireAttached()
        initializeGameTimer()
        observerJob = CoroutineScope(coroutineContext).launch {
            preferencesRepository.turnTimer.collect { turnTimerEnabled ->
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
        timerStateRepository.save(gameTimer.timerState.value)
    }
}
