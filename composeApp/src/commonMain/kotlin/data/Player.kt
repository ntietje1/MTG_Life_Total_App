package data


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import composable.lifecounter.CounterType

/**
 * Represents a player in the game
 * @param life The player's current life total
 * @param recentChange The player's most recent change to their life total
 * @param imageUri The player's image URI
 * @param color The player's color
 * @param textColor The player's text color
 * @param playerNum The player's number
 * @param name The player's name
 * @param monarch Whether or not the player is the monarch
 * @param commanderDamage The player's commander damage
 * @param counters The player's counters
 * @param activeCounters The player's active (visible) counters
 * @param setDead Whether or not the player artificially set to dead
 * @param partnerMode Whether or not the player is in partner mode
 */
@Serializable(with = PlayerSerializer::class)
class Player(
    life: Int = -1,
    recentChange: Int = 0,
    imageUri: String? = null,
    color: Color = Color.LightGray,
    textColor: Color = Color.White,
    playerNum: Int = -1,
    name: String = "Placeholder",
    monarch: Boolean = false,
    commanderDamage: List<Int> = mutableListOf<Int>().apply {
        repeat(MAX_PLAYERS *2) { add(0) }
    },
    counters: List<Int> = mutableListOf<Int>().apply {
        repeat(CounterType.entries.size) { add(0) }
    },
    activeCounters: List<CounterType> = mutableListOf(),
    setDead: Boolean = false,
    partnerMode: Boolean = false
) {
    var life: Int by mutableIntStateOf(life)
    var imageUri: String? by mutableStateOf(imageUri)
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
    private var scope = CoroutineScope(Dispatchers.IO)

    /**
     * Gets the value of a specific counter
     * @param counterType The type of counter to get
     */
    fun getCounterValue(counterType: CounterType): Int {
        return counters[counterType.ordinal]
    }

    /**
     * Increments a specific counter
     * @param counterType The type of counter to increment
     * @param value The value to increment by
     */
    fun incrementCounterValue(counterType: CounterType, value: Int) {
        counters[counterType.ordinal] += value
    }

    /**
     * Gets the commander damage dealt to this player by the current dealer
     * @param currentDealer The current dealer
     * @param partner Whether or not to get the partner commander damage
     */
    fun getCommanderDamage(currentDealer: Player, partner: Boolean = false): Int {
        val index = (currentDealer.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        return this.commanderDamage[index]
    }

    /**
     * Increments the commander damage dealt to this player by the current dealer
     * @param currentDealer The current dealer
     * @param value The value to increment by
     * @param partner Whether or not to increment the partner commander damage
     */
    fun incrementCommanderDamage(currentDealer: Player, value: Int, partner: Boolean = false) {
        val index = (this.playerNum - 1) + (if (partner) MAX_PLAYERS else 0)
        currentDealer.commanderDamage[index] += value
    }

    /**
     * Copies the settings from another player
     * @param other The player to copy the settings from
     */
    fun copySettings(other: Player) {
        this.imageUri = other.imageUri
        this.color = other.color
        this.textColor = other.textColor
        this.name = other.name
    }

    /**
     * Checks if the player has a default name
     */
    fun isDefaultName(): Boolean {
        return name.matches("P[1-$MAX_PLAYERS]".toRegex())
    }

    /**
     * Increments the player's life total
     * @param value The value to increment by
     */
    fun incrementLife(value: Int) {
        life += value
        recentChange += value
        resetRecentChange()
    }

    /**
     * Resets the player's recent change number
     */
    private fun resetRecentChange() {
        scope.cancel()
        scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            delay(1500)
            recentChange = 0
        }
    }

    /**
     * Resets the player's state
     * @param startingLife The starting life total
     */
    fun resetState(startingLife: Int) {
        life = startingLife
        recentChange = 0
        monarch = false
        setDead = false
        commanderDamage.apply {
            clear()
            for (i in 0 until MAX_PLAYERS *2) {
                add(0)
            }
        }
        counters.apply {
            clear()
            for (i in 0 until CounterType.entries.size) {
                add(0)
            }
        }
        activeCounters.clear()
    }

    companion object {
        const val MAX_PLAYERS = 6
    }
}

/**
 * Serializer for [Player] class
 */
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
                imageUri = if (imageUri == "null") null else imageUri,
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