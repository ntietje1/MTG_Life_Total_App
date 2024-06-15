package composable.dialog.coinflip

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.ViewModel
import data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CoinFlipViewModel(
    val settingsManager: SettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(CoinFlipState())
    val state: StateFlow<CoinFlipState> = _state.asStateFlow()
    private val maxHistoryLength: Int = 19

    val coinControllers = List(1) {
        CoinController(settingsManager) { coinFace ->
            addToCounter(coinFace)
            addToHistory(coinFace)
        }
    }

    fun toggleKrarksThumbs() { //TODO: this should really be an incrementable amount
        _state.value = state.value.copy(krarksThumbs = if (state.value.krarksThumbs == 0) 1 else 0)
    }

    fun flipFor(target: CoinFace) {
        coinControllers.forEach { coinController ->
            coinController.randomFlip()
        }
    }

    //TODO: "last result" is a variable in here that agregates the last results from the coinControllers
    fun flipUntil(target: CoinFace) { //TODO: this needs to look at result of all coins, then determine if continue flip
        coinControllers.forEach { coinController ->
            coinController.flipUntil(target)
        }
    }

    /**
     *TODO: if 1 or more krark's thumbs, can't tap on actual coin anymore. must use "flip for heads" or "flip for tails" or "flip until..." buttons
     */
    fun randomFlip() {
        coinControllers.first().randomFlip()
    }

    val flipInProgress: Boolean
        get() = false //TODO: implement

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
            historyString = AnnotatedString(" "),
            headCount = 0,
            tailCount = 0
        )
    }

}