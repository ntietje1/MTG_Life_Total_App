package ui.lifecounter

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.IImageManager
import data.ISettingsManager
import data.Player
import data.Player.Companion.MAX_PLAYERS
import di.NotificationManager
import domain.game.GameStateManager
import domain.player.CommanderDamageManager
import domain.player.CounterManager
import domain.player.PlayerCustomizationManager
import domain.player.PlayerManager
import domain.timer.TimerCoordinator
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
    private val playerManager: PlayerManager,
    private val commanderManager: CommanderDamageManager,
    private val counterManager: CounterManager,
    private val imageManager: IImageManager,
    protected val notificationManager: NotificationManager,
    private val playerCustomizationManager: PlayerCustomizationManager,
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

    private var timerCoordinator = TimerCoordinator(
        gameStateManager = gameStateManager,
        playerViewModels = playerButtonViewModels.value,
        numPlayersFlow = settingsManager.numPlayers
    )

    init {
        _playerButtonViewModels.value = generatePlayerButtonViewModels()
        viewModelScope.launch {
            timerCoordinator.setupTimerStateObserver()
        }

        playerCustomizationManager.init(playerButtonViewModels)

        registerCommanderListener()
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
            playerCustomizationManager.resetPlayerPreferences(
                playerManager.generatePlayer(
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

    fun onTimerEnabledChange(timerEnabled: Boolean) {
        viewModelScope.launch {
            timerCoordinator.onTimerEnabledChange(timerEnabled)
        }
    }

    fun setTimerEnabled(value: Boolean) {
        viewModelScope.launch {
            timerCoordinator.onTimerEnabledChange(value)
        }
    }

    fun setFirstPlayer(index: Int?) {
        viewModelScope.launch {
            timerCoordinator.handleFirstPlayerSelection(index)
        }
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
            playerManager = playerManager,
            commanderManager = commanderManager,
            counterManager = counterManager,
            playerCustomizationManager = playerCustomizationManager,
            setMonarchy = { setMonarchy(player.playerNum, it) },
            triggerSave = { savePlayerStates() },
            moveTimerCallback = { gameStateManager.moveTimer() },
        )
    }

    fun savePlayerPrefs() {
        playerButtonViewModels.value.forEach {
            it.savePlayerPref()
        }
    }

    fun savePlayerStates() {
        settingsManager.savePlayerStates(playerButtonViewModels.value.map { it.state.value.player })
    }

    private fun resetAllPlayerStates() {
        playerButtonViewModels.value.forEach { it.resetState(settingsManager.startingLife.value) }
    }

    fun resetAllPrefs() {
        playerButtonViewModels.value.forEach {
            it.resetPlayerPref()
            it.copyPrefs(it.state.value.player)
        }
    }

    open fun setMiddleButtonDialogState(value: MiddleButtonDialogState?) {
        _state.value = _state.value.copy(middleButtonDialogState = value)
    }

    private fun setAllButtonStates(pbState: PBState) {
        playerButtonViewModels.value.forEach { it.setPlayerButtonState(pbState) }
    }

    protected fun setMonarchy(targetPlayerNum: Int, value: Boolean) {
        val updatedPlayers = gameStateManager.setMonarch(
            players = playerButtonViewModels.value.map { it.state.value.player },
            targetPlayerNum = targetPlayerNum,
            value = value
        )
        updatedPlayers.forEachIndexed { index, player ->
            playerButtonViewModels.value[index].setPlayer(player)
        }
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
        //TODO: reset counters?
        planeChaseViewModel.onResetGame()
        setAllButtonStates(PBState.NORMAL)
        playerButtonViewModels.value.forEach {
            val resetPlayer = playerManager.resetPlayerState(it.state.value.player, settingsManager.startingLife.value)
            it.setPlayer(resetPlayer)
        }
        savePlayerStates()
        timerCoordinator.reset()
        viewModelScope.launch {
            timerCoordinator.promptForFirstPlayer()
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
        _state.value = _state.value.copy(counters = _state.value.counters.toMutableList().apply { set(index, _state.value.counters[index] + value) }.toList())
    }

    fun resetCounters() {
        _state.value = _state.value.copy(counters = List(COUNTER_DIALOG_ENTRIES) { 0 })
    }

    fun toggleDayNight() {
        setDayNight(gameStateManager.toggleDayNight(_state.value.dayNight))
    }
}