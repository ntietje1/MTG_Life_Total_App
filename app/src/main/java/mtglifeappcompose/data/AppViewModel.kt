package mtglifeappcompose.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import mtglifeappcompose.composable.lifecounter.PlayerButtonState
import mtglifeappcompose.ui.theme.allPlayerColors

enum class DayNightState {
    NONE, DAY, NIGHT
}

class AppViewModel : ViewModel() {
    private var currentPlayers: MutableList<Player> = mutableListOf()
    private val numPlayers = mutableIntStateOf(SharedPreferencesManager.loadNumPlayers())
    private val buttonStates = mutableListOf<MutableState<PlayerButtonState>>()
    var currentDealer: Player? = null
    val alt4PlayerLayout = mutableStateOf(SharedPreferencesManager.load4PlayerLayout())
    var blurBackground = mutableStateOf(false)

    var dayNight by mutableStateOf(DayNightState.NONE)

    fun toggleDayNight() {
        dayNight = when (dayNight) {
            DayNightState.NONE -> DayNightState.DAY
            DayNightState.DAY -> DayNightState.NIGHT
            DayNightState.NIGHT -> DayNightState.DAY
        }
    }

    fun currentDealerIsPartnered(): Boolean {
        return currentDealer?.partnerMode ?: false
    }

    fun set4PlayerLayout(value: Boolean) {
        alt4PlayerLayout.value = value
        SharedPreferencesManager.save4PlayerLayout(alt4PlayerLayout.value)
    }

    fun generatePlayers() {
        val startingLife = SharedPreferencesManager.loadStartingLife()
        while (currentPlayers.size < Player.MAX_PLAYERS) {
            currentPlayers.add(generatePlayer(startingLife))
        }
    }

    fun resetPlayers() {
        val startingLife = SharedPreferencesManager.loadStartingLife()
        updateAllStates(PlayerButtonState.NORMAL)
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

    fun setPlayerNum(num: Int, allowOverride: Boolean = false) {
        if (allowOverride || (numPlayers.value == 0 && num in 1..Player.MAX_PLAYERS)) {
            numPlayers.value = num
            SharedPreferencesManager.saveNumPlayers(num)
        }
    }

    fun setStartingLife(playerDataManager: SharedPreferencesManager, life: Int) {
        playerDataManager.saveStartingLife(life)
        resetPlayers()
    }

    fun getActivePlayers(): List<Player> {
        return currentPlayers.subList(0, numPlayers.value)
    }
}