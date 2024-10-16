package data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject


class GifApiRetriever(
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

    suspend fun searchGifs(query: String, limit: Int): List<MediaFormat> {
//        println("SEARCHING GIFS: $query")
        clearNext()
        val response = getSearchResults(query, limit)
//        println("RESPONSE: $response")
        if (response == null) {
            println("Error getting gifs. returning empty list")
            return emptyList()
        }
        val gifResponse = json.decodeFromString<GifResponse>(response.toString())
        next = gifResponse.next
        lastQuery = query
        return gifResponse.results.map { it.formats }
    }

    suspend fun getNextGifs(limit: Int): List<MediaFormat> {
        if (next == null || lastQuery == null) {
            throw Exception("Attempted to get next gifs without a previous search")
        }
        val response = getSearchResults(lastQuery!!, limit)
        if (response == null) {
            println("Error getting gifs. returning empty list")
            return emptyList()
        }
        val gifResponse = json.decodeFromString<GifResponse>(response.toString())
        println("INCREMENTING NEXT FROM: $next TO: ${gifResponse.next}")
        next = gifResponse.next
        return gifResponse.results.map { it.formats }
    }

    /**
     * Get Search Result GIFs
     */
    private suspend fun getSearchResults(query: String, limit: Int): JsonObject? {

        val url = "https://tenor.googleapis.com/v2/search?q=${query}&key=${API_KEY}&limit=${limit}" + if (next != null) "&pos=$next" else ""
        println("URL: $url")

        try {
            return get(url)
        } catch (e: kotlinx.io.IOException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Construct and run a GET request
     */
    private suspend fun get(url: String): JsonObject {
        try {
            val response: HttpResponse = client.get(url) {
                headers {
                    append("Content-Type", "application/json; charSet=UTF-8")
                    append("Accept", "application/json")
                }
            }

            // Handle failure
            val statusCode = response.status.value
            println("STATUS CODE: $statusCode")
            if (statusCode != HttpStatusCode.OK.value && statusCode != HttpStatusCode.Created.value) {
                println("Error getting gifs!!")
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
            throw err
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