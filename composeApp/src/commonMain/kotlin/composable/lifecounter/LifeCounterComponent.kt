package composable.lifecounter

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import data.Player
import data.SettingsManager
import data.SettingsManager.numPlayers
import data.SettingsManager.savePlayerStates
import data.SettingsManager.startingLife
import data.serializable.Card
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import theme.allPlayerColors

/**
 * The component for the life counter screen for state and navigation
 */
class LifeCounterComponent(
    componentContext: ComponentContext,
    val goToPlayerSelectScreen: () -> Unit,
    val returnToLifeCounterScreen: () -> Unit,
    val setNumPlayers: (Int) -> Unit
) : ComponentContext by componentContext {
    val showButtons = mutableStateOf(false)
    private val allPlayers: MutableList<Player> = SettingsManager.loadPlayerStates().toMutableList()
    val activePlayers: List<Player> get() = allPlayers.subList(0, numPlayers)
    private val buttonStates = mutableListOf<MutableState<PlayerButtonState>>()
    var currentDealer: Player? = null
    var blurBackground = mutableStateOf(false)
    var dayNight by mutableStateOf(DayNightState.NONE)
    val coinFlipHistory = mutableStateListOf<String>()
    val counters = List(7) { mutableIntStateOf(0) }

    enum class DayNightState {
        NONE, DAY, NIGHT
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun delayedSave() {
        delay(1000 * 10)
        savePlayerStates()
        scope.launch {
            delayedSave()
        }
    }

    init {
        lifecycle.subscribe(
            object : Lifecycle.Callbacks {
                override fun onResume() {
                    super.onResume()
                    scope.launch {
                        delayedSave()
                    }
                }

                override fun onPause() {
                    super.onPause()
                    savePlayerStates()
                    scope.cancel()
                }
            }
        )
    }


    var planarDeck = mutableStateListOf<Card>().apply {
        addAll(SettingsManager.loadPlanarDeck())
    }
    val planarBackStack = mutableStateListOf<Card>()

    init {
        generatePlayers()
    }

    /**
     * Generates players if they aren't already generated
     */
    private fun generatePlayers() {
        while (allPlayers.size < Player.MAX_PLAYERS) {
            allPlayers.add(generatePlayer(startingLife))
        }
    }

    /**
     * Resets the player prefs for a given player
     * @param player The player to reset
     */
    fun resetPlayerPrefs(player: Player) {
        player.apply {
            name = "P${playerNum}"
            imageUri = null
            color = getRandColor()
            textColor = Color.White
        }
    }

    /**
     * Resets all player prefs
     */
    fun savePlayerStates() {
        savePlayerStates(allPlayers)
    }

    /**
     * Resets all player prefs
     */
    fun resetPlayerStates() {
        showButtons.value = false
        updateAllStates(PlayerButtonState.NORMAL)
        allPlayers.forEach {
            it.resetState(startingLife)
        }
        savePlayerStates()
        returnToLifeCounterScreen()
    }

    /**
     * Generates a new player
     * @param startingLife The starting life total for the player
     * @return The new player
     */
    private fun generatePlayer(startingLife: Int): Player {
        val playerColor = getRandColor()
        val playerNum = allPlayers.size + 1
        val name = "P$playerNum"
        return Player(color = playerColor, life = startingLife, name = name, playerNum = playerNum)
    }

    /**
     * Generates a random color that isn't already used
     * @return The random color
     */
    private fun getRandColor(): Color {
        do {
            val color = allPlayerColors.random()
            if (allPlayers.none { it.color == color }) return color
        } while (true)
    }

    /**
     * Toggles the monarch state for a given player
     * @param player The player to toggle
     * @param value The value to set the monarch state to, if null it will toggle the current state
     */
    fun toggleMonarch(player: Player, value: Boolean? = null) {
        val targetValue = value ?: !player.monarch
        if (targetValue) {
            allPlayers.forEach { it.monarch = false }
        }
        player.monarch = targetValue
    }

    /**
     * Registers a button state to be updated when all button states change
     * @param state The state to register
     */
    fun registerButtonState(state: MutableState<PlayerButtonState>) {
        buttonStates.add(state)
    }

    /**
     * Updates all button states to a given state
     * @param state The state to update to
     */
    fun updateAllStates(state: PlayerButtonState) {
        buttonStates.forEach { it.value = state }
    }

    /**
     * Is the current dealer partnered?
     * @return Whether the current dealer is partnered
     */
    fun currentDealerIsPartnered(): Boolean {
        return currentDealer?.partnerMode ?: false
    }

    /**
     * Goes back a plane
     */
    fun backPlane() {
        if (planarBackStack.isNotEmpty()) {
            val card = planarBackStack.removeLast()
            planarDeck.remove(card)
            planarDeck.add(card)
        }
    }

    /**
     * Planeswalks to the next plane
     * @return The next plane
     */
    fun planeswalk(): Card? {
        if (planarDeck.isNotEmpty()) {
            val card = planarDeck.removeLast()
            planarBackStack.add(card)
            planarDeck = (listOf(card).plus(planarDeck)).toMutableStateList() // send to bottom
            return card
        }
        return null
    }

    /**
     * Gets the current plane
     * @return The current plane
     */
    fun currentPlane(): Card? {
        return planarDeck.lastOrNull()
    }

    /**
     * Toggles the day/night state
     */
    fun toggleDayNight() {
        dayNight = when (dayNight) {
            DayNightState.NONE -> DayNightState.DAY
            DayNightState.DAY -> DayNightState.NIGHT
            DayNightState.NIGHT -> DayNightState.DAY
        }
    }
}