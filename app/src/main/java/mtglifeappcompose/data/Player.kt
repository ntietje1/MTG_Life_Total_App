package mtglifeappcompose.data


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import mtglifeappcompose.ui.theme.allPlayerColors

// add player name functionality
class Player(
    private var _life: MutableState<Int> = mutableStateOf(startingLife),
    public var _playerColor: MutableState<Color> = mutableStateOf(Color.LightGray),
    private var _monarch: MutableState<Boolean> = mutableStateOf(false),
    private var _name: MutableState<String> = mutableStateOf("#Placeholder"),
    private var _recentLifeChange: MutableState<Int> = mutableStateOf(0)
) : Parcelable {

    val playerNum get() = currentPlayers.indexOf(this) + 1

    var life: Int by _life
        private set

    var playerColor: Color by _playerColor

    var monarch: Boolean
        get() = _monarch.value
        set(v) = run {
            if (v) {
                for (player in currentPlayers) {
                    player.monarch = false
                }
            }
            this._monarch.value = v
        }

    var name: String by _name

    val commanderDamage: ArrayList<Int> = arrayListOf<Int>().apply {
        for (i in 0 until MAX_PLAYERS) {
            add(0)
        }
    }

    val isDead get() = (life <= 0)

    val recentLifeChange by _recentLifeChange

    private val handler: Handler = Handler(Looper.getMainLooper())

    private val resetRecentChangeRunnable = Runnable {
        zeroRecentChange()
    }

    fun resetPlayer() {
        life = startingLife
        monarch = false
        resetCommanderDamage()
    }

    private fun resetCommanderDamage() {
        for (i in 0 until 6) {
            commanderDamage.removeAt(0)
            commanderDamage.add(0)
        }
    }


    fun incrementLife(value: Int) {
        println("PLAYER INCREMENT LIFE: $value")
        this.life += value
        _recentLifeChange.value += value
        resetRecentChangeRunnable()
    }

    fun incrementCommander(value: Int) {
        this.life -= value
        _recentLifeChange.value += value
        resetRecentChangeRunnable()
    }

    private fun resetRecentChangeRunnable() {
        handler.removeCallbacks(resetRecentChangeRunnable)
        handler.postDelayed(resetRecentChangeRunnable, 1500)
    }

    fun zeroRecentChange() {
        _recentLifeChange.value = 0
    }

    override fun toString(): String {
        val colorInt = playerColor.toArgb()
        return "$name:$colorInt"
    }

    fun fromString(s: String) {
        println("PARSING PLAYER:$s")
        val parts = s.split(":")
        if (parts.size == 2) {
            name = parts[0]
            playerColor = Color(parts[1].toInt()) //Might break with converting to Color
        } else {
            return
        }
    }


    companion object CREATOR : Parcelable.Creator<Player> {
        const val MAX_PLAYERS = 6
        var currentPlayers: MutableList<Player> = mutableListOf()
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

        val allColors: List<Color> = allPlayerColors

        override fun createFromParcel(parcel: Parcel): Player {
            return Player(parcel)
        }

        override fun newArray(size: Int): Array<Player?> {
            return arrayOfNulls(size)
        }

        private fun getRandColor(): Color {
            var color = allColors.random()
            while (currentPlayers.any { it.playerColor == color }) {
                color = allColors.random()
            }
            return color
        }

        fun generatePlayer(): Player {
            val playerColor = getRandColor()
            val player = Player(mutableIntStateOf(startingLife), mutableStateOf(playerColor))
            currentPlayers.add(player)
            player.name = ("P" + player.playerNum)
            return player
        }

        fun resetPlayers() {
            currentPlayers.forEach { player ->
                player.resetPlayer()
            }
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
        mutableIntStateOf(parcel.readInt()),
        mutableStateOf(Color(parcel.readInt())),
        mutableStateOf(parcel.readBoolean())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(life)
        parcel.writeInt(playerColor.toArgb())
        parcel.writeBoolean(monarch)
        println("WRITING PLAYER: $name, IS MONARCH: $monarch")
    }

    override fun describeContents(): Int {
        return 0
    }
}