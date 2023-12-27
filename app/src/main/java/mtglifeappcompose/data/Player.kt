package mtglifeappcompose.data


import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import mtglifeappcompose.composable.lifecounter.CounterType

@Serializable(with = PlayerSerializer::class)
class Player(
    life: Int = -1,
    imageUri: Uri? = null,
    color: Color = Color.LightGray,
    textColor: Color = Color.White,
    playerNum: Int = -1,
    name: String = "Placeholder",
    monarch: Boolean = false,
) {
    var life: Int by mutableIntStateOf(life)
    var imageUri: Uri? by mutableStateOf(imageUri)
    var color: Color by mutableStateOf(color)
    var textColor: Color by mutableStateOf(textColor)
    var name: String by mutableStateOf(name)
    var monarch: Boolean by mutableStateOf(monarch)
    var recentChange: Int by mutableIntStateOf(0)
    var partnerMode: Boolean by mutableStateOf(false)
    private var playerNum by mutableIntStateOf(playerNum)

    val isDead get() = (life <= 0)

    private var commanderDamage = mutableStateListOf<Int>().apply {
        for (i in 0 until MAX_PLAYERS*2) {
            add(0)
        }
    }

    private val counters = mutableStateListOf<Int>().apply {
        for (i in 0 until CounterType.values().size) {
            add(0)
        }
    }

    fun getCounterValue(counterType: CounterType): Int {
        return counters[counterType.ordinal]
    }

    fun incrementCounterValue(counterType: CounterType, value: Int) {
        counters[counterType.ordinal] += value
    }

    fun getCommanderDamage(currentDealer: Player, partner: Boolean = false): Int {
        val index = (this.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        return currentDealer.commanderDamage[index]
    }

    fun incrementCommanderDamage(currentDealer: Player, value: Int, partner: Boolean = false) {
        val index = (currentDealer.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        this.commanderDamage[index] += value
    }

    fun copySettings(other: Player) {
        this.imageUri = other.imageUri
        this.color = other.color
        this.textColor = other.textColor
        this.name = other.name
    }

    fun isDefaultName(): Boolean {
        return name.matches("P[1-$MAX_PLAYERS]".toRegex())
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

    fun resetPlayer(startingLife: Int) {
        life = startingLife
        recentChange = 0
        monarch = false
        commanderDamage = mutableStateListOf<Int>().apply {
            for (i in 0 until MAX_PLAYERS*2) {
                add(0)
            }
        }
    }

    companion object {
        const val MAX_PLAYERS = 6
    }
}

object PlayerSerializer : KSerializer<Player> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Player") {
        element<String>("playerName")
        element<String>("imageUri")
        element<Int>("color")
        element<Int>("textColor")
    }

    override fun serialize(encoder: Encoder, value: Player) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.name)
            encodeStringElement(descriptor, 1, value.imageUri.toString())
            encodeIntElement(descriptor, 2, value.color.toArgb())
            encodeIntElement(descriptor, 3, value.textColor.toArgb())
        }
    }

    override fun deserialize(decoder: Decoder): Player {
        return decoder.decodeStructure(descriptor) {
            var name = ""
            var imageUri = ""
            var color = 0
            var textColor = 0

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> name = decodeStringElement(descriptor, 0)
                    1 -> imageUri = decodeStringElement(descriptor, 1)
                    2 -> color = decodeIntElement(descriptor, 2)
                    3 -> textColor = decodeIntElement(descriptor, 3)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            Player(
                name = name,
                imageUri = if (imageUri == "null") null else Uri.parse(imageUri),
                color = Color(color),
                textColor = Color(textColor)
            )
        }
    }
}
