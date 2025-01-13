package domain.game

import domain.storage.ISettingsManager
import model.Player
import ui.lifecounter.DayNightState
import ui.lifecounter.playerbutton.PlayerButtonViewModel

/**
 * Manages a game state that is shared among all players
 */
class GameStateManager(
    private val settingsManager: ISettingsManager
) : AttachableFlowManager<List<PlayerButtonViewModel>>() {
    fun toggleDayNight(currentState: DayNightState): DayNightState {
        return when (currentState) {
            DayNightState.NONE -> DayNightState.DAY
            DayNightState.DAY -> DayNightState.NIGHT
            DayNightState.NIGHT -> DayNightState.DAY
        }
    }

    fun setMonarchy(targetPlayerNum: Int, value: Boolean) {
        val playerButtonViewModels = requireAttached().value
        playerButtonViewModels.forEach { playerButtonViewModel ->
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
        val playerButtonViewModels = requireAttached().value
        settingsManager.savePlayerStates(playerButtonViewModels.map { it.state.value.player })
    }
} 