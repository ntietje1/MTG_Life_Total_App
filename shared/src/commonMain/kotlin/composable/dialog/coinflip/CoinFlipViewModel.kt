package composable.dialog.coinflip

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import composable.flippable.FlipAnimationType
import composable.flippable.FlippableController
import data.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class CoinFlipViewModel(
    val settingsManager: SettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(CoinFlipState())
    val state: StateFlow<CoinFlipState> = _state.asStateFlow()
    private val totalFlips: Int = 3
    private val initialDuration: Int = if (settingsManager.fastCoinFlip) 115 else 175
    private val additionalDuration: Int = 20
    private val maxHistoryLength: Int = 19
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

    private fun setCurrentFace(coinFace: CoinFace) {
        _state.value = _state.value.copy(currentFace = coinFace)
    }

    private fun addToHistory(coinFace: CoinFace) {
        if (coinFace != CoinFace.DIVIDER) {
            setCurrentFace(coinFace)
        }
        val newHistory = state.value.history + coinFace
        _state.value = state.value.copy(
            history = newHistory,
            historyString = buildAnnotatedString {
                newHistory.takeLast(maxHistoryLength).forEach { result ->
                    withStyle(style = SpanStyle(color = result.color)) {
                        append("${result.letter} ")
                    }
                }
            }
        )
    }

    fun reset() {
        _state.value = state.value.copy(
            history = listOf(),
            historyString = AnnotatedString(""),
            headCount = 0,
            tailCount = 0
        )
    }

    private fun decrementFlipCount() {
        setFlipCount(state.value.flipCount - 1)
        setDuration(state.value.duration + additionalDuration)
    }

    // method hat "resets" the coin flip, should be called before each flip
    private fun setNextResult(nextResult: CoinFace? = null) {
        setDuration(initialDuration)
        if (nextResult == null) {
            setFlipCount(totalFlips)
            if (Random.nextBoolean()) decrementFlipCount()
        } else if (state.value.currentFace != nextResult) {
            setFlipCount(totalFlips)
        } else {
            setFlipCount(totalFlips - 1)
        }
    }

    private fun setFlipInProgress(inProgress: Boolean) {
        _state.value = state.value.copy(flipInProgress = inProgress)
    }

    private fun setFlipCount(count: Int) {
        _state.value = state.value.copy(flipCount = count)
    }

    private fun setDuration(duration: Int) {
        _state.value = state.value.copy(duration = duration)
    }

    private fun setFlippingUntil(target: CoinFace?) {
        _state.value = state.value.copy(flippingUntil = target)
    }

    fun continueFlip(): Boolean {
        setFlipInProgress(true)
        if (state.value.flipCount > 0) {
            toggleAnimationType()
            flipController.flip()
            decrementFlipCount()
            return false
        } else {
            viewModelScope.launch {
                delay(150)
                setFlipInProgress(false)
            }

            return true
        }
    }

    fun randomFlip() {
        setNextResult()
        continueFlip()
    }

    fun flipUntil(target: CoinFace) {
        if (state.value.history.lastOrNull() != CoinFace.DIVIDER) addToHistory(CoinFace.DIVIDER)
        setFlippingUntil(target)
        randomFlip()
    }

    private fun addToCounter(value: CoinFace) {
        when (value) {
            CoinFace.HEADS -> _state.value = state.value.copy(headCount = state.value.headCount + 1)
            CoinFace.TAILS -> _state.value = state.value.copy(tailCount = state.value.tailCount + 1)
            else -> return
        }
    }

    fun onResult(currentSide: CoinFace) {
        if (state.value.flippingUntil == null)  {
            addToHistory(currentSide)
            addToCounter(currentSide)
        } else if (currentSide == state.value.flippingUntil) {
            setFlippingUntil(null)
            addToCounter(currentSide)
            addToHistory(currentSide)
            addToHistory(CoinFace.DIVIDER)
        } else {
            addToHistory(currentSide)
            randomFlip()
        }
    }
}