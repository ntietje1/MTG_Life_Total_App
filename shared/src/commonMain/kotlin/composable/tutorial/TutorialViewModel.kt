package composable.tutorial

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TutorialViewModel : ViewModel() {
    private val _state = MutableStateFlow(TutorialState(0, SingleTutorialScreen.entries.size))
    val state: StateFlow<TutorialState> = _state.asStateFlow()

    fun setCurrentPage(page: Int) {
        _state.value = _state.value.copy(currentPage = page)
    }
}