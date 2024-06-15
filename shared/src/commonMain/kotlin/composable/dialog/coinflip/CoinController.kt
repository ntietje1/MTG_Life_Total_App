package composable.dialog.coinflip

import composable.flippable.FlipAnimationType
import composable.flippable.FlippableController
import data.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class CoinController(
    settingsManager: SettingsManager,
    private val notifyResult: (CoinFace) -> Unit
) {
    private val totalFlips: Int = 3
    private val initialDuration: Int = if (settingsManager.fastCoinFlip) 115 else 175
    private val additionalDuration: Int = 20

    var flipAnimationType: FlipAnimationType = FlipAnimationType.VERTICAL_ANTI_CLOCKWISE
    private var flippingUntil: CoinFace? = null
    private var flipCount: Int = Int.MAX_VALUE
    var duration: Int = Int.MAX_VALUE
    private var currentFace: CoinFace = CoinFace.HEADS
    var flipInProgress: Boolean = false
//    private var resultCallback: ((CoinFace) -> Unit)? = null


    val flipController = FlippableController()

    init {
        setNextResult()
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
        flipInProgress = true
        if (flipCount > 0) {
            toggleAnimationType()
            flipController.flip()
            decrementFlipCount()
            return false
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                delay(150)
                flipInProgress = false
            }
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

    fun randomFlip() {
        setNextResult()
        continueFlip()
    }

    fun flipUntil(target: CoinFace) {
//        if (state.value.history.lastOrNull() != CoinFace.DIVIDER) addToHistory(CoinFace.DIVIDER)
        flippingUntil = target
        randomFlip()
    }

    fun onResult(currentSide: CoinFace) {
        if (flippingUntil == null)  {
            currentFace = currentSide
            notifyResult(currentSide)
//            addToHistory(currentSide)
//            addToCounter(currentSide)
        } else if (currentSide == flippingUntil) {
            flippingUntil = null
            currentFace = currentSide
            notifyResult(currentSide)
//            addToCounter(currentSide)
//            addToHistory(currentSide)
//            addToHistory(CoinFace.DIVIDER)
        } else {
//            addToHistory(currentSide)
            randomFlip()
        }
    }
}