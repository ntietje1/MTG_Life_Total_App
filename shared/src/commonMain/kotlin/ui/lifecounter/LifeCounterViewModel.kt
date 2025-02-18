package ui.lifecounter

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.game.CommanderDamageManager
import domain.game.CommanderState
import domain.game.timer.TimerManager
import domain.state.game.MonarchyState
import domain.storage.ISettingsStore
import domain.system.NotificationManager
import domain.usecase.game.LoadGameStateUseCase
import domain.usecase.game.NewGameUseCase
import domain.usecase.game.SaveGameUseCase
import domain.usecase.player.NewPlayerUseCase
import domain.usecase.player.state.ManagePlayerStateUseCase
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import ui.dialog.COUNTER_DIALOG_ENTRIES
import ui.dialog.MiddleButtonDialogState
import ui.dialog.planechase.PlaneChaseViewModel
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonState
import ui.lifecounter.playerbutton.PlayerButtonViewModel

open class LifeCounterViewModel(
    private val settingsManager: ISettingsStore,
    internal val commanderManager: CommanderDamageManager,
    protected val notificationManager: NotificationManager,
    private val monarchyState: MonarchyState,
    private val planeChaseViewModel: PlaneChaseViewModel,
    private val newGameUseCase: NewGameUseCase,
    private val saveGameUseCase: SaveGameUseCase,
    private val loadGameStateUseCase: LoadGameStateUseCase,
    private val managePlayerStateUseCase: ManagePlayerStateUseCase,
    private val newPlayerUseCase: NewPlayerUseCase,
    internal val timerManager: TimerManager,
    initialState: LifeCounterState = LifeCounterState(),
) : ViewModel(), KoinComponent {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<LifeCounterState> = _state.asStateFlow()

    private val _playerButtonViewModels = MutableStateFlow<List<PlayerButtonViewModel>>(emptyList())
    val playerButtonViewModels: StateFlow<List<PlayerButtonViewModel>> = _playerButtonViewModels.asStateFlow()

    private var gameStateJob: Job? = null

    init {
        getOrGenerateCurrentGameWithPlayers()
        init()
    }

    private fun init() {
        setAllButtonStates(PBState.NORMAL)
        setMiddleButtonState(MiddleButtonState.DEFAULT)
        setMiddleButtonDialogState(null)

        monarchyState.registerSaveCallback {
            _state.value = _state.value.copy(game = state.value.game.copy(monarchyPlayerNum = monarchyState.currentMonarchyState.value?.toLong()))
            saveGameUseCase.saveMonarchy(_state.value.game.id, _state.value.game.monarchyPlayerNum?.toInt())
            println("Monarchy state saved: gameid = ${state.value.game.id} player ${state.value.game.monarchyPlayerNum}")
        }
        timerManager.attach(playerButtonViewModels)
    }

    private fun getOrGenerateCurrentGameWithPlayers() {
        try {
            settingsManager.currentGameId.value?.let { currentGameId ->
                loadGameStateUseCase(currentGameId).also {
                    println("Loaded game with id $currentGameId, monarchy player num: ${it.game.monarchyPlayerNum}")
                }
            } ?: generateNewGame(samePlayers = false)
        } catch (e: Exception) {
            generateNewGame(samePlayers = false)
        }.also {
            generatePlayerButtonViewModels(it.players)
            setGame(it.game)
            monarchyState.setMonarchState(it.game.monarchyPlayerNum?.toInt())
        }
    }

    private fun generateNewGame(samePlayers: Boolean): GameWithPlayers {
        val usedColors = mutableSetOf<Color>()
        val gameWithPlayers = newGameUseCase { playerNum ->
            if (samePlayers) {
                commanderManager.resetCommanderDamage( //TODO: remove this when this becomes part of managerPlayerStateUseCase
                    managePlayerStateUseCase.resetPlayerState(
                        playerButtonViewModels.value[playerNum - 1].state.value.player
                    )
                )
            } else {
                newPlayerUseCase.invoke(playerNum = playerNum, usedColors = usedColors).also { usedColors += it.color }
            }
        }
        return gameWithPlayers
    }

    fun onResetGame(samePlayers: Boolean) {
        timerManager.reset()
        planeChaseViewModel.onResetGame()
        resetCounters()
        generateNewGame(samePlayers).also {
            generatePlayerButtonViewModels(it.players)
            setGame(it.game)
        }
        init()
        restartButtons()
    }

    private fun generatePlayerButtonViewModels(players: List<Player>) {
        _playerButtonViewModels.value = List(MAX_PLAYERS) { i ->
            if (_playerButtonViewModels.value.getOrNull(i) != null) {
                println("Modifying existing player button view model: ${players[i]}")
                _playerButtonViewModels.value[i].apply {
                    resetPlayer(players[i])
                }
            } else {
                println("Creating new player button view model: ${players[i]}")
                generatePlayerButtonViewModel(players[i])
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameStateJob?.cancel()
        commanderManager.onClear()
        timerManager.detach()
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
        val initialState = PlayerButtonState(player)
        return get<PlayerButtonViewModel> { parametersOf(initialState) }
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
        saveGameUseCase(state.value.game)
    }

    open fun setNumPlayers(value: Int) {
        if (value < 1 || value > MAX_PLAYERS) throw IllegalArgumentException("Invalid number of players")
        settingsManager.setDefaultNumPlayers(value)
        setGame(state.value.game.copy(numPlayers = value))
        saveGameUseCase(state.value.game)
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
        saveGameUseCase(state.value.game)
    }

    fun toggleDayNight() {
        val newState = when (state.value.game.dayNightState) {
            DayNightState.NONE -> DayNightState.DAY
            DayNightState.DAY -> DayNightState.NIGHT
            DayNightState.NIGHT -> DayNightState.DAY
        }
        setDayNight(newState)
        saveGameUseCase(state.value.game)
    }
}