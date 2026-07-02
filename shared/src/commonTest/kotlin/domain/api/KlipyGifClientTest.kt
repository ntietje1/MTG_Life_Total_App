package domain.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class KlipyGifClientTest {
    @Test
    fun searchesKlipyAndMapsGifAssets() = runTest {
        var requestedHost = ""
        var requestedPath = ""
        var requestedQuery = ""
        var requestedLimit = ""

        val client = HttpClient(MockEngine { request ->
            requestedHost = request.url.host
            requestedPath = request.url.encodedPath
            requestedQuery = request.url.parameters["q"].orEmpty()
            requestedLimit = request.url.parameters["limit"].orEmpty()

            respond(
                content = """
                    {
                      "next": "next-page",
                      "results": [
                        {
                          "id": "gif-1",
                          "itemurl": "https://klipy.example/gif-1",
                          "media_formats": {
                            "gif": { "url": "https://cdn.example/full.gif", "preview": "", "dims": [480, 270], "size": 1000 },
                            "nanogif": { "url": "https://cdn.example/preview.gif", "preview": "", "dims": [120, 68], "size": 100 }
                          }
                        }
                      ]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val result = KlipyGifClient(client = client, apiKey = KlipyApiKey("test-key")).search(
            query = "lightning bolt",
            limit = 12,
            cursor = null
        )

        val page = assertIs<GifSearchResult.Success>(result).page
        assertEquals("api.klipy.com", requestedHost)
        assertEquals("/v2/search", requestedPath)
        assertEquals("lightning bolt", requestedQuery)
        assertEquals("12", requestedLimit)
        assertEquals("next-page", page.nextCursor)
        assertEquals(
            GifAsset(
                id = "gif-1",
                previewUrl = "https://cdn.example/preview.gif",
                fullUrl = "https://cdn.example/full.gif",
                width = 480,
                height = 270,
                provider = GifProvider.KLIPY,
                attributionUrl = "https://klipy.example/gif-1"
            ),
            page.items.single()
        )
    }

    @Test
    fun sendsCursorWhenRequestingNextPage() = runTest {
        var requestedCursor = ""
        val client = HttpClient(MockEngine { request ->
            requestedCursor = request.url.parameters["pos"].orEmpty()
            respond(
                content = """{"next":"","results":[]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val result = KlipyGifClient(client = client, apiKey = KlipyApiKey("test-key")).search(
            query = "token",
            limit = 6,
            cursor = "cursor-2"
        )

        assertIs<GifSearchResult.Success>(result)
        assertEquals("cursor-2", requestedCursor)
    }

    @Test
    fun returnsFailureForMissingApiKeyWithoutMakingRequest() = runTest {
        var requestCount = 0
        val client = HttpClient(MockEngine {
            requestCount += 1
            respond("""{"next":"","results":[]}""")
        })

        val result = KlipyGifClient(client = client, apiKey = KlipyApiKey("")).search(
            query = "angel",
            limit = 4,
            cursor = null
        )

        assertEquals(0, requestCount)
        assertEquals(GifSearchFailure.MISSING_API_KEY, assertIs<GifSearchResult.Failure>(result).reason)
    }

    @Test
    fun returnsFailureForMalformedResponse() = runTest {
        val client = HttpClient(MockEngine {
            respond(
                content = """{"results":[{"id":"broken"}]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val result = KlipyGifClient(client = client, apiKey = KlipyApiKey("test-key")).search(
            query = "dragon",
            limit = 4,
            cursor = null
        )

        assertEquals(GifSearchFailure.MALFORMED_RESPONSE, assertIs<GifSearchResult.Failure>(result).reason)
    }

    @Test
    fun returnsFailureForProviderError() = runTest {
        val client = HttpClient(MockEngine {
            respond(
                content = """{"error":"rate limited"}""",
                status = HttpStatusCode.TooManyRequests,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val result = KlipyGifClient(client = client, apiKey = KlipyApiKey("test-key")).search(
            query = "wizard",
            limit = 4,
            cursor = null
        )

        assertEquals(GifSearchFailure.PROVIDER_ERROR, assertIs<GifSearchResult.Failure>(result).reason)
    }

    @Test
    fun mapsEmptyResponseAsSuccessfulEmptyPage() = runTest {
        val client = HttpClient(MockEngine {
            respond(
                content = """{"next":"","results":[]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val result = KlipyGifClient(client = client, apiKey = KlipyApiKey("test-key")).search(
            query = "nothing",
            limit = 4,
            cursor = null
        )

        assertTrue(assertIs<GifSearchResult.Success>(result).page.items.isEmpty())
    }
}
