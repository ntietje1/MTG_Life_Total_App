package com.example.kotlinmtglifetotalapp.ui.lifecounter

import android.graphics.Color


data class Player(
    var life: Int,
    var playerNum: Int,
    var playerColor: Int
) {

    companion object {
        private var currentPlayers: Int = 0

        private var availableColors: MutableList<Int> = mutableListOf(
            Color.parseColor("#F75FA8"),
            Color.parseColor("#F75F5F"),
            Color.parseColor("#F7C45F"),
            Color.parseColor("#92F75F"),
            Color.parseColor("#5FEAF7"),
            Color.parseColor("#625FF7"),
            Color.parseColor("#C25FF7"),
        )

        fun generatePlayer(): Player {
            val maxLife = 40
            currentPlayers++
            availableColors.shuffle()
            val playerColor = availableColors.removeAt(0)
            return Player(maxLife, currentPlayers, playerColor)
        }
    }

    override fun toString() : String {
        return "P${playerNum}"
    }

    fun increment(value: Int) {
        this.life += value
    }
}