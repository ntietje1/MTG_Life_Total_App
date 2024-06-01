package composable.tutorial

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
    }

    fun showWarningDialog(value: Boolean? = null) {
        _state.value = _state.value.copy(showWarningDialog = value ?: !_state.value.showWarningDialog)
    }
}