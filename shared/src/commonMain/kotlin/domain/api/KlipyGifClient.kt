package domain.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class KlipyGifClient(
    private val client: HttpClient = HttpClient(),
    private val apiKey: KlipyApiKey
) : GifSearchClient {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun search(query: String, limit: Int, cursor: String?): GifSearchResult {
        if (apiKey.value.isBlank()) {
            return GifSearchResult.Failure(GifSearchFailure.MISSING_API_KEY)
        }

        return try {
            val response = client.get(KLIPY_SEARCH_URL) {
                header(HttpHeaders.Accept, "application/json")
                parameter("q", query)
                parameter("key", apiKey.value)
                parameter("limit", limit)
                if (!cursor.isNullOrBlank()) {
                    parameter("pos", cursor)
                }
            }

            if (response.status != HttpStatusCode.OK) {
                return GifSearchResult.Failure(GifSearchFailure.PROVIDER_ERROR)
            }

            val page = json.decodeFromString<KlipyGifResponse>(response.bodyAsText()).toGifPage()
                ?: return GifSearchResult.Failure(GifSearchFailure.MALFORMED_RESPONSE)
            GifSearchResult.Success(page)
        } catch (_: SerializationException) {
            GifSearchResult.Failure(GifSearchFailure.MALFORMED_RESPONSE)
        } catch (_: Exception) {
            GifSearchResult.Failure(GifSearchFailure.NETWORK_ERROR)
        }
    }
}

private const val KLIPY_SEARCH_URL = "https://api.klipy.com/v2/search"

@Serializable
private data class KlipyGifResponse(
    val next: String = "",
    val results: List<KlipyGifObject> = emptyList()
) {
    fun toGifPage(): GifPage? {
        val assets = results.map { it.toGifAsset() ?: return null }
        return GifPage(
            items = assets,
            nextCursor = next.takeIf { it.isNotBlank() }
        )
    }
}

@Serializable
private data class KlipyGifObject(
    val id: String,
    @SerialName("media_formats") val formats: KlipyMediaFormats,
    val itemurl: String
) {
    fun toGifAsset(): GifAsset? {
        val full = formats.normal() ?: return null
        val preview = formats.preview() ?: return null
        val width = full.dims.getOrNull(0) ?: return null
        val height = full.dims.getOrNull(1) ?: return null
        return GifAsset(
            id = id,
            previewUrl = preview.url,
            fullUrl = full.url,
            width = width,
            height = height,
            provider = GifProvider.KLIPY,
            attributionUrl = itemurl
        )
    }
}

@Serializable
private data class KlipyMediaFormats(
    val gif: KlipyMediaObject? = null,
    @SerialName("mediumgif") val mediumGif: KlipyMediaObject? = null,
    @SerialName("tinygif") val tinyGif: KlipyMediaObject? = null,
    @SerialName("nanogif") val nanoGif: KlipyMediaObject? = null
) {
    fun normal(): KlipyMediaObject? = gif ?: mediumGif ?: tinyGif ?: nanoGif
    fun preview(): KlipyMediaObject? = nanoGif ?: tinyGif ?: mediumGif ?: gif
}

@Serializable
private data class KlipyMediaObject(
    val preview: String = "",
    val url: String,
    val dims: List<Int> = emptyList(),
    val size: Int = 0
)
