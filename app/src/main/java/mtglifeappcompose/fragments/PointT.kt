package mtglifeappcompose.fragments

import kotlin.math.pow

class PointT(x: Float, y: Float) {
    private val historyX: MutableList<Float> = mutableListOf(x)
    private val historyY: MutableList<Float> = mutableListOf(y)

    companion object {
        private const val HISTORY_SIZE = 7f
    }

    var x: Float
        get() = historyX.last()
        set(value) {
            historyX.add(value)
            if (historyX.size > HISTORY_SIZE) {
                historyX.removeAt(0)
            }
        }

    var y: Float
        get() = historyY.last()
        set(value) {
            historyY.add(value)
            if (historyY.size > HISTORY_SIZE) {
                historyY.removeAt(0)
            }
        }

    val nextX: Float
        get() {
            var adj = 0f
            for (i in 0 until historyY.size) {
                adj += (i + 1f).pow(HISTORY_SIZE - i) * (x - historyX[i])
            }
            return x + adj / HISTORY_SIZE.pow(HISTORY_SIZE / 1.85f)
        }

    val nextY: Float
        get() {
            var adj = 0f
            for (i in 0 until historyY.size) {
                adj += (i + 1f).pow(HISTORY_SIZE -i) * (y - historyY[i])
            }
            return y + adj / HISTORY_SIZE.pow(HISTORY_SIZE / 1.85f)
        }

    private val baseSize = 1.0f

    val size get() = baseSize * sizeMultiplier

    var sizeMultiplier: Float = 1f

    init {
    }
}
