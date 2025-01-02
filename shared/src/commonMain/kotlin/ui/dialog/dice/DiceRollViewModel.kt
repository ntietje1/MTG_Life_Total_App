package ui.dialog.dice

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DiceRollViewModel : ViewModel() {
    private val _state = MutableStateFlow(DiceRollState())
    val state: StateFlow<DiceRollState> = _state.asStateFlow()

    fun setTextFieldValue(value: TextFieldValue) {
        val newText = value.text
        if (newText.isEmpty()) {
            _state.value = _state.value.copy(
                textFieldValue = TextFieldValue(""),
                customDieValue = 0u
            )
        } else {
            newText.toUIntOrNull()?.let { number ->
                _state.value = _state.value.copy(
                    textFieldValue = value,
                    customDieValue = number
                )
            }
        }
    }

    fun setLastResult(result: UInt, faceValue: UInt) {
        _state.value = _state.value.copy(
            lastResult = result,
            faceValue = faceValue
        )
    }
} 