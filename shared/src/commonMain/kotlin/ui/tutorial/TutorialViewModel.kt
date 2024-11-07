package ui.tutorial

import androidx.lifecycle.ViewModel
import data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TutorialViewModel(
    val settingsManager: SettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(TutorialState(0, SingleTutorialScreen.entries.size))
    val state: StateFlow<TutorialState> = _state.asStateFlow()

    fun setCurrentPage(page: Int) {
        _state.value = _state.value.copy(currentPage = page)
        onChangePage()
    }

    fun onChangePage() {
        showHint(false)
        showSuccess(false)
    }

    fun showWarningDialog(value: Boolean) {
        _state.value = _state.value.copy(showWarningDialog = value)
    }

    fun showHint(value: Boolean) {
        _state.value = _state.value.copy(showHint = value)
        if (value) {
            showSuccess(false)
        }
    }

    fun showSuccess(value: Boolean) {
        if (state.value.completed[state.value.currentPage]) return
        _state.value = _state.value.copy(showSuccess = value)
        if (value) {
            _state.value = _state.value.copy(completed = _state.value.completed.toMutableList().apply {
                set(_state.value.currentPage, true)
            })
            showHint(false)
        }
    }
}