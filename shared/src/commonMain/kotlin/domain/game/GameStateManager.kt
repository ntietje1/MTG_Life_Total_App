package domain.game

import data.ISettingsManager
import data.Player
import domain.timer.GameTimer
import domain.timer.GameTimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.lifecounter.DayNightState

/**
 * Manages a game state that is shared among all players
 */
class GameStateManager(
    private val scope: CoroutineScope,
    private val settingsManager: ISettingsManager
) {
    private val gameTimer: GameTimer = GameTimer(settingsManager.savedTimerState.value ?: GameTimerState())
    private var timerJob: Job? = null

    val timerState
        get() = gameTimer.timerState

    fun toggleDayNight(currentState: DayNightState): DayNightState {
        return when (currentState) {
            DayNightState.NONE -> DayNightState.DAY
            DayNightState.DAY -> DayNightState.NIGHT
            DayNightState.NIGHT -> DayNightState.DAY
        }
    }

    fun setMonarch(players: List<Player>, targetPlayerNum: Int, value: Boolean): List<Player> {
        return players.map { player ->
            player.copy(monarch = value && player.playerNum == targetPlayerNum)
        }
    }

    fun initializeTimer(playerCount: Int, deadCheck: (Int) -> Boolean) {
        gameTimer.initialize(playerCount, deadCheck)
    }

    fun setTimerEnabled(enabled: Boolean) {
        gameTimer.setTimerEnabled(enabled)
        if (enabled) {
            startTimerLoop()
        } else {
            stopTimerLoop()
        }
        saveTimerState()
    }

    private fun startTimerLoop() {
        stopTimerLoop()
        timerJob = scope.launch {
            while (true) {
                gameTimer.tick()
                delay(1000L)
                saveTimerState()
            }
        }
    }

    private fun stopTimerLoop() {
        timerJob?.cancel()
        timerJob = null
    }

    fun setFirstPlayer(index: Int?) {
        gameTimer.setFirstPlayer(index)
        saveTimerState()
    }

    fun moveTimer() {
        gameTimer.moveTimer()
        saveTimerState()
    }

    fun resetTimer() {
        stopTimerLoop()
        gameTimer.reset()
        saveTimerState()
    }

    private fun saveTimerState() {
        settingsManager.setSavedTimerState(gameTimer.timerState.value)
    }
} 