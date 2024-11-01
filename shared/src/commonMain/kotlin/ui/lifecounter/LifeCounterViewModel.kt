package ui.lifecounter

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.ImageManager
import data.Player
import data.Player.Companion.MAX_PLAYERS
import data.Player.Companion.allPlayerColors
import data.SettingsManager
import data.TurnTimer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ui.dialog.COUNTER_DIALOG_ENTRIES
import ui.dialog.planechase.PlaneChaseViewModel
import ui.lifecounter.playerbutton.AbstractPlayerButtonViewModel
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonViewModel

interface ILifeCounterViewModel {
    val state: StateFlow<LifeCounterState>
    val playerButtonViewModels: List<AbstractPlayerButtonViewModel>

    fun promptFirstPlayer()
    fun setFirstPlayer(index: Int?)
    fun setTimerEnabled(value: Boolean = true)
    fun killTimer()
    fun onNavigate(firstNavigation: Boolean)
    fun onCommanderDealerButtonClicked(playerButtonViewModel: AbstractPlayerButtonViewModel)
    fun savePlayerPrefs()
    fun savePlayerStates()
    fun resetAllPrefs()
    fun setFirstPlayerSelectionActive(value: Boolean)
    fun openMiddleButtonDialog(value: Boolean = true)
    fun setNumPlayers(value: Int)
    fun resetGameState()
    fun setShowButtons(value: Boolean)
    fun setBlurBackground(value: Boolean)
    fun setDayNight(value: DayNightState)
    fun incrementCounter(index: Int, value: Int)
    fun resetCounters()
    fun toggleDayNight()
}

class LifeCounterViewModel(
    private val settingsManager: SettingsManager,
    private val imageManager: ImageManager,
    private val planeChaseViewModel: PlaneChaseViewModel
) : ViewModel(), ILifeCounterViewModel {
    private val _state = MutableStateFlow(LifeCounterState())
    override val state: StateFlow<LifeCounterState> = _state.asStateFlow()

    override val playerButtonViewModels: List<AbstractPlayerButtonViewModel>

    init {
        playerButtonViewModels = generatePlayers()
        savePlayerStates()
    }

    private fun generatePlayers(): List<AbstractPlayerButtonViewModel> {
        val startingLife = settingsManager.startingLife
        val savedPlayers = settingsManager.loadPlayerStates().toMutableList()
        val viewModels = savedPlayers.map { generatePlayerButtonViewModel(it) }.toMutableList()
        while (savedPlayers.size < MAX_PLAYERS) {
            val playerNum = savedPlayers.size + 1
            val newColor = allPlayerColors.filter { it !in getUsedColors(viewModels) }.random()
            savedPlayers += generatePlayer(startingLife, playerNum, newColor)
            viewModels += generatePlayerButtonViewModel(savedPlayers.last())
        }
        return viewModels
    }

    override fun promptFirstPlayer() {
        if (settingsManager.numPlayers.value == 1) {
            setFirstPlayer(0)
            setTimerEnabled(settingsManager.turnTimer.value)
        } else if (state.value.firstPlayer == null) {
            for (i in playerButtonViewModels.indices) {
                playerButtonViewModels[i].pushBackStack { playerButtonViewModels[i].setPlayerButtonState(PBState.NORMAL) }
                playerButtonViewModels[i].setPlayerButtonState(PBState.SELECT_FIRST_PLAYER)
            }
            setFirstPlayerSelectionActive(true)
        } else {
            setTimerEnabled(settingsManager.turnTimer.value)
        }
    }

    override fun setFirstPlayer(index: Int?) {
        _state.value = _state.value.copy(firstPlayer = index)
        if (index != null) {
            for (i in playerButtonViewModels.indices) {
                if (playerButtonViewModels[i].state.value.buttonState == PBState.SELECT_FIRST_PLAYER) {
                    playerButtonViewModels[i].popBackStack()
                }
            }
            setTimerEnabled(settingsManager.turnTimer.value)
            setFirstPlayerSelectionActive(false)
        }
    }

    override fun setTimerEnabled(value: Boolean) {
        if (value) {
            if (_state.value.activeTimerIndex == null) {
                setActiveTimerIndex(_state.value.firstPlayer)
            }
            initTimer()
        } else {
            killTimer()
        }
    }

    private var timerJob: Job? = null

    //TODO: move timer logic to it's own module, also save it's state to local storage
    private fun initTimer() {
        if (_state.value.turnTimer == null) {
            _state.value = _state.value.copy(turnTimer = TurnTimer(-1, 1))
        }
        killTimer()
        timerJob = viewModelScope.launch {
            while (true) {
                if (_state.value.activeTimerIndex != null && _state.value.turnTimer != null) {
                    val newTimer = _state.value.turnTimer!!.tick()
                    _state.value = _state.value.copy(turnTimer = newTimer)
                    updatePlayerButtonTimers(_state.value.activeTimerIndex!!, newTimer)
                }
                delay(1000L)
            }
        }
    }

    override fun killTimer() {
        timerJob?.cancel()
        timerJob = null
        updatePlayerButtonTimers(-1, null)
    }

    private fun resetTimer() {
        _state.value = _state.value.copy(turnTimer = null)
        setActiveTimerIndex(null)
        updatePlayerButtonTimers(-1, null)
        killTimer()
    }

    private fun incrementTurn(value: Int = 1) {
        if (_state.value.activeTimerIndex == null) throw IllegalStateException("Attempted to increment turn timer when no one has an active timer")
        _state.value = _state.value.copy(turnTimer = _state.value.turnTimer!!.copy(turn = _state.value.turnTimer!!.turn + value))
    }

    private fun getNextPlayerIndex(currentIndex: Int): Int {
        var nextPlayerIndex = currentIndex
        do {
            nextPlayerIndex = (nextPlayerIndex + 1) % settingsManager.numPlayers.value
        } while (playerButtonViewModels[nextPlayerIndex].isDead() && !playerButtonViewModels.subList(0, settingsManager.numPlayers.value).all { it.isDead() })
        return nextPlayerIndex
    }

    private fun moveTimer() {
        if (_state.value.activeTimerIndex == null || _state.value.firstPlayer == null) throw IllegalStateException("Attempted to move timer when no one has an active timer")
        val nextPlayerIndex = getNextPlayerIndex(_state.value.activeTimerIndex!!)
        val firstActivePlayerIndex = getNextPlayerIndex((_state.value.firstPlayer!! - 1 + settingsManager.numPlayers.value) % settingsManager.numPlayers.value)
        if (nextPlayerIndex == firstActivePlayerIndex) {
            incrementTurn()
        }
        setActiveTimerIndex(nextPlayerIndex)
        val newTimer = _state.value.turnTimer!!.resetTime()
        _state.value = _state.value.copy(turnTimer = newTimer)
        updatePlayerButtonTimers(nextPlayerIndex, newTimer)
    }

    private fun updatePlayerButtonTimers(targetIndex: Int, newTimer: TurnTimer?) {
        for (i in playerButtonViewModels.indices) {
            if (i == targetIndex) {
                playerButtonViewModels[i].setTimer(newTimer)
            } else {
                playerButtonViewModels[i].setTimer(null)
            }
        }
    }

    override fun onNavigate(firstNavigation: Boolean) {
        if (settingsManager.loadPlayerStates().isEmpty()) generatePlayers()
        if (firstNavigation) {
            viewModelScope.launch {
                showLoadingScreen(true)
                setShowButtons(true)
                delay(1000) // Delay to allow for animations to finish
                setShowButtons(false)
                showLoadingScreen(false)
                delay(25)
                setShowButtons(true)
            }
        } else {
            showLoadingScreen(false)
            setShowButtons(true)
        }
    }

    private fun getUsedColors(viewModels: List<AbstractPlayerButtonViewModel> = playerButtonViewModels): List<Color> {
        return viewModels.map { it.state.value.player.color }
    }

    private fun resetPlayerColor(player: Player): Player {
        return player.copy(color = allPlayerColors.filter { it !in getUsedColors() }.random())
    }

    private fun onNormalCommanderButtonClicked(playerButtonViewModel: AbstractPlayerButtonViewModel) {
        setCurrentDealer(playerButtonViewModel)
        setAllButtonStates(PBState.COMMANDER_RECEIVER)
        playerButtonViewModel.setPlayerButtonState(PBState.COMMANDER_DEALER)
    }

    override fun onCommanderDealerButtonClicked(playerButtonViewModel: AbstractPlayerButtonViewModel) {
        setCurrentDealer(null)
        setAllButtonStates(PBState.NORMAL)
        playerButtonViewModel.setPlayerButtonState(PBState.NORMAL)
    }

    private fun onCommanderButtonClicked(playerButtonViewModel: AbstractPlayerButtonViewModel) {
            when (playerButtonViewModel.state.value.buttonState) {
                PBState.NORMAL -> {
                    onNormalCommanderButtonClicked(playerButtonViewModel)
                }
                PBState.COMMANDER_DEALER -> {
                    onCommanderDealerButtonClicked(playerButtonViewModel)
                }
                else -> {} // do nothing
            }
    }

    private fun generatePlayerButtonViewModel(player: Player): PlayerButtonViewModel {
        return PlayerButtonViewModel(
            initialPlayer = player,
            settingsManager = settingsManager,
            imageManager = imageManager,
            onCommanderButtonClickedCallback = { onCommanderButtonClicked(it) },
            setAllMonarchy = { setAllMonarchy(it) },
            getCurrentDealer = { state.value.currentDealer },
            updateCurrentDealerMode = { setCurrentDealerIsPartnered(it) },
            triggerSave = { savePlayerStates() },
            resetPlayerColor = { resetPlayerColor(it) },
            moveTimerCallback = { moveTimer() },
        )
    }

    override fun savePlayerPrefs() {
        playerButtonViewModels.forEach {
            it.savePlayerPref()
        }
    }

    override fun savePlayerStates() {
        settingsManager.savePlayerStates(playerButtonViewModels.map { it.state.value.player })
    }

    private fun resetAllPlayerStates() {
        playerButtonViewModels.forEach { it.resetState(settingsManager.startingLife) }
    }

    override fun resetAllPrefs() {
        playerButtonViewModels.forEach {
            it.resetPlayerPref()
            it.copyPrefs(it.state.value.player)
        }
    }

    override fun setFirstPlayerSelectionActive(value: Boolean) {
        _state.value = _state.value.copy(firstPlayerSelectionActive = value)
    }

    override fun openMiddleButtonDialog(value: Boolean) {
        _state.value = _state.value.copy(showMiddleButtonDialog = value)
    }

    private fun setActiveTimerIndex(index: Int?) {
        _state.value = _state.value.copy(activeTimerIndex = index)
    }

    private fun setAllButtonStates(pbState: PBState) {
        playerButtonViewModels.forEach { it.setPlayerButtonState(pbState) }
    }

    private fun setCurrentDealerIsPartnered(value: Boolean) {
        _state.value = _state.value.copy(currentDealerIsPartnered = value)
    }

    private fun setAllMonarchy(value: Boolean) {
        playerButtonViewModels.forEach { it.toggleMonarch(value) }
    }

    private fun generatePlayer(startingLife: Int, playerNum: Int, color: Color): Player {
        val name = "P$playerNum"
        return Player(color = color, life = startingLife, name = name, playerNum = playerNum)
    }

    private fun showLoadingScreen(value: Boolean) {
        _state.value = _state.value.copy(showLoadingScreen = value)
    }

    private fun setCurrentDealer(dealer: AbstractPlayerButtonViewModel?) {
        _state.value = _state.value.copy(currentDealer = dealer)
    }

    override fun setNumPlayers(value: Int) {
        println("SETTING NUM PLAYERS: $value")
//        _state.value = _state.value.copy(numPlayers = value)
        settingsManager.setNumPlayers(value)
    }

    private fun restartButtons() {
        setShowButtons(false)
        viewModelScope.launch {
            delay(10)
            setShowButtons(true)
        }
    }

    override fun resetGameState() {
        //TODO: reset counters?
        planeChaseViewModel.onResetGame()
        setAllButtonStates(PBState.NORMAL)
        resetAllPlayerStates()
        savePlayerStates()
        setFirstPlayer(null)
        resetTimer()
        restartButtons()
    }

    override fun setShowButtons(value: Boolean) {
        _state.value = _state.value.copy(showButtons = value)
    }

    override fun setBlurBackground(value: Boolean) {
        _state.value = _state.value.copy(blurBackground = value)
    }

    override fun setDayNight(value: DayNightState) {
        _state.value = _state.value.copy(dayNight = value)
    }

    override fun incrementCounter(index: Int, value: Int) {
        _state.value = _state.value.copy(counters = _state.value.counters.toMutableList().apply { set(index, _state.value.counters[index] + value) }.toList())
    }

    override fun resetCounters() {
        _state.value = _state.value.copy(counters = List(COUNTER_DIALOG_ENTRIES) { 0 })
    }

    override fun toggleDayNight() {
        setDayNight(
            when (_state.value.dayNight) {
                DayNightState.NONE -> DayNightState.DAY
                DayNightState.DAY -> DayNightState.NIGHT
                DayNightState.NIGHT -> DayNightState.DAY
            }
        )
    }
}