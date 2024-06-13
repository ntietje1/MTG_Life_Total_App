package composable.dialog.coinflip

import androidx.lifecycle.ViewModel
import composable.flippable.FlipAnimationType
import composable.flippable.FlippableController
import data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class CoinFlipViewModel(
    val settingsManager: SettingsManager
): ViewModel() {
    private val _state = MutableStateFlow(CoinFlipState())
    val state: StateFlow<CoinFlipState> = _state.asStateFlow()

    val flipController = FlippableController()

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

    fun decrementFlipCount() {
        _state.value = state.value.copy(flipCount = state.value.flipCount - 1)
//        duration += additionalDuration
    }

    fun resetCount() {
        _state.value = state.value.copy(flipCount = state.value.totalFlips)
        if (Random.nextBoolean()) decrementFlipCount() // source of randomness
//        duration = initialDuration
    }

    fun toggleAnimationType() {
        _state.value = state.value.copy(flipAnimationType = if (state.value.flipAnimationType == FlipAnimationType.VERTICAL_ANTI_CLOCKWISE) {
            FlipAnimationType.VERTICAL_CLOCKWISE
        } else {
            FlipAnimationType.VERTICAL_ANTI_CLOCKWISE
        })
    }
}