package composable.dialog.coinflip

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CoinFlipViewModel(
): ViewModel() {
    private val _state = MutableStateFlow(CoinFlipState())
    val state: StateFlow<CoinFlipState> = _state.asStateFlow()

    fun setLastResult(coinFace: CoinFace) {
        _state.value = _state.value.copy(currentFace = coinFace)
        addToHistory(coinFace)
    }

    private fun addToHistory(coinFace: CoinFace) {
        _state.value.history.add(coinFace)
        println("History: ${_state.value.history}")
    }

    fun resetHistory() {
        _state.value.history.clear()
    }
}