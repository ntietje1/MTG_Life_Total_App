package ui.lifecounter.playerbutton

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.IImageManager
import data.ISettingsManager
import data.Player
import data.Player.Companion.MAX_PLAYERS
import di.NotificationManager
import domain.player.CommanderDamageManager
import domain.player.CounterManager
import domain.player.PlayerCustomizationManager
import domain.player.PlayerManager
import features.timer.TurnTimer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ui.dialog.customization.CustomizationViewModel
import ui.lifecounter.CounterType

open class PlayerButtonViewModel(
    initialPlayer: Player,
    private val settingsManager: ISettingsManager,
    private val imageManager: IImageManager,
    private val counterManager: CounterManager,
    private val commanderManager: CommanderDamageManager,
    private val setMonarchy: (Boolean) -> Unit,
    private val triggerSave: () -> Unit,
    private val resetPlayerColor: (Player) -> Player,
    private val moveTimerCallback: () -> Unit,
    protected val notificationManager: NotificationManager,
    private val playerManager: PlayerManager,
    private val playerCustomizationManager: PlayerCustomizationManager,
) : ViewModel() {
    private var _state = MutableStateFlow(PlayerButtonState(initialPlayer))
    val state: StateFlow<PlayerButtonState> = _state.asStateFlow()

    val isDead: StateFlow<Boolean> = combine(
        settingsManager.autoKo,
        state
    ) { autoKo, playerState ->
        playerManager.isPlayerDead(playerState.player, autoKo)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentDealer: StateFlow<Player?> = commanderManager.currentDealer

    private var recentChangeJob: Job? = null

    init {
        updateRecentChange()
    }

    override fun onCleared() {
        super.onCleared()
        recentChangeJob?.cancel()
    }

    constructor(
        initialState: PlayerButtonState,
        settingsManager: ISettingsManager,
        imageManager: IImageManager,
        counterManager: CounterManager,
        commanderManager: CommanderDamageManager,
        setMonarchy: (Boolean) -> Unit,
        triggerSave: () -> Unit,
        resetPlayerColor: (Player) -> Player,
        moveTimerCallback: () -> Unit,
        notificationManager: NotificationManager,
        playerManager: PlayerManager,
        playerCustomizationManager: PlayerCustomizationManager,
    ) : this(
        initialState.player,
        settingsManager,
        imageManager,
        counterManager,
        commanderManager,
        setMonarchy,
        triggerSave,
        resetPlayerColor,
        moveTimerCallback,
        notificationManager,
        playerManager,
        playerCustomizationManager
    ) {
        _state.value = initialState
    }

    internal fun setPlayer(player: Player) {
        _state.value = state.value.copy(player = player)
    }

    private fun updateRecentChange() {
        recentChangeJob?.cancel()
        recentChangeJob = viewModelScope.launch {
            delay(1500)
            setPlayer(
                state.value.player.copy(
                    recentChange = 0
                )
            )
        }
    }

    open fun incrementLife(value: Int) {
        setPlayer(playerManager.incrementLife(state.value.player, value))
        updateRecentChange()
        triggerSave()
    }

    private var _customizationViewmodel: CustomizationViewModel? = null
    open val customizationViewmodel: CustomizationViewModel?
        get() = _customizationViewmodel

    fun setTimer(timer: TurnTimer?) {
        _state.value = state.value.copy(timer = timer)
    }

    fun setPlayerButtonState(buttonState: PBState) {
        if (buttonState == PBState.COMMANDER_RECEIVER) {
            clearBackStack()
        }

        _state.value = state.value.copy(buttonState = buttonState)
    }

    fun onMoveTimer() {
        moveTimerCallback()
    }

    open fun onMonarchyButtonClicked(value: Boolean) {
        setPlayerButtonState(PBState.NORMAL)
        setMonarchy(value)
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
            pushBackStack { setPlayerButtonState(PBState.NORMAL) }
        } else {
            closeSettingsMenu()
        }
    }

    open fun onKOButtonClicked() {
        setPlayer(playerManager.toggleSetDead(state.value.player))
        closeSettingsMenu()
        clearBackStack()
        triggerSave()
    }

    open fun popBackStack() {
        if (state.value.backStack.isEmpty()) return
        val back = state.value.backStack.last()
        _state.value = state.value.copy(backStack = state.value.backStack.dropLast(1))
        back.invoke()
    }

    private fun pushBackStack(back: () -> Unit) {
        _state.value = state.value.copy(backStack = state.value.backStack + back)
    }

    private fun clearBackStack() {
        _state.value = state.value.copy(backStack = listOf())
    }

    private fun closeSettingsMenu() {
        setPlayerButtonState(PBState.NORMAL)
        clearBackStack()
    }

    fun resetPlayerPref() {
        setPlayer(
            playerCustomizationManager.resetPlayerPreferences(
                resetPlayerColor(state.value.player)
            )
        )
        resetCustomizationMenuViewModel()
    }

    fun savePlayerPref() {
        settingsManager.savePlayerPref(state.value.player)
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
            settingsManager.savePlayerPref(state.value.player)
        }
    }

    open fun onShowCustomizeMenu(value: Boolean) {
        if (value && customizationViewmodel == null) {
            resetCustomizationMenuViewModel()
        }
        if (!value) {
            onCustomizationApply()
            setPlayerButtonState(PBState.NORMAL)
            clearBackStack()
        }
        _state.value = state.value.copy(showCustomizeMenu = value)
    }


    fun onFirstPlayerPrompt() {
        pushBackStack { setPlayerButtonState(PBState.NORMAL) }
        setPlayerButtonState(PBState.SELECT_FIRST_PLAYER)
    }

    open fun onCountersButtonClicked() {
        setPlayerButtonState(PBState.COUNTERS_VIEW)
        pushBackStack { setPlayerButtonState(PBState.SETTINGS) }
    }

    open fun onAddCounterButtonClicked() {
        setPlayerButtonState(PBState.COUNTERS_SELECT)
        pushBackStack { setPlayerButtonState(PBState.COUNTERS_VIEW) }
    }

    fun togglePartnerMode(value: Boolean) {
        setPlayer(commanderManager.togglePartnerMode(state.value.player, value))
        triggerSave()
    }

    fun incrementCounterValue(counterType: CounterType, value: Int) {
        setPlayer(counterManager.incrementCounter(state.value.player, counterType, value))
        triggerSave()
    }

    fun setActiveCounter(counterType: CounterType, active: Boolean): Boolean {
        setPlayer(counterManager.setActiveCounters(state.value.player, counterType, active))
        triggerSave()
        return state.value.player.activeCounters.contains(counterType)
    }

    fun getCommanderDamage(partner: Boolean): Int {
        val currentDealer = commanderManager.currentDealer.value ?: return 0
        val index = (currentDealer.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        return state.value.player.commanderDamage[index]
    }

    open fun incrementCommanderDamage(value: Int, partner: Boolean) {
        val currentDealer = commanderManager.currentDealer.value ?: return
        val index = (currentDealer.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        receiveCommanderDamage(index, value)
    }

    protected open fun receiveCommanderDamage(index: Int, value: Int) {
        setPlayer(state.value.player.copy(commanderDamage = state.value.player.commanderDamage.toMutableList().apply {
            this[index] += value
        }.toList()))
        triggerSave()
    }

    open fun copyPrefs(other: Player) {
        setPlayer(playerCustomizationManager.copyPlayerPreferences(state.value.player, other))
    }

    fun resetState(startingLife: Int) {
        setPlayer(playerManager.resetPlayerState(state.value.player, startingLife))
        triggerSave()
    }
}