package lifelinked.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import lifelinked.composable.lifecounter.PlayerButtonState
import lifelinked.ui.theme.allPlayerColors

enum class DayNightState {
    NONE, DAY, NIGHT
}

class AppViewModel : ViewModel() {
    private var currentPlayers: MutableList<Player> = SharedPreferencesManager.loadPlayerStates().toMutableList()
    private val numPlayers = mutableIntStateOf(SharedPreferencesManager.loadNumPlayers())
    private val buttonStates = mutableListOf<MutableState<PlayerButtonState>>()
    var currentDealer: Player? = null
    var blurBackground = mutableStateOf(false)
    var planarDeck = mutableStateListOf<Card>().apply {
        addAll(SharedPreferencesManager.loadPlanarDeck())
    }
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

    var dayNight by mutableStateOf(DayNightState.NONE)

    fun savePlanarDeck() {
        SharedPreferencesManager.savePlanarDeck(planarDeck)
    }

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
        savePlayerStates()
    }

    fun resetPlayerPrefs(player: Player) {
        player.apply {
            name = "P${playerNum}"
            imageUri = null
            color = getRandColor()
            textColor = Color.White
        }
    }

    fun savePlayerStates() {
        SharedPreferencesManager.savePlayerStates(currentPlayers)
    }

    fun resetPlayerStates() {
        val startingLife = SharedPreferencesManager.loadStartingLife()
        updateAllStates(PlayerButtonState.NORMAL)
        currentPlayers.forEach {
            it.resetState(startingLife)
        }
        SharedPreferencesManager.savePlayerStates(currentPlayers)
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

    fun toggleMonarch(player: Player, value: Boolean? = null) {
        val targetValue = value ?: !player.monarch
        if (!targetValue) {
            currentPlayers.forEach { it.monarch = false }
        }
        player.monarch = targetValue
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
        resetPlayerStates()
    }

    fun getActivePlayers(): List<Player> {
        return currentPlayers.subList(0, numPlayers.value)
    }
}