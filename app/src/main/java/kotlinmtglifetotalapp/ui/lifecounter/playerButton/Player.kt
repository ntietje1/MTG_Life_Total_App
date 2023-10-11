package kotlinmtglifetotalapp.ui.lifecounter.playerButton

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable

// add player name functionality
class Player(
    private var _life: Int,
    private val originalPlayerColor: Int
): Parcelable {

    val playerNum get() = currentPlayers.indexOf(this) + 1

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

    fun resetPlayer(){
        _life = startingLife
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

    // Parcelable related code

    companion object CREATOR : Parcelable.Creator<Player>{
        private const val MAX_PLAYERS = 6
        var currentPlayers: ArrayList<Player> = arrayListOf()
        var startingLife = 40

        private var availableColors: ArrayList<Int> = arrayListOf(
            Color.parseColor("#F75FA8"),
            Color.parseColor("#F75F5F"),
            Color.parseColor("#F7C45F"),
            Color.parseColor("#92F75F"),
            Color.parseColor("#5FEAF7"),
            Color.parseColor("#625FF7"),
            Color.parseColor("#C25FF7"),
        )

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
            return player
        }

        fun packBundle(outState: Bundle) {
            outState.putInt("numPlayers", currentPlayers.size)
            for (i in 1..currentPlayers.size) {
                val pString = "P$i"
                val player = currentPlayers[i-1]
                println("Saved $pString")
                outState.putParcelable(pString, player)
            }
        }
    }

    constructor(parcel: Parcel) : this(
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