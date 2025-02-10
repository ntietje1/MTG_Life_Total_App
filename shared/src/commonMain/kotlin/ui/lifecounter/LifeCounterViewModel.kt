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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    val numPlayers: StateFlow<Int> = settingsManager.numPlayers
    val alt4PlayerLayout: StateFlow<Boolean> = settingsManager.alt4PlayerLayout
    val turnTimerEnabled: StateFlow<Boolean> = settingsManager.turnTimer

    private val _playerButtonViewModels = MutableStateFlow<List<PlayerButtonViewModel>>(emptyList())
    val playerButtonViewModels: StateFlow<List<PlayerButtonViewModel>> = _playerButtonViewModels.asStateFlow()

//    private val _currentGame = MutableStateFlow<Game>(Game())

    init {
        playerCustomizationManager.attach(playerButtonViewModels)
        commanderManager.attach(playerButtonViewModels)
        playerStateManager.attach(playerButtonViewModels)
        timerManager.attach(playerButtonViewModels)


        println("HERE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

        val gameWithPlayers = getOrGenerateCurrentGameWithPlayers()
        onGameWithPlayersChange(gameWithPlayers)


        viewModelScope.launch {
            registerCommanderListener()
            timerManager.registerTimerStateObserver()
        }
    }

    private fun getOrGenerateCurrentGameWithPlayers(): GameWithPlayers {
        return settingsManager.currentGameId.value?.let { currentGameId ->
            gameStateManager.loadGameState(currentGameId)
        } ?: generateNewGame(samePlayers = false)
    }

    private fun generateNewGame(samePlayers: Boolean): GameWithPlayers {
        val usedColors = mutableSetOf<Color>()
        return gameStateManager.newGame(numPlayers = settingsManager.numPlayers.value) { playerNum ->
            if (samePlayers) {
                playerStateManager.resetPlayerState(
                    playerButtonViewModels.value[playerNum - 1].state.value.player
                )
            } else
                playerCustomizationManager.resetPlayerPrefs(
                    player = playerStateManager.generatePlayer(playerNum = playerNum),
                    usedColors = usedColors
                ).also { usedColors += it.color }
        }
    }

    private fun getGameWithPlayers(): GameWithPlayers { //TODO: this will eventually be removed
        return GameWithPlayers(
            game = Game(
                id = settingsManager.currentGameId.value ?: -1,
                numPlayers = settingsManager.numPlayers.value
            ),
            players = playerButtonViewModels.value.map { it.state.value.player }
        )
    }

    private fun onGameWithPlayersChange(gameWithPlayers: GameWithPlayers) {
        val players = gameWithPlayers.players
        val game = gameWithPlayers.game
        for (i in 0 until MAX_PLAYERS) {
            if (_playerButtonViewModels.value.size <= i) _playerButtonViewModels.value += generatePlayerButtonViewModel(players[i])
            else _playerButtonViewModels.value[i].setPlayer(players[i])
        }
        setNumPlayers(game.numPlayers)
    }

    override fun onCleared() {
//        savePlayerPrefs()
//        savePlayerStates()
        playerCustomizationManager.detach()
        commanderManager.detach()
        timerManager.detach()
        playerStateManager.detach()
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

//    // Generate viewmodels for all players and update the viewmodel list flow
//    private fun generatePlayerButtonViewModels() {
//        val savedPlayers = gameStateManager.getAllPlayers().toMutableList()
//        println("Saved players: $savedPlayers")
//        println("saved players length: ${savedPlayers.size}")
//
//
////        val savedPlayers = settingsManager.loadPlayerStates().toMutableList()
//        _playerButtonViewModels.value = savedPlayers.map { generatePlayerButtonViewModel(it) }.toMutableList()
//        println("Player button viewmodels: ${_playerButtonViewModels.value}")
//        while (savedPlayers.size < MAX_PLAYERS) {
//            val newPlayer = playerCustomizationManager.resetPlayerPrefs(
//                playerStateManager.generatePlayer(playerNum = savedPlayers.size + 1)
//            )
//            savedPlayers += newPlayer
//            _playerButtonViewModels.value += generatePlayerButtonViewModel(newPlayer)
//        }
//    }

//    private suspend fun generateInitialPlayers() {
////        val players = mutableListOf<Player>()
//        for (i in 0 until MAX_PLAYERS) {
//            val newPlayer = playerCustomizationManager.resetPlayerPrefs(
//                playerStateManager.generatePlayer(playerNum = i + 1)
//            )
////            players.add(newPlayer)
//            gameStateManager.savePlayerState(newPlayer)
//        }
//    }

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

//    fun savePlayerPrefs() {
//        playerCustomizationManager.saveAllPlayerPrefs()
//    }

//    fun savePlayerStates() {
//        gameStateManager.saveGameState(
//            GameWithPlayers(
//                game = Game(
//                    id = settingsManager.currentGameId.value ?: 0,
//                    numPlayers = numPlayers.value
//                ),
//                players = playerButtonViewModels.value.map { it.state.value.player }
//            )
//        )
//    }

//    fun resetAllPrefs() {
//        playerCustomizationManager.resetAllPlayerPrefs()
//    }

    open fun setMiddleButtonDialogState(value: MiddleButtonDialogState?) {
        _state.value = _state.value.copy(middleButtonDialogState = value)
    }

    private fun setAllButtonStates(pbState: PBState) {
        playerButtonViewModels.value.forEach { it.setPlayerButtonState(pbState) }
    }

    protected fun setMonarchy(targetPlayerNum: Int, value: Boolean) {
        gameStateManager.setMonarchy(targetPlayerNum, value)
//        savePlayerStates()
    }

    private fun showLoadingScreen(value: Boolean) {
        _state.value = _state.value.copy(showLoadingScreen = value)
    }

    open fun setNumPlayers(value: Int) {
        if (value < 1 || value > MAX_PLAYERS) throw IllegalArgumentException("Invalid number of players")
        settingsManager.setNumPlayers(value)
        gameStateManager.saveGameState(getGameWithPlayers())
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
        onGameWithPlayersChange(gameWithPlayers)
        restartButtons()
    }

    private fun setShowButtons(value: Boolean) {
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