package ui.playerselect

import androidx.lifecycle.ViewModel
import domain.storage.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayerSelectViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerSelectState())
    val state: StateFlow<PlayerSelectState> = _state.asStateFlow()

    fun setHelperText(value: HelperTextState) {
        _state.value = _state.value.copy(showHelperText = value)
    }

    fun setNumPlayers(allowChangeNumPlayers: Boolean, numPlayers: Int) {
        if (allowChangeNumPlayers) {
            preferencesRepository.setNumPlayers(numPlayers)
        }
    }
}
