package ui.dialog.startinglife

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import domain.storage.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StartingLifeViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _state = MutableStateFlow(StartingLifeState())
    val state: StateFlow<StartingLifeState> = _state.asStateFlow()

    fun setTextFieldValue(textFieldValue: TextFieldValue) {
        _state.value = _state.value.copy(textFieldValue = textFieldValue)
    }

    fun setStartingLife(life: Int) {
        settingsManager.setStartingLife(life)
    }

    fun parseStartingLife(): Int? {
        return state.value.textFieldValue.text.toIntOrNull()
    }
}