package composable.lifecounter.playerbutton

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkrockstudios.libraries.mpfilepicker.MPFile
import composable.lifecounter.CounterType
import data.ImageManager
import data.Player
import data.Player.Companion.MAX_PLAYERS
import data.SettingsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerButtonViewModel(
    initialPlayer: Player,
    val settingsManager: SettingsManager,
    val imageManager: ImageManager,
    private val setAllButtonStates: (PBState) -> Unit,
    private val setAllMonarchy: (Boolean) -> Unit,
    private val getCurrentDealer: () -> PlayerButtonViewModel?,
    private val updateCurrentDealerMode: (Boolean) -> Unit,
    val currentDealerIsPartnered: StateFlow<Boolean>,
    private val triggerSave: () -> Unit,
    private val resetPlayerColor: (Player) -> Player,
) : ViewModel() {
    private var _state = MutableStateFlow(PlayerButtonState(initialPlayer))
    val state: StateFlow<PlayerButtonState> = _state.asStateFlow()

    private var recentChangeJob: Job? = null

    fun setPlayerButtonState(buttonState: PBState) {
        _state.value = state.value.copy(buttonState = buttonState)
        updateCurrentDealerMode(state.value.player.partnerMode)
    }

    private fun setPlayerInfo(player: Player) {
        _state.value = state.value.copy(player = player)
    }

    private fun updatePlayerInfo(update: Player.() -> Player) {
        setPlayerInfo(state.value.player.update())
    }

    fun onCommanderButtonClicked() {
        setPlayerButtonState(
            buttonState = when (state.value.buttonState) {
                PBState.NORMAL -> {
                    setAllButtonStates(PBState.COMMANDER_RECEIVER)
                    PBState.COMMANDER_DEALER
                }
                PBState.COMMANDER_DEALER -> {
                    setAllButtonStates(PBState.NORMAL)
                    PBState.NORMAL
                }
                else -> throw Exception("Invalid state for commanderButtonOnClick")
            }
        )
    }

    fun onFileSelected(file: MPFile<Any>?) {
        showFilePicker(false)
        if (file == null) return
        viewModelScope.launch {
            val copiedUri = imageManager.copyImageToLocalStorage(file.path, state.value.player.name)
            setImageUri(copiedUri)
        }
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
            setPlayerButtonState(PBState.SETTINGS_DEFAULT)
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

    init {
        updateRecentChange()
    }

    fun resetPlayerPref() {
        updatePlayerInfo {
            state.value.player.copy(
                name = "P${state.value.player.playerNum}",
                textColor = Color.White,
                imageUri = null
            )
        }
        updatePlayerInfo {
            resetPlayerColor(state.value.player)
        }
    }

    private fun savePlayerPref() {
        settingsManager.savePlayerPref(state.value.player)
    }

    fun setImageUri(uri: String?) {
        setPlayerInfo(state.value.player.copy(imageUri = uri))
        savePlayerPref()
    }

    private fun setBackgroundColor(color: Color) {
        setPlayerInfo(state.value.player.copy(color = color))
        savePlayerPref()
    }

    private fun setTextColor(color: Color) {
        setPlayerInfo(state.value.player.copy(textColor = color))
        savePlayerPref()
    }


    fun setChangeNameField(value: TextFieldValue) {
        _state.value = state.value.copy(changeNameTextField = value)
    }

    fun setName(name: String) {
        setPlayerInfo(state.value.player.copy(name = name))
        savePlayerPref()
    }

    fun isDead(autoKo: Boolean = settingsManager.autoKo): Boolean {
        val playerInfo = state.value .player
        return ((autoKo && (playerInfo.life <= 0 || playerInfo.commanderDamage.any { it >= 21 })) || playerInfo.setDead)
    }

    fun getCounterValue(counterType: CounterType): Int {
        return state.value.player.counters[counterType.ordinal]
    }

    fun showChangeNameField(value: Boolean? = null) {
        _state.value = state.value.copy(showChangeNameField = value ?: !state.value.showChangeNameField)
    }

    fun showBackgroundColorPicker(value: Boolean? = null) {
        _state.value = state.value.copy(showBackgroundColorPicker = value ?: !state.value.showBackgroundColorPicker)
    }

    fun showTextColorPicker(value: Boolean? = null) {
        _state.value = state.value.copy(showTextColorPicker = value ?: !state.value.showTextColorPicker)
    }

    fun showScryfallSearch(value: Boolean? = null) {
        _state.value = state.value.copy(showScryfallSearch = value ?: !state.value.showScryfallSearch)
    }

    fun showCameraWarning(value: Boolean? = null) {
        _state.value = state.value.copy(showCameraWarning = value ?: !state.value.showCameraWarning)
    }

    fun showFilePicker(value: Boolean? = null) {
        _state.value = state.value.copy(showFilePicker = value ?: !state.value.showFilePicker)
    }

    fun showResetPrefsDialog(value: Boolean? = null) {
        _state.value = state.value.copy(showResetPrefsDialog = value ?: !state.value.showResetPrefsDialog)
    }

    fun toggleMonarch(value: Boolean? = null) {
        setPlayerInfo(state.value.player.copy(monarch = value ?: !state.value.player.monarch))
        triggerSave()
    }

    fun togglePartnerMode(value: Boolean? = null) {
        setPlayerInfo(state.value.player.copy(partnerMode = value ?: !state.value.player.partnerMode))
        updateCurrentDealerMode(state.value.player.partnerMode)
        triggerSave()
    }

    fun toggleSetDead(value: Boolean? = null) {
        setPlayerInfo(state.value.player.copy(setDead = value ?: !state.value.player.setDead))
        triggerSave()
    }

    fun incrementCounterValue(counterType: CounterType, value: Int) {
        setPlayerInfo(state.value.player.copy(counters = state.value.player.counters.toMutableList().apply {
            this[counterType.ordinal] += value
        }))
        triggerSave()
    }

    fun setActiveCounter(counterType: CounterType, active: Boolean? = null): Boolean {
        setPlayerInfo(state.value.player.copy(activeCounters = state.value.player.activeCounters.toMutableList().apply {
            val previousValue = this.contains(counterType)
            val targetValue = active ?: !previousValue
            if (targetValue) {
                println("adding counterType: $counterType")
                this.add(counterType)
            } else {
                println("removing counterType: $counterType")
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
        setPlayerInfo(state.value.player.copy(commanderDamage = state.value.player.commanderDamage.toMutableList().apply {
            this[index] += value
        }.toList()))
        triggerSave()
    }

    fun onChangeBackgroundColor(color: Color) {
        setImageUri(null)
        setBackgroundColor(color)
        savePlayerPref()
    }

    fun onChangeTextColor(color: Color) {
        setTextColor(color)
        savePlayerPref()
    }

    fun copySettings(other: Player) {
        setPlayerInfo(
            state.value.player.copy(
                imageUri = other.imageUri,
                color = other.color,
                textColor = other.textColor,
                name = other.name
            )
        )
        setChangeNameField(TextFieldValue(other.name, selection = TextRange(other.name.length)))
    }

    fun incrementLife(value: Int) {
        setPlayerInfo(
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
            setPlayerInfo(
                state.value.player.copy(
                    recentChange = 0
                )
            )
        }
    }

    fun resetState(startingLife: Int) {
        updatePlayerInfo {
            state.value.player.copy(
                life = startingLife,
                recentChange = 0,
                monarch = false,
                setDead = false,
                commanderDamage = List(MAX_PLAYERS * 2) { 0 },
                counters = List(CounterType.entries.size) { 0 },
                activeCounters = listOf()
            )
        }
        setChangeNameField(TextFieldValue(state.value.player.name, selection = TextRange(state.value.player.name.length)))
        triggerSave()
    }
}