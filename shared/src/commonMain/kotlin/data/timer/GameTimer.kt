package data.timer

import data.ISettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class GameTimer(
    private val scope: CoroutineScope,
    private val numPlayersFlow: StateFlow<Int>,
    private val isDead: (Int) -> Boolean,
    private val settingsManager: ISettingsManager
) {
    private val _timerState = MutableStateFlow(settingsManager.savedTimerState.value ?: GameTimerState())
    val timerState = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private var updateTimerCallbacks: List<(Int, TurnTimer?) -> Unit>? = null

    private fun initTimer() {
        if (_timerState.value.turnTimer == null) {
            _timerState.value = _timerState.value.copy(turnTimer = TurnTimer(seconds = -1, turn = 1))
        }
        
        killTimer()
        startTimerLoop()
    }

    private fun startTimerLoop() {
        timerJob = scope.launch {
            while (true) {
                val currentState = _timerState.value
                val activePlayer = currentState.activePlayerIndex
                val currentTimer = currentState.turnTimer

                if (activePlayer != null && currentTimer != null) {
                    val newTimer = currentTimer.tick()
                    _timerState.value = currentState.copy(turnTimer = newTimer)
                    updateTimerCallbacks?.forEach { callback ->
                        callback(activePlayer, newTimer)
                    }
                }
                
                saveTimerState()
                delay(1000L)
            }
        }
    }

    private fun killTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun registerTimerCallbacks(callbacks: List<(Int, TurnTimer?) -> Unit>) {
        updateTimerCallbacks = callbacks
    }

    fun setFirstPlayer(index: Int?) {
        _timerState.value = _timerState.value.copy(
            firstPlayer = index
        )

        if (index != null && settingsManager.turnTimer.value) {
            setTimerEnabled(true)
        }
        saveTimerState()
    }

    fun setTimerEnabled(enabled: Boolean) {
        settingsManager.setTurnTimer(enabled)
        if (enabled) {
            if (_timerState.value.activePlayerIndex == null) {
                setActiveTimerIndex(_timerState.value.firstPlayer)
            }
            initTimer()
        } else {
            reset()
        }
        saveTimerState()
    }

    fun moveTimer() {
        val currentState = _timerState.value
        requireNotNull(currentState.activePlayerIndex) { "No active timer found" }
        requireNotNull(currentState.firstPlayer) { "First player not set" }
        requireNotNull(currentState.turnTimer) { "Turn timer not set" }

        val nextPlayer = getNextPlayerIndex(currentState.activePlayerIndex)
        val firstActivePlayer = getNextPlayerIndex(
            (currentState.firstPlayer - 1 + numPlayersFlow.value) % numPlayersFlow.value
        )

        val currentTurn = currentState.turnTimer.turn
        val newTurn = if (nextPlayer == firstActivePlayer) currentTurn + 1 else currentTurn

        _timerState.value = currentState.copy(
            activePlayerIndex = nextPlayer,
            turnTimer = currentState.turnTimer.copy(seconds = 0, turn = newTurn)
        )
        
        updateTimerCallbacks?.forEach { it(nextPlayer, _timerState.value.turnTimer) }
        saveTimerState()
    }

    private fun getNextPlayerIndex(currentIndex: Int): Int {
        var nextPlayerIndex = currentIndex
        do {
            nextPlayerIndex = (nextPlayerIndex + 1) % numPlayersFlow.value
        } while (isDead(nextPlayerIndex) && !allPlayersDead())
        return nextPlayerIndex
    }

    private fun allPlayersDead(): Boolean {
        return (0 until numPlayersFlow.value).all { isDead(it) }
    }

    private fun setActiveTimerIndex(index: Int?) {
        _timerState.value = _timerState.value.copy(activePlayerIndex = index)
    }

    fun reset() {
        _timerState.value = GameTimerState()
        killTimer()
        updateTimerCallbacks?.forEach { it(-1, null) }
        saveTimerState()
    }

    private fun saveTimerState() {
        settingsManager.setSavedTimerState(_timerState.value)
    }

    fun promptFirstPlayer() {
        if (numPlayersFlow.value == 1) {
            setFirstPlayer(0)
        }
        setTimerEnabled(true)
    }
}

@Serializable
data class GameTimerState(
    val firstPlayer: Int? = null,
    val activePlayerIndex: Int? = null,
    val turnTimer: TurnTimer? = null
)