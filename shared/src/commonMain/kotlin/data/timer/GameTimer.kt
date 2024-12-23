package data.timer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameTimer(
    private val scope: CoroutineScope,
    private val numPlayersFlow: StateFlow<Int>,
    private val isDead: (Int) -> Boolean
) {
    private val _timerState = MutableStateFlow(GameTimerState())
    val timerState: StateFlow<GameTimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null
    private var updateTimerCallbacks: List<(Int, TurnTimer?) -> Unit>? = null

    private fun initTimer() {
        if (_timerState.value.turnTimer == null) {
            _timerState.value = _timerState.value.copy(turnTimer = TurnTimer(-1, 1))
        }
        killTimer()
        timerJob = scope.launch {
            while (true) {
                if (_timerState.value.activePlayerIndex != null && _timerState.value.turnTimer != null) {
                    val newTimer = _timerState.value.turnTimer!!.tick()
                    _timerState.value = _timerState.value.copy(turnTimer = newTimer)
                    updateTimerCallbacks?.forEach { 
                        _timerState.value.activePlayerIndex?.let { index -> it(index, newTimer) }
                    }
                }
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
        _timerState.value = _timerState.value.copy(firstPlayer = index)
    }

    fun setTimerEnabled(enabled: Boolean) {
        if (enabled) {
            if (_timerState.value.activePlayerIndex == null) {
                setActiveTimerIndex(_timerState.value.firstPlayer)
            }
            initTimer()
        } else {
            killTimer()
        }
    }

    fun moveTimer() {
        if (_timerState.value.activePlayerIndex == null || _timerState.value.firstPlayer == null) {
            throw IllegalStateException("Attempted to move timer when no one has an active timer")
        }

        val nextPlayerIndex = getNextPlayerIndex(_timerState.value.activePlayerIndex!!)
        val firstActivePlayerIndex = getNextPlayerIndex(
            (_timerState.value.firstPlayer!! - 1 + numPlayersFlow.value) % numPlayersFlow.value
        )

        if (nextPlayerIndex == firstActivePlayerIndex) {
            incrementTurn()
        }

        setActiveTimerIndex(nextPlayerIndex)
        val newTimer = resetTime()
        _timerState.value = _timerState.value.copy(turnTimer = newTimer)
        updateTimerCallbacks?.forEach { it(nextPlayerIndex, newTimer) }
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
    }

    private fun incrementTurn(value: Int = 1) {
        if (_timerState.value.activePlayerIndex == null) {
            throw IllegalStateException("Attempted to increment turn timer when no one has an active timer")
        }
        _timerState.value = _timerState.value.copy(
            turnTimer = _timerState.value.turnTimer?.copy(
                turn = _timerState.value.turnTimer!!.turn + value
            )
        )
    }

    private fun resetTime(): TurnTimer? {
        return _timerState.value.turnTimer?.resetTime()
    }
}

data class GameTimerState(
    val firstPlayer: Int? = null,
    val activePlayerIndex: Int? = null,
    val turnTimer: TurnTimer? = null
)