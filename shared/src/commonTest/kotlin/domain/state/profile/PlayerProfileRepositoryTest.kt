package domain.state.profile

import androidx.compose.ui.graphics.Color
import domain.common.NumberWithRecentChange
import domain.state.game.PlayerProfileId
import domain.storage.TestSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ui.lifecounter.CounterType as LegacyCounterType

class PlayerProfileRepositoryTest {
    @Test
    fun migratesLegacyPlayerPrefsWithoutGameState() {
        val settings = TestSettings()
        val legacyPlayer = Player(
            name = "Jace",
            playerNum = 4,
            lifeTotal = NumberWithRecentChange(9, -31),
            color = Color(-123),
            textColor = Color(-456),
            imageString = "jace.png",
            counters = List(LegacyCounterType.entries.size) { 7 },
            activeCounters = LegacyCounterType.entries,
            setDead = true
        )
        settings.putString("playerPrefs", Json.encodeToString(listOf(legacyPlayer)))
        val repository = PlayerProfileRepository(settings = settings)

        val profiles = repository.loadProfiles()

        assertEquals(1, profiles.size)
        assertEquals(PlayerProfileId("Jace"), profiles.single().id)
        assertEquals("Jace", profiles.single().displayName)
        assertEquals(PlayerColors(-123, -456), profiles.single().colors)
        assertEquals(PlayerBackground.LocalImage("jace.png"), profiles.single().background)
        val savedJson = settings.getString(PlayerProfileRepository.ProfileKey, "")
        assertFalse(savedJson.contains("lifeTotal"))
        assertFalse(savedJson.contains("playerNum"))
        assertFalse(savedJson.contains("counters"))
        assertFalse(savedJson.contains("setDead"))
    }

    @Test
    fun savingProfileReplacesSameDisplayName() {
        val repository = PlayerProfileRepository(settings = TestSettings())

        repository.saveProfile(PlayerProfile(PlayerProfileId("Jace"), "Jace", PlayerColors(1, 2)))
        repository.saveProfile(PlayerProfile(PlayerProfileId("Jace"), "Jace", PlayerColors(3, 4)))

        val profiles = repository.loadProfiles()
        assertEquals(1, profiles.size)
        assertEquals(PlayerColors(3, 4), profiles.single().colors)
    }

    @Test
    fun deletingProfileRemovesOnlyThatProfile() {
        val repository = PlayerProfileRepository(settings = TestSettings())
        repository.saveProfile(PlayerProfile(PlayerProfileId("Jace"), "Jace"))
        repository.saveProfile(PlayerProfile(PlayerProfileId("Nissa"), "Nissa"))

        repository.deleteProfile(PlayerProfileId("Jace"))

        val profiles = repository.loadProfiles()
        assertEquals(listOf("Nissa"), profiles.map { it.displayName })
    }

    @Test
    fun corruptProfilePayloadIsPreservedAndProfilesReset() {
        val settings = TestSettings()
        settings.putString(PlayerProfileRepository.ProfileKey, "{broken")
        val repository = PlayerProfileRepository(settings = settings)

        val profiles = repository.loadProfiles()

        assertEquals(emptyList(), profiles)
        assertEquals("{broken", settings.getString(PlayerProfileRepository.CorruptProfileKey, ""))
        assertNull(settings.getStringOrNull(PlayerProfileRepository.ProfileKey))
    }
}
