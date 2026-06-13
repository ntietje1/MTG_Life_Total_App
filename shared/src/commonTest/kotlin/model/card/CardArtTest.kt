package model.card

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CardArtTest {
    @Test
    fun selectsSingleFaceImageUris() {
        val card = testCard(imageUris = testImageUris("front"))

        assertEquals(
            CardArt(
                small = "https://example.com/front-small.jpg",
                normal = "https://example.com/front-normal.jpg",
                large = "https://example.com/front-large.jpg",
                artCrop = "https://example.com/front-art.jpg"
            ),
            card.art()
        )
    }

    @Test
    fun selectsFirstFaceWithImagesForDoubleFaceCards() {
        val card = testCard(
            imageUris = null,
            cardFaces = listOf(
                CardFace(imageUris = null, artist = "No Image Artist"),
                CardFace(imageUris = testImageUris("back"), artist = "Back Artist")
            )
        )

        assertEquals("https://example.com/back-normal.jpg", card.art()?.normal)
    }

    @Test
    fun returnsNullWhenNoFacesHaveImages() {
        val card = testCard(
            imageUris = null,
            cardFaces = listOf(CardFace(imageUris = null, artist = "No Image Artist"))
        )

        assertNull(card.art())
    }

    @Test
    fun returnsNullWhenCardHasNoImageFields() {
        val card = testCard(imageUris = null, cardFaces = null)

        assertNull(card.art())
    }

    private fun testCard(
        imageUris: ImageUris?,
        cardFaces: List<CardFace>? = null
    ): Card {
        return Card(
            name = "Test Card",
            id = "test-card",
            imageUris = imageUris,
            cardFaces = cardFaces,
            artist = "Artist",
            setName = "Set",
            printsSearchUri = "https://api.scryfall.com/cards/search?q=test"
        )
    }

    private fun testImageUris(prefix: String): ImageUris {
        return ImageUris(
            small = "https://example.com/$prefix-small.jpg",
            normal = "https://example.com/$prefix-normal.jpg",
            large = "https://example.com/$prefix-large.jpg",
            artCrop = "https://example.com/$prefix-art.jpg"
        )
    }
}
