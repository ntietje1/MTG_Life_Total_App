package composable.dialog.coinflip

import composable.flippable.FlipAnimationType
import composable.flippable.FlippableController
import data.SettingsManager
import kotlin.random.Random

class CoinController(
    settingsManager: SettingsManager,
) {
    private val totalFlips: Int = 3
    private val initialDuration: Int = if (settingsManager.fastCoinFlip) 115 else 175
    private val additionalDuration: Int = 20

    var flipAnimationType: FlipAnimationType = FlipAnimationType.VERTICAL_ANTI_CLOCKWISE
    private var flipCount: Int = Int.MAX_VALUE
    var duration: Int = Int.MAX_VALUE
    private var currentFace: CoinFace = CoinFace.HEADS
    var flipInProgress: Boolean = false
    private var onResultCallback: ((CoinFace) -> Unit)? = null
//    private var resultCallback: ((CoinFace) -> Unit)? = null


    val flipController = FlippableController()

    init {
        setNextResult()
    }

    private fun updateFlipInProgress(value: Boolean) {
        flipInProgress = value
    }

    // method hat "resets" the coin flip, should be called before each flip
    private fun setNextResult(nextResult: CoinFace? = null) {
        duration = initialDuration
        if (nextResult == null) {
            flipCount = totalFlips
            if (Random.nextBoolean()) decrementFlipCount()
        } else if (currentFace != nextResult) {
            flipCount = totalFlips
        } else {
            flipCount = totalFlips - 1
        }
    }

    private fun toggleAnimationType() {
        flipAnimationType = if (flipAnimationType == FlipAnimationType.VERTICAL_ANTI_CLOCKWISE) {
            FlipAnimationType.VERTICAL_CLOCKWISE
        } else {
            FlipAnimationType.VERTICAL_ANTI_CLOCKWISE
        }
    }

    private fun decrementFlipCount() {
        flipCount -= 1
        duration += additionalDuration
    }

    fun continueFlip(): Boolean {
        updateFlipInProgress(true)
        if (flipCount > 0) {
            toggleAnimationType()
            flipController.flip()
            decrementFlipCount()
            return false
        } else {
//            CoroutineScope(Dispatchers.IO).launch {
//                delay(150)
                updateFlipInProgress(false)
//            }
            return true
        }
    }

//    suspend fun randomFlip(): CoinFace = suspendCancellableCoroutine { continuation ->
//        resultCallback = { result ->
//            continuation.resume(result)
//            resultCallback = null
//        }
//        setNextResult()
//        continueFlip()
//    }
//
//    suspend fun flipUntil(target: CoinFace): CoinFace = suspendCancellableCoroutine { continuation ->
//        resultCallback = { result ->
//            if (result == target) {
//                continuation.resume(result)
//                resultCallback = null
//            } else {
//                randomFlip()
//            }
//        }
//        flippingUntil = target
//        randomFlip()
//    }

    fun randomFlip(onResult: ((CoinFace) -> Unit)? = null) {
        onResult?.let { onResultCallback = it }
        setNextResult()
        continueFlip()
    }

    fun onResult(currentSide: CoinFace) {
        currentFace = currentSide
        onResultCallback!!(currentSide)
    }
//        if (flippingUntil == null)  {
//            currentFace = currentSide
//            onResultCallback!!(currentSide)
////            addToHistory(currentSide)
////            addToCounter(currentSide)
//        } else if (currentSide == flippingUntil) {
//            flippingUntil = null
//            currentFace = currentSide
//            onResultCallback!!(currentSide)
//            addRightDivider()
////            addToCounter(currentSide)
////            addToHistory(currentSide)
////            addToHistory(CoinFace.DIVIDER)
//        } else {
////            addToHistory(currentSide)
//            onResultCallback!!(currentSide)
//            randomFlip()
//        }
//    }
}