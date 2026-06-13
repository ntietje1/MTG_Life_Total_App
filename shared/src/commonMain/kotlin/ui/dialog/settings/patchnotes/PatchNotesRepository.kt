package ui.dialog.settings.patchnotes

import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PatchNotesRepository(
    private val settings: Settings
) {
    fun load(): PatchNotesResponse? {
        val savedJson = settings.getStringOrNull(CacheKey) ?: return null
        return try {
            JsonFormat.decodeFromString<PatchNotesResponse>(savedJson)
        } catch (error: Exception) {
            settings.putString(CorruptCacheKey, savedJson)
            settings.remove(CacheKey)
            null
        }
    }

    fun save(response: PatchNotesResponse) {
        settings.putString(CacheKey, JsonFormat.encodeToString(response))
    }

    companion object {
        const val CacheKey = "patchNotes"
        const val CorruptCacheKey = "patchNotes.corrupt"

        private val JsonFormat = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}
