package domain.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ScryfallApiTest {
    @Test
    fun searchCardsBuildsEncodedSearchRequestWithRequiredHeaders() = runTest {
        var requestedPath = ""
        var requestedQuery = ""
        var requestedUserAgent = ""
        var requestedAccept = ""
        val client = HttpClient(MockEngine { request ->
            requestedPath = request.url.encodedPath
            requestedQuery = request.url.encodedQuery
            requestedUserAgent = request.headers[HttpHeaders.UserAgent].orEmpty()
            requestedAccept = request.headers[HttpHeaders.Accept].orEmpty()
            respond(
                content = cardSearchResponse(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val result = ScryfallApi(client = client, requestDelayMillis = 0).searchCards("Lightning Bolt")

        val page = assertIs<ScryfallResult.Success<ScryfallPage>>(result).value
        assertEquals("/cards/search", requestedPath)
        assertEquals("q=Lightning+Bolt", requestedQuery)
        assertTrue(requestedUserAgent.startsWith("LifeLinked/"))
        assertEquals("application/json", requestedAccept)
        assertEquals("Lightning Bolt", page.cards.single().name)
        assertEquals("https://api.scryfall.com/cards/search?page=2&q=Lightning+Bolt", page.nextPageUrl)
    }

    @Test
    fun searchCardsAllowsScryfallAbsoluteUrlsForPagingAndPrintsSearches() = runTest {
        var requestedUrl = ""
        val client = HttpClient(MockEngine { request ->
            requestedUrl = request.url.toString()
            respond(
                content = cardSearchResponse(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val result = ScryfallApi(client = client, requestDelayMillis = 0)
            .searchCards("https://api.scryfall.com/cards/search?order=released&q=%21%22Lightning%20Bolt%22&unique=prints")

        assertIs<ScryfallResult.Success<ScryfallPage>>(result)
        assertEquals(
            "https://api.scryfall.com/cards/search?order=released&q=%21%22Lightning%20Bolt%22&unique=prints",
            requestedUrl
        )
    }

    @Test
    fun rejectsNonScryfallAbsoluteUrls() = runTest {
        var requestCount = 0
        val client = HttpClient(MockEngine {
            requestCount += 1
            respond(cardSearchResponse())
        })

        val result = ScryfallApi(client = client, requestDelayMillis = 0)
            .searchCards("https://example.com/cards/search?q=Lightning+Bolt")

        assertEquals(0, requestCount)
        assertEquals(ScryfallFailure.REJECTED_URL, assertIs<ScryfallResult.Failure>(result).reason)
    }

    @Test
    fun mapsNotFoundToExplicitFailure() = runTest {
        val client = HttpClient(MockEngine {
            respondError(HttpStatusCode.NotFound, """{"object":"error"}""")
        })

        val result = ScryfallApi(client = client, requestDelayMillis = 0).searchCards("missing card")

        assertEquals(ScryfallFailure.NOT_FOUND, assertIs<ScryfallResult.Failure>(result).reason)
    }

    @Test
    fun mapsRateLimitToExplicitFailure() = runTest {
        val client = HttpClient(MockEngine {
            respondError(HttpStatusCode.TooManyRequests, """{"object":"error"}""")
        })

        val result = ScryfallApi(client = client, requestDelayMillis = 0).searchCards("too fast")

        assertEquals(ScryfallFailure.RATE_LIMITED, assertIs<ScryfallResult.Failure>(result).reason)
    }

    @Test
    fun mapsNetworkFailureToExplicitFailure() = runTest {
        val client = HttpClient(MockEngine {
            error("network unavailable")
        })

        val result = ScryfallApi(client = client, requestDelayMillis = 0).searchCards("Forest")

        assertEquals(ScryfallFailure.NETWORK_ERROR, assertIs<ScryfallResult.Failure>(result).reason)
    }

    @Test
    fun mapsMalformedJsonToExplicitFailure() = runTest {
        val client = HttpClient(MockEngine {
            respond(
                content = """{"data":[{"name":"Broken"}]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val result = ScryfallApi(client = client, requestDelayMillis = 0).searchCards("Broken")

        assertEquals(ScryfallFailure.MALFORMED_RESPONSE, assertIs<ScryfallResult.Failure>(result).reason)
    }

    @Test
    fun searchRulingsUsesAllowedRulingsUrl() = runTest {
        var requestedPath = ""
        val client = HttpClient(MockEngine { request ->
            requestedPath = request.url.encodedPath
            respond(
                content = """
                    {
                      "object": "list",
                      "has_more": false,
                      "data": [
                        {
                          "comment": "A ruling.",
                          "published_at": "2024-01-01",
                          "source": "wotc"
                        }
                      ]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })

        val result = ScryfallApi(client = client, requestDelayMillis = 0)
            .searchRulings("https://api.scryfall.com/cards/abc/rulings")

        val rulings = assertIs<ScryfallResult.Success<List<*>>>(result).value
        assertEquals("/cards/abc/rulings", requestedPath)
        assertEquals(1, rulings.size)
    }

    private fun cardSearchResponse(): String = """
        {
          "object": "list",
          "total_cards": 1,
          "has_more": true,
          "next_page": "https://api.scryfall.com/cards/search?page=2&q=Lightning+Bolt",
          "data": [
            {
              "name": "Lightning Bolt",
              "id": "card-1",
              "artist": "Christopher Rush",
              "set_name": "Limited Edition Alpha",
              "prints_search_uri": "https://api.scryfall.com/cards/search?order=released&q=%21%22Lightning%20Bolt%22&unique=prints",
              "rulings_uri": "https://api.scryfall.com/cards/card-1/rulings"
            }
          ]
        }
    """.trimIndent()
}
