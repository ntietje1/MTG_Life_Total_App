package composable.dialog.coinflip

import composable.flippable.FlipAnimationType
import composable.flippable.FlippableController
import data.SettingsManager
import kotlin.random.Random

class CoinController(
    private val settingsManager: SettingsManager,
) {
    companion object {
        fun generateFlipDurations(fastCoinFlip: Boolean, changeSides: Boolean): List<Int> {
            val totalDuration = if (fastCoinFlip) 500 else 750
            val totalFlips = if (changeSides) 3 else 4
            val durations = List(totalFlips) { totalDuration / totalFlips }
            return durations
        }
    }

    val duration: Int
        get() = try {
            durations[flipIndex]
        } catch (e: IndexOutOfBoundsException) {
            0
        }

    private var durations = generateFlipDurations(settingsManager.fastCoinFlip, Random.nextBoolean())

    var flipAnimationType: FlipAnimationType = FlipAnimationType.VERTICAL_ANTI_CLOCKWISE
    private var flipIndex: Int = 0
    private var currentFace: CoinFace = CoinFace.HEADS
    var flipInProgress: Boolean = false
    private var onResultCallback: ((CoinFace) -> Unit)? = null

    val flipController = FlippableController()

    init {
        setNextResult()
    }

    private fun updateFlipInProgress(value: Boolean) {
        flipInProgress = value
    }

    // method hat "resets" the coin flip, should be called before each flip
    private fun setNextResult(nextResult: CoinFace? = null) {
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

    private fun incrementFlipIndex() {
        flipIndex += 1
    }

    fun continueFlip(): Boolean {
        updateFlipInProgress(true)
        if (flipIndex < durations.size) {
            toggleAnimationType()
            flipController.flip()
            incrementFlipIndex()
            return false
        } else {
            flipIndex = 0
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