package domain.state.planechase

import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.card.Card
import model.card.ImageUris

data class PlanechaseSnapshot(
    val allPlanes: List<Card> = emptyList(),
    val planarDeck: List<Card> = emptyList(),
    val planarBackStack: List<Card> = emptyList()
)

class PlanechaseRepository(
    private val settings: Settings
) {
    fun load(): PlanechaseSnapshot {
        val savedJson = settings.getStringOrNull(StateKey)
        if (savedJson != null) {
            return loadSaved(savedJson)
        }
        return migrateLegacyState()
    }

    fun save(snapshot: PlanechaseSnapshot) {
        settings.putString(StateKey, JsonFormat.encodeToString(PlanechaseStateDto.fromDomain(snapshot)))
    }

    private fun loadSaved(savedJson: String): PlanechaseSnapshot {
        return try {
            JsonFormat.decodeFromString<PlanechaseStateDto>(savedJson).toDomain()
        } catch (error: Exception) {
            settings.putString(CorruptStateKey, savedJson)
            settings.remove(StateKey)
            PlanechaseSnapshot()
        }
    }

    private fun migrateLegacyState(): PlanechaseSnapshot {
        val hasLegacyState = LegacyKeys.any(settings::hasKey)
        if (!hasLegacyState) return PlanechaseSnapshot()

        return try {
            val snapshot = PlanechaseSnapshot(
                allPlanes = JsonFormat.decodeFromString(settings.getString("allPlanes", "[]")),
                planarDeck = JsonFormat.decodeFromString(settings.getString("planarDeck", "[]")),
                planarBackStack = JsonFormat.decodeFromString(settings.getString("planarBackStack", "[]"))
            )
            save(snapshot)
            LegacyKeys.forEach(settings::remove)
            snapshot
        } catch (error: Exception) {
            LegacyKeys.forEach { key ->
                settings.getStringOrNull(key)?.let { raw -> settings.putString("$key.corrupt", raw) }
                settings.remove(key)
            }
            PlanechaseSnapshot()
        }
    }

    companion object {
        const val StateKey = "planechaseState"
        const val CorruptStateKey = "planechaseState.corrupt"

        private val LegacyKeys = listOf("allPlanes", "planarDeck", "planarBackStack")

        private val JsonFormat = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}

@Serializable
private data class PlanechaseStateDto(
    val version: Int = CurrentVersion,
    val allPlanes: List<PlanechaseCardDto> = emptyList(),
    val planarDeck: List<PlanechaseCardDto> = emptyList(),
    val planarBackStack: List<PlanechaseCardDto> = emptyList()
) {
    fun toDomain(): PlanechaseSnapshot {
        return PlanechaseSnapshot(
            allPlanes = allPlanes.map(PlanechaseCardDto::toDomain),
            planarDeck = planarDeck.map(PlanechaseCardDto::toDomain),
            planarBackStack = planarBackStack.map(PlanechaseCardDto::toDomain)
        )
    }

    companion object {
        private const val CurrentVersion = 1

        fun fromDomain(snapshot: PlanechaseSnapshot): PlanechaseStateDto {
            return PlanechaseStateDto(
                allPlanes = snapshot.allPlanes.map(PlanechaseCardDto::fromDomain),
                planarDeck = snapshot.planarDeck.map(PlanechaseCardDto::fromDomain),
                planarBackStack = snapshot.planarBackStack.map(PlanechaseCardDto::fromDomain)
            )
        }
    }
}

@Serializable
private data class PlanechaseCardDto(
    val id: String,
    val name: String,
    val oracleText: String? = null,
    val imageUris: ImageUris,
    val artist: String,
    val setName: String,
    val rulingsUri: String? = null
) {
    fun toDomain(): Card {
        return Card(
            name = name,
            id = id,
            oracleText = oracleText,
            imageUris = imageUris,
            artist = artist,
            setName = setName,
            printsSearchUri = "",
            rulingsUri = rulingsUri
        )
    }

    companion object {
        fun fromDomain(card: Card): PlanechaseCardDto {
            return PlanechaseCardDto(
                id = card.id,
                name = card.name,
                oracleText = card.oracleText,
                imageUris = card.getUris(),
                artist = card.artist,
                setName = card.setName,
                rulingsUri = card.rulingsUri
            )
        }
    }
}
