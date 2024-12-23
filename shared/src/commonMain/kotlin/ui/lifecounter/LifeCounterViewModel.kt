package ui.lifecounter

import TimerCoordinator
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.IImageManager
import data.ISettingsManager
import data.Player
import data.Player.Companion.MAX_PLAYERS
import data.Player.Companion.allPlayerColors
import data.timer.GameTimer
import di.NotificationManager
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
    private val imageManager: IImageManager,
    protected val notificationManager: NotificationManager,
    private val planeChaseViewModel: PlaneChaseViewModel,
) : ViewModel() {
    private val _state = MutableStateFlow(LifeCounterState())
    val state: StateFlow<LifeCounterState> = _state.asStateFlow()

    val numPlayers: StateFlow<Int> = settingsManager.numPlayers
    val alt4PlayerLayout: StateFlow<Boolean> = settingsManager.alt4PlayerLayout
    val turnTimerEnabled: StateFlow<Boolean> = settingsManager.turnTimer

    private var _playerButtonViewModels: List<PlayerButtonViewModel>
    val playerButtonViewModels: List<PlayerButtonViewModel>
        get() = _playerButtonViewModels

    internal val gameTimer = GameTimer(
        scope = viewModelScope,
        numPlayersFlow = settingsManager.numPlayers,
        isDead = { index -> playerButtonViewModels[index].isDead.value },
        settingsManager = settingsManager
    )

    private var timerCoordinator: TimerCoordinator

    init {
        _playerButtonViewModels = generatePlayers()

        timerCoordinator = TimerCoordinator(
            gameTimer = gameTimer,
            playerViewModels = playerButtonViewModels,
        )
    }

    constructor(
        initialState: LifeCounterState,
        settingsManager: ISettingsManager,
        imageManager: IImageManager,
        notificationManager: NotificationManager,
        planeChaseViewModel: PlaneChaseViewModel
    ) : this(settingsManager, imageManager, notificationManager, planeChaseViewModel) {
        _state.value = initialState
        _playerButtonViewModels = settingsManager.loadPlayerStates().map { generatePlayerButtonViewModel(it) }
    }

    private fun generatePlayers(): List<PlayerButtonViewModel> {
        val startingLife = settingsManager.startingLife.value
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

    fun onTimerEnabledChange(timerEnabled: Boolean) {
        timerCoordinator.onTimerEnabledChange(timerEnabled)
    }

    fun setFirstPlayer(index: Int?) {
        timerCoordinator.handleFirstPlayerSelection(index)
    }

    fun setTimerEnabled(value: Boolean) {
        timerCoordinator.onTimerEnabledChange(value)
    }

    fun onNavigate(firstNavigation: Boolean) {
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

    private fun getUsedColors(viewModels: List<PlayerButtonViewModel> = playerButtonViewModels): List<Color> {
        return viewModels.map { it.state.value.player.color }
    }

    protected fun resetPlayerColor(player: Player): Player {
        return player.copy(color = allPlayerColors.filter { it !in getUsedColors() }.random())
    }

    private fun onNormalCommanderButtonClicked(playerButtonViewModel: PlayerButtonViewModel) {
        setCurrentDealer(playerButtonViewModel)
        setAllButtonStates(PBState.COMMANDER_RECEIVER)
        playerButtonViewModel.setPlayerButtonState(PBState.COMMANDER_DEALER)
    }

    fun onCommanderDealerButtonClicked(playerButtonViewModel: PlayerButtonViewModel) {
        setCurrentDealer(null)
        setAllButtonStates(PBState.NORMAL)
        playerButtonViewModel.setPlayerButtonState(PBState.NORMAL)
    }

    protected fun onCommanderButtonClicked(playerButtonViewModel: PlayerButtonViewModel) {
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

    open fun generatePlayerButtonViewModel(player: Player): PlayerButtonViewModel {
        return PlayerButtonViewModel(
            initialPlayer = player,
            settingsManager = settingsManager,
            imageManager = imageManager,
            notificationManager = notificationManager,
            onCommanderButtonClickedCallback = { onCommanderButtonClicked(it) },
            setAllMonarchy = { setAllMonarchy(it) },
            getCurrentDealer = { state.value.currentDealer },
            updateCurrentDealerMode = { setCurrentDealerIsPartnered(it) },
            triggerSave = { savePlayerStates() },
            resetPlayerColor = { resetPlayerColor(it) },
            moveTimerCallback = { gameTimer.moveTimer() },
        )
    }

    fun savePlayerPrefs() {
        playerButtonViewModels.forEach {
            it.savePlayerPref()
        }
    }

    fun savePlayerStates() {
        settingsManager.savePlayerStates(playerButtonViewModels.map { it.state.value.player })
    }

    private fun resetAllPlayerStates() {
        playerButtonViewModels.forEach { it.resetState(settingsManager.startingLife.value) }
    }

    fun resetAllPrefs() {
        playerButtonViewModels.forEach {
            it.resetPlayerPref()
            it.copyPrefs(it.state.value.player)
        }
    }

    open fun setMiddleButtonDialogState(value: MiddleButtonDialogState?) {
        _state.value = _state.value.copy(middleButtonDialogState = value)
    }

    private fun setAllButtonStates(pbState: PBState) {
        playerButtonViewModels.forEach { it.setPlayerButtonState(pbState) }
    }

    protected fun setCurrentDealerIsPartnered(value: Boolean) {
        _state.value = _state.value.copy(currentDealerIsPartnered = value)
    }

    protected fun setAllMonarchy(value: Boolean) {
        playerButtonViewModels.forEach { it.toggleMonarch(value) }
    }

    private fun generatePlayer(startingLife: Int, playerNum: Int, color: Color): Player {
        val name = "P$playerNum"
        return Player(color = color, life = startingLife, name = name, playerNum = playerNum)
    }

    private fun showLoadingScreen(value: Boolean) {
        _state.value = _state.value.copy(showLoadingScreen = value)
    }

    private fun setCurrentDealer(dealer: PlayerButtonViewModel?) {
        _state.value = _state.value.copy(currentDealer = dealer)
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
        resetAllPlayerStates()
        savePlayerStates()
        timerCoordinator.reset()
        timerCoordinator.promptForFirstPlayer()
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
        setDayNight(
            when (_state.value.dayNight) {
                DayNightState.NONE -> DayNightState.DAY
                DayNightState.DAY -> DayNightState.NIGHT
                DayNightState.NIGHT -> DayNightState.DAY
            }
        )
    }
}