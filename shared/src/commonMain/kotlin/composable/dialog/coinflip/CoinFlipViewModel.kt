package composable.dialog.coinflip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.floor
import kotlin.math.pow

class CoinFlipViewModel(
    val settingsManager: SettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(CoinFlipState())
    val state: StateFlow<CoinFlipState> = _state.asStateFlow()

    private var flippingUntil: CoinFace? = null
    private var triggerCancel: Boolean = false

    val coinControllers = MutableList(1) {
        generateCoinController()
    }

    val flipInProgress: Boolean
        get() = coinControllers.any { it.flipInProgress }

    fun rows(thumbs: Int = state.value.krarksThumbs): Int {
        return 2.0.pow(floor(thumbs / 2.0)).toInt()
    }

    fun columns(thumbs: Int = state.value.krarksThumbs): Int {
        return rows(thumbs + 1)
    }

    fun incrementKrarksThumbs(value: Int) {
        if (state.value.krarksThumbs + value < 0) return
        _state.value = state.value.copy(krarksThumbs = state.value.krarksThumbs + value)
        updateNumberOfCoins()
    }

    fun flipUntil(target: CoinFace) {
        triggerCancel = false
        resetLastResults()
        addToHistory(CoinFace.L_DIVIDER)
        flippingUntil = target
        flipUntilHelper(target)
    }

    private fun flipUntilHelper(target: CoinFace) {
        val flipResults = mutableListOf<CoinFace>()
        coinControllers.forEach { coinController ->
            coinController.randomFlip { result ->
                addToCounter(result)
                addToLastResults(result)
                flipResults.add(result)
                if (flipResults.size == coinControllers.size) { // end case
                    if (triggerCancel) { // reset triggered
                        reset()
                        triggerCancel = false
                        flippingUntil = null
                        return@randomFlip
                    }
                    else if (flipResults.all { it == target }) { // done
                        flippingUntil = null
                        addToHistory(target)
                        addToHistory(CoinFace.R_DIVIDER)
                    } else { // keep flipping
                        addToHistory(if (target == CoinFace.HEADS) CoinFace.TAILS else CoinFace.HEADS)
                        addToHistory(CoinFace.COMMA)
                        addToLastResults(CoinFace.COMMA)
                        viewModelScope.launch {
                            delay(250)
                            flipUntilHelper(target)
                        }
                    }
                }
            }
        }
    }

    fun randomFlip() {
        resetLastResults()
        addToHistory(CoinFace.L_DIVIDER)
        coinControllers.forEachIndexed { index, coinController ->
            coinController.randomFlip {
                addToCounter(it)
                addToLastResults(it)
                addToHistory(it)
                if (state.value.lastResults.size == coinControllers.size) {
                    addToHistory(CoinFace.R_DIVIDER)
                }
            }
        }
    }

    fun reset() {
        cancelFlipUntil()
        _state.value = state.value.copy(
            history = listOf(),
            lastResults = listOf(),
            headCount = 0,
            tailCount = 0
        )
    }

    private fun resetLastResults() {
        _state.value = state.value.copy(lastResults = listOf())
    }

    private fun addToLastResults(value: CoinFace) {
        val newResults = state.value.lastResults + value
        _state.value = state.value.copy(lastResults = newResults)
    }

    private fun addToCounter(value: CoinFace) {
        when (value) {
            CoinFace.HEADS -> _state.value = state.value.copy(headCount = state.value.headCount + 1)
            CoinFace.TAILS -> _state.value = state.value.copy(tailCount = state.value.tailCount + 1)
            else -> return
        }
    }

    private fun addToHistory(coinFace: CoinFace) {
        val newHistory = state.value.history + coinFace
        _state.value = state.value.copy(
            history = newHistory,
        )
    }

    private fun cancelFlipUntil() {
        triggerCancel = true
        flippingUntil = null
    }

    private fun generateCoinController(): CoinController {
        return CoinController(
            settingsManager = settingsManager
        )
    }

    private fun updateNumberOfCoins() {
        while (coinControllers.size > 2.toDouble().pow(state.value.krarksThumbs)) {
            coinControllers.removeLast()
        }
        while (coinControllers.size < 2.toDouble().pow(state.value.krarksThumbs)) {
            coinControllers.add(generateCoinController())
        }
    }
}