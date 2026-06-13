package ui.lifecounter.playerbutton

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.common.Backstack
import domain.common.NumberWithRecentChange
import domain.game.timer.TurnTimer
import domain.state.game.GameCommand
import domain.state.game.GameSessionStore
import domain.state.profile.PlayerProfileRepository
import domain.storage.IFileImageStore
import domain.storage.PreferencesRepository
import domain.system.NotificationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import model.Player
import ui.dialog.customization.CustomizationViewModel
import ui.lifecounter.CounterType
import ui.lifecounter.GameSessionUiMapper

open class PlayerButtonViewModel(
    initialState: PlayerButtonState,
    private val preferencesRepository: PreferencesRepository,
    private val profileRepository: PlayerProfileRepository,
    private val fileImageStore: IFileImageStore,
    protected val notificationManager: NotificationManager,
    private val gameSessionStore: GameSessionStore,
    private val resetPlayerPrefs: (Player) -> Player,
    private val copyPlayerPrefs: (Player, Player) -> Player,
    private val savePlayerPrefs: (Player) -> Unit,
    private val onFirstPlayerSelected: (Int) -> Unit,
    private val onMoveTimerRequested: () -> Unit
) : ViewModel() {
    private var _state = MutableStateFlow(initialState)
    val state: StateFlow<PlayerButtonState> = _state.asStateFlow()

    val isDead: StateFlow<Boolean> = combine(
        preferencesRepository.autoKo, state
    ) { autoKo, playerState ->
        playerState.player.setDead ||
            (autoKo && (playerState.player.life <= 0 || playerState.player.commanderDamage.any { it.number >= 21 }))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _commanderState = MutableStateFlow<CommanderState>(CommanderState.Inactive)
    val commanderState: StateFlow<CommanderState> = _commanderState.asStateFlow()

    private val backstack = Backstack()

    val showBackButton: StateFlow<Boolean> = combine(
        backstack.isEmpty, state
    ) { isEmpty, state ->
        state.buttonState !in listOf(
            PBState.SELECT_FIRST_PLAYER, PBState.COMMANDER_RECEIVER, PBState.COMMANDER_DEALER
        ) && !isEmpty
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private var _customizationViewmodel: CustomizationViewModel? = null
    open val customizationViewmodel: CustomizationViewModel?
        get() = _customizationViewmodel

    internal fun setPlayer(player: Player) {
        _state.value = state.value.copy(player = player)
    }

    internal fun setCommanderState(commanderState: CommanderState) {
        _commanderState.value = commanderState
    }

    fun onAction(action: PlayerButtonAction) {
        when (action) {
            PlayerButtonAction.IncrementLife -> incrementLife(1)
            PlayerButtonAction.DecrementLife -> incrementLife(-1)
            is PlayerButtonAction.IncrementCommanderDamage -> incrementCommanderDamage(value = 1, partner = action.partner)
            is PlayerButtonAction.DecrementCommanderDamage -> incrementCommanderDamage(value = -1, partner = action.partner)
            is PlayerButtonAction.SetMonarch -> onMonarchyButtonClicked(action.monarch)
            is PlayerButtonAction.SetManualDeath -> {
                dispatchPlayerButtonAction(action)
                closeSettingsMenu()
                backstack.clear()
            }
            is PlayerButtonAction.SetCommanderPartnerMode -> togglePartnerMode(action.enabled)
            is PlayerButtonAction.ChangeCounter -> incrementCounterValue(action.counter, action.delta)
            is PlayerButtonAction.SetCounterActive -> setActiveCounter(action.counter, action.active)
            PlayerButtonAction.ToggleCommanderDealer -> onCommanderButtonClicked()
            PlayerButtonAction.ToggleSettings -> onSettingsButtonClicked()
            PlayerButtonAction.PopBackStack -> popBackStack()
            PlayerButtonAction.OpenCounters -> onCountersButtonClicked()
            PlayerButtonAction.OpenCounterSelection -> onAddCounterButtonClicked()
            PlayerButtonAction.OpenCustomization -> onShowCustomizeMenu(true)
            PlayerButtonAction.CloseCustomization -> onShowCustomizeMenu(false)
            PlayerButtonAction.SelectFirstPlayer -> setFirstPlayer()
            PlayerButtonAction.MoveTimer -> onMoveTimer()
        }
    }

    open fun incrementLife(value: Int) {
        when (value) {
            1 -> dispatchPlayerButtonAction(PlayerButtonAction.IncrementLife)
            -1 -> dispatchPlayerButtonAction(PlayerButtonAction.DecrementLife)
            else -> dispatchGameCommand(GameCommand.ChangeLife(seatId, value))
        }
    }

    fun setTimer(timer: TurnTimer?) {
        _state.value = state.value.copy(timer = timer)
    }

    fun setFirstPlayer() {
        onFirstPlayerSelected(state.value.player.playerNum - 1)
    }

    fun setPlayerButtonState(buttonState: PBState) {
        if (buttonState == PBState.COMMANDER_RECEIVER) {
            backstack.clear()
        }

        _state.value = state.value.copy(buttonState = buttonState)
    }

    fun onMoveTimer() {
        onMoveTimerRequested()
    }

    open fun onMonarchyButtonClicked(value: Boolean) {
        dispatchPlayerButtonAction(PlayerButtonAction.SetMonarch(value))
    }

    open fun onCommanderButtonClicked() {
        when (state.value.buttonState) {
            PBState.NORMAL -> {
                dispatchGameCommand(GameCommand.SetCommanderDealer(seatId))
            }

            PBState.COMMANDER_DEALER -> {
                dispatchGameCommand(GameCommand.SetCommanderDealer(null))
            }

            else -> {} // do nothing
        }
    }

    open fun onSettingsButtonClicked() {
        if (state.value.buttonState == PBState.NORMAL) {
            setPlayerButtonState(PBState.SETTINGS)
            backstack.push { setPlayerButtonState(PBState.NORMAL) }
        } else {
            closeSettingsMenu()
        }
    }

    open fun onKOButtonClicked() {
        dispatchPlayerButtonAction(PlayerButtonAction.SetManualDeath(!state.value.player.setDead))
        closeSettingsMenu()
        backstack.clear()
    }

    open fun popBackStack() {
        if (backstack.isEmpty.value) return
        backstack.pop().invoke()
    }

    private fun closeSettingsMenu() {
        setPlayerButtonState(PBState.NORMAL)
        backstack.clear()
    }

    fun resetPlayerPref() {
        setPlayer(resetPlayerPrefs(state.value.player))
        resetCustomizationMenuViewModel()
    }

    fun savePlayerPref() {
        savePlayerPrefs(state.value.player)
    }

    fun getCounterValue(counterType: CounterType): Int {
        return state.value.player.counters[counterType.ordinal]
    }

    private fun resetCustomizationMenuViewModel() {
        _customizationViewmodel = CustomizationViewModel(
            initialPlayer = state.value.player,
            fileImageStore = fileImageStore,
            profileRepository = profileRepository,
            preferencesRepository = preferencesRepository,
        )
    }

    private fun onCustomizationApply() {
        val customizationViewmodel = requireNotNull(customizationViewmodel)
        val player = customizationViewmodel.state.value.player
        viewModelScope.launch {
            copyPrefs(player.copy(imageString = null))
            delay(50)
            copyPrefs(player)
            resetCustomizationMenuViewModel()
            savePlayerPrefs(state.value.player)
        }
    }

    open fun onShowCustomizeMenu(value: Boolean) {
        if (value && customizationViewmodel == null) {
            resetCustomizationMenuViewModel()
        }
        if (!value) {
            onCustomizationApply()
            setPlayerButtonState(PBState.NORMAL)
            backstack.clear()
        }
        _state.value = state.value.copy(showCustomizeMenu = value)
    }


    fun onFirstPlayerPrompt() {
        backstack.push { setPlayerButtonState(PBState.NORMAL) }
        setPlayerButtonState(PBState.SELECT_FIRST_PLAYER)
    }

    open fun onCountersButtonClicked() {
        setPlayerButtonState(PBState.COUNTERS_VIEW)
        backstack.push { setPlayerButtonState(PBState.SETTINGS) }
    }

    open fun onAddCounterButtonClicked() {
        setPlayerButtonState(PBState.COUNTERS_SELECT)
        backstack.push { setPlayerButtonState(PBState.COUNTERS_VIEW) }
    }

    fun togglePartnerMode(value: Boolean) {
        dispatchGameCommand(GameCommand.SetCommanderPartnerMode(value))
    }

    fun incrementCounterValue(counterType: CounterType, value: Int) {
        dispatchGameCommand(
            GameCommand.ChangeSeatCounter(
                seatId = seatId,
                counter = GameSessionUiMapper.domainCounterFor(counterType),
                delta = value
            )
        )
    }

    fun setActiveCounter(counterType: CounterType, active: Boolean): Boolean {
        dispatchGameCommand(
            GameCommand.SetSeatCounterActive(
                seatId = seatId,
                counter = GameSessionUiMapper.domainCounterFor(counterType),
                active = active
            )
        )
        return active
    }

    fun getCommanderDamage(partner: Boolean): NumberWithRecentChange {
        return when (val state = commanderState.value) {
            is CommanderState.Active -> this.state.value.player.commanderDamage[state.getDealerIndex(partner)]
            CommanderState.Inactive -> NumberWithRecentChange(0, 0)
        }
    }

    open fun incrementCommanderDamage(value: Int, partner: Boolean) {
        when (value) {
            1 -> dispatchPlayerButtonAction(PlayerButtonAction.IncrementCommanderDamage(partner))
            -1 -> dispatchPlayerButtonAction(PlayerButtonAction.DecrementCommanderDamage(partner))
            else -> dispatchCommanderDamage(value = value, partner = partner)
        }
    }

    open fun copyPrefs(other: Player) {
        setPlayer(copyPlayerPrefs(state.value.player, other))
    }

    private val seatId get() = GameSessionUiMapper.seatIdForPlayerNumber(state.value.player.playerNum)

    private fun dispatchPlayerButtonAction(action: PlayerButtonAction) {
        action.toGameCommands(seatId = seatId, commanderState = commanderState.value)
            .forEach(::dispatchGameCommand)
    }

    private fun dispatchCommanderDamage(value: Int, partner: Boolean) {
        val activeCommander = commanderState.value as? CommanderState.Active ?: return
        dispatchGameCommand(
            GameCommand.ChangeCommanderDamage(
                dealerSeatId = GameSessionUiMapper.seatIdForPlayerNumber(activeCommander.dealer.playerNum),
                receiverSeatId = seatId,
                partner = partner,
                delta = value
            )
        )
    }

    private fun dispatchGameCommand(command: GameCommand) {
        viewModelScope.launch {
            gameSessionStore.dispatchLoaded(command)
        }
    }
}
