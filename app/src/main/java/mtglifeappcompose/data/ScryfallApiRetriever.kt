package mtglifeappcompose.data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ScryfallApiRetriever() {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun searchScryfall(query: String): String = withContext(Dispatchers.IO) {
        val url = if (!query.startsWith("https://api.scryfall.com/")) {
            URL("https://api.scryfall.com/cards/search?q=$query")
        } else {
            URL(query)
        }
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            return@withContext "{}"
        }

        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = reader.readText()
        reader.close()

        return@withContext response
    }

    fun parseScryfallResponse(response: String): List<Card> {
        val jsonResponse = json.decodeFromString<ScryfallResponse>(response)
        return jsonResponse.data
    }
}

@Serializable
data class ScryfallResponse(
    @SerialName("object") val type: String = "error",
    @SerialName("details") val details: String? = null,
    @SerialName("total_cards") val totalCards: Int? = 0,
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("next_page") val nextPage: String? = null,
    @SerialName("data") val data: List<Card> = listOf()
)

@Serializable
data class Card(
    @SerialName("name") val name: String,
    @SerialName("image_uris") val imageUris: ImageUris? = null,
    @SerialName("card_faces") val cardFaces: List<CardFace>? = null,
    @SerialName("artist") val artist: String,
    @SerialName("set_name") val setName: String,
    @SerialName("prints_search_uri") val printsSearchUri: String,
) {
    fun getUris(): ImageUris {
        return if (imageUris != null) {
            imageUris
        } else if (cardFaces != null) {
            cardFaces[0].imageUris!!
        } else {
            throw Exception("Error parsing imageuri for card $name")
        }
    }
}

@Serializable
data class CardFace(
    @SerialName("image_uris") val imageUris: ImageUris? = null,
    @SerialName("artist") val artist: String
)

@Serializable
data class ImageUris(
    @SerialName("small") val small: String,
    @SerialName("normal") val normal: String,
    @SerialName("large") val large: String,
    @SerialName("art_crop") val artCrop: String,

)