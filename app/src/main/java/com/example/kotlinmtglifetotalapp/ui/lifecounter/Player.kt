package com.example.kotlinmtglifetotalapp.ui.lifecounter

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable


data class Player(
    var life: Int,
    var playerNum: Int,
    var playerColor: Int
): Parcelable {

    companion object CREATOR : Parcelable.Creator<Player>{
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

        override fun createFromParcel(parcel: Parcel): Player {
            return Player(parcel)
        }

        override fun newArray(size: Int): Array<Player?> {
            return arrayOfNulls(size)
        }

        fun generatePlayer(): Player {
            val maxLife = 40
            currentPlayers++
            availableColors.shuffle()
            val playerColor = availableColors.removeAt(0)
            return Player(maxLife, currentPlayers, playerColor)
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun toString() : String {
        return "P${playerNum}"
    }

    fun increment(value: Int) {
        this.life += value
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(life)
        parcel.writeInt(playerNum)
        parcel.writeInt(playerColor)
    }

    override fun describeContents(): Int {
        return 0
    }
}