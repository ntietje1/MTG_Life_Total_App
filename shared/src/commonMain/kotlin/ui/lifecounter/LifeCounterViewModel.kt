package ui.lifecounter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.common.NumberWithRecentChange
import domain.game.PlayerCustomizationHost
import domain.game.PlayerCustomizationManager
import domain.game.timer.TimerManager
import domain.game.timer.TimerManagerHost
import domain.game.timer.TurnTimer
import domain.state.game.GameCommand
import domain.state.game.GameSession
import domain.state.game.GameSessionStore
import domain.state.profile.PlayerProfileRepository
import domain.storage.IFileImageStore
import domain.storage.PreferencesRepository
import domain.system.NotificationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Player
import model.Player.Companion.MAX_PLAYERS
import ui.dialog.MiddleButtonDialogState
import ui.dialog.planechase.PlaneChaseViewModel
import ui.lifecounter.playerbutton.CommanderState
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonState
import ui.lifecounter.playerbutton.PlayerButtonViewModel

open class LifeCounterViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val profileRepository: PlayerProfileRepository,
    private val fileImageStore: IFileImageStore,
    protected val notificationManager: NotificationManager,
    internal val playerCustomizationManager: PlayerCustomizationManager,
    private val planeChaseViewModel: PlaneChaseViewModel,
    private val gameSessionStore: GameSessionStore,
    internal val timerManager: TimerManager,
    initialState: LifeCounterState = LifeCounterState(),
) : ViewModel(), TimerManagerHost, PlayerCustomizationHost {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<LifeCounterState> = _state.asStateFlow()

    val numPlayers: StateFlow<Int> = preferencesRepository.numPlayers
    val alt4PlayerLayout: StateFlow<Boolean> = preferencesRepository.alt4PlayerLayout
    val turnTimerEnabled: StateFlow<Boolean> = preferencesRepository.turnTimer

    private val _playerButtonViewModels = MutableStateFlow<List<PlayerButtonViewModel>>(emptyList())
    val playerButtonViewModels: StateFlow<List<PlayerButtonViewModel>> = _playerButtonViewModels.asStateFlow()

    init {
        playerCustomizationManager.attach(this)
        timerManager.attach(this)

        generatePlayerButtonViewModels()

        viewModelScope.launch {
            timerManager.registerTimerStateObserver()
        }
        viewModelScope.launch {
            gameSessionStore.loadActiveSession()
            gameSessionStore.session.collect { session ->
                if (session != null) applyGameSession(session)
            }
        }
    }

    override fun onCleared() {
        savePlayerPrefs()
        playerCustomizationManager.detach()
        timerManager.detach()
    }

    override val playerCount: Int
        get() = preferencesRepository.numPlayers.value

    override val players: List<Player>
        get() = playerButtonViewModels.value.map { it.state.value.player }

    override fun isPlayerDead(index: Int): Boolean {
        return playerButtonViewModels.value.getOrNull(index)?.isDead?.value ?: false
    }

    override fun promptForFirstPlayer() {
        playerButtonViewModels.value.forEach { it.onFirstPlayerPrompt() }
    }

    override fun clearFirstPlayerPrompt() {
        playerButtonViewModels.value.forEach { playerButtonViewModel ->
            if (playerButtonViewModel.state.value.buttonState == PBState.SELECT_FIRST_PLAYER) {
                playerButtonViewModel.popBackStack()
            }
        }
    }

    override fun showTimer(activePlayerIndex: Int?, timer: TurnTimer?) {
        playerButtonViewModels.value.forEachIndexed { index, playerButtonViewModel ->
            val shouldShowTimer = index == activePlayerIndex
            playerButtonViewModel.setTimer(if (shouldShowTimer) timer else null)
        }
    }

    override fun replacePlayer(player: Player) {
        playerButtonViewModels.value
            .firstOrNull { it.state.value.player.playerNum == player.playerNum }
            ?.setPlayer(player)
    }


    // Generate viewmodels for all players and update the viewmodel list flow
    private fun generatePlayerButtonViewModels() {
        _playerButtonViewModels.value = (1..MAX_PLAYERS).map { playerNum ->
            generatePlayerButtonViewModel(generatePlayer(playerNum))
        }
    }

    private fun generatePlayer(playerNum: Int): Player {
        return Player(
            lifeTotal = NumberWithRecentChange(preferencesRepository.startingLife.value, 0),
            name = "P$playerNum",
            playerNum = playerNum
        )
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
        dispatchGameCommand(GameCommand.SetCommanderDealer(null))
    }

    // Return a viewmodel for a player button
    open fun generatePlayerButtonViewModel(player: Player): PlayerButtonViewModel {
        return PlayerButtonViewModel(
            initialState = PlayerButtonState(player),
            preferencesRepository = preferencesRepository,
            profileRepository = profileRepository,
            fileImageStore = fileImageStore,
            notificationManager = notificationManager,
            gameSessionStore = gameSessionStore,
            resetPlayerPrefs = playerCustomizationManager::resetPlayerPrefs,
            copyPlayerPrefs = playerCustomizationManager::copyPlayerPrefs,
            savePlayerPrefs = playerCustomizationManager::savePlayerPrefs,
            onFirstPlayerSelected = { index -> timerManager.handleFirstPlayerSelection(index) },
            onMoveTimerRequested = { timerManager.moveTimer() }
        )
    }

    fun savePlayerPrefs() {
        playerCustomizationManager.saveAllPlayerPrefs()
    }

    fun resetAllPrefs() {
        playerCustomizationManager.resetAllPlayerPrefs()
    }

    fun openModal(value: LifeCounterModal) {
        _state.value = _state.value.copy(modalStack = _state.value.modalStack.open(value))
    }

    fun replaceModal(value: LifeCounterModal) {
        _state.value = _state.value.copy(modalStack = _state.value.modalStack.replace(value))
    }

    fun closeModal() {
        _state.value = _state.value.copy(modalStack = LifeCounterModalStack.Empty)
    }

    fun goBackInModal() {
        _state.value = _state.value.copy(modalStack = _state.value.modalStack.goBack())
    }

    open fun setMiddleButtonDialogState(value: MiddleButtonDialogState?) {
        if (value == null) {
            closeModal()
        } else {
            replaceModal(value)
        }
    }

    private fun setAllButtonStates(pbState: PBState) {
        playerButtonViewModels.value.forEach { it.setPlayerButtonState(pbState) }
    }

    private fun applyGameSession(session: GameSession) {
        _state.value = GameSessionUiMapper.mapLifeCounterUiState(session, _state.value, fileImageStore)
        val commanderDealerPlayer = session.commanderMode?.let { mode ->
            GameSessionUiMapper.mapPlayerButtonState(
                session = session,
                seatId = mode.dealerSeatId,
                current = PlayerButtonState(Player(playerNum = mode.dealerSeatId.value.removePrefix("seat-").toInt())),
                fileImageStore = fileImageStore
            ).player.copy(partnerMode = mode.partnerMode)
        }
        playerButtonViewModels.value.forEach { playerButtonViewModel ->
            val seatId = GameSessionUiMapper.seatIdForPlayerNumber(
                playerButtonViewModel.state.value.player.playerNum
            )
            if (session.seats.any { seat -> seat.id == seatId }) {
                val mappedState = GameSessionUiMapper.mapPlayerButtonState(
                    session = session,
                    seatId = seatId,
                    current = playerButtonViewModel.state.value,
                    fileImageStore = fileImageStore
                )
                playerButtonViewModel.setPlayer(mappedState.player)
                when {
                    session.commanderMode != null -> {
                        playerButtonViewModel.setPlayerButtonState(
                            GameSessionUiMapper.mapCommanderButtonState(session, seatId)
                        )
                    }
                    playerButtonViewModel.state.value.buttonState in listOf(
                        PBState.COMMANDER_DEALER,
                        PBState.COMMANDER_RECEIVER
                    ) -> {
                        playerButtonViewModel.setPlayerButtonState(PBState.NORMAL)
                    }
                }
                playerButtonViewModel.setCommanderState(
                    commanderDealerPlayer?.let(CommanderState::Active) ?: CommanderState.Inactive
                )
            }
        }
    }

    private fun dispatchGameCommand(command: GameCommand) {
        viewModelScope.launch {
            gameSessionStore.dispatchLoaded(command)
        }
    }

    private fun showLoadingScreen(value: Boolean) {
        _state.value = _state.value.copy(showLoadingScreen = value)
    }

    open fun setNumPlayers(value: Int) {
        if (value < 1 || value > MAX_PLAYERS) throw IllegalArgumentException("Invalid number of players")
        preferencesRepository.setNumPlayers(value)
    }

    private fun restartButtons() {
        setShowButtons(false)
        viewModelScope.launch {
            delay(10)
            setShowButtons(true)
        }
    }

    fun resetGameState() {
        dispatchGameCommand(GameCommand.ResetGame)
        planeChaseViewModel.onResetGame()
        setAllButtonStates(PBState.NORMAL)
        viewModelScope.launch {
            timerManager.reset()
        }
        restartButtons()
    }

    fun toggleKeepScreenOn(value: Boolean? = null) {
        preferencesRepository.setKeepScreenOn(value ?: !preferencesRepository.keepScreenOn.value)
    }

    open fun toggleDarkTheme(value: Boolean? = null) {
        preferencesRepository.setDarkTheme(value ?: !preferencesRepository.darkTheme.value)
    }

    fun setAlt4PlayerLayout(value: Boolean) {
        preferencesRepository.setAlt4PlayerLayout(value)
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
        dispatchGameCommand(
            GameCommand.ChangeTableCounter(
                counter = GameSessionUiMapper.tableCounterForIndex(index),
                delta = value
            )
        )
    }

    fun resetCounters() {
        dispatchGameCommand(GameCommand.ResetTableCounters)
    }

    fun toggleDayNight() {
        dispatchGameCommand(GameCommand.ToggleDayNight)
    }
}
