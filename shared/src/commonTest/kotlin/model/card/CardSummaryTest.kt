package model.card

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CardSummaryTest {
    @Test
    fun mapsScryfallCardToCompactSummary() {
        val summary = Card(
            name = "Lightning Bolt",
            id = "card-1",
            oracleText = "Lightning Bolt deals 3 damage to any target.",
            imageUris = ImageUris(
                small = "https://example.com/small.jpg",
                normal = "https://example.com/normal.jpg",
                large = "https://example.com/large.jpg",
                artCrop = "https://example.com/art.jpg"
            ),
            artist = "Christopher Rush",
            setName = "Limited Edition Alpha",
            printsSearchUri = "https://api.scryfall.com/cards/search?q=bolt",
            rulingsUri = "https://api.scryfall.com/cards/card-1/rulings"
        ).toSummary()

        assertEquals("card-1", summary.id)
        assertEquals("Lightning Bolt", summary.name)
        assertEquals("Lightning Bolt deals 3 damage to any target.", summary.oracleText)
        assertEquals("https://example.com/normal.jpg", summary.art?.normal)
        assertEquals("Christopher Rush", summary.artist)
        assertEquals("Limited Edition Alpha", summary.setName)
        assertEquals("https://api.scryfall.com/cards/search?q=bolt", summary.printsSearchUri)
        assertEquals("https://api.scryfall.com/cards/card-1/rulings", summary.rulingsUri)
    }

    @Test
    fun mapsCardWithoutArtToCompactSummaryWithNullArt() {
        val summary = Card(
            name = "Text Card",
            id = "card-2",
            artist = "Artist",
            setName = "Set",
            printsSearchUri = "https://api.scryfall.com/cards/search?q=text"
        ).toSummary()

        assertNull(summary.art)
    }

    @Test
    fun mapsRulingToCompactSummary() {
        val summary = Ruling(
            comment = "A ruling.",
            publishedAt = "2024-01-01",
            source = "wotc"
        ).toSummary()

        assertEquals("A ruling.", summary.comment)
        assertEquals("2024-01-01", summary.publishedAt)
        assertEquals("wotc", summary.source)
    }
}
