package domain.state.profile

import androidx.compose.ui.graphics.toArgb
import com.russhwolf.settings.Settings
import domain.state.game.PlayerProfileId
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Player

class PlayerProfileRepository(
    private val settings: Settings
) {
    fun loadProfiles(): List<PlayerProfile> {
        val savedJson = settings.getStringOrNull(ProfileKey)
        if (savedJson != null) {
            return loadSavedProfiles(savedJson)
        }

        val profiles = loadLegacyPlayerPrefs().map(::legacyPlayerToProfile)
        if (profiles.isNotEmpty()) {
            saveProfiles(profiles)
        }
        return profiles
    }

    fun saveProfile(profile: PlayerProfile) {
        val profiles = loadProfiles()
            .filterNot { it.id == profile.id || it.displayName == profile.displayName } + profile
        saveProfiles(profiles)
    }

    fun deleteProfile(profileId: PlayerProfileId) {
        saveProfiles(loadProfiles().filterNot { it.id == profileId })
    }

    private fun loadSavedProfiles(savedJson: String): List<PlayerProfile> {
        return try {
            JsonFormat.decodeFromString<PlayerProfilesDto>(savedJson).profiles.map { it.toDomain() }
        } catch (error: Exception) {
            settings.putString(CorruptProfileKey, savedJson)
            settings.remove(ProfileKey)
            emptyList()
        }
    }

    private fun saveProfiles(profiles: List<PlayerProfile>) {
        settings.putString(ProfileKey, JsonFormat.encodeToString(PlayerProfilesDto.fromDomain(profiles)))
    }

    private fun loadLegacyPlayerPrefs(): List<Player> {
        val savedJson = settings.getStringOrNull(LegacyPlayerPrefsKey) ?: return emptyList()
        return try {
            JsonFormat.decodeFromString<List<Player>>(savedJson).reversed()
        } catch (error: Exception) {
            settings.putString(CorruptLegacyPlayerPrefsKey, savedJson)
            settings.remove(LegacyPlayerPrefsKey)
            emptyList()
        }
    }

    private fun legacyPlayerToProfile(player: Player): PlayerProfile {
        return PlayerProfile(
            id = PlayerProfileId(player.name),
            displayName = player.name,
            colors = PlayerColors(
                backgroundArgb = player.color.toArgb(),
                textArgb = player.textColor.toArgb()
            ),
            background = player.imageString?.let(PlayerBackground::LocalImage) ?: PlayerBackground.None
        )
    }

    companion object {
        const val ProfileKey = "playerProfiles"
        const val CorruptProfileKey = "playerProfiles.corrupt"
        const val LegacyPlayerPrefsKey = "playerPrefs"
        const val CorruptLegacyPlayerPrefsKey = "playerPrefs.corrupt"

        private val JsonFormat = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}

@Serializable
private data class PlayerProfilesDto(
    val version: Int = CurrentVersion,
    val profiles: List<PlayerProfileDto> = emptyList()
) {
    companion object {
        private const val CurrentVersion = 1

        fun fromDomain(profiles: List<PlayerProfile>): PlayerProfilesDto {
            return PlayerProfilesDto(
                profiles = profiles.map(PlayerProfileDto::fromDomain)
            )
        }
    }
}

@Serializable
private data class PlayerProfileDto(
    val id: String,
    val displayName: String,
    val backgroundArgb: Int = PlayerColors().backgroundArgb,
    val textArgb: Int = PlayerColors().textArgb,
    val background: PlayerBackground = PlayerBackground.None
) {
    fun toDomain(): PlayerProfile {
        return PlayerProfile(
            id = PlayerProfileId(id),
            displayName = displayName,
            colors = PlayerColors(backgroundArgb, textArgb),
            background = background
        )
    }

    companion object {
        fun fromDomain(profile: PlayerProfile): PlayerProfileDto {
            return PlayerProfileDto(
                id = profile.id.value,
                displayName = profile.displayName,
                backgroundArgb = profile.colors.backgroundArgb,
                textArgb = profile.colors.textArgb,
                background = profile.background
            )
        }
    }
}
