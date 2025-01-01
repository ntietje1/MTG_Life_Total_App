package ui.lifecounter.playerbutton

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.IImageManager
import data.ISettingsManager
import data.Player
import di.NotificationManager
import domain.common.Backstack
import domain.game.GameStateManager
import domain.player.CommanderDamageManager
import domain.player.PlayerCustomizationManager
import domain.player.PlayerStateManager
import domain.player.PlayerStateManager.Companion.RECENT_CHANGE_DELAY
import domain.timer.TimerManager
import domain.timer.TurnTimer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ui.dialog.customization.CustomizationViewModel
import ui.lifecounter.CounterType

open class PlayerButtonViewModel(
    initialState: PlayerButtonState,
    private val settingsManager: ISettingsManager,
    private val imageManager: IImageManager,
    private val commanderManager: CommanderDamageManager,
    protected val notificationManager: NotificationManager,
    private val playerStateManager: PlayerStateManager,
    private val playerCustomizationManager: PlayerCustomizationManager,
    private val gameStateManager: GameStateManager,
    private val timerManager: TimerManager
) : ViewModel() {
    private var _state = MutableStateFlow(initialState)
    val state: StateFlow<PlayerButtonState> = _state.asStateFlow()

    val isDead: StateFlow<Boolean> = combine(
        settingsManager.autoKo,
        state
    ) { autoKo, playerState ->
        playerStateManager.isPlayerDead(playerState.player, autoKo)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentDealer: StateFlow<Player?> = commanderManager.currentDealer

    private var recentChangeJob: Job? = null

    private val backstack = Backstack()

    val showBackButton: StateFlow<Boolean> = combine(
        backstack.isEmpty,
        state
    ) { isEmpty, state ->
        state.buttonState !in listOf(
            PBState.SELECT_FIRST_PLAYER,
            PBState.COMMANDER_RECEIVER,
            PBState.COMMANDER_DEALER
        ) && !isEmpty
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private var _customizationViewmodel: CustomizationViewModel? = null
    open val customizationViewmodel: CustomizationViewModel?
        get() = _customizationViewmodel

    init {
        updateRecentChange()
    }

    override fun onCleared() {
        super.onCleared()
        recentChangeJob?.cancel()
        recentChangeJob = null
    }

    internal fun setPlayer(player: Player) {
        _state.value = state.value.copy(player = player)
    }

    private fun updateRecentChange() {
        recentChangeJob?.cancel()
        recentChangeJob = viewModelScope.launch {
            delay(RECENT_CHANGE_DELAY)
            setPlayer(playerStateManager.clearRecentChange(state.value.player))
        }
    }

    open fun incrementLife(value: Int) {
        setPlayer(playerStateManager.incrementLife(state.value.player, value))
        updateRecentChange()
        gameStateManager.saveGameState()
    }

    fun setTimer(timer: TurnTimer?) {
        _state.value = state.value.copy(timer = timer)
    }

    fun setFirstPlayer() {
        timerManager.handleFirstPlayerSelection(index = state.value.player.playerNum - 1)
    }

    fun setPlayerButtonState(buttonState: PBState) {
        if (buttonState == PBState.COMMANDER_RECEIVER) {
            backstack.clear()
        }

        _state.value = state.value.copy(buttonState = buttonState)
    }

    fun onMoveTimer() {
        timerManager.moveTimer()
    }

    open fun onMonarchyButtonClicked(value: Boolean) {
        gameStateManager.setMonarchy(state.value.player.playerNum, value)
    }

    open fun onCommanderButtonClicked() {
        when (state.value.buttonState) {
            PBState.NORMAL -> {
                commanderManager.setCurrentDealer(state.value.player)
            }

            PBState.COMMANDER_DEALER -> {
                commanderManager.setCurrentDealer(null)
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
        setPlayer(playerStateManager.toggleSetDead(state.value.player))
        closeSettingsMenu()
        backstack.clear()
        gameStateManager.saveGameState()
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
        setPlayer(playerCustomizationManager.resetPlayerPrefs(state.value.player))
        resetCustomizationMenuViewModel()
    }

    fun savePlayerPref() {
        playerCustomizationManager.savePlayerPrefs(state.value.player)
    }

    fun getCounterValue(counterType: CounterType): Int {
        return state.value.player.counters[counterType.ordinal]
    }

    private fun resetCustomizationMenuViewModel() {
        _customizationViewmodel = CustomizationViewModel(
            initialPlayer = state.value.player,
            imageManager = imageManager,
            settingsManager = settingsManager,
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
            playerCustomizationManager.savePlayerPrefs(state.value.player)
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
        setPlayer(commanderManager.togglePartnerMode(state.value.player, value))
        gameStateManager.savePlayerState(state.value.player)
    }

    fun incrementCounterValue(counterType: CounterType, value: Int) {
        setPlayer(playerStateManager.incrementCounter(state.value.player, counterType, value))
        gameStateManager.savePlayerState(state.value.player)
    }

    fun setActiveCounter(counterType: CounterType, active: Boolean): Boolean {
        setPlayer(playerStateManager.setActiveCounters(state.value.player, counterType, active))
        gameStateManager.savePlayerState(state.value.player)
        return state.value.player.activeCounters.contains(counterType)
    }

    fun getCommanderDamage(partner: Boolean): Int {
        return commanderManager.getCommanderDamage(state.value.player, partner)
    }

    open fun incrementCommanderDamage(value: Int, partner: Boolean) {
        setPlayer(commanderManager.incrementCommanderDamage(state.value.player, value, partner))
        gameStateManager.savePlayerState(state.value.player)
    }

    open fun copyPrefs(other: Player) {
        setPlayer(playerCustomizationManager.copyPlayerPrefs(state.value.player, other))
    }

    fun resetState(startingLife: Int) {
        setPlayer(playerStateManager.resetPlayerState(state.value.player, startingLife))
        gameStateManager.savePlayerState(state.value.player)
    }
}