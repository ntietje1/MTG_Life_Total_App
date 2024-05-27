package data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import composable.lifecounter.CounterType
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

@Serializable(with = PlayerSerializer::class)
data class Player(
    val life: Int = -1,
    val recentChange: Int = 0,
    val imageUri: String? = null,
    val color: Color = Color.LightGray,
    val textColor: Color = Color.White,
    val playerNum: Int = -1,
    val name: String = "Placeholder",
    val monarch: Boolean = false,
    val commanderDamage: List<Int> = List(MAX_PLAYERS * 2) { 0 },
    val counters: List<Int> = List(CounterType.entries.size * 2) { 0 },
    val activeCounters: List<CounterType> = listOf(),
    val setDead: Boolean = false,
    val partnerMode: Boolean = false,
) {
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

object PlayerSerializer : KSerializer<Player> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PlayerInfo") {
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