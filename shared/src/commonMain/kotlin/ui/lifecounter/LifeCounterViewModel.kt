package ui.lifecounter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.IImageManager
import data.ISettingsManager
import data.Player
import data.Player.Companion.MAX_PLAYERS
import di.NotificationManager
import domain.game.GameStateManager
import domain.player.CommanderDamageManager
import domain.player.PlayerCustomizationManager
import domain.player.PlayerStateManager
import domain.timer.TimerManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ui.dialog.COUNTER_DIALOG_ENTRIES
import ui.dialog.MiddleButtonDialogState
import ui.dialog.planechase.PlaneChaseViewModel
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonViewModel

open class LifeCounterViewModel(
    private val settingsManager: ISettingsManager,
    internal val playerStateManager: PlayerStateManager,
    internal val commanderManager: CommanderDamageManager,
    private val imageManager: IImageManager,
    protected val notificationManager: NotificationManager,
    internal val playerCustomizationManager: PlayerCustomizationManager,
    private val planeChaseViewModel: PlaneChaseViewModel,
    initialState: LifeCounterState = LifeCounterState(),
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<LifeCounterState> = _state.asStateFlow()

    val numPlayers: StateFlow<Int> = settingsManager.numPlayers
    val alt4PlayerLayout: StateFlow<Boolean> = settingsManager.alt4PlayerLayout
    val turnTimerEnabled: StateFlow<Boolean> = settingsManager.turnTimer

    private val _playerButtonViewModels = MutableStateFlow<List<PlayerButtonViewModel>>(emptyList())
    val playerButtonViewModels: StateFlow<List<PlayerButtonViewModel>> = _playerButtonViewModels.asStateFlow()

    internal val gameStateManager = GameStateManager(
        settingsManager = settingsManager
    )

    internal var timerManager = TimerManager(
        settingsManager = settingsManager,
    )

    init {
        _playerButtonViewModels.value = generatePlayerButtonViewModels()

        playerCustomizationManager.attach(playerButtonViewModels)
        gameStateManager.attach(playerButtonViewModels)
        commanderManager.attach(playerButtonViewModels)
        timerManager.attach(playerButtonViewModels)
        timerManager.setupTimerStateObserver(viewModelScope)

        registerCommanderListener()
    }

    override fun onCleared() {
        savePlayerPrefs()
        savePlayerStates()
        playerCustomizationManager.detach()
        gameStateManager.detach()
        commanderManager.detach()
        timerManager.detach()
    }

    private fun registerCommanderListener() {
        viewModelScope.launch {
            commanderManager.currentDealer.collect { dealer ->
                if (dealer == null) {
                    setAllButtonStates(PBState.NORMAL)
                    setMiddleButtonState(MiddleButtonState.DEFAULT)
                } else {
                    setAllButtonStates(PBState.COMMANDER_RECEIVER)
                    setMiddleButtonState(MiddleButtonState.COMMANDER_EXIT)
                    playerButtonViewModels.value.find {
                        it.state.value.player.playerNum == dealer.playerNum
                    }?.setPlayerButtonState(PBState.COMMANDER_DEALER)
                }
            }
        }
    }

    private fun generatePlayerButtonViewModels(): List<PlayerButtonViewModel> {
        val savedPlayers = settingsManager.loadPlayerStates().toMutableList()
        val viewModels = savedPlayers.map { generatePlayerButtonViewModel(it) }.toMutableList()
        while (savedPlayers.size < MAX_PLAYERS) {
            playerCustomizationManager.resetPlayerPrefs(
                playerStateManager.generatePlayer(
                    startingLife = settingsManager.startingLife.value,
                    playerNum = savedPlayers.size + 1,
                )
            ).also { newPlayer ->
                savedPlayers += newPlayer
                viewModels += generatePlayerButtonViewModel(newPlayer)
            }
        }
        return viewModels
    }

    fun setTimerEnabled(value: Boolean) {
        viewModelScope.launch {
            timerManager.onTimerEnabledChange(value)
        }
    }

    fun setFirstPlayer(index: Int?) {
        timerManager.handleFirstPlayerSelection(index)
    }

    fun onNavigate(firstNavigation: Boolean) {
        if (settingsManager.loadPlayerStates().isEmpty()) generatePlayerButtonViewModels()
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

    private fun setMiddleButtonState(value: MiddleButtonState) {
        _state.value = _state.value.copy(middleButtonState = value)
    }

    fun onCommanderDealerButtonClicked() {
        commanderManager.setCurrentDealer(null)
        setAllButtonStates(PBState.NORMAL)
    }

    open fun generatePlayerButtonViewModel(player: Player): PlayerButtonViewModel {
        return PlayerButtonViewModel(
            initialPlayer = player,
            settingsManager = settingsManager,
            imageManager = imageManager,
            notificationManager = notificationManager,
            playerStateManager = playerStateManager,
            commanderManager = commanderManager,
            playerCustomizationManager = playerCustomizationManager,
            gameStateManager = gameStateManager,
            timerManager = timerManager
        )
    }

    fun savePlayerPrefs() {
        playerCustomizationManager.saveAllPlayerPrefs()
    }

    fun savePlayerStates() {
        gameStateManager.saveGameState()
    }

    fun resetAllPrefs() {
        playerCustomizationManager.resetAllPlayerPrefs()
    }

    open fun setMiddleButtonDialogState(value: MiddleButtonDialogState?) {
        _state.value = _state.value.copy(middleButtonDialogState = value)
    }

    private fun setAllButtonStates(pbState: PBState) {
        playerButtonViewModels.value.forEach { it.setPlayerButtonState(pbState) }
    }

    protected fun setMonarchy(targetPlayerNum: Int, value: Boolean) {
        gameStateManager.setMonarchy(targetPlayerNum, value)
    }

    private fun showLoadingScreen(value: Boolean) {
        _state.value = _state.value.copy(showLoadingScreen = value)
    }

    open fun setNumPlayers(value: Int) {
        if (value < 1 || value > MAX_PLAYERS) throw IllegalArgumentException("Invalid number of players")
        settingsManager.setNumPlayers(value)
    }

    private fun restartButtons() {
        setShowButtons(false)
        viewModelScope.launch {
            delay(10)
            setShowButtons(true)
        }
    }

    fun resetGameState() {
        resetCounters()
        planeChaseViewModel.onResetGame()
        setAllButtonStates(PBState.NORMAL)
        playerButtonViewModels.value.forEach {
            it.setPlayer(
                playerStateManager.resetPlayerState(
                    player = it.state.value.player,
                    startingLife = settingsManager.startingLife.value
                )
            )
        }
        savePlayerStates()
        viewModelScope.launch {
            timerManager.reset()
        }
        restartButtons()
    }

    fun toggleKeepScreenOn(value: Boolean? = null) {
        settingsManager.setKeepScreenOn(value ?: !settingsManager.keepScreenOn.value)
    }

    open fun toggleDarkTheme(value: Boolean? = null) {
        settingsManager.setDarkTheme(value ?: !settingsManager.darkTheme.value)
    }

    fun setAlt4PlayerLayout(value: Boolean) {
        settingsManager.setAlt4PlayerLayout(value)
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

    fun incrementCounter(index: Int, value: Int) {
        _state.value = _state.value.copy(counters = _state.value.counters.toMutableList().apply {
            set(index, _state.value.counters[index] + value)
        }.toList())
    }

    fun resetCounters() {
        _state.value = _state.value.copy(counters = List(COUNTER_DIALOG_ENTRIES) { 0 })
    }

    fun toggleDayNight() {
        setDayNight(gameStateManager.toggleDayNight(_state.value.dayNight))
    }
}