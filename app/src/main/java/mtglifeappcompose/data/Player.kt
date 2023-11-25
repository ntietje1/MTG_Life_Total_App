package mtglifeappcompose.data


import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import mtglifeappcompose.ui.theme.allPlayerColors

@Serializable(with = PlayerSerializer::class)
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

    companion object {
        const val MAX_PLAYERS = 6
        var currentPlayers: MutableList<Player> = mutableListOf()
        var startingLife = 40

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

object PlayerSerializer : KSerializer<Player> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Player") {
        element<String>("playerName")
        element<Int>("color")
        element<Int>("textColor")
    }

    override fun serialize(encoder: Encoder, value: Player) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.name)
            encodeIntElement(descriptor, 1, value.color.toArgb())
            encodeIntElement(descriptor, 2, value.textColor.toArgb())
        }
    }

    override fun deserialize(decoder: Decoder): Player {
        return decoder.decodeStructure(descriptor) {
            var name = ""
            var color = 0
            var textColor = 0

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> name = decodeStringElement(descriptor, 0)
                    1 -> color = decodeIntElement(descriptor, 1)
                    2 -> textColor = decodeIntElement(descriptor, 2)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            Player(name = name, color = Color(color), textColor = Color(textColor))
        }
    }
}