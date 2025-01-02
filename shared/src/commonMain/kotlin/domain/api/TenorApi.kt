package domain.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.io.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject


class ListResultOrError<T>(val result: List<T>? = listOf(), val error: Exception? = null)
enum class GifError {
    SEARCH_FAILED,
    NEXT_SEARCH_FAILED
}

class TenorApi(
    private val client: HttpClient = HttpClient()
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    private var next: String? = null
    private var lastQuery: String? = null

    private fun clearNext() {
        next = null
        lastQuery = null
    }

    suspend fun searchGifs(query: String, limit: Int): ListResultOrError<MediaFormat> {
        clearNext()
        val gifResponse = getSearchResults(query, limit)
        if (gifResponse == null) {
            println("Error getting gifs. returning empty list")
            return ListResultOrError(error = Exception(GifError.SEARCH_FAILED.name))
        }
        next = gifResponse.next
        lastQuery = query
        return ListResultOrError(gifResponse.results.map { it.formats })
    }

    suspend fun getNextGifs(limit: Int): ListResultOrError<MediaFormat> {
        if (next == null || lastQuery == null) {
            throw Exception("Attempted to get next gifs without a previous search")
        }
        val gifResponse = getSearchResults(lastQuery!!, limit)
        if (gifResponse == null) {
            println("Error getting gifs. returning empty list")
            return ListResultOrError(error = Exception(GifError.NEXT_SEARCH_FAILED.name))
        }
        next = gifResponse.next
        return ListResultOrError(gifResponse.results.map { it.formats })
    }

    /**
     * Get Search Result GIFs
     */
    private suspend fun getSearchResults(query: String, limit: Int): GifResponse? {
        try {
            val url = "https://tenor.googleapis.com/v2/search?q=${query}&key=$API_KEY&limit=${limit}" + if (next != null) "&pos=$next" else ""
            println("URL: $url")

            val response = get(url)
            if (response == null) {
                println("Error getting gifs. returning null")
                return null
            }
            val gifResponse = json.decodeFromString<GifResponse>(response.toString())
            return gifResponse
        } catch (err: Exception) {
            err.printStackTrace()
            return null
        }
    }

    /**
     * Construct and run a GET request
     */
    private suspend fun get(url: String): JsonObject? {
        try {
            val response: HttpResponse = client.get(url) {
                headers {
                    append("Content-Type", "application/json; charSet=UTF-8")
                    append("Accept", "application/json")
                }
            }

            // Handle failure
            val statusCode = response.status.value
            if (statusCode != HttpStatusCode.OK.value && statusCode != HttpStatusCode.Created.value) {
                val responseText = response.bodyAsText()
                throw ClientRequestException(response, responseText)
            }

            // Parse response
            val content = response.bodyAsText()
            if (content.isBlank()) {
                throw IOException("Empty response")
            }
            val jsonElement: JsonElement = json.parseToJsonElement(content)
            return jsonElement.jsonObject
        } catch (err: Exception) {
            err.printStackTrace()
            return null
        }
    }

    companion object {
        private val API_KEY by lazy {
            runBlocking {
                CredentialManager().fetchTenorApiKey()
            }
        }
    }
}

@Serializable
data class GifResponse(
    val next: String,
    val results: List<GifObject>
)

@Serializable
data class GifObject(
//    val created: Float,
//    val hasaudio: Boolean,
//    val id: String,
    @SerialName("media_formats")  val formats: MediaFormat,
//    val tags: List<String>,
//    val title: String,
    val itemurl: String,
//    val hascaption: Boolean? = null,
    val url: String
)

@Serializable
data class MediaFormat(
    val gif: MediaObject? = null,
    val mediumGif: MediaObject? = null,
    val tinyGif: MediaObject? = null,
    val nanoGif: MediaObject? = null,
    val mp4: MediaObject? = null,
    val loopedMp4: MediaObject? = null,
    val tinyMp4: MediaObject? = null,
    val nanoMp4: MediaObject? = null,
    val webm: MediaObject? = null,
    val tinyWebm: MediaObject? = null,
    val nanoWebm: MediaObject? = null
) {
    fun getNormalGif(): MediaObject {
        return gif ?: mediumGif ?: tinyGif ?: nanoGif ?: throw Exception("No gif found")
    }

    fun getPreviewGif(): MediaObject {
        return nanoGif ?: tinyGif ?: mediumGif ?: gif ?: throw Exception("No preview found")
    }
}

@Serializable
data class MediaObject(
    val preview: String,
    val url: String,
    val dims: List<Int>,
    val size: Int,
)