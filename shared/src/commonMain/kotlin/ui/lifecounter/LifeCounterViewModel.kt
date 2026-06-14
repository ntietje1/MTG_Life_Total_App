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
import domain.state.game.SeatId
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
import ui.dialog.customization.CustomizationViewModel
import ui.dialog.planechase.PlaneChaseViewModel
import ui.lifecounter.playerbutton.CommanderState
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonAction
import ui.lifecounter.playerbutton.toGameCommands

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

    private val customizationViewModels = mutableMapOf<SeatId, CustomizationViewModel>()

    init {
        playerCustomizationManager.attach(this)
        timerManager.attach(this)

        initializePlayerSeats()

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
        get() = state.value.players.map { it.player }

    override fun isPlayerDead(index: Int): Boolean {
        return state.value.players.getOrNull(index)?.isDead ?: false
    }

    override fun promptForFirstPlayer() {
        _state.value = _state.value.promptForFirstPlayer()
    }

    override fun clearFirstPlayerPrompt() {
        _state.value = _state.value.clearFirstPlayerPrompt()
    }

    override fun showTimer(activePlayerIndex: Int?, timer: TurnTimer?) {
        _state.value = _state.value.showTimer(activePlayerIndex = activePlayerIndex, timer = timer)
    }

    override fun replacePlayer(player: Player) {
        _state.value = _state.value.replacePlayer(player)
    }


    private fun initializePlayerSeats() {
        if (_state.value.players.isNotEmpty()) return
        _state.value = _state.value.copy(
            players = (1..MAX_PLAYERS).map { playerNumber ->
                val player = generatePlayer(playerNumber)
                PlayerSeatUiState(
                    seatId = GameSessionUiMapper.seatIdForPlayerNumber(player.playerNum),
                    player = player
                )
            }
        )
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

    open fun onPlayerButtonAction(seatId: SeatId, action: PlayerButtonAction) {
        when (action) {
            PlayerButtonAction.ToggleSettings -> {
                _state.value = _state.value.openPlayerSettings(seatId)
            }
            PlayerButtonAction.PopBackStack -> {
                _state.value = _state.value.popPlayerButtonBackStack(seatId)
            }
            PlayerButtonAction.OpenCounters -> {
                _state.value = _state.value.openPlayerCounters(seatId)
            }
            PlayerButtonAction.OpenCounterSelection -> {
                _state.value = _state.value.openPlayerCounterSelection(seatId)
            }
            is PlayerButtonAction.SetManualDeath -> {
                dispatchPlayerButtonAction(seatId, action)
                _state.value = _state.value.closePlayerMenu(seatId)
            }
            PlayerButtonAction.ToggleCommanderDealer -> onCommanderButtonAction(seatId)
            PlayerButtonAction.SelectFirstPlayer -> {
                timerManager.handleFirstPlayerSelection(index = seatId.toPlayerIndex())
            }
            PlayerButtonAction.MoveTimer -> {
                timerManager.moveTimer()
            }
            PlayerButtonAction.OpenCustomization,
            PlayerButtonAction.CloseCustomization -> onPlayerCustomizationAction(seatId, action)
            else -> dispatchPlayerButtonAction(seatId, action)
        }
    }

    open fun customizationViewModelFor(seatId: SeatId): CustomizationViewModel? {
        return customizationViewModels[seatId]
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
        _state.value = _state.value.setAllPlayerButtonStates(pbState)
    }

    private fun applyGameSession(session: GameSession) {
        _state.value = GameSessionUiMapper.mapLifeCounterUiState(
            session = session,
            current = _state.value,
            fileImageStore = fileImageStore,
            autoKo = preferencesRepository.autoKo.value
        )
    }

    private fun dispatchGameCommand(command: GameCommand) {
        viewModelScope.launch {
            gameSessionStore.dispatchLoaded(command)
        }
    }

    private fun dispatchPlayerButtonAction(seatId: SeatId, action: PlayerButtonAction) {
        action.toGameCommands(
            seatId = seatId,
            commanderState = state.value.players.firstOrNull { player -> player.seatId == seatId }?.commanderState
                ?: CommanderState.Inactive
        ).forEach(::dispatchGameCommand)
    }

    private fun onCommanderButtonAction(seatId: SeatId) {
        when (state.value.players.firstOrNull { player -> player.seatId == seatId }?.buttonState) {
            PBState.NORMAL -> dispatchGameCommand(GameCommand.SetCommanderDealer(seatId))
            PBState.COMMANDER_DEALER -> dispatchGameCommand(GameCommand.SetCommanderDealer(null))
            else -> Unit
        }
    }

    private fun onPlayerCustomizationAction(seatId: SeatId, action: PlayerButtonAction) {
        when (action) {
            PlayerButtonAction.OpenCustomization -> {
                ensureCustomizationViewModel(seatId)
                _state.value = _state.value.openPlayerCustomization(seatId)
            }
            PlayerButtonAction.CloseCustomization -> applyCustomization(seatId)
            else -> Unit
        }
    }

    private fun ensureCustomizationViewModel(seatId: SeatId): CustomizationViewModel {
        return customizationViewModels.getOrPut(seatId) {
            createCustomizationViewModel(seatId = seatId, player = requirePlayer(seatId))
        }
    }

    protected open fun createCustomizationViewModel(seatId: SeatId, player: Player): CustomizationViewModel {
        return CustomizationViewModel(
            initialPlayer = player,
            fileImageStore = fileImageStore,
            profileRepository = profileRepository,
            preferencesRepository = preferencesRepository,
        )
    }

    private fun applyCustomization(seatId: SeatId) {
        val customizationViewModel = customizationViewModels[seatId] ?: return
        val player = customizationViewModel.state.value.player
        viewModelScope.launch {
            replacePlayer(playerCustomizationManager.copyPlayerPrefs(requirePlayer(seatId), player.copy(imageString = null)))
            delay(50)
            replacePlayer(playerCustomizationManager.copyPlayerPrefs(requirePlayer(seatId), player))
            customizationViewModels[seatId] = createCustomizationViewModel(seatId, requirePlayer(seatId))
            playerCustomizationManager.savePlayerPrefs(requirePlayer(seatId))
        }
        _state.value = _state.value.closePlayerCustomization(seatId)
    }

    private fun requirePlayer(seatId: SeatId): Player {
        return requireNotNull(state.value.players.firstOrNull { it.seatId == seatId }) {
            "Missing player for ${seatId.value}"
        }.player
    }

    private fun SeatId.toPlayerIndex(): Int {
        return value.removePrefix("seat-").toInt() - 1
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
