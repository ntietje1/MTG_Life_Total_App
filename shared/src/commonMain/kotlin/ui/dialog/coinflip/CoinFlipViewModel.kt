package ui.dialog.coinflip

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.storage.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class CoinFlipViewModel(
    val settingsManager: SettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(CoinFlipState())
    val state: StateFlow<CoinFlipState> = _state.asStateFlow()
    private val maxCoins = 64

    private var flippingUntil: CoinHistoryItem? = null

    private var onFlip: () -> Unit = {}

    val coinControllers = MutableList(1) {
        generateCoinController()
    }

    fun setOnFlip(onFlip: () -> Unit) {
        this.onFlip = onFlip
    }

    fun calculateCoinCount(baseCoins: Int = state.value.baseCoins, thumbs: Int = state.value.krarksThumbs): Int {
        return baseCoins * 2.0.pow(thumbs).toInt()
    }

    private fun findClosestSquare(value: Int): Int {
        val sqrt = sqrt(value.toDouble())
        val floor = sqrt.toInt()
        val ceil = sqrt.toInt() + 1
        return if ((value - floor * floor) < (ceil * ceil - value)) {
            floor
        } else {
            ceil
        }
    }

    fun rows(baseCoins: Int = state.value.baseCoins, thumbs: Int = state.value.krarksThumbs): Int {
        return findClosestSquare(calculateCoinCount(baseCoins, thumbs))
    }

    fun columns(baseCoins: Int = state.value.baseCoins, thumbs: Int = state.value.krarksThumbs): Int {
        val closestSqr = findClosestSquare(calculateCoinCount(baseCoins, thumbs))
        return if (closestSqr * closestSqr < calculateCoinCount(baseCoins, thumbs)) {
            closestSqr + 1
        } else {
            closestSqr
        }
    }

    fun buildLastResultString(): AnnotatedString {
        return buildAnnotatedString {
            if (state.value.lastResults.isEmpty() || (state.value.flipInProgress && flippingUntil == null)) {
                append(" ".repeat((coinControllers.size * 2.5f + 1).roundToInt()))
            } else if (state.value.lastResults.count { it == CoinHistoryItem.HEADS || it == CoinHistoryItem.TAILS } == 1) {
                append(" ")
                state.value.lastResults.forEach { result ->
                    when (result) {
                        CoinHistoryItem.HEADS -> {
                            withStyle(style = SpanStyle(color = result.color)) {
                                append("Heads")
                            }
                        }

                        CoinHistoryItem.TAILS -> {
                            withStyle(style = SpanStyle(color = result.color)) {
                                append("Tails")
                            }
                        }

                        else -> {
//                            append(result.letter)
                        }
                    }
                }
                append(" ")
            } else {
                append(" ")
                state.value.lastResults.forEach { result ->
                    withStyle(style = SpanStyle(color = result.color)) {
                        when (result) {
                            CoinHistoryItem.R_DIVIDER_LIST, CoinHistoryItem.R_DIVIDER_SINGLE -> {
                                append(" ")
                                append(result.letter)
                            }

                            CoinHistoryItem.COMMA, CoinHistoryItem.L_DIVIDER_LIST, CoinHistoryItem.L_DIVIDER_SINGLE -> {
                                append(result.letter)
                                append(" ")
                            }

                            else -> {
                                append(result.letter)
                            }
                        }
                    }
                }
                append(" ")
            }
        }
    }

//    fun repairHistoryString() {
//        while (state.value.history.count { it == CoinHistoryItem.L_DIVIDER_SINGLE } > state.value.history.count { it == CoinHistoryItem.R_DIVIDER_SINGLE }) {
//            _state.value = state.value.copy(history = state.value.history.subList(0, state.value.history.size - 1))
//        }
//
//        val leftDividerIndex = state.value.history.indexOfLast { it == CoinHistoryItem.L_DIVIDER_LIST }
//        val rightDividerIndex = state.value.history.indexOfLast { it == CoinHistoryItem.R_DIVIDER_LIST }
//        if (leftDividerIndex > rightDividerIndex) {
//            addToHistory(CoinHistoryItem.R_DIVIDER_LIST)
//        }
//    }

    fun buildHistoryString(): AnnotatedString {
        var index = 0
        val historySize = state.value.history.size
        return buildAnnotatedString {
            append(" ")
            while (index < historySize) {
                when (val coinHistoryItem = state.value.history[index]) {
                    CoinHistoryItem.L_DIVIDER_LIST -> {
                        val (params, counts) = parseHistory(index)
                        val (_, target) = params
                        val (heads, tails) = counts
                        withStyle(style = SpanStyle(color = CoinHistoryItem.L_DIVIDER_LIST.color)) {
                            append("${CoinHistoryItem.L_DIVIDER_LIST.letter} ")
                        }
                        when (target) {
                            CoinHistoryItem.HEADS -> {
                                withStyle(style = SpanStyle(color = CoinHistoryItem.TAILS.color)) {
                                    append("$tails Tails")
                                }
                                append(" in ")
                                withStyle(style = SpanStyle(color = CoinHistoryItem.HEADS.color)) {
                                    append("${heads + tails} Flips ")
                                }
                            }

                            CoinHistoryItem.TAILS -> {
                                withStyle(style = SpanStyle(color = CoinHistoryItem.HEADS.color)) {
                                    append("$heads Heads")
                                }
                                append(" in ")
                                withStyle(style = SpanStyle(color = CoinHistoryItem.TAILS.color)) {
                                    append("${heads + tails} Flips ")
                                }
                            }

                            else -> {
                                append(" Error :(")
                            }
                        }

                        withStyle(style = SpanStyle(color = CoinHistoryItem.R_DIVIDER_LIST.color)) {
                            append("${CoinHistoryItem.R_DIVIDER_LIST.letter} ")
                        }


                        val rightIndexSublist = state.value.history.subList(index, historySize).indexOf(CoinHistoryItem.R_DIVIDER_LIST)
                        index = if (rightIndexSublist != -1) rightIndexSublist + index else historySize
                    }

                    CoinHistoryItem.L_DIVIDER_SINGLE -> {
                        val (params, counts) = parseHistory(index)
                        val (multiMode, _) = params
                        val (heads, tails) = counts
                        if (multiMode) {
                            withStyle(style = SpanStyle(color = CoinHistoryItem.L_DIVIDER_SINGLE.color)) {
                                append("${CoinHistoryItem.L_DIVIDER_SINGLE.letter} ")
                            }

                            withStyle(style = SpanStyle(color = CoinHistoryItem.HEADS.color)) {
                                append("${heads}H")
                            }
                            withStyle(style = SpanStyle(color = CoinHistoryItem.L_DIVIDER_SINGLE.color)) {
                                append(" & ")
                            }
                            withStyle(style = SpanStyle(color = CoinHistoryItem.TAILS.color)) {
                                append("${tails}T ")
                            }

                            withStyle(style = SpanStyle(color = CoinHistoryItem.R_DIVIDER_SINGLE.color)) {
                                append("${CoinHistoryItem.R_DIVIDER_SINGLE.letter} ")
                            }
                        } else {
                            if (heads > 0) {
                                withStyle(style = SpanStyle(color = CoinHistoryItem.HEADS.color)) {
                                    append("H ")
                                }
                            } else if (tails > 0) {
                                withStyle(style = SpanStyle(color = CoinHistoryItem.TAILS.color)) {
                                    append("T ")
                                }
                            }
                        }

                        val rightIndexSublist = state.value.history.subList(index, historySize).indexOf(CoinHistoryItem.R_DIVIDER_SINGLE)
                        index = if (rightIndexSublist != -1) rightIndexSublist + index else historySize
                    }

                    CoinHistoryItem.R_DIVIDER_SINGLE, CoinHistoryItem.R_DIVIDER_LIST -> {
                        index++
                    }

                    else -> {
                        withStyle(style = SpanStyle(color = coinHistoryItem.color)) {
                            append("${coinHistoryItem.letter} ")
                        }
                        index++
                    }
                }
            }
        }
    }

    private fun parseHistory(startIndex: Int): Pair<Pair<Boolean, CoinHistoryItem?>, Pair<Int, Int>> {
        var countHeads = 0
        var countTails = 0
        var isCounting = false
        var target: CoinHistoryItem? = null
        var multiMode = false

        state.value.history.subList(startIndex, state.value.history.size).forEach { coinHistoryItem ->
            when (coinHistoryItem) {
                CoinHistoryItem.L_DIVIDER_LIST, CoinHistoryItem.L_DIVIDER_SINGLE -> {
                    isCounting = true
                    countHeads = 0
                    countTails = 0
                }

                CoinHistoryItem.R_DIVIDER_LIST, CoinHistoryItem.R_DIVIDER_SINGLE -> {
                    if (isCounting) {
                        isCounting = false
                        return Pair(Pair(multiMode, target), Pair(countHeads, countTails))
                    }
                }

                CoinHistoryItem.HEADS -> {
                    if (isCounting) {
                        countHeads++
                    }
                }

                CoinHistoryItem.TAILS -> {
                    if (isCounting) {
                        countTails++
                    }
                }

                CoinHistoryItem.HEADS_TARGET_MARKER -> {
                    target = CoinHistoryItem.HEADS
                }

                CoinHistoryItem.TAILS_TARGET_MARKER -> {
                    target = CoinHistoryItem.TAILS
                }

                CoinHistoryItem.MULTI_MODE_MARKER -> {
                    multiMode = true
                }

                CoinHistoryItem.SINGLE_MODE_MARKER -> {
                    multiMode = false
                }

                else -> {
                    // Do nothing
                }
            }
        }
        return Pair(Pair(multiMode, target), Pair(countHeads, countTails))
    }

    fun incrementBaseCoins(value: Int) {
        if (state.value.baseCoins + value <= 0 || state.value.flipInProgress || calculateCoinCount(baseCoins = state.value.baseCoins + value) > maxCoins) return
        _state.value = state.value.copy(baseCoins = state.value.baseCoins + value)
        updateNumberOfCoins()
    }

    fun incrementKrarksThumbs(value: Int) {
        if (state.value.krarksThumbs + value < 0 || state.value.flipInProgress || calculateCoinCount(thumbs = state.value.krarksThumbs + value) > maxCoins) return
        _state.value = state.value.copy(krarksThumbs = state.value.krarksThumbs + value)
        updateNumberOfCoins()
    }

    fun flipUntil(target: CoinHistoryItem) {
        resetLastResults()
        setFlipInProgress(true)
        setUserInteractionEnabled(false)
        flippingUntil = target
        addToHistory(CoinHistoryItem.L_DIVIDER_LIST)
        if (state.value.baseCoins > 1) {
            addToLastResults(CoinHistoryItem.L_DIVIDER_SINGLE)
        }
        when (target) {
            CoinHistoryItem.HEADS -> addToHistory(CoinHistoryItem.HEADS_TARGET_MARKER)
            CoinHistoryItem.TAILS -> addToHistory(CoinHistoryItem.TAILS_TARGET_MARKER)
            else -> return
        }
        addToHistory(CoinHistoryItem.MULTI_MODE_MARKER)
        val groupedCoinControllers = coinControllers.chunked(calculateCoinCount(baseCoins = 1))
        val done = MutableList(groupedCoinControllers.size) { false }
        for (index in groupedCoinControllers.indices) {
            val group = groupedCoinControllers[index]
            flipUntilHelper(target = target, coinControllers = group) {
                done[index] = true
                if (done.all { it }) {
                    setFlipInProgress(false)
                    setUserInteractionEnabled(true)
                    flippingUntil = null
                    addToHistory(CoinHistoryItem.R_DIVIDER_LIST)
                    if (state.value.baseCoins > 1) {
                        addToLastResults(CoinHistoryItem.R_DIVIDER_SINGLE)
                    }
                } else {
                    addToLastResults(CoinHistoryItem.R_DIVIDER_SINGLE)
                    addToLastResults(CoinHistoryItem.COMMA)
                    addToLastResults(CoinHistoryItem.L_DIVIDER_SINGLE)
                }
            }
        }
    }

    private fun flipUntilHelper(
        target: CoinHistoryItem, coinControllers: List<CoinController> = this.coinControllers, totalFlipResults: MutableList<CoinHistoryItem> = mutableListOf(), onDone: () -> Unit = {}
    ) {
        val flipResults = mutableListOf<CoinHistoryItem>()
        coinControllers.forEach { coinController ->
            coinController.randomFlip { result ->
                addToCounter(result)
                totalFlipResults.add(result)
                flipResults.add(result)
                if (flipResults.size != coinControllers.size) return@randomFlip // not all coins have flipped
                onFlip()
                if (flipResults.all { it == target }) { // done
                    totalFlipResults.forEach { addToLastResults(it) }
                    addToHistory(target)
                    onDone()
                } else { // keep flipping
                    addToHistory(if (target == CoinHistoryItem.HEADS) CoinHistoryItem.TAILS else CoinHistoryItem.HEADS)
                    viewModelScope.launch {
                        delay(250)
                        totalFlipResults.add(CoinHistoryItem.COMMA)
                        flipUntilHelper(target = target, coinControllers = coinControllers, totalFlipResults = totalFlipResults, onDone = onDone)
                    }
                }
            }
        }
    }

    fun singleFlip() {
        resetLastResults()
        setFlipInProgress(true)
        setUserInteractionEnabled(false)
        addToHistory(CoinHistoryItem.L_DIVIDER_SINGLE)
        if (calculateCoinCount() > 1) {
            addToHistory(CoinHistoryItem.MULTI_MODE_MARKER)
        } else {
            addToHistory(CoinHistoryItem.SINGLE_MODE_MARKER)
        }
        coinControllers.forEach { coinController ->
            coinController.randomFlip { res ->
                addToCounter(res)
                addToLastResults(res)
                addToHistory(res)
                val resultCount = state.value.lastResults.count { it == CoinHistoryItem.TAILS || it == CoinHistoryItem.HEADS }
                if (resultCount == coinControllers.size) {
                    onFlip()
                    addToHistory(CoinHistoryItem.R_DIVIDER_SINGLE)
                    setUserInteractionEnabled(true)
                    setFlipInProgress(false)
                } else if (resultCount % calculateCoinCount(baseCoins = 1) == 0) {
                    addToLastResults(CoinHistoryItem.COMMA)
                }
            }
        }
    }

    fun softReset() {
        resetCoinControllers()
        flippingUntil = null
        _state.value = state.value.copy(
            flipInProgress = false, userInteractionEnabled = true
        )
    }

    fun reset() {
        flippingUntil = null
        _state.value = state.value.copy(
            history = listOf(), lastResults = listOf(), headCount = 0, tailCount = 0, flipInProgress = false, userInteractionEnabled = false
        )
        resetCoinControllers()
        setUserInteractionEnabled(true)
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

    private fun addToLastResults(value: CoinHistoryItem) {
        val newResults = state.value.lastResults + value
        _state.value = state.value.copy(lastResults = newResults)
    }

    private fun addToCounter(value: CoinHistoryItem) {
        when (value) {
            CoinHistoryItem.HEADS -> _state.value = state.value.copy(headCount = state.value.headCount + 1)
            CoinHistoryItem.TAILS -> _state.value = state.value.copy(tailCount = state.value.tailCount + 1)
            else -> return
        }
    }

    private val maxHistoryLength = 256 + 2

    private fun addToHistory(coinHistoryItem: CoinHistoryItem) {
        val newHistory = (state.value.history + coinHistoryItem).takeLast(maxHistoryLength)
        _state.value = state.value.copy(
            history = newHistory,
        )
    }

    private fun resetCoinControllers() {
        coinControllers.forEach {
            it.reset()
        }
    }


    private fun generateCoinController(): CoinController {
        return CoinController(
            settingsManager = settingsManager,
        )
    }

    private fun updateNumberOfCoins() {
        val coinCount = calculateCoinCount()
        while (coinControllers.size > coinCount) {
            coinControllers.removeLast()
        }
        while (coinControllers.size < coinCount) {
            coinControllers.add(generateCoinController())
        }
    }
}