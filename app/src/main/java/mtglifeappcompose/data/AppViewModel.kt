package mtglifeappcompose.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import mtglifeappcompose.composable.lifecounter.PlayerButtonState
import mtglifeappcompose.ui.theme.allPlayerColors

class AppViewModel : ViewModel() {
    private var currentPlayers: MutableList<Player> = mutableListOf()
    private val numPlayers = mutableIntStateOf(0)
    private val buttonStates = mutableListOf<MutableState<PlayerButtonState>>()
    var currentDealer: Player? = null

    fun generatePlayers() {
        val startingLife = PlayerDataManager.loadStartingLife()
        while (currentPlayers.size < Player.MAX_PLAYERS) {
            currentPlayers.add(generatePlayer(startingLife))
        }
    }

    fun resetPlayers() {
        val startingLife = PlayerDataManager.loadStartingLife()
        currentPlayers.forEach {
            it.resetPlayer(startingLife)
        }
    }

    private fun generatePlayer(startingLife: Int): Player {
        val playerColor = getRandColor()
        val playerNum = currentPlayers.size + 1
        val name = "P$playerNum"
        return Player(color = playerColor, life = startingLife, name = name, playerNum = playerNum)
    }

    private fun getRandColor(): Color {
        do {
            val color = allPlayerColors.random()
            if (currentPlayers.none { it.color == color }) return color
        } while (true)
    }

    fun toggleMonarch(player: Player) {
        if (!player.monarch) {
            currentPlayers.forEach { it.monarch = false }
        }
        player.monarch = !player.monarch
    }

    fun registerButtonState(state: MutableState<PlayerButtonState>) {
        buttonStates.add(state)
    }

    fun updateAllStates(state: PlayerButtonState) {
        buttonStates.forEach { it.value = state }
    }

    fun setPlayerNum(num: Int, allowOverride: Boolean = true) {
        if (!allowOverride && numPlayers.value != 0) return
        numPlayers.value = num
    }

    fun setStartingLife(playerDataManager: PlayerDataManager, life: Int) {
        playerDataManager.saveStartingLife(life)
        resetPlayers()
    }

    fun getActivePlayers(): List<Player> {
        return currentPlayers.subList(0, numPlayers.value)
    }
}