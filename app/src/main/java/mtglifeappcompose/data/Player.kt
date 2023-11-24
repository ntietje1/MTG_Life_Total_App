package mtglifeappcompose.data


import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import mtglifeappcompose.ui.theme.allPlayerColors

class Player(
    life: Int = startingLife,
    color: Color = Color.LightGray,
    textColor: Color = Color.White,
    name: String = "Placeholder",
    monarch: Boolean = false
) {
    var life: Int by mutableIntStateOf(life)
    var color: Color by mutableStateOf(color)
    var textColor: Color by mutableStateOf(textColor)
    var name: String by mutableStateOf(name)
    var monarch: Boolean by mutableStateOf(monarch)
    var recentChange: Int by mutableIntStateOf(0)
    val playerNum get() = currentPlayers.indexOf(this) + 1
    val isDead get() = (life <= 0)

    val commanderDamage: ArrayList<Int> = arrayListOf<Int>().apply {
        for (i in 0 until MAX_PLAYERS) {
            add(0)
        }
    }

    fun incrementLife(value: Int) {
        life += value
        recentChange += value
        resetRecentChangeRunnable()
    }

    private val handler: Handler = Handler(Looper.getMainLooper())

    private val resetRecentChangeRunnable = Runnable {
        recentChange = 0
    }

    private fun resetRecentChangeRunnable() {
        handler.removeCallbacks(resetRecentChangeRunnable)
        handler.postDelayed(resetRecentChangeRunnable, 1500)
    }

    private fun resetPlayer() {
        life = startingLife
        recentChange = 0
        monarch = false
    }

    override fun toString(): String {
        val colorInt = color.toArgb()
        return "$name:$colorInt"
    }

    fun fromString(s: String) {
        val parts = s.split(":")
        if (parts.size == 2) {
            name = parts[0]
            color = Color(parts[1].toInt())
        } else {
            return
        }
    }

    companion object {
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

        private fun getRandColor(): Color {
            var color = allPlayerColors.random()
            while (currentPlayers.any { it.color == color }) {
                color = allPlayerColors.random()
            }
            return color
        }

        fun generatePlayer(): Player {
            val playerColor = getRandColor()
            val player = Player(color = playerColor)
            currentPlayers.add(player)
            player.name = ("P" + player.playerNum)
            return player
        }

        fun resetPlayers() {
            currentPlayers.forEach { player ->
                player.resetPlayer()
            }
        }
    }

}
//class Player(
//    private var _life: MutableState<Int> = mutableStateOf(startingLife),
//    public var _playerColor: MutableState<Color> = mutableStateOf(Color.LightGray),
//    private var _monarch: MutableState<Boolean> = mutableStateOf(false),
//    private var _name: MutableState<String> = mutableStateOf("#Placeholder"),
//    private var _recentLifeChange: MutableState<Int> = mutableStateOf(0)
//) {
//
//    val playerNum get() = currentPlayers.indexOf(this) + 1
//
//    var life: Int by _life
//        private set
//
//    var playerColor: Color by _playerColor
//
//    var monarch: Boolean
//        get() = _monarch.value
//        set(v) = run {
//            if (v) {
//                for (player in currentPlayers) {
//                    player.monarch = false
//                }
//            }
//            this._monarch.value = v
//        }
//
//    var name: String by _name
//
//    val isDead get() = (life <= 0)
//
//    val recentLifeChange by _recentLifeChange
//
//    private val handler: Handler = Handler(Looper.getMainLooper())
//
//    private val resetRecentChangeRunnable = Runnable {
//        zeroRecentChange()
//    }
//
//    fun resetPlayer() {
//        life = startingLife
//        monarch = false
//    }
//
//    fun incrementLife(value: Int) {
//        println("PLAYER INCREMENT LIFE: $value")
//        this.life += value
//        _recentLifeChange.value += value
//        resetRecentChangeRunnable()
//    }
//
//    private fun resetRecentChangeRunnable() {
//        handler.removeCallbacks(resetRecentChangeRunnable)
//        handler.postDelayed(resetRecentChangeRunnable, 1500)
//    }
//
//    private fun zeroRecentChange() {
//        _recentLifeChange.value = 0
//    }
//
//    override fun toString(): String {
//        val colorInt = playerColor.toArgb()
//        return "$name:$colorInt"
//    }
//
//    fun fromString(s: String) {
//        println("PARSING PLAYER:$s")
//        val parts = s.split(":")
//        if (parts.size == 2) {
//            name = parts[0]
//            playerColor = Color(parts[1].toInt())
//        } else {
//            return
//        }
//    }
//
//
//    companion object {
//        const val MAX_PLAYERS = 6
//        var currentPlayers: MutableList<Player> = mutableListOf()
//        var startingLife = 40
//
//        fun allToString(players: ArrayList<Player>): String {
//            var res = ""
//            if (players.isEmpty()) {
//                return res
//            }
//            for (player in players) {
//                val pString = player.toString()
//                res += "$pString,"
//            }
//            return res.substring(0, res.length - 1)
//        }
//
//        val allColors: List<Color> = allPlayerColors
//
//        private fun getRandColor(): Color {
//            var color = allColors.random()
//            while (currentPlayers.any { it.playerColor == color }) {
//                color = allColors.random()
//            }
//            return color
//        }
//
//        fun generatePlayer(): Player {
//            val playerColor = getRandColor()
//            val player = Player(mutableIntStateOf(startingLife), mutableStateOf(playerColor))
//            currentPlayers.add(player)
//            player.name = ("P" + player.playerNum)
//            return player
//        }
//
//        fun resetPlayers() {
//            currentPlayers.forEach { player ->
//                player.resetPlayer()
//            }
//        }
//    }
//}