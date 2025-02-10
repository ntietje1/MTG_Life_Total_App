package ui.lifecounter

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.game.CommanderDamageManager
import domain.game.CommanderState
import domain.game.GameStateManager
import domain.game.PlayerCustomizationManager
import domain.game.PlayerStateManager
import domain.game.timer.TimerManager
import domain.storage.IImageManager
import domain.storage.ISettingsManager
import domain.system.NotificationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _playerButtonViewModels = MutableStateFlow<List<PlayerButtonViewModel>>(emptyList())
    val playerButtonViewModels: StateFlow<List<PlayerButtonViewModel>> = _playerButtonViewModels.asStateFlow()

    private var gameStateJob: Job? = null

    init {
        timerManager.attach(playerButtonViewModels)

        val initialGameWithPlayers = getOrGenerateCurrentGameWithPlayers()
        generatePlayerButtonViewModels(initialGameWithPlayers.players)
        setGame(initialGameWithPlayers.game)

        viewModelScope.launch {
            gameStateManager.attachMonarchyObserver(playerButtonViewModels)
            registerCommanderListener()
            timerManager.registerTimerStateObserver()
        }
    }

    private fun getOrGenerateCurrentGameWithPlayers(): GameWithPlayers {
        return try {
            settingsManager.currentGameId.value?.let { currentGameId ->
                gameStateManager.loadGameState(currentGameId)
            } ?: generateNewGame(samePlayers = false)
        } catch (e: Exception) {
            generateNewGame(samePlayers = false)
        }
    }

    private fun generateNewGame(samePlayers: Boolean): GameWithPlayers {
        val usedColors = mutableSetOf<Color>()
        return gameStateManager.newGame(numPlayers = settingsManager.defaultNumPlayers.value) { playerNum ->
            if (samePlayers) {
                commanderManager.resetCommanderDamage(
                    playerStateManager.resetPlayerState(
                        playerButtonViewModels.value[playerNum - 1].state.value.player
                    )
                )
            } else
                playerCustomizationManager.resetPlayerPrefs(
                    player = playerStateManager.generatePlayer(playerNum = playerNum),
                    usedColors = usedColors
                ).also { usedColors += it.color }
        }
    }

    private fun generatePlayerButtonViewModels(players: List<Player>) {
        _playerButtonViewModels.value = List(MAX_PLAYERS) { i ->
            generatePlayerButtonViewModel(players[i])
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameStateJob?.cancel()
        commanderManager.onClear()
        timerManager.detach()
        playerStateManager.onClear()
    }

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

    fun setFirstPlayer(index: Int?) {
        timerManager.handleFirstPlayerSelection(index)
    }

    fun onNavigate(firstNavigation: Boolean) {
//        if (settingsManager.loadPlayerStates().isEmpty()) generatePlayerButtonViewModels()
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

    open fun setAltPlayerLayout(value: Boolean) {
        setGame(state.value.game.copy(altPlayerLayout = value))
        settingsManager.setDefaultAltPlayerLayout(value)
        saveGameState()
    }

    open fun setNumPlayers(value: Int) {
        if (value < 1 || value > MAX_PLAYERS) throw IllegalArgumentException("Invalid number of players")
        settingsManager.setDefaultNumPlayers(value)
        setGame(state.value.game.copy(numPlayers = value))
        saveGameState()
    }

    private fun setGame(game: Game) {
        _state.value = _state.value.copy(game = game)
    }

    fun restartButtons() {
        setShowButtons(false)
        viewModelScope.launch {
            delay(10)
            setShowButtons(true)
        }
    }

    fun onResetGame(samePlayers: Boolean) {
        setAllButtonStates(PBState.NORMAL)
        setMiddleButtonState(MiddleButtonState.DEFAULT)
        setMiddleButtonDialogState(null)
        timerManager.reset()
        planeChaseViewModel.onResetGame()
        resetCounters()
        val gameWithPlayers = generateNewGame(samePlayers)
        generatePlayerButtonViewModels(gameWithPlayers.players)
        setGame(gameWithPlayers.game)
        restartButtons()
    }

    private fun setShowButtons(value: Boolean) {
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

    fun setDayNight(value: DayNightState) {
        _state.value = _state.value.copy(game = state.value.game.copy(dayNightState = value))
        saveGameState()
    }

    fun toggleDayNight() {
        val newState = when (state.value.game.dayNightState) {
            DayNightState.NONE -> DayNightState.DAY
            DayNightState.DAY -> DayNightState.NIGHT
            DayNightState.NIGHT -> DayNightState.DAY
        }
        setDayNight(newState)
        saveGameState()
    }

    private fun saveGameState() {
        gameStateManager.saveGame(state.value.game)}
}