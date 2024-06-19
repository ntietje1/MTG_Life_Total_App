package composable.dialog.coinflip

import composable.flippable.FlipAnimationType
import composable.flippable.FlippableController
import data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class CoinController(
    private val settingsManager: SettingsManager,
) {
    companion object {
        private var animationCorrectionFactor = 1.0f
        fun setAnimationCorrectionFactor(value: Float) {
            animationCorrectionFactor = value
        }
        fun generateFlipDurations(fastCoinFlip: Boolean, changeSides: Boolean): List<Int> {
            val baseDuration = ((if (fastCoinFlip) 125 else 200) / animationCorrectionFactor).toInt()
            val additionalDuration = baseDuration / 2
            val totalFlips = (if (fastCoinFlip) 4 else 6) - (if (changeSides) 1 else 0)
            val durations = List(totalFlips) { index ->
                (baseDuration / totalFlips) + additionalDuration * (index + 1) / totalFlips +
                if (index == totalFlips - 1) additionalDuration / 2 else 0
            }
            return durations
        }
    }

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration

    val flipController = FlippableController()

    private var durations = generateFlipDurations(settingsManager.fastCoinFlip, Random.nextBoolean())

    var flipAnimationType: FlipAnimationType = FlipAnimationType.VERTICAL_ANTI_CLOCKWISE
    private var flipIndex: Int = 0
    private var currentFace: CoinFace = CoinFace.HEADS
    private var flipInProgress: Boolean = false
    private var onResultCallback: ((CoinFace) -> Unit)? = null

    init {
        setNextResult()
    }

    fun reset() {
//        onResultCallback = { reset() }
        onResultCallback = null
        flipIndex = durations.size
    }

    private fun updateDuration() {
        _duration.value = try {
            durations[flipIndex]
        } catch (e: IndexOutOfBoundsException) {
            durations.last()
        }
    }

    private fun resetFlipIndex() {
        flipIndex = 0
        updateDuration()
    }

    private fun incrementFlipIndex() {
        flipIndex += 1
        updateDuration()
    }

    private fun updateFlipInProgress(value: Boolean) {
        flipInProgress = value
    }

    // method hat "resets" the coin flip, should be called before each flip
    private fun setNextResult(nextResult: CoinFace? = null) {
        flipAnimationType = FlipAnimationType.VERTICAL_CLOCKWISE
        if (nextResult == null) {
            flipIndex = 0
            durations = generateFlipDurations(settingsManager.fastCoinFlip, Random.nextBoolean())
        } else if (currentFace != nextResult) {
            durations = generateFlipDurations(settingsManager.fastCoinFlip, true)
        } else {
            durations = generateFlipDurations(settingsManager.fastCoinFlip, false)
        }
    }

    private fun toggleAnimationType() {
        flipAnimationType = if (flipAnimationType == FlipAnimationType.VERTICAL_ANTI_CLOCKWISE) {
            FlipAnimationType.VERTICAL_CLOCKWISE
        } else {
            FlipAnimationType.VERTICAL_ANTI_CLOCKWISE
        }
    }

    fun continueFlip(): Boolean {
        updateFlipInProgress(true)
        incrementFlipIndex()
        toggleAnimationType()
        if (flipIndex < durations.size) {
            flipController.flip()
            return false
        } else {
            resetFlipIndex()
            updateFlipInProgress(false)
            return true
        }
    }

    fun randomFlip(onResult: ((CoinFace) -> Unit)? = null) {
        onResult?.let { onResultCallback = it }
        setNextResult()
        continueFlip()
    }

    fun onResult(currentSide: CoinFace) {
        currentFace = currentSide
        onResultCallback?.let { it((currentSide)) }
    }
}