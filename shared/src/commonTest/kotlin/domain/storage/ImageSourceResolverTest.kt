package domain.storage

import domain.state.profile.PlayerBackground
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageSourceResolverTest {
    @Test
    fun resolvesExplicitImageSourcesWithoutStringPrefixChecks() {
        val store = FakeFileImageStore(
            localUris = mapOf("local-id" to "file:///images/local-id")
        )

        assertNull(PlayerBackground.None.displayUri(store))
        assertEquals("file:///images/local-id", PlayerBackground.LocalImage("local-id").displayUri(store))
        assertEquals("https://example.com/provider.gif", PlayerBackground.ProviderImage("https://example.com/provider.gif").displayUri(store))
        assertEquals("https://cards.example/card.jpg", PlayerBackground.CardArt("https://cards.example/card.jpg").displayUri(store))
    }
}

private class FakeFileImageStore(
    private val localUris: Map<String, String>
) : IFileImageStore {
    override suspend fun saveImage(bytes: ByteArray): String = "local-id"

    override fun localImageUri(imageId: String): String? {
        return localUris[imageId]
    }

    override fun deleteImage(imageId: String) = Unit
}
