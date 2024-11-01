package ui.lifecounter.playerbutton

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.ImageManager
import data.Player
import data.Player.Companion.MAX_PLAYERS
import data.SettingsManager
import data.TurnTimer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ui.dialog.customization.CustomizationViewModel
import ui.lifecounter.CounterType

abstract class AbstractPlayerButtonViewModel(state: PlayerButtonState) : ViewModel() {
    internal var _state = MutableStateFlow(state)
    val state: StateFlow<PlayerButtonState> = _state.asStateFlow()

    abstract val customizationViewmodel: CustomizationViewModel?

    private var recentChangeJob: Job? = null

    init {
        updateRecentChange()
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
        setPlayer(
            state.value.player.copy(
                life = state.value.player.life + value,
                recentChange = state.value.player.recentChange + value
            )
        )
        updateRecentChange()
    }

    abstract fun setTimer(timer: TurnTimer?)
    abstract fun setPlayerButtonState(buttonState: PBState)
    abstract fun onMoveTimer()
    abstract fun onMonarchyButtonClicked(value: Boolean)
    abstract fun onCommanderButtonClicked()
    abstract fun onSettingsButtonClicked()
    abstract fun onKOButtonClicked()
    abstract fun popBackStack()
    abstract fun pushBackStack(back: () -> Unit)
    abstract fun resetPlayerPref()
    abstract fun savePlayerPref()
    abstract fun isDead(): Boolean
    abstract fun getCounterValue(counterType: CounterType): Int
    abstract fun showCustomizeMenu(value: Boolean)
    abstract fun toggleMonarch(value: Boolean)
    abstract fun togglePartnerMode(value: Boolean)
    abstract fun incrementCounterValue(counterType: CounterType, value: Int)
    abstract fun setActiveCounter(counterType: CounterType, active: Boolean): Boolean
    abstract fun getCommanderDamage(partner: Boolean): Int
    abstract fun incrementCommanderDamage(value: Int, partner: Boolean)
    abstract fun copyPrefs(other: Player)
    abstract fun resetState(startingLife: Int)
}

class PlayerButtonViewModel(
    initialPlayer: Player,
    private val settingsManager: SettingsManager,
    private val imageManager: ImageManager,
    private val onCommanderButtonClickedCallback: (AbstractPlayerButtonViewModel) -> Unit,
    private val setAllMonarchy: (Boolean) -> Unit,
    val getCurrentDealer: () -> AbstractPlayerButtonViewModel?,
    private val updateCurrentDealerMode: (Boolean) -> Unit,
    private val triggerSave: () -> Unit,
    private val resetPlayerColor: (Player) -> Player,
    private val moveTimerCallback: () -> Unit,
) : AbstractPlayerButtonViewModel(PlayerButtonState(initialPlayer)) {
    private var _customizationViewmodel: CustomizationViewModel? = null
    override val customizationViewmodel: CustomizationViewModel?
        get() = _customizationViewmodel

    override fun setTimer(timer: TurnTimer?) {
        _state.value = state.value.copy(timer = timer)
    }

    override fun setPlayerButtonState(buttonState: PBState) {
        if (buttonState == PBState.COMMANDER_RECEIVER) {
            clearBackStack()
        }
        _state.value = state.value.copy(buttonState = buttonState)
        updateCurrentDealerMode(state.value.player.partnerMode)
    }

    override fun onMoveTimer() {
        moveTimerCallback()
    }

    override fun onMonarchyButtonClicked(value: Boolean) {
        if (value) {
            setAllMonarchy(false)
        }
        toggleMonarch(value)
    }

    override fun onCommanderButtonClicked() {
        onCommanderButtonClickedCallback(this)
    }

    override fun onSettingsButtonClicked() {
        if (state.value.buttonState == PBState.NORMAL) {
            setPlayerButtonState(PBState.SETTINGS)
            pushBackStack { setPlayerButtonState(PBState.NORMAL) }
        } else {
            closeSettingsMenu()
        }
    }

    override fun onKOButtonClicked() {
        toggleSetDead()
        closeSettingsMenu()
        clearBackStack()
    }

    override fun popBackStack() {
        if (state.value.backStack.isEmpty()) return
        val back = state.value.backStack.last()
        _state.value = state.value.copy(backStack = state.value.backStack.dropLast(1))
        back.invoke()
    }

    override fun pushBackStack(back: () -> Unit) {
        _state.value = state.value.copy(backStack = state.value.backStack + back)
    }

    private fun clearBackStack() {
        _state.value = state.value.copy(backStack = listOf())
    }

    private fun closeSettingsMenu() {
        setPlayerButtonState(PBState.NORMAL)
        clearBackStack()
    }

    override fun resetPlayerPref() {
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

    override fun savePlayerPref() {
        settingsManager.savePlayerPref(state.value.player)
    }

    override fun isDead(): Boolean {
        val autoKo = settingsManager.autoKo
        return ((autoKo && (state.value.player.life <= 0 || state.value.player.commanderDamage.any { it >= 21 })) || state.value.player.setDead)
    }

    override fun getCounterValue(counterType: CounterType): Int {
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

    override fun showCustomizeMenu(value: Boolean) {
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

    override fun toggleMonarch(value: Boolean) {
        setPlayer(state.value.player.copy(monarch = value))
        triggerSave()
    }

    override fun togglePartnerMode(value: Boolean) {
        setPlayer(state.value.player.copy(partnerMode = value))
        updateCurrentDealerMode(state.value.player.partnerMode)
        triggerSave()
    }

    private fun toggleSetDead(value: Boolean? = null) {
        setPlayer(state.value.player.copy(setDead = value ?: !state.value.player.setDead))
        triggerSave()
    }

    override fun incrementCounterValue(counterType: CounterType, value: Int) {
        setPlayer(state.value.player.copy(counters = state.value.player.counters.toMutableList().apply {
            this[counterType.ordinal] += value
        }))
        triggerSave()
    }

    override fun setActiveCounter(counterType: CounterType, active: Boolean): Boolean {
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

    override fun getCommanderDamage(partner: Boolean): Int {
        val currentDealer: AbstractPlayerButtonViewModel = getCurrentDealer() ?: return 0
        val index = (currentDealer.state.value.player.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        return state.value.player.commanderDamage[index]
    }

    override fun incrementCommanderDamage(value: Int, partner: Boolean) {
        val currentDealer: AbstractPlayerButtonViewModel = getCurrentDealer() ?: return
        val index = (currentDealer.state.value.player.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        this.receiveCommanderDamage(index, value)
        triggerSave()
    }

    private fun receiveCommanderDamage(index: Int, value: Int) {
        setPlayer(state.value.player.copy(commanderDamage = state.value.player.commanderDamage.toMutableList().apply {
            this[index] += value
        }.toList()))
        triggerSave()
    }

    override fun copyPrefs(other: Player) {
        setPlayer(
            state.value.player.copy(
                imageString = other.imageString,
                color = other.color,
                textColor = other.textColor,
                name = other.name
            )
        )
    }

    override fun resetState(startingLife: Int) {
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

    override fun incrementLife(value: Int) {
        super.incrementLife(value)
        triggerSave()
    }
}