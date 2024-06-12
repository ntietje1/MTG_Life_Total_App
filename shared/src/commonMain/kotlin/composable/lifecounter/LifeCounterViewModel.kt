package composable.lifecounter

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import composable.dialog.COUNTER_DIALOG_ENTRIES
import composable.dialog.planechase.PlaneChaseViewModel
import composable.lifecounter.playerbutton.PBState
import composable.lifecounter.playerbutton.PlayerButtonViewModel
import data.ImageManager
import data.Player
import data.Player.Companion.MAX_PLAYERS
import data.Player.Companion.allPlayerColors
import data.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LifeCounterViewModel(
    val settingsManager: SettingsManager,
    private val imageManager: ImageManager,
    private val planeChaseViewModel: PlaneChaseViewModel
) : ViewModel() {
    private val _state = MutableStateFlow(LifeCounterState())
    val state: StateFlow<LifeCounterState> = _state.asStateFlow()

    private val _currentDealerIsPartnered = MutableStateFlow(false)
    private val currentDealerIsPartnered = _currentDealerIsPartnered.asStateFlow()

    lateinit var playerButtonViewModels: List<PlayerButtonViewModel>

    init {
        generatePlayers()
        savePlayerStates()
    }

    fun onNavigate(firstNavigation: Boolean) {
        if (settingsManager.loadPlayerStates().isEmpty()) generatePlayers()
        if (firstNavigation) {
            viewModelScope.launch {
                showLoadingScreen(true)
                setShowButtons(true)
                delay(1000) // Delay to allow for animations to finish
                setShowButtons(false)
                delay(10)
                showLoadingScreen(false)
                setShowButtons(true)
            }
        } else {
            showLoadingScreen(false)
            setShowButtons(true)
        }
    }

    private fun getUsedColors(): List<Color> {
        return playerButtonViewModels.map { it.state.value.player.color }
    }

    fun generatePlayers() {
        val startingLife = settingsManager.startingLife
        val savedPlayers = settingsManager.loadPlayerStates().toMutableList()
        playerButtonViewModels = savedPlayers.map { generatePlayerButtonViewModel(it) }
        while (savedPlayers.size < MAX_PLAYERS) {
            val playerNum = savedPlayers.size + 1
            val newColor = allPlayerColors.filter { it !in getUsedColors() }.random()
            savedPlayers += generatePlayer(startingLife, playerNum, newColor)
            playerButtonViewModels += generatePlayerButtonViewModel(savedPlayers.last())
        }
        savePlayerStates()
    }

    private fun resetPlayerColor(player: Player): Player {
        return player.copy(color = allPlayerColors.filter { it !in getUsedColors() }.random())
    }

//    private val _timer = MutableStateFlow(0)
//    val timer = _timer.asStateFlow()

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
            currentDealerIsPartnered = currentDealerIsPartnered,
            triggerSave = { savePlayerStates() },
            resetPlayerColor = { resetPlayerColor(it) }
        )
    }

    private fun savePlayerStates() {
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

    private fun generatePlayer(startingLife: Int, playerNum: Int, color: Color): Player {
        val name = "P$playerNum"
        return Player(color = color, life = startingLife, name = name, playerNum = playerNum)
    }

    fun showLoadingScreen(value: Boolean) {
        _state.value = _state.value.copy(showLoadingScreen = value)
    }

    fun setNumPlayers(value: Int) {
        _state.value = _state.value.copy(numPlayers = value)
        settingsManager.numPlayers = value
    }

    fun resetPlayerStates() {
        setShowButtons(false)
        planeChaseViewModel.onResetGame()
        viewModelScope.launch {
            delay(10)
            setShowButtons(true)
        }
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

//    private fun startTimer() {
//        viewModelScope.launch {
//            while (true) {
//                delay(1000 * 10)
//                savePlayerStates()
//                _timer.value++
//            }
//        }
//    }

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