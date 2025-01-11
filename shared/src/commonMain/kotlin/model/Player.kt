package model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import domain.common.NumberWithRecentChange
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
import theme.PlayerColor1
import theme.PlayerColor2
import theme.PlayerColor3
import theme.PlayerColor4
import theme.PlayerColor5
import theme.PlayerColor6
import theme.PlayerColor7
import theme.PlayerColor8
import theme.PlayerColor9
import ui.lifecounter.CounterType

@Serializable
object PlayerSerializer : KSerializer<Player> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PlayerInfo") {
        element<String>("playerName")
        element<String>("imageUri")
        element<Int>("color")
        element<Int>("textColor")
        element<NumberWithRecentChange>("lifeTotal")
        element<Int>("playerNum")
        element<Boolean>("monarch")
        element<List<NumberWithRecentChange>>("commanderDamage")
        element<List<Int>>("counters")
        element<Boolean>("setDead")
        element<Boolean>("partnerMode")
        element<List<CounterType>>("activeCounters")
    }

    override fun serialize(encoder: Encoder, value: Player) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.name)
            encodeStringElement(descriptor, 1, value.imageString.toString())
            encodeIntElement(descriptor, 2, value.color.toArgb())
            encodeIntElement(descriptor, 3, value.textColor.toArgb())
            encodeSerializableElement(descriptor, 4, NumberWithRecentChange.serializer(), value.lifeTotal)
            encodeIntElement(descriptor, 5, value.playerNum)
            encodeBooleanElement(descriptor, 6, value.monarch)
            encodeSerializableElement(descriptor, 7, ListSerializer(NumberWithRecentChange.serializer()), value.commanderDamage)
            encodeSerializableElement(descriptor, 8, ListSerializer(Int.serializer()), value.counters)
            encodeBooleanElement(descriptor, 9, value.setDead)
            encodeBooleanElement(descriptor, 10, value.partnerMode)
            encodeSerializableElement(descriptor, 11, ListSerializer(CounterType.serializer()), value.activeCounters)
        }
    }

    override fun deserialize(decoder: Decoder): Player {
        return decoder.decodeStructure(descriptor) {
            var name = ""
            var imageUri = ""
            var color = 0
            var textColor = 0
            var lifeTotal = NumberWithRecentChange(40, 0)
            var playerNum = 0
            var monarch = false
            var commanderDamage = mutableListOf<NumberWithRecentChange>()
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
                    4 -> lifeTotal = decodeSerializableElement(descriptor, 4, NumberWithRecentChange.serializer())
                    5 -> playerNum = decodeIntElement(descriptor, 5)
                    6 -> monarch = decodeBooleanElement(descriptor, 6)
                    7 -> commanderDamage = decodeSerializableElement(descriptor, 7, ListSerializer(NumberWithRecentChange.serializer())).toMutableList()
                    8 -> counters = decodeSerializableElement(descriptor, 8, ListSerializer(Int.serializer())).toMutableList()
                    9 -> setDead = decodeBooleanElement(descriptor, 9)
                    10 -> partnerMode = decodeBooleanElement(descriptor, 10)
                    11 -> activeCounters = decodeSerializableElement(descriptor, 11, ListSerializer(CounterType.serializer())).toMutableList()
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            Player(
                lifeTotal = lifeTotal,
                imageString = if (imageUri == "null") null else imageUri,
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

@Serializable(with = PlayerSerializer::class)
data class Player(
    val lifeTotal: NumberWithRecentChange = NumberWithRecentChange(-1, 0),
    val imageString: String? = null, // represents a local file name or scryfall url
    //TODO: make a PlayerBackground interface/sealed class that can be a local file, scryfall url, or a color
    val color: Color = Color.LightGray,
    val textColor: Color = Color.White,
    val playerNum: Int = -1,
    val name: String = "Placeholder",
    val monarch: Boolean = false,
    val commanderDamage: List<NumberWithRecentChange> = List(MAX_PLAYERS * 2) { NumberWithRecentChange(0, 0) },
    val counters: List<Int> = List(CounterType.entries.size * 2) { 0 },
    val activeCounters: List<CounterType> = listOf(),
    val setDead: Boolean = false,
    val partnerMode: Boolean = false,
) {

    val life: Int get() = lifeTotal.number
    val recentChange: Int get() = lifeTotal.recentChange

    fun isDefaultOrEmptyName(): Boolean {
        return name.matches("P[1-$MAX_PLAYERS]".toRegex()) || name == "Placeholder" || name.isEmpty()
    }

    companion object {
        const val MAX_PLAYERS = 6

        val allPlayerColors = listOf(
            PlayerColor1,
            PlayerColor2,
            PlayerColor3,
            PlayerColor4,
            PlayerColor5,
            PlayerColor6,
            PlayerColor7,
            PlayerColor8,
            PlayerColor9
        )
    }
}