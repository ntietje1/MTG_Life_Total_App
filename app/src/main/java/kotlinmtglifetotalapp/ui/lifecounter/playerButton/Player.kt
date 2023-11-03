package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import java.util.Collections.addAll

// add player name functionality
class Player(
    private var _life: Int,
    public var playerColor: Int = Color.LTGRAY,
    private var _monarch: Boolean = false
) : Parcelable {

    val playerNum get() = currentPlayers.indexOf(this) + 1

    val life: Int
        get() = _life

    var monarch: Boolean
        get() = _monarch
        set(v) = run {
            if (v) {
                for (player in currentPlayers) {
                    player.monarch = false
                }
            }
            this._monarch = v
            notifyObserver()
        }

    var name: String = "#Placeholder"

    val commanderDamage: ArrayList<Int> = arrayListOf<Int>().apply {
        for (i in 0 until MAX_PLAYERS) {
            add(0)
        }
    }

    val isDead get() = (life <= 0)

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

    fun resetPlayer() {
        _life = startingLife
        monarch = false
        resetCommanderDamage()
        notifyObserver()
    }

    private fun resetCommanderDamage() {
        for (i in 0 until 6) {
            commanderDamage.removeAt(0)
            commanderDamage.add(0)
        }
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

    override fun toString(): String {
        return "$name:$playerColor"
    }

    fun fromString(s: String) {
        println("PARSING PLAYER:$s")
        val parts = s.split(":")
        if (parts.size == 2) {
            name = parts[0]
            playerColor = parts[1].toInt()
        } else {
            return
        }
    }


    companion object CREATOR : Parcelable.Creator<Player> {
        private const val MAX_PLAYERS = 6
        var currentPlayers: ArrayList<Player> = arrayListOf()
        var startingLife = 40

        fun allToString(players: ArrayList<Player>): String {
            var res = ""
            if (players.isEmpty()) {
                return res
            }
            for (player in players) {
                val pString = player.toString()
                res += "$pString,"
            }
            return res.substring(0, res.length - 1)
        }
//
//        /**
//         * Return a list of strings representing editable player data
//         */
//        fun allFromString(s: String): ArrayList<Player> {
//            val res = arrayListOf<Player>()
//            val pstrings = s.split(",")
//            for (p in pstrings) {
//                val player = Player(40, 0)
//                player.fromString(p)
//                res.add(player)
//            }
//            return res
//        }

        val allColors: ArrayList<Int> = arrayListOf(
            Color.parseColor("#F75FA8"),
            Color.parseColor("#F75F5F"),
            Color.parseColor("#F7C45F"),
            Color.parseColor("#92F75F"),
            Color.parseColor("#5FEAF7"),
            Color.parseColor("#625FF7"),
            Color.parseColor("#f78e55"),
            Color.parseColor("#c28efc"),
            Color.parseColor("#409c5a")
        )

        private var availableColors: ArrayList<Int> = arrayListOf<Int>().apply {
            addAll(allColors)
        }

        private var unavailableColors: ArrayList<Int> = arrayListOf()


        override fun createFromParcel(parcel: Parcel): Player {
            return Player(parcel)
        }

        override fun newArray(size: Int): Array<Player?> {
            return arrayOfNulls(size)
        }

        private fun getRandColor(): Int {
            availableColors.shuffle()
            if (availableColors.size == 0) {
                availableColors = unavailableColors
                unavailableColors = arrayListOf()
            }
            var playerColor = availableColors.removeLast()
            val usedColors = arrayListOf<Int>()
            for (player in currentPlayers) {
                usedColors.add(player.playerColor)
            }
            while (playerColor in usedColors) {
                playerColor = getRandColor()
            }

            unavailableColors.add(playerColor)
            return playerColor
        }

        fun generatePlayer(): Player {
            val playerColor = getRandColor()
            val player = Player(startingLife, playerColor)
            currentPlayers.add(player)
            player.name = ("P" + player.playerNum)
            return player
        }

        fun packBundle(outState: Bundle) {
            outState.putInt("numPlayers", currentPlayers.size)
            for (i in 1..currentPlayers.size) {
                val pString = "P$i"
                val player = currentPlayers[i - 1]
                println("Saved $pString")
                outState.putParcelable(pString, player)
            }
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readBoolean()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(life)
        parcel.writeInt(playerColor)
        parcel.writeBoolean(monarch)
    }

    override fun describeContents(): Int {
        return 0
    }
}