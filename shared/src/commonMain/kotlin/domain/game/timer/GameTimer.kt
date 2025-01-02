package domain.game.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * A Timer that interacts with game state such as dead players and the active player
 */
class GameTimer(
    initialState: GameTimerState = GameTimerState()
) {
    private val _timerState = MutableStateFlow(initialState)
    val timerState = _timerState.asStateFlow()

    private var numPlayers: Int = 0
    private var isPlayerDead: ((Int) -> Boolean)? = null

    fun initialize(playerCount: Int, deadCheck: (Int) -> Boolean) {
        numPlayers = playerCount
        isPlayerDead = deadCheck
    }

    fun tick() {
        val currentState = _timerState.value
        if (currentState.activePlayerIndex != null && currentState.turnTimer != null) {
            _timerState.value = currentState.copy(
                turnTimer = currentState.turnTimer.tick()
            )
        }
    }

    fun setFirstPlayer(index: Int?) {
        _timerState.value = _timerState.value.copy(firstPlayer = index)
    }

    fun setTimerEnabled(enabled: Boolean) {
        if (enabled) {
            initTimer()
        } else {
            reset()
        }
    }

    private fun initTimer() {
        if (_timerState.value.activePlayerIndex == null) {
            setActiveTimerIndex(_timerState.value.firstPlayer)
        }
        if (_timerState.value.turnTimer == null) {
            _timerState.value = _timerState.value.copy(
                turnTimer = TurnTimer(seconds = 0, turn = 1)
            )
        }
    }

    fun moveTimer() {
        val currentState = _timerState.value
        requireNotNull(currentState.activePlayerIndex) { "No active timer found" }
        requireNotNull(currentState.firstPlayer) { "First player not set" }
        requireNotNull(currentState.turnTimer) { "Turn timer not set" }

        val nextPlayer = getNextPlayerIndex(currentState.activePlayerIndex)
        val firstActivePlayer = getNextPlayerIndex(
            (currentState.firstPlayer - 1 + numPlayers) % numPlayers
        )

        val currentTurn = currentState.turnTimer.turn
        val newTurn = if (nextPlayer == firstActivePlayer) currentTurn + 1 else currentTurn

        _timerState.value = currentState.copy(
            activePlayerIndex = nextPlayer,
            turnTimer = currentState.turnTimer.copy(seconds = 0, turn = newTurn)
        )
    }

    private fun getNextPlayerIndex(currentIndex: Int): Int {
        var nextPlayerIndex = currentIndex
        do {
            nextPlayerIndex = (nextPlayerIndex + 1) % numPlayers
        } while (isPlayerDead?.invoke(nextPlayerIndex) == true && !allPlayersDead())
        return nextPlayerIndex
    }

    private fun allPlayersDead(): Boolean {
        return (0 until numPlayers).all { isPlayerDead?.invoke(it) == true }
    }

    private fun setActiveTimerIndex(index: Int?) {
        _timerState.value = _timerState.value.copy(activePlayerIndex = index)
    }

    fun reset() {
        _timerState.value = GameTimerState()
    }
}

@Serializable
data class GameTimerState(
    val firstPlayer: Int? = null,
    val activePlayerIndex: Int? = null,
    val turnTimer: TurnTimer? = null
)