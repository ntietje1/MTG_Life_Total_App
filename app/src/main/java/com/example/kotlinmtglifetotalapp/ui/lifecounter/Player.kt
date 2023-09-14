package com.example.kotlinmtglifetotalapp.ui.lifecounter

import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable

// add player name functionality
data class Player(
    private var _life: Int,
    private val playerNum: Int,
    private val originalPlayerColor: Int
): Parcelable {

    val life: Int
        get() = _life

    val playerColor: Int
        get() = if (life <= 0) Color.GRAY else originalPlayerColor

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
    )

    override fun toString() : String {
        return "P${playerNum}"
    }

    fun increment(value: Int) {
        this._life += value
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(life)
        parcel.writeInt(playerNum)
        parcel.writeInt(originalPlayerColor)
    }

    override fun describeContents(): Int {
        return 0
    }
}