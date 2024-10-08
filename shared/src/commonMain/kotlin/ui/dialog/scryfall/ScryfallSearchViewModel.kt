package ui.dialog.scryfall

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScryfallSearchViewModel(): ViewModel() {
    private val _state = MutableStateFlow(ScryfallSearchState())
    val state: StateFlow<ScryfallSearchState> = _state.asStateFlow()

    fun setTextFieldValue(textFieldValue: TextFieldValue) {
        _state.value = _state.value.copy(textFieldValue = textFieldValue)
    }
}