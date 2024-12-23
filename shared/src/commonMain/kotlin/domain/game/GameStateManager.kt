package domain.game

import data.Player
import ui.lifecounter.DayNightState

class GameStateManager {
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
} 