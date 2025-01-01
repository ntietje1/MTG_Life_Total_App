package domain.game

import data.ISettingsManager
import data.Player
import domain.base.AttachableManager
import domain.timer.GameTimer
import domain.timer.GameTimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ui.lifecounter.DayNightState
import ui.lifecounter.playerbutton.PlayerButtonViewModel
import kotlin.coroutines.coroutineContext

/**
 * Manages a game state that is shared among all players
 */
class GameStateManager(
    private val settingsManager: ISettingsManager
) : AttachableManager() {
    fun toggleDayNight(currentState: DayNightState): DayNightState {
        return when (currentState) {
            DayNightState.NONE -> DayNightState.DAY
            DayNightState.DAY -> DayNightState.NIGHT
            DayNightState.NIGHT -> DayNightState.DAY
        }
    }

    fun setMonarchy(targetPlayerNum: Int, value: Boolean) {
        checkAttached()
        playerViewModelsFlow!!.value.forEach { playerButtonViewModel ->
            playerButtonViewModel.setPlayer(
                updateMonarchy(
                    player = playerButtonViewModel.state.value.player,
                    targetPlayerNum = targetPlayerNum,
                    value = value
                )
            )
        }
        saveGameState()
    }

    private fun updateMonarchy(player: Player, targetPlayerNum: Int, value: Boolean): Player {
        return player.copy(monarch = value && player.playerNum == targetPlayerNum)
    }

    fun savePlayerState(player: Player) {
        saveGameState() // eventually will be replaced with a more efficient method
    }

    fun saveGameState() {
        checkAttached()
        settingsManager.savePlayerStates(playerViewModelsFlow!!.value.map { it.state.value.player })
    }
} 