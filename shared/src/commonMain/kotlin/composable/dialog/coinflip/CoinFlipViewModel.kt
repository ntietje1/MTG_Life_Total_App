package composable.dialog.coinflip

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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
import kotlin.math.roundToInt

class CoinFlipViewModel(
    val settingsManager: SettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(CoinFlipState())
    val state: StateFlow<CoinFlipState> = _state.asStateFlow()

    private var flippingUntil: CoinFace? = null

    val coinControllers = MutableList(1) {
        generateCoinController()
    }

    fun rows(thumbs: Int = state.value.krarksThumbs): Int {
        return 2.0.pow(floor(thumbs / 2.0)).toInt()
    }

    fun columns(thumbs: Int = state.value.krarksThumbs): Int {
        return rows(thumbs + 1)
    }

    fun buildLastResultString(): AnnotatedString {
        return buildAnnotatedString {
            if (state.value.lastResults.isEmpty() || (state.value.flipInProgress && flippingUntil == null)) {
                append(" ".repeat((coinControllers.size * 3.85f + 1).roundToInt()))
            } else {
                append(" ")
                state.value.lastResults.forEach { result ->
                    withStyle(style = SpanStyle(color = result.color)) {
                        append("${result.letter} ")
                    }
                }
            }
        }
    }

    fun repairHistoryString() {
        val leftDividerIndex = state.value.history.indexOfLast { it == CoinFace.L_DIVIDER_LIST }
        val rightDividerIndex = state.value.history.indexOfLast { it == CoinFace.R_DIVIDER_LIST }
        if (leftDividerIndex > rightDividerIndex) {
            addToHistory(CoinFace.R_DIVIDER_LIST)
        }
    }

    fun buildHistoryString(): AnnotatedString {
        var index = 0
        val historySize = state.value.history.size
        return buildAnnotatedString {
            append(" ")
            while (index < historySize) {
                val coinFace = state.value.history[index]
                withStyle(style = SpanStyle(color = coinFace.color)) {
                    append("${coinFace.letter} ")
                }
                when (coinFace) {
                    CoinFace.L_DIVIDER_LIST -> {
                        val (heads, tails) = parseHistory(index)
                        withStyle(style = SpanStyle(color = CoinFace.HEADS.color)) {
                            append("${heads}H")
                        }
                        withStyle(style = SpanStyle(color = CoinFace.L_DIVIDER_LIST.color)) {
                            append(" / ")
                        }
                        withStyle(style = SpanStyle(color = CoinFace.TAILS.color)) {
                            append("${tails}T ")
                        }
                        val rightIndexSublist = state.value.history.subList(index, historySize).indexOf(CoinFace.R_DIVIDER_LIST)
                        index = if (rightIndexSublist != -1) rightIndexSublist + index else historySize
                    }

                    else -> {
                        index++
                    }
                }
            }
        }
    }

    private fun parseHistory(startIndex: Int): Pair<Int, Int> {
        var countHeads = 0
        var countTails = 0
        var isCounting = false

        state.value.history.subList(startIndex, state.value.history.size).forEach { coinFace ->
            when (coinFace) {
                CoinFace.L_DIVIDER_LIST -> {
                    isCounting = true
                    countHeads = 0
                    countTails = 0
                }

                CoinFace.R_DIVIDER_LIST -> {
                    if (isCounting) {
                        isCounting = false
                        return Pair(countHeads, countTails)
                    }
                }

                CoinFace.HEADS -> {
                    if (isCounting) {
                        countHeads++
                    }
                }

                CoinFace.TAILS -> {
                    if (isCounting) {
                        countTails++
                    }
                }

                else -> {
                    // Do nothing
                }
            }
        }
        return Pair(countHeads, countTails)
    }

    fun incrementKrarksThumbs(value: Int) {
        if (state.value.krarksThumbs + value < 0 || state.value.flipInProgress) return
        _state.value = state.value.copy(krarksThumbs = state.value.krarksThumbs + value)
        updateNumberOfCoins()
    }

    fun flipUntil(target: CoinFace) {
        resetLastResults()
        setFlipInProgress(true)
        setUserInteractionEnabled(false)
        flippingUntil = target
        addToHistory(CoinFace.L_DIVIDER_LIST)
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
                    if (flipResults.all { it == target }) { // done
                        setFlipInProgress(false)
                        setUserInteractionEnabled(true)
                        flippingUntil = null
                        addToHistory(target)
                        addToHistory(CoinFace.R_DIVIDER_LIST)
                    } else { // keep flipping
                        addToHistory(if (target == CoinFace.HEADS) CoinFace.TAILS else CoinFace.HEADS)
//                        addToHistory(CoinFace.COMMA)
                        addToLastResults(CoinFace.COMMA)
                        viewModelScope.launch {
                            setFlipInProgress(false)
                            delay(100)
                            setFlipInProgress(true)
                            delay(150)
                            if (state.value.flipInProgress) {
                                flipUntilHelper(target)
                            }
                        }
                    }
                }
            }
        }
    }

    fun randomFlip() {
        resetLastResults()
        setFlipInProgress(true)
        setUserInteractionEnabled(false)
        addToHistory(CoinFace.L_DIVIDER_SINGLE)
        coinControllers.forEach { coinController ->
            coinController.randomFlip {
                addToCounter(it)
                addToLastResults(it)
                addToHistory(it)
                if (state.value.lastResults.size == coinControllers.size) {
                    addToHistory(CoinFace.R_DIVIDER_SINGLE)
                    setUserInteractionEnabled(true)
                    setFlipInProgress(false)
                }
            }
        }
    }

    fun reset() {
        resetCoinControllers()
        flippingUntil = null
        _state.value = state.value.copy(
            history = listOf(), lastResults = listOf(), headCount = 0, tailCount = 0, flipInProgress = false, userInteractionEnabled = false
        )
        viewModelScope.launch {
            delay(100)
            setUserInteractionEnabled(true)
        }
    }

    private fun setUserInteractionEnabled(value: Boolean) {
        _state.value = state.value.copy(userInteractionEnabled = value)
    }

    private fun setFlipInProgress(value: Boolean) {
        _state.value = state.value.copy(flipInProgress = value)
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

    private val maxHistoryLength = 256 + 2

    private fun addToHistory(coinFace: CoinFace) {
        val newHistory = (state.value.history + coinFace).takeLast(maxHistoryLength)
        _state.value = state.value.copy(
            history = newHistory,
        )
    }

    fun resetCoinControllers() {
        coinControllers.forEach {
            it.reset()
        }
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