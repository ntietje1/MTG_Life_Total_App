package composable.playerselect

import androidx.lifecycle.ViewModel
import data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerSelectViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerSelectState())
    val state: StateFlow<PlayerSelectState> = _state.asStateFlow()

    fun setHelperText(value: HelperTextState) {
        _state.value = _state.value.copy(showHelperText = value)
    }

    fun setNumPlayers(allowChangeNumPlayers: Boolean, numPlayers: Int) {
        if (allowChangeNumPlayers) {
            settingsManager.numPlayers = numPlayers
        }
    }
}