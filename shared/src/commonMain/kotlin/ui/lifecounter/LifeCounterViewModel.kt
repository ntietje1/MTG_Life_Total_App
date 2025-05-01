package ui.lifecounter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.game.CommanderDamageManager
import domain.game.CommanderState
import domain.game.GameStateManager
import domain.game.PlayerCustomizationManager
import domain.game.PlayerStateManager
import domain.game.getPlayer
import domain.game.timer.TimerManager
import domain.storage.IImageManager
import domain.storage.ISettingsManager
import domain.system.NotificationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import model.DayNightState
import model.Game
import model.GameWithPlayers
import model.Player
import model.Player.Companion.MAX_PLAYERS
import ui.dialog.COUNTER_DIALOG_ENTRIES
import ui.dialog.MiddleButtonDialogState
import ui.dialog.planechase.PlaneChaseViewModel
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonState
import ui.lifecounter.playerbutton.PlayerButtonViewModel

open class LifeCounterViewModel(
    private val settingsManager: ISettingsManager,
    internal val playerStateManager: PlayerStateManager,
    internal val commanderManager: CommanderDamageManager,
    private val imageManager: IImageManager,
    protected val notificationManager: NotificationManager,
    internal val playerCustomizationManager: PlayerCustomizationManager,
    private val planeChaseViewModel: PlaneChaseViewModel,
    internal val gameStateManager: GameStateManager,
    internal val timerManager: TimerManager,
    initialState: LifeCounterState = LifeCounterState(),
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<LifeCounterState> = _state.asStateFlow()

    //    val numPlayers: StateFlow<Int> = settingsManager.defaultNumPlayers
//    val alt4PlayerLayout: StateFlow<Boolean> = settingsManager.defaultAltPlayerLayout
    val turnTimerEnabled: StateFlow<Boolean> = settingsManager.turnTimer

    private val _gameState = MutableStateFlow(gameStateManager.gameState.value)
    val gameState: StateFlow<Game?> = _gameState.asStateFlow()

    private val _playerButtonViewModels = MutableStateFlow<List<PlayerButtonViewModel>>(emptyList())
    val playerButtonViewModels: StateFlow<List<PlayerButtonViewModel>> = _playerButtonViewModels.asStateFlow()

    init {
        playerCustomizationManager.attach(playerButtonViewModels)
        commanderManager.attach(playerButtonViewModels)
        playerStateManager.attach(playerButtonViewModels)
        timerManager.attach(playerButtonViewModels)

        // load current game or generate a new game if there isn't one (i.e. first time opening the app or data was cleared)
        var currentGameWithPlayers = gameStateManager.loadCurrentGameState()
        if (currentGameWithPlayers == null) {
            currentGameWithPlayers = generateNewGame(samePlayers = false)
        }
        gameStateManager.init(currentGameWithPlayers.game)
        generatePlayerButtonsViewModels(currentGameWithPlayers.players)
        observeGameState()

        viewModelScope.launch {
            registerCommanderListener()
            timerManager.registerTimerEnabledObserver()
        }
    }

    private fun observeGameState() {
        viewModelScope.launch {
            gameStateManager.gameState.map { it }.collect { game ->
                _gameState.value = game
            }
        }
    }

    private fun generateNewGame(samePlayers: Boolean): GameWithPlayers {
        val newGame = if (samePlayers) {
            gameStateManager.newGame {
                commanderManager.resetCommanderDamage(
                    playerStateManager.resetPlayerState(
                        playerButtonViewModels.value.getPlayer(it)
                    )
                )
            }
        } else {
            gameStateManager.newGame {
                playerCustomizationManager.resetPlayerPrefs(
                    commanderManager.resetCommanderDamage(
                        playerStateManager.resetPlayerState(
                            playerStateManager.generatePlayer(playerNum = it)
                        )
                    )
                )
            }
        }
        settingsManager.setCurrentGameId(newGame.game.id)
        return newGame
    }

    override fun onCleared() {
        playerCustomizationManager.detach()
        commanderManager.detach()
        timerManager.detach()
        playerStateManager.detach()
    }

    /**
     * Register the listener that updates player button states when a player is dealing commander damage
     */
    private suspend fun registerCommanderListener() {
        commanderManager.commanderState.collect { commanderState ->
            when (commanderState) {
                is CommanderState.Inactive -> {
                    setAllButtonStates(PBState.NORMAL)
                    setMiddleButtonState(MiddleButtonState.DEFAULT)
                }

                is CommanderState.Active -> {
                    setMiddleButtonState(MiddleButtonState.COMMANDER_EXIT)
                    playerButtonViewModels.value.forEach {
                        it.setPlayerButtonState(
                            if (it.state.value.player.playerNum == commanderState.dealer.playerNum) {
                                PBState.COMMANDER_DEALER
                            } else {
                                PBState.COMMANDER_RECEIVER
                            }
                        )
                    }
                }
            }
        }
    }

    fun setTimerEnabled(value: Boolean) {
        viewModelScope.launch {
            timerManager.onTimerEnabledChange(value)
        }
    }

    fun onNavigate(firstNavigation: Boolean) {
        if (firstNavigation) { //TODO: show a progress bar that says "loading game" or something
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
        viewModelScope.launch {
            timerManager.registerTimerEnabledObserver()
        }
    }

    private fun setMiddleButtonState(value: MiddleButtonState) {
        _state.value = _state.value.copy(middleButtonState = value)
    }

    fun onCommanderDealerButtonClicked() {
        commanderManager.setCurrentDealer(null)
        setAllButtonStates(PBState.NORMAL)
    }

    private fun generatePlayerButtonsViewModels(players: List<Player>) {
        for (i in 0 until MAX_PLAYERS) {
            val player = players[i]
            if (_playerButtonViewModels.value.size <= i) _playerButtonViewModels.value += generatePlayerButtonViewModel(player)
            else _playerButtonViewModels.value[i].setPlayer(player)
        }
    }

    // Return a viewmodel for a player button
    open fun generatePlayerButtonViewModel(player: Player): PlayerButtonViewModel {
        println("Generating view model for player: $player")
        return PlayerButtonViewModel(
            initialState = PlayerButtonState(player),
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

    open fun setMiddleButtonDialogState(value: MiddleButtonDialogState?) {
        _state.value = _state.value.copy(middleButtonDialogState = value)
    }

    private fun setAllButtonStates(pbState: PBState) {
        playerButtonViewModels.value.forEach { it.setPlayerButtonState(pbState) }
    }

    private fun showLoadingScreen(value: Boolean) {
        _state.value = _state.value.copy(showLoadingScreen = value)
    }

    fun savePlayerPrefs() = playerCustomizationManager.saveAllPlayerPrefs()
    fun resetAllPrefs() = playerCustomizationManager.resetAllPlayerPrefs()

    open fun setNumPlayers(value: Int) = gameStateManager.setNumPlayers(value)
    fun setAltPlayerLayout(value: Boolean) = gameStateManager.setAltPlayerLayout(value)

    fun toggleDayNight() = gameStateManager.toggleDayNight()
    fun resetDayNight() = gameStateManager.setDayNight(DayNightState.NONE)


    private fun restartButtons() {
        setShowButtons(false)
        viewModelScope.launch {
            delay(10)
            setShowButtons(true)
        }
    }

    fun onResetGame(samePlayers: Boolean) {
        val currentGameWithPlayers = generateNewGame(samePlayers = samePlayers)
        gameStateManager.init(currentGameWithPlayers.game)
        generatePlayerButtonsViewModels(currentGameWithPlayers.players)
        resetCounters()
        planeChaseViewModel.onResetGame()
        viewModelScope.launch {
            timerManager.onTimerEnabledChange()
        }
        restartButtons()
    }

    fun toggleKeepScreenOn(value: Boolean? = null) = settingsManager.setKeepScreenOn(value ?: !settingsManager.keepScreenOn.value)
    open fun toggleDarkTheme(value: Boolean? = null) = settingsManager.setDarkTheme(value ?: !settingsManager.darkTheme.value)


    fun setShowButtons(value: Boolean) {
        _state.value = _state.value.copy(showButtons = value)
    }

    fun setBlurBackground(value: Boolean) {
        _state.value = _state.value.copy(blurBackground = value)
    }

    fun incrementCounter(index: Int, value: Int) {
        _state.value = _state.value.copy(counters = _state.value.counters.toMutableList().apply {
            set(index, _state.value.counters[index] + value)
        }.toList())
    }

    fun resetCounters() {
        _state.value = _state.value.copy(counters = List(COUNTER_DIALOG_ENTRIES) { 0 })
    }

}