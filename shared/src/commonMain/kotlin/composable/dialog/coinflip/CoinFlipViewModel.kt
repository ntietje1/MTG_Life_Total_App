package composable.dialog.coinflip

import androidx.lifecycle.ViewModel
import composable.flippable.FlipAnimationType
import composable.flippable.FlippableController
import composable.flippable.FlippableState
import data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class CoinFlipViewModel(
    val settingsManager: SettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(CoinFlipState())
    val state: StateFlow<CoinFlipState> = _state.asStateFlow()

    val flipController = FlippableController()

    init {
        setNextResult()
    }

    private fun toggleAnimationType() {
        _state.value = state.value.copy(
            flipAnimationType = if (state.value.flipAnimationType == FlipAnimationType.VERTICAL_ANTI_CLOCKWISE) {
                FlipAnimationType.VERTICAL_CLOCKWISE
            } else {
                FlipAnimationType.VERTICAL_ANTI_CLOCKWISE
            }
        )
    }

    private fun setLastResult(coinFace: CoinFace) {
        _state.value = _state.value.copy(currentFace = coinFace)
        addToHistory(coinFace)
    }

    private fun addToHistory(coinFace: CoinFace) {
        _state.value.history.add(coinFace)
    }

    fun resetHistory() {
        _state.value.history.clear()
    }

    private fun decrementFlipCount() {
        _state.value = state.value.copy(flipCount = state.value.flipCount - 1)
        _state.value = state.value.copy(duration = state.value.duration + state.value.additionalDuration)
    }

    private fun setNextResult(nextResult: CoinFace? = null) {
        if (nextResult == null) {
            _state.value = state.value.copy(flipCount = state.value.totalFlips)
            if (Random.nextBoolean()) decrementFlipCount()
        } else if (state.value.currentFace != nextResult) {
            _state.value = state.value.copy(flipCount = state.value.totalFlips)
        } else {
            _state.value = state.value.copy(flipCount = state.value.totalFlips - 1)
        }
        _state.value = state.value.copy(duration = state.value.initialDuration)
    }

    private fun setFlipInProgress(inProgress: Boolean) {
        _state.value = state.value.copy(flipInProgress = inProgress)
    }

    fun continueFlip(): Boolean {
        setFlipInProgress(true)
        if (state.value.flipCount > 0) {
            toggleAnimationType()
            flipController.flip()
            decrementFlipCount()
            return false
        } else {
            setFlipInProgress(false)
            return true
        }
    }

    fun randomFlip() {
        setNextResult()
        continueFlip()
    }

    fun onResult(currentSide: FlippableState) {
        setLastResult(if (currentSide == FlippableState.FRONT) CoinFace.HEADS else CoinFace.TAILS)
    }
}