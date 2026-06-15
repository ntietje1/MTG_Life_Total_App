package domain.api

import model.card.Card
import model.card.CardResponse
import model.card.Ruling
import model.card.RulingResponse
import model.card.CardSummary
import model.card.RulingSummary
import model.card.toSummary
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

interface ScryfallClient {
    suspend fun searchRulings(query: String): ScryfallResult<List<RulingSummary>>
    suspend fun searchCards(query: String): ScryfallResult<ScryfallPage>
}

class ScryfallApi(
    private val client: HttpClient = HttpClient(),
    private val requestDelayMillis: Long = 100
) : ScryfallClient {

    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val requestMutex = Mutex()
    private var lastRequest: TimeMark? = null

    override suspend fun searchRulings(query: String): ScryfallResult<List<RulingSummary>> {
        return when (val response = requestScryfall(query)) {
            is ScryfallResult.Failure -> response
            is ScryfallResult.Success -> decodeRulings(response.value)
        }
    }

    override suspend fun searchCards(query: String): ScryfallResult<ScryfallPage> {
        return when (val response = requestScryfall(query)) {
            is ScryfallResult.Failure -> response
            is ScryfallResult.Success -> decodeCards(response.value)
        }
    }

    private suspend fun requestScryfall(query: String): ScryfallResult<String> {
        val absoluteUrl = query.takeIf { it.startsWith("http://") || it.startsWith("https://") }
        if (absoluteUrl != null && !absoluteUrl.startsWith(SCRYFALL_API_BASE)) {
            return ScryfallResult.Failure(ScryfallFailure.REJECTED_URL)
        }

        return try {
            waitForRequestSlot()
            val response = if (absoluteUrl != null) {
                client.get(absoluteUrl) {
                    addScryfallHeaders()
                }
            } else {
                client.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = SCRYFALL_API_HOST
                        path("cards", "search")
                    }
                    addScryfallHeaders()
                    parameter("q", query.ifBlank { " " })
                }
            }

            when (response.status) {
                HttpStatusCode.OK -> ScryfallResult.Success(response.bodyAsText())
                HttpStatusCode.NotFound -> ScryfallResult.Failure(ScryfallFailure.NOT_FOUND)
                HttpStatusCode.TooManyRequests -> ScryfallResult.Failure(ScryfallFailure.RATE_LIMITED)
                else -> ScryfallResult.Failure(ScryfallFailure.HTTP_ERROR)
            }
        } catch (_: Exception) {
            ScryfallResult.Failure(ScryfallFailure.NETWORK_ERROR)
        }
    }

    private fun io.ktor.client.request.HttpRequestBuilder.addScryfallHeaders() {
        header(HttpHeaders.UserAgent, SCRYFALL_USER_AGENT)
        header(HttpHeaders.Accept, "application/json")
    }

    private suspend fun waitForRequestSlot() {
        if (requestDelayMillis <= 0) {
            return
        }

        requestMutex.withLock {
            val requiredDelay = requestDelayMillis.milliseconds
            val elapsed = lastRequest?.elapsedNow()
            if (elapsed != null && elapsed < requiredDelay) {
                delay(requiredDelay - elapsed)
            }
            lastRequest = TimeSource.Monotonic.markNow()
        }
    }

    private fun decodeCards(response: String): ScryfallResult<ScryfallPage> {
        return try {
            val decoded = json.decodeFromString<CardResponse>(response)
            ScryfallResult.Success(
                ScryfallPage(
                    cards = decoded.data.map(Card::toSummary),
                    nextPageUrl = decoded.nextPage
                )
            )
        } catch (_: SerializationException) {
            ScryfallResult.Failure(ScryfallFailure.MALFORMED_RESPONSE)
        } catch (_: IllegalArgumentException) {
            ScryfallResult.Failure(ScryfallFailure.MALFORMED_RESPONSE)
        }
    }

    private fun decodeRulings(response: String): ScryfallResult<List<RulingSummary>> {
        return try {
            ScryfallResult.Success(json.decodeFromString<RulingResponse>(response).data.map(Ruling::toSummary))
        } catch (_: SerializationException) {
            ScryfallResult.Failure(ScryfallFailure.MALFORMED_RESPONSE)
        } catch (_: IllegalArgumentException) {
            ScryfallResult.Failure(ScryfallFailure.MALFORMED_RESPONSE)
        }
    }
}

private const val SCRYFALL_API_HOST = "api.scryfall.com"
private const val SCRYFALL_API_BASE = "https://api.scryfall.com/"
private const val SCRYFALL_USER_AGENT = "LifeLinked/1.9.0 MTG life counter"

data class ScryfallPage(
    val cards: List<CardSummary>,
    val nextPageUrl: String?
)

sealed interface ScryfallResult<out T> {
    data class Success<T>(val value: T) : ScryfallResult<T>
    data class Failure(val reason: ScryfallFailure) : ScryfallResult<Nothing>
}

enum class ScryfallFailure {
    REJECTED_URL,
    NOT_FOUND,
    RATE_LIMITED,
    HTTP_ERROR,
    MALFORMED_RESPONSE,
    NETWORK_ERROR
}


