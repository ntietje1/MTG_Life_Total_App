package ui.dialog.settings.patchnotes

import domain.storage.TestSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PatchNotesRepositoryTest {
    @Test
    fun savesAndLoadsPatchNotesCache() {
        val settings = TestSettings()
        val repository = PatchNotesRepository(settings)
        val response = PatchNotesResponse(
            patchNotes = listOf(PatchNotesItem("1.2.3", "Title", "2026-06-13", listOf("Note"))),
            inProgress = listOf("Next")
        )

        repository.save(response)

        assertEquals(response, repository.load())
    }

    @Test
    fun preservesCorruptPatchNotesCacheAndClearsActiveCache() {
        val settings = TestSettings()
        settings.putString(PatchNotesRepository.CacheKey, "{broken")
        val repository = PatchNotesRepository(settings)

        val response = repository.load()

        assertNull(response)
        assertEquals("{broken", settings.getString(PatchNotesRepository.CorruptCacheKey, ""))
        assertNull(settings.getStringOrNull(PatchNotesRepository.CacheKey))
    }

    @Test
    fun readsExistingRawPatchNotesJson() {
        val settings = TestSettings()
        val response = PatchNotesResponse(
            patchNotes = listOf(PatchNotesItem("1.0.0", "Launch", "2024-01-01", listOf("Initial"))),
            inProgress = emptyList()
        )
        settings.putString(PatchNotesRepository.CacheKey, Json.encodeToString(response))
        val repository = PatchNotesRepository(settings)

        assertEquals(response, repository.load())
    }
}
