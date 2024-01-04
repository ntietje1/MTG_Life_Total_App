package lifelinked.data


import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import lifelinked.composable.lifecounter.CounterType

@Serializable(with = PlayerSerializer::class)
class Player(
    life: Int = -1,
    recentChange: Int = 0,
    imageUri: Uri? = null,
    color: Color = Color.LightGray,
    textColor: Color = Color.White,
    playerNum: Int = -1,
    name: String = "Placeholder",
    monarch: Boolean = false,
    commanderDamage: List<Int> = mutableListOf<Int>().apply {
        repeat(MAX_PLAYERS*2) { add(0) }
    },
    counters: List<Int> = mutableListOf<Int>().apply {
        repeat(CounterType.values().size) { add(0) }
    },
    activeCounters: List<CounterType> = mutableListOf(),
    setDead: Boolean = false,
    partnerMode: Boolean = false
) {
    var life: Int by mutableIntStateOf(life)
    var imageUri: Uri? by mutableStateOf(imageUri)
    var color: Color by mutableStateOf(color)
    var textColor: Color by mutableStateOf(textColor)
    var name: String by mutableStateOf(name)
    var monarch: Boolean by mutableStateOf(monarch)
    var recentChange: Int by mutableIntStateOf(recentChange)
    var partnerMode: Boolean by mutableStateOf(partnerMode)
    var playerNum by mutableIntStateOf(playerNum)
    var setDead by mutableStateOf(setDead)

    val isDead get() = (life <= 0 || setDead || commanderDamage.any { it >= 21 } )

    val commanderDamage = commanderDamage.toMutableStateList()
    val counters = counters.toMutableStateList()
    val activeCounters = activeCounters.toMutableStateList()


    fun getCounterValue(counterType: CounterType): Int {
        return counters[counterType.ordinal]
    }

    fun incrementCounterValue(counterType: CounterType, value: Int) {
        counters[counterType.ordinal] += value
    }

    fun getCommanderDamage(currentDealer: Player, partner: Boolean = false): Int {
        val index = (currentDealer.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        return this.commanderDamage[index]
    }

    fun incrementCommanderDamage(currentDealer: Player, value: Int, partner: Boolean = false) {
        val index = (this.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        currentDealer.commanderDamage[index] += value
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
        this@Player.recentChange = 0
    }

    private fun resetRecentChangeRunnable() {
        handler.removeCallbacks(resetRecentChangeRunnable)
        handler.postDelayed(resetRecentChangeRunnable, 1500)
    }

    fun resetState(startingLife: Int) {
        life = startingLife
        recentChange = 0
        monarch = false
        setDead = false
        commanderDamage.apply {
            clear()
            for (i in 0 until MAX_PLAYERS*2) {
                add(0)
            }
        }
        counters.apply {
            clear()
            for (i in 0 until CounterType.values().size) {
                add(0)
            }
        }
        activeCounters.clear()
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
        element<Int>("life")
        element<Int>("recentChange")
        element<Int>("playerNum")
        element<Boolean>("monarch")
        element<List<Int>>("commanderDamage")
        element<List<Int>>("counters")
        element<Boolean>("setDead")
        element<Boolean>("partnerMode")
        element<List<CounterType>>("activeCounters")
    }

    override fun serialize(encoder: Encoder, value: Player) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.name)
            encodeStringElement(descriptor, 1, value.imageUri.toString())
            encodeIntElement(descriptor, 2, value.color.toArgb())
            encodeIntElement(descriptor, 3, value.textColor.toArgb())
            encodeIntElement(descriptor, 4, value.life)
            encodeIntElement(descriptor, 5, value.recentChange)
            encodeIntElement(descriptor, 6, value.playerNum)
            encodeBooleanElement(descriptor, 7, value.monarch)
            encodeSerializableElement(descriptor, 8, ListSerializer(Int.serializer()), value.commanderDamage)
            encodeSerializableElement(descriptor, 9, ListSerializer(Int.serializer()), value.counters)
            encodeBooleanElement(descriptor, 10, value.setDead)
            encodeBooleanElement(descriptor, 11, value.partnerMode)
            encodeSerializableElement(descriptor, 12, ListSerializer(CounterType.serializer()), value.activeCounters)
        }
    }

    override fun deserialize(decoder: Decoder): Player {
        return decoder.decodeStructure(descriptor) {
            var name = ""
            var imageUri = ""
            var color = 0
            var textColor = 0
            var life = 0
            var recentChange = 0
            var playerNum = 0
            var monarch = false
            var commanderDamage = mutableListOf<Int>()
            var counters = mutableListOf<Int>()
            var setDead = false
            var partnerMode = false
            var activeCounters = mutableListOf<CounterType>()

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> name = decodeStringElement(descriptor, 0)
                    1 -> imageUri = decodeStringElement(descriptor, 1)
                    2 -> color = decodeIntElement(descriptor, 2)
                    3 -> textColor = decodeIntElement(descriptor, 3)
                    4 -> life = decodeIntElement(descriptor, 4)
                    5 -> recentChange = decodeIntElement(descriptor, 5)
                    6 -> playerNum = decodeIntElement(descriptor, 6)
                    7 -> monarch = decodeBooleanElement(descriptor, 7)
                    8 -> commanderDamage = decodeSerializableElement(descriptor, 8, ListSerializer(Int.serializer())).toMutableList()
                    9 -> counters = decodeSerializableElement(descriptor, 9, ListSerializer(Int.serializer())).toMutableList()
                    10 -> setDead = decodeBooleanElement(descriptor, 10)
                    11 -> partnerMode = decodeBooleanElement(descriptor, 11)
                    12 -> activeCounters = decodeSerializableElement(descriptor, 12, ListSerializer(CounterType.serializer())).toMutableList()
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }
            Player(
                life = life,
                recentChange = recentChange,
                imageUri = if (imageUri == "null") null else Uri.parse(imageUri),
                color = Color(color),
                textColor = Color(textColor),
                playerNum = playerNum,
                name = name,
                monarch = monarch,
                commanderDamage = commanderDamage,
                counters = counters,
                setDead = setDead,
                partnerMode = partnerMode,
                activeCounters = activeCounters
            )
        }
    }
}