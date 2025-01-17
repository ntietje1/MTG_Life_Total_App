package ui.lifecounter.playerbutton

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.common.Backstack
import domain.common.NumberWithRecentChange
import domain.game.CommanderDamageManager
import domain.game.CommanderState
import domain.game.GameStateManager
import domain.game.PlayerCustomizationManager
import domain.game.PlayerStateManager
import domain.game.timer.TimerManager
import domain.game.timer.TurnTimer
import domain.storage.IImageManager
import domain.storage.ISettingsManager
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
        settingsManager.autoKo, state
    ) { autoKo, playerState ->
        playerStateManager.isPlayerDead(playerState.player, autoKo)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val commanderState: StateFlow<CommanderState> = commanderManager.commanderState

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

    init {
        viewModelScope.launch {
            playerStateManager.attachLifeTracker(
                initialPlayer = state.value.player,
                onUpdate = ::setLifeTotal
            )

            commanderManager.attachCommanderTrackers(
                initialPlayer = state.value.player,
                onUpdate = ::setCommanderDamage
            )
        }
    }

    private fun setLifeTotal(updatedPlayer: Player) {
        setPlayer(state.value.player.copy(lifeTotal = updatedPlayer.lifeTotal))
    }

    private fun setCommanderDamage(updatedPlayer: Player) {
        setPlayer(state.value.player.copy(commanderDamage = updatedPlayer.commanderDamage))
    }

    internal fun setPlayer(player: Player) {
        _state.value = state.value.copy(player = player)
    }

    open fun incrementLife(value: Int) {
        playerStateManager.incrementLife(state.value.player, value)
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
        gameStateManager.saveGameState()
    }

    fun incrementCounterValue(counterType: CounterType, value: Int) {
        setPlayer(playerStateManager.incrementCounter(state.value.player, counterType, value))
        gameStateManager.saveGameState()
    }

    fun setActiveCounter(counterType: CounterType, active: Boolean): Boolean {
        setPlayer(playerStateManager.setActiveCounters(state.value.player, counterType, active))
        gameStateManager.saveGameState()
        return state.value.player.activeCounters.contains(counterType)
    }

    fun getCommanderDamage(partner: Boolean): NumberWithRecentChange {
        return commanderManager.getCommanderDamage(state.value.player, partner)
    }

    open fun incrementCommanderDamage(value: Int, partner: Boolean) {
        commanderManager.incrementCommanderDamage(state.value.player, value, partner)
        gameStateManager.saveGameState()
    }

    open fun copyPrefs(other: Player) {
        setPlayer(playerCustomizationManager.copyPlayerPrefs(state.value.player, other))
    }

    fun resetState() {
        setPlayer(playerStateManager.resetPlayerState(state.value.player))
        setPlayer(commanderManager.resetCommanderDamage(state.value.player))
        gameStateManager.saveGameState()
    }
}