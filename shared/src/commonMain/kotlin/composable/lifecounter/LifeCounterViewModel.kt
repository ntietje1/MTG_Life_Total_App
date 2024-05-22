package composable.lifecounter

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import composable.dialog.COUNTER_DIALOG_ENTRIES
import composable.lifecounter.playerbutton.PBState
import composable.lifecounter.playerbutton.PlayerButtonViewModel
import data.ImageManager
import data.Player
import data.Player.Companion.MAX_PLAYERS
import data.SettingsManager
import data.serializable.Card
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import theme.allPlayerColors

/**
 * The component for the life counter screen for state and navigation
 */
class LifeCounterViewModel(
    private val helloWorld: String,
    val settingsManager: SettingsManager,
    private val imageManager: ImageManager
) : ViewModel() {
    private val _state = MutableStateFlow(LifeCounterState())
    val state: StateFlow<LifeCounterState> = _state.asStateFlow()

    private val _currentDealerIsPartnered = MutableStateFlow(false)
    val currentDealerIsPartnered = _currentDealerIsPartnered.asStateFlow()

    lateinit var playerButtonViewModels: List<PlayerButtonViewModel>

    init {
        println(helloWorld)
        startTimer()
        generatePlayers()
        savePlayerStates()
    }

    fun generatePlayers() {
        val savedPlayers = settingsManager.loadPlayerStates().toMutableList()
        val startingLife = settingsManager.startingLife
        while (savedPlayers.size < MAX_PLAYERS) {
//            val usedColors = savedPlayers.map { it.color }
            val playerNum = savedPlayers.size + 1
            println("playerNum: $playerNum")
            savedPlayers += generatePlayer(startingLife, playerNum)
        }
        playerButtonViewModels = savedPlayers.map { generatePlayerButtonViewModel(it) }
        savePlayerStates()
    }

    private val _timer = MutableStateFlow(0)
    val timer = _timer.asStateFlow()

    private val currentDealer: PlayerButtonViewModel?
        get() = playerButtonViewModels.find { it.state.value.buttonState == PBState.COMMANDER_DEALER }

    private fun generatePlayerButtonViewModel(player: Player): PlayerButtonViewModel {
        return PlayerButtonViewModel(
            initialPlayer = player,
            settingsManager = settingsManager,
            imageManager = imageManager,
            setAllButtonStates = { setAllButtonStates(it) },
            setAllMonarchy = { setAllMonarchy(it) },
            getCurrentDealer = { currentDealer },
            updateCurrentDealerMode = { setDealerMode(it) },
            currentDealerIsPartnered = currentDealerIsPartnered
        )
    }

    fun savePlayerStates() {
        settingsManager.savePlayerStates(playerButtonViewModels.map { it.state.value.player })
    }

    private fun resetAllPlayerStates() {
        playerButtonViewModels.forEach { it.resetState(settingsManager.startingLife) }
    }

    fun resetAllPrefs() {
        playerButtonViewModels.forEach { it.resetPlayerPref() }
    }

    private fun setAllButtonStates(pbState: PBState) {
        playerButtonViewModels.forEach { it.setPlayerButtonState(pbState) }
    }

    private fun setDealerMode(value: Boolean) {
        _currentDealerIsPartnered.value = value
    }

    private fun setAllMonarchy(value: Boolean) {
        playerButtonViewModels.forEach { it.toggleMonarch(value) }
    }

    private fun generatePlayer(startingLife: Int, playerNum: Int): Player {
        val playerColor = allPlayerColors.random()
        val name = "P$playerNum"
        return Player(color = playerColor, life = startingLife, name = name, playerNum = playerNum)
    }

    private fun getRandColor(usedColors: List<Color>): Color {
//        do {
//            val color = allPlayerColors.random()
//            if (usedColors.none { it.toArgb() == color.toArgb() }) return color
//        } while (true)
//        println("usedColors: $usedColors")
        //TODO: this is broken??
        val availableColors = allPlayerColors.filter { color -> color !in usedColors }
//        println("availableColors: $availableColors")
        return if (availableColors.isNotEmpty()) {
            availableColors.random()
        } else {
            Color.Gray
        }
    }

    fun setNumPlayers(value: Int) {
        _state.value = _state.value.copy(numPlayers = value)
        settingsManager.numPlayers = value
    }

    fun resetPlayerStates() {
        setShowButtons(false)
        setAllButtonStates(PBState.NORMAL)
        resetAllPlayerStates()
        savePlayerStates()
    }

    fun setShowButtons(value: Boolean) {
        _state.value = _state.value.copy(showButtons = value)
    }

    fun setBlurBackground(value: Boolean) {
        _state.value = _state.value.copy(blurBackground = value)
    }

    fun setDayNight(value: DayNightState) {
        _state.value = _state.value.copy(dayNight = value)
    }

    private fun setCoinFlipHistory(value: List<String>) {
        _state.value = _state.value.copy(coinFlipHistory = value)
    }

    fun incrementCounter(index: Int, value: Int) {
        _state.value = _state.value.copy(counters = _state.value.counters.toMutableList().apply { set(index, _state.value.counters[index] + value) }.toList())
    }

    fun resetCounters() {
        _state.value = _state.value.copy(counters = List(COUNTER_DIALOG_ENTRIES) { 0 })
    }

    fun addToCoinFlipHistory(value: String) {
        setCoinFlipHistory(_state.value.coinFlipHistory.toMutableList().apply { add(value) }.toList())
    }

    fun resetCoinFlipHistory() {
        setCoinFlipHistory(emptyList())
    }

    private fun pushPlanarDeck(value: Card) {
        _state.value = _state.value.copy(planarDeck = state.value.planarDeck.apply {
            remove(value)
            addLast(value)
        })
    }

    private fun sendToBottomPlanarDeck(value: Card) {
        _state.value = _state.value.copy(planarDeck = state.value.planarDeck.apply {
            remove(value)
            addFirst(value)
        })
    }

    private fun popPlanarDeck(): Card? {
        val card = _state.value.planarDeck.lastOrNull()
        if (card != null) {
            _state.value = _state.value.copy(planarDeck = _state.value.planarDeck.apply { remove(card) })
        }
        return card
    }

    private fun pushPlanarBackStack(value: Card) {
        _state.value = _state.value.copy(planarBackStack = _state.value.planarBackStack.apply { addLast(value) })
    }

    private fun popPlanarBackStack(): Card? {
        val card = _state.value.planarBackStack.lastOrNull()
        if (card != null) {
            _state.value = _state.value.copy(planarBackStack = _state.value.planarBackStack.apply { remove(card) })
        }
        return card
    }

    private fun clearPlanarBackStack() {
        _state.value = _state.value.copy(planarBackStack = ArrayDeque())
    }

    fun selectPlane(card: Card) {
        pushPlanarDeck(card)
        clearPlanarBackStack()
    }

    fun deselectPlane(card: Card) {
        pushPlanarBackStack(card)
        clearPlanarBackStack()
    }

    fun addAllPlanarDeck(cards: List<Card>) {
        _state.value = _state.value.copy(planarDeck = _state.value.planarDeck.apply { addAll(cards) })
    }

    fun removeAllPlanarDeck(cards: List<Card>) {
        _state.value = _state.value.copy(planarDeck = _state.value.planarDeck.apply { removeAll(cards) })
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000 * 10)
                savePlayerStates()
                _timer.value++
            }
        }
    }

    fun backPlane() {
        if (_state.value.planarBackStack.isNotEmpty()) {
            val card = popPlanarBackStack()
            card?.let { pushPlanarDeck(card) }
        }
    }

    fun planeswalk(): Card? {
        if (state.value.planarDeck.isNotEmpty()) {
            val card = popPlanarDeck()
            card?.let {
                pushPlanarBackStack(card)
                sendToBottomPlanarDeck(card)
            }
            return card
        }
        return null
    }

    fun currentPlane(): Card? {
        return state.value.planarDeck.lastOrNull()
    }

    fun toggleDayNight() {
        setDayNight(
            when (_state.value.dayNight) {
                DayNightState.NONE -> DayNightState.DAY
                DayNightState.DAY -> DayNightState.NIGHT
                DayNightState.NIGHT -> DayNightState.DAY
            }
        )
    }
}