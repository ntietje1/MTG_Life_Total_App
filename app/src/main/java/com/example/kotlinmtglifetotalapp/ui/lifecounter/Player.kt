package com.example.kotlinmtglifetotalapp.ui.lifecounter

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable

// add player name functionality
class Player(
    private var _life: Int,
    val playerNum: Int,
    private val originalPlayerColor: Int
): Parcelable {

    val life: Int
        get() = _life

    val playerColor: Int
        get() = if (isDead) Color.GRAY else originalPlayerColor

    val commanderDamage: ArrayList<Int> = arrayListOf<Int>().apply {
        for (i in 0 until MAX_PLAYERS) {
            add(0)
        }
    }

    private val isDead get() = life <= 0

    private var _recentChange = 0
    val recentChange get() = _recentChange


    private val handler: Handler = Handler(Looper.getMainLooper())

    private val resetRecentChangeRunnable = Runnable {
        zeroRecentChange()
        notifyObserver()
    }

    private var observer: PlayerObserver? = null

    fun setObserver(observer: PlayerObserver) {
        this.observer = observer
    }

    private fun notifyObserver() {
        observer?.onPlayerUpdated()
    }


    override fun toString() : String {
        return "P${playerNum}"
    }

    fun incrementLife(value: Int) {
        this._life += value
        _recentChange += value
        resetRecentChangeRunnable()
        notifyObserver()
    }

    fun incrementCommander(value: Int) {
        this._life -= value
        _recentChange += value
        resetRecentChangeRunnable()
        notifyObserver()
    }

    private fun resetRecentChangeRunnable() {
        handler.removeCallbacks(resetRecentChangeRunnable)
        handler.postDelayed(resetRecentChangeRunnable, 1500)
    }

    fun zeroRecentChange() {
        _recentChange = 0
    }

    // Parcelable related code

    companion object CREATOR : Parcelable.Creator<Player>{
        private const val MAX_PLAYERS = 6
        var currentPlayers: ArrayList<Player> = arrayListOf()

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
            availableColors.shuffle()
            val playerColor = availableColors.removeAt(0)
            val player = Player(maxLife, currentPlayers.size + 1, playerColor)
            currentPlayers.add(player)
            return player
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(life)
        parcel.writeInt(playerNum)
        parcel.writeInt(originalPlayerColor)
    }

    override fun describeContents(): Int {
        return 0
    }
}