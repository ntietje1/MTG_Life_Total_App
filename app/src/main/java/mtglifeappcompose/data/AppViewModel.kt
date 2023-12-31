package mtglifeappcompose.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
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
    var firstPlayerSelect by mutableStateOf(true)
    var blurBackground = mutableStateOf(false)
    var planarDeck = mutableStateListOf<Card>()
    val planeBackStack = mutableListOf<Card>()

    val alt4PlayerLayout = mutableStateOf(SharedPreferencesManager.load4PlayerLayout())
    var fastCoinFlip by mutableStateOf(SharedPreferencesManager.loadFastCoinFlip())
        private set
    var cameraRollDisabled by mutableStateOf(SharedPreferencesManager.loadCameraRollDisabled())
        private set
    var rotatingMiddleButton by mutableStateOf(SharedPreferencesManager.loadRotatingMiddleButton())
        private set
    var autoKo by mutableStateOf(SharedPreferencesManager.loadAutoKo())
        private set
    var keepScreenOn by mutableStateOf(SharedPreferencesManager.loadKeepScreenOn())
        private set
    var autoSkip by mutableStateOf(SharedPreferencesManager.loadAutoSkip())
        private set
    var disableBackButton by mutableStateOf(SharedPreferencesManager.loadDisableBackButton())
        private set

    var dayNight by mutableStateOf(DayNightState.NONE)

    fun backPlane() {
        if (planeBackStack.isNotEmpty()) {
            val card = planeBackStack.removeLast()
            planarDeck.remove(card)
            planarDeck.add(card)
        }
    }

    fun planeswalk(): Card? {
        if (planarDeck.isNotEmpty()) {
            val card = planarDeck.removeLast()
            planeBackStack.add(card)
            planarDeck = (listOf(card).plus(planarDeck)).toMutableStateList() // send to bottom
            return card
        }
        return null
    }

    fun currentPlane(): Card? {
        return planarDeck.lastOrNull()
    }

    fun toggleDisableBackButton(value: Boolean?) {
        disableBackButton = value ?: !disableBackButton
        SharedPreferencesManager.saveDisableBackButton(disableBackButton)
    }

    fun toggleAutoSkip(value: Boolean?) {
        autoSkip = value ?: !autoSkip
        SharedPreferencesManager.saveAutoSkip(autoSkip)
    }

    fun toggleKeepScreenOn(value: Boolean?) {
        keepScreenOn = value ?: !keepScreenOn
        SharedPreferencesManager.saveKeepScreenOn(keepScreenOn)
    }

    fun toggleAutoKo(value: Boolean?) {
        autoKo = value ?: !autoKo
        SharedPreferencesManager.saveAutoKo(autoKo)
    }

    fun toggleRotatingMiddleButton(value: Boolean?) {
        rotatingMiddleButton = value ?: !rotatingMiddleButton
        SharedPreferencesManager.saveRotatingMiddleButton(rotatingMiddleButton)
    }

    fun toggleCameraRollEnabled(value: Boolean?) {
        cameraRollDisabled = value ?: !cameraRollDisabled
        SharedPreferencesManager.saveCameraRollDisabled(cameraRollDisabled)
    }

    fun toggleFastCoinFlip(value: Boolean?) {
        fastCoinFlip = value ?: !fastCoinFlip
        SharedPreferencesManager.saveFastCoinFlip(fastCoinFlip)
    }

    fun toggle4PlayerLayout(value: Boolean?) {
        alt4PlayerLayout.value = value ?: !alt4PlayerLayout.value
        SharedPreferencesManager.save4PlayerLayout(alt4PlayerLayout.value)
    }

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