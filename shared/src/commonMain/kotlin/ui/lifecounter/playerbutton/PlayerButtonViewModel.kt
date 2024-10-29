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

class PlayerButtonViewModel(
    initialPlayer: Player,
    val settingsManager: SettingsManager,
    val imageManager: ImageManager,
    val onCommanderButtonClicked: (PlayerButtonViewModel) -> Unit,
    private val setAllMonarchy: (Boolean) -> Unit,
    private val getCurrentDealer: () -> PlayerButtonViewModel?,
    private val updateCurrentDealerMode: (Boolean) -> Unit,
    val currentDealerIsPartnered: StateFlow<Boolean>,
    private val triggerSave: () -> Unit,
    private val resetPlayerColor: (Player) -> Player,
    val moveTimer: () -> Unit,
) : ViewModel() {
    private var _state = MutableStateFlow(PlayerButtonState(initialPlayer))
    val state: StateFlow<PlayerButtonState> = _state.asStateFlow()

    var customizationViewmodel: CustomizationViewModel? = null

    private var recentChangeJob: Job? = null

    init {
        updateRecentChange()
    }

    fun setTimer(timer: TurnTimer?) {
        _state.value = state.value.copy(timer = timer)
    }

    fun setPlayerButtonState(buttonState: PBState) {
        _state.value = state.value.copy(buttonState = buttonState)
        updateCurrentDealerMode(state.value.player.partnerMode)
    }

    private fun setPlayer(player: Player) {
        _state.value = state.value.copy(player = player)
    }

    fun onMonarchyButtonClicked(value: Boolean? = null) {
        val targetValue = value ?: !state.value.player.monarch
        if (targetValue) {
            setAllMonarchy(false)
        }
        toggleMonarch(targetValue)
    }

    fun onSettingsButtonClicked() {
        if (state.value.buttonState == PBState.NORMAL) {
            setPlayerButtonState(PBState.SETTINGS)
            pushBackStack { setPlayerButtonState(PBState.NORMAL) }
        } else {
            closeSettingsMenu()
        }
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

    fun clearBackStack() {
        _state.value = state.value.copy(backStack = listOf())
    }

    fun closeSettingsMenu() {
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

    fun isDead(autoKo: Boolean = settingsManager.autoKo): Boolean {
        return ((autoKo && (state.value.player.life <= 0 || state.value.player.commanderDamage.any { it >= 21 })) || state.value.player.setDead)
    }

    fun getCounterValue(counterType: CounterType): Int {
        return state.value.player.counters[counterType.ordinal]
    }

    private fun resetCustomizationMenuViewModel() {
        customizationViewmodel = CustomizationViewModel(
            initialPlayer = state.value.player,
            imageManager = imageManager,
            settingsManager = settingsManager,
        )
    }

    fun onCustomizationApply() {
        val player = customizationViewmodel?.state?.value?.player ?: throw IllegalStateException("CustomizationViewModel is null")
        viewModelScope.launch {
            copyPrefs(player.copy(imageString = null))
            delay(50)
            copyPrefs(player)
            resetCustomizationMenuViewModel()
            settingsManager.savePlayerPref(state.value.player)
        }
    }

    fun showCustomizeMenu(value: Boolean? = null) {
        if (value == true && customizationViewmodel == null) {
            resetCustomizationMenuViewModel()
        }
        _state.value = state.value.copy(showCustomizeMenu = value ?: !state.value.showCustomizeMenu)
    }

    fun toggleMonarch(value: Boolean? = null) {
        setPlayer(state.value.player.copy(monarch = value ?: !state.value.player.monarch))
        triggerSave()
    }

    fun togglePartnerMode(value: Boolean? = null) {
        setPlayer(state.value.player.copy(partnerMode = value ?: !state.value.player.partnerMode))
        updateCurrentDealerMode(state.value.player.partnerMode)
        triggerSave()
    }

    fun toggleSetDead(value: Boolean? = null) {
        setPlayer(state.value.player.copy(setDead = value ?: !state.value.player.setDead))
        triggerSave()
    }

    fun incrementCounterValue(counterType: CounterType, value: Int) {
        setPlayer(state.value.player.copy(counters = state.value.player.counters.toMutableList().apply {
            this[counterType.ordinal] += value
        }))
        triggerSave()
    }

    fun setActiveCounter(counterType: CounterType, active: Boolean? = null): Boolean {
        setPlayer(state.value.player.copy(activeCounters = state.value.player.activeCounters.toMutableList().apply {
            val previousValue = this.contains(counterType)
            val targetValue = active ?: !previousValue
            if (targetValue) {
                this.add(counterType)
            } else {
                this.remove(counterType)
            }
        }))
        triggerSave()
        return state.value.player.activeCounters.contains(counterType)
    }

    fun getCommanderDamage(currentDealer: PlayerButtonViewModel? = getCurrentDealer(), partner: Boolean = false): Int {
        if (currentDealer == null) return 0
        val index = (currentDealer.state.value.player.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        return state.value.player.commanderDamage[index]
    }

    fun incrementCommanderDamage(currentDealer: PlayerButtonViewModel? = getCurrentDealer(), value: Int, partner: Boolean = false) {
        if (currentDealer == null) return
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

    fun incrementLife(value: Int) {
        setPlayer(
            state.value.player.copy(
                life = state.value.player.life + value,
                recentChange = state.value.player.recentChange + value
            )
        )
        updateRecentChange()
        triggerSave()
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