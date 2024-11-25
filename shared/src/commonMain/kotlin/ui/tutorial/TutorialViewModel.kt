package ui.tutorial

import androidx.lifecycle.ViewModel
import data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TutorialViewModel(
    val settingsManager: SettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(TutorialState(currentPage = 0, totalPages = 5))
    val state: StateFlow<TutorialState> = _state.asStateFlow()

    fun setCurrentPage(page: Int) {
        _state.value = _state.value.copy(currentPage = page)
        onChangePage()
    }

    fun onChangePage() {
        showHint(false)
    }

    fun showWarningDialog(value: Boolean) {
        _state.value = _state.value.copy(showWarningDialog = value)
    }

    fun showCloseDialog(value: Boolean) {
        _state.value = _state.value.copy(showCloseDialog = value)
    }

    fun showHint(value: Boolean) {
        _state.value = _state.value.copy(showHint = value)
    }

    fun setBlur(value: Boolean) {
        _state.value = _state.value.copy(blur = value)
    }

    fun setSuccess(value: Boolean) {
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