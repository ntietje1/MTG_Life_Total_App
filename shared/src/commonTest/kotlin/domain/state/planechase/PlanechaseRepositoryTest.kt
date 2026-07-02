package domain.state.planechase

import domain.storage.TestSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.card.CardSummary
import model.card.Card
import model.card.CardArt
import model.card.ImageUris
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class PlanechaseRepositoryTest {
    @Test
    fun savesAndLoadsVersionedPlanechaseSnapshot() {
        val settings = TestSettings()
        val repository = PlanechaseRepository(settings)
        val allPlanes = listOf(testCard("all"))
        val deck = listOf(testCard("deck"))
        val backStack = listOf(testCard("back"))

        repository.save(
            PlanechaseSnapshot(
                allPlanes = allPlanes,
                planarDeck = deck,
                planarBackStack = backStack
            )
        )

        val savedJson = settings.getString(PlanechaseRepository.StateKey, "")
        assertFalse(savedJson.contains("prints_search_uri"))
        assertFalse(savedJson.contains("card_faces"))
        val loaded = repository.load()
        assertEquals(listOf("all"), loaded.allPlanes.map { it.id })
        assertEquals(listOf("deck"), loaded.planarDeck.map { it.id })
        assertEquals(listOf("back"), loaded.planarBackStack.map { it.id })
        assertEquals("https://example.com/all-normal.jpg", loaded.allPlanes.single().art?.normal)
    }

    @Test
    fun migratesLegacyThreeKeyPlanechaseState() {
        val settings = TestSettings()
        val allPlanes = listOf(legacyCard("all"))
        val deck = listOf(legacyCard("deck"))
        val backStack = listOf(legacyCard("back"))
        settings.putString("allPlanes", Json.encodeToString(allPlanes))
        settings.putString("planarDeck", Json.encodeToString(deck))
        settings.putString("planarBackStack", Json.encodeToString(backStack))
        val repository = PlanechaseRepository(settings)

        val snapshot = repository.load()

        assertEquals(listOf("all"), snapshot.allPlanes.map { it.id })
        assertEquals(listOf("deck"), snapshot.planarDeck.map { it.id })
        assertEquals(listOf("back"), snapshot.planarBackStack.map { it.id })
        assertNull(settings.getStringOrNull("allPlanes"))
        assertNull(settings.getStringOrNull("planarDeck"))
        assertNull(settings.getStringOrNull("planarBackStack"))
    }

    @Test
    fun preservesCorruptPlanechasePayloadAndResetsOnlyPlanechaseState() {
        val settings = TestSettings()
        settings.putString(PlanechaseRepository.StateKey, "{broken")
        val repository = PlanechaseRepository(settings)

        val snapshot = repository.load()

        assertEquals(PlanechaseSnapshot(), snapshot)
        assertEquals("{broken", settings.getString(PlanechaseRepository.CorruptStateKey, ""))
        assertNull(settings.getStringOrNull(PlanechaseRepository.StateKey))
    }
}

private fun testCard(id: String): CardSummary {
    return CardSummary(
        id = id,
        name = "Plane $id",
        oracleText = "When you planeswalk.",
        art = CardArt(
            small = "https://example.com/$id-small.jpg",
            normal = "https://example.com/$id-normal.jpg",
            large = "https://example.com/$id-large.jpg",
            artCrop = "https://example.com/$id-art.jpg"
        ),
        artist = "Artist",
        setName = "Set",
        printsSearchUri = "https://api.scryfall.com/cards/search?q=$id",
        rulingsUri = "https://api.scryfall.com/cards/$id/rulings"
    )
}

private fun legacyCard(id: String): Card {
    return Card(
        id = id,
        name = "Plane $id",
        oracleText = "When you planeswalk.",
        imageUris = ImageUris(
            small = "https://example.com/$id-small.jpg",
            normal = "https://example.com/$id-normal.jpg",
            large = "https://example.com/$id-large.jpg",
            artCrop = "https://example.com/$id-art.jpg"
        ),
        artist = "Artist",
        setName = "Set",
        printsSearchUri = "https://api.scryfall.com/cards/search?q=$id",
        rulingsUri = "https://api.scryfall.com/cards/$id/rulings"
    )
}
