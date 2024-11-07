package ui.lifecounter.playerbutton

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.IImageManager
import data.ISettingsManager
import data.Player
import data.Player.Companion.MAX_PLAYERS
import data.TurnTimer
import di.NotificationManager
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

//abstract class AbstractPlayerButtonViewModel(state: PlayerButtonState) : ViewModel() {
//    internal var _state = MutableStateFlow(state)
//    val state: StateFlow<PlayerButtonState> = _state.asStateFlow()
//
//    abstract val customizationViewmodel: CustomizationViewModel?
//
//    private var recentChangeJob: Job? = null
//
//    init {
//        updateRecentChange()
//    }
//
//    internal fun setPlayer(player: Player) {
//        _state.value = state.value.copy(player = player)
//    }
//
//    private fun updateRecentChange() {
//        recentChangeJob?.cancel()
//        recentChangeJob = viewModelScope.launch {
//            delay(1500)
//            setPlayer(
//                state.value.player.copy(
//                    recentChange = 0
//                )
//            )
//        }
//    }
//
//    open fun incrementLife(value: Int) {
//        setPlayer(
//            state.value.player.copy(
//                life = state.value.player.life + value,
//                recentChange = state.value.player.recentChange + value
//            )
//        )
//        updateRecentChange()
//    }
//
//    abstract fun setTimer(timer: TurnTimer?)
//    abstract fun setPlayerButtonState(buttonState: PBState)
//    abstract fun onMoveTimer()
//    abstract fun onMonarchyButtonClicked(value: Boolean)
//    abstract fun onCommanderButtonClicked()
//    abstract fun onSettingsButtonClicked()
//    abstract fun onKOButtonClicked()
//    abstract fun popBackStack()
//    abstract fun pushBackStack(back: () -> Unit)
//    abstract fun resetPlayerPref()
//    abstract fun savePlayerPref()
//    abstract fun isDead(): Boolean
//    abstract fun getCounterValue(counterType: CounterType): Int
//    abstract fun showCustomizeMenu(value: Boolean)
//    abstract fun toggleMonarch(value: Boolean)
//    abstract fun togglePartnerMode(value: Boolean)
//    abstract fun incrementCounterValue(counterType: CounterType, value: Int)
//    abstract fun setActiveCounter(counterType: CounterType, active: Boolean): Boolean
//    abstract fun getCommanderDamage(partner: Boolean): Int
//    abstract fun incrementCommanderDamage(value: Int, partner: Boolean)
//    abstract fun copyPrefs(other: Player)
//    abstract fun resetState(startingLife: Int)
//}

open class PlayerButtonViewModel(
    initialPlayer: Player,
    private val settingsManager: ISettingsManager,
    private val imageManager: IImageManager,
    private val onCommanderButtonClickedCallback: (PlayerButtonViewModel) -> Unit,
    private val setAllMonarchy: (Boolean) -> Unit,
    val getCurrentDealer: () -> PlayerButtonViewModel?,
    private val updateCurrentDealerMode: (Boolean) -> Unit,
    private val triggerSave: () -> Unit,
    private val resetPlayerColor: (Player) -> Player,
    private val moveTimerCallback: () -> Unit,
    protected val notificationManager: NotificationManager
) : ViewModel() {
    private var _state = MutableStateFlow(PlayerButtonState(initialPlayer))
    val state: StateFlow<PlayerButtonState> = _state.asStateFlow()

    val isDead: StateFlow<Boolean> = combine(
        settingsManager.autoKo,
        state
    ) { autoKo, playerState ->
        (autoKo && (playerState.player.life <= 0 || playerState.player.commanderDamage.any { it >= 21 })) || playerState.player.setDead
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private var recentChangeJob: Job? = null

    init {
        updateRecentChange()
    }

    constructor(
        initialState: PlayerButtonState,
        settingsManager: ISettingsManager,
        imageManager: IImageManager,
        onCommanderButtonClickedCallback: (PlayerButtonViewModel) -> Unit,
        setAllMonarchy: (Boolean) -> Unit,
        getCurrentDealer: () -> PlayerButtonViewModel?,
        updateCurrentDealerMode: (Boolean) -> Unit,
        triggerSave: () -> Unit,
        resetPlayerColor: (Player) -> Player,
        moveTimerCallback: () -> Unit,
        notificationManager: NotificationManager
    ) : this(
        initialState.player,
        settingsManager,
        imageManager,
        onCommanderButtonClickedCallback,
        setAllMonarchy,
        getCurrentDealer,
        updateCurrentDealerMode,
        triggerSave,
        resetPlayerColor,
        moveTimerCallback,
        notificationManager
    ) {
        _state.value = initialState
    }

    private fun setPlayer(player: Player) {
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
        setPlayer(
            state.value.player.copy(
                life = state.value.player.life + value,
                recentChange = state.value.player.recentChange + value
            )
        )
        updateRecentChange()
        triggerSave()
    }

    private var _customizationViewmodel: CustomizationViewModel? = null
    val customizationViewmodel: CustomizationViewModel?
        get() = _customizationViewmodel

    fun setTimer(timer: TurnTimer?) {
        _state.value = state.value.copy(timer = timer)
    }

    fun setPlayerButtonState(buttonState: PBState) {
        if (buttonState == PBState.COMMANDER_RECEIVER) {
            clearBackStack()
        }
        _state.value = state.value.copy(buttonState = buttonState)
        updateCurrentDealerMode(state.value.player.partnerMode)
    }

    fun onMoveTimer() {
        moveTimerCallback()
    }

    fun onMonarchyButtonClicked(value: Boolean) {
        if (value) {
            setAllMonarchy(false)
        }
        toggleMonarch(value)
    }

    open fun onCommanderButtonClicked() {
        onCommanderButtonClickedCallback(this)
    }

    open fun onSettingsButtonClicked() {
        if (state.value.buttonState == PBState.NORMAL) {
            setPlayerButtonState(PBState.SETTINGS)
            pushBackStack { setPlayerButtonState(PBState.NORMAL) }
        } else {
            closeSettingsMenu()
        }
    }

    fun onKOButtonClicked() {
        toggleSetDead()
        closeSettingsMenu()
        clearBackStack()
    }

    fun popBackStack() {
        if (state.value.backStack.isEmpty()) return
        val back = state.value.backStack.last()
        _state.value = state.value.copy(backStack = state.value.backStack.dropLast(1))
        back.invoke()
    }

    fun pushBackStack(back: () -> Unit) {
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
            state.value.player.copy(
                name = "P${state.value.player.playerNum}",
                textColor = Color.White,
                imageString = null
            )
        )
        setPlayer(
            resetPlayerColor(state.value.player)
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
        val player = customizationViewmodel?.state?.value?.player ?: throw IllegalStateException("CustomizationViewModel is null")
        viewModelScope.launch {
            copyPrefs(player.copy(imageString = null))
            delay(50)
            copyPrefs(player)
            resetCustomizationMenuViewModel()
            settingsManager.savePlayerPref(state.value.player)
        }
    }

    fun showCustomizeMenu(value: Boolean) {
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


    fun toggleMonarch(value: Boolean) {
        setPlayer(state.value.player.copy(monarch = value))
        triggerSave()
    }

    fun togglePartnerMode(value: Boolean) {
        println("togglePartnerMode: $value")
        setPlayer(state.value.player.copy(partnerMode = value))
        updateCurrentDealerMode(state.value.player.partnerMode)
        triggerSave()
    }

    private fun toggleSetDead(value: Boolean? = null) {
        setPlayer(state.value.player.copy(setDead = value ?: !state.value.player.setDead))
        triggerSave()
    }

    fun incrementCounterValue(counterType: CounterType, value: Int) {
        setPlayer(state.value.player.copy(counters = state.value.player.counters.toMutableList().apply {
            this[counterType.ordinal] += value
        }))
        triggerSave()
    }

    fun setActiveCounter(counterType: CounterType, active: Boolean): Boolean {
        setPlayer(state.value.player.copy(activeCounters = state.value.player.activeCounters.toMutableList().apply {
            if (active) {
                this.add(counterType)
            } else {
                this.remove(counterType)
            }
        }))
        triggerSave()
        return state.value.player.activeCounters.contains(counterType)
    }

    fun getCommanderDamage(partner: Boolean): Int {
        val currentDealer: PlayerButtonViewModel = getCurrentDealer() ?: return 0
        val index = (currentDealer.state.value.player.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        return state.value.player.commanderDamage[index]
    }

    open fun incrementCommanderDamage(value: Int, partner: Boolean) {
        val currentDealer: PlayerButtonViewModel = getCurrentDealer() ?: return
        val index = (currentDealer.state.value.player.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        this.receiveCommanderDamage(index, value)
    }

    protected open fun receiveCommanderDamage(index: Int, value: Int) {
        if (state.value.player.commanderDamage[index] + value < 0) {
            notificationManager.showNotification("Commander damage cannot be negative", 1000)
        } else {
            setPlayer(state.value.player.copy(commanderDamage = state.value.player.commanderDamage.toMutableList().apply {
                this[index] += value
            }.toList()))
            triggerSave()
        }
    }

    fun copyPrefs(other: Player) {
        setPlayer(
            state.value.player.copy(
                imageString = other.imageString,
                color = other.color,
                textColor = other.textColor,
                name = other.name
            )
        )
    }

    fun resetState(startingLife: Int) {
        setPlayer(
            state.value.player.copy(
                life = startingLife,
                recentChange = 0,
                monarch = false,
                setDead = false,
                commanderDamage = List(MAX_PLAYERS * 2) { 0 },
                counters = List(CounterType.entries.size) { 0 },
                activeCounters = listOf()
            )
        )
        triggerSave()
    }
}