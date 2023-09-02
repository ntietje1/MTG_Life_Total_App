package com.example.kotlinmtglifetotalapp.ui.home

class PointT(x: Float, y: Float) {
    private val historyX: MutableList<Float> = mutableListOf(x)
    private val historyY: MutableList<Float> = mutableListOf(y)

    var x: Float
        get() = historyX.last()
        set(value) {
            historyX.add(value)
            if (historyX.size > 5) {
                historyX.removeAt(0)
            }
        }

    var y: Float
        get() = historyY.last()
        set(value) {
            historyY.add(value)
            if (historyY.size > 5) {
                historyY.removeAt(0)
            }
        }

    val nextX: Float
        get() {
            var adj = 0f
            for (i in 0 until historyY.size) {
                adj += (5-i) * (x - historyX[i])
            }
            return x + adj / 30
        }

    val nextY: Float
        get() {
            var adj = 0f
            for (i in 0 until historyY.size) {
                adj += (5-i) * (y - historyY[i])
            }
            return y + adj / 30
        }

    var size: Float = 1f

    init {
    }
}
