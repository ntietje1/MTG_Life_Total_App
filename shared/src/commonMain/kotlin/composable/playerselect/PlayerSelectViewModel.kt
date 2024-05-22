package composable.playerselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerSelectViewModel(
//    private val helloWorld: String
): ViewModel() {

    private val _state = MutableStateFlow(PlayerSelectState())
    val state: StateFlow<PlayerSelectState> = _state.asStateFlow()

    fun setHelperText(value: Boolean?) {
        _state.value = _state.value.copy(showHelperText = value ?: !_state.value.showHelperText)
    }

    private val _timer = MutableStateFlow(0)
    val timer = _timer.asStateFlow()

    init {
        startTimer()
//        println(helloWorld)
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _timer.value++
            }
        }
    }
}