package di

import domain.api.GifAsset
import domain.api.GifPage
import domain.api.GifProvider
import domain.api.GifSearchClient
import domain.api.GifSearchResult
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSProcessInfo

internal fun iosE2EModuleIfEnabled(): Module? = if (NSProcessInfo.processInfo.arguments.contains("--lifelinked-e2e")) {
    module {
        single<GifSearchClient> { FakeGifSearchClient() }
    }
} else {
    null
}

private class FakeGifSearchClient : GifSearchClient {
    override suspend fun search(query: String, limit: Int, cursor: String?): GifSearchResult {
        return GifSearchResult.Success(
            GifPage(
                items = listOf(
                    GifAsset(
                        id = "e2e-gif",
                        previewUrl = "https://example.test/e2e-preview.gif",
                        fullUrl = "https://example.test/e2e-full.gif",
                        width = 320,
                        height = 180,
                        provider = GifProvider.KLIPY,
                        attributionUrl = "https://example.test/e2e"
                    )
                ),
                nextCursor = null
            )
        )
    }
}
