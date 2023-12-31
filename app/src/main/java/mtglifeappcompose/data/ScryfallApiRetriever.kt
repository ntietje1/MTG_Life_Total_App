package mtglifeappcompose.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ScryfallApiRetriever {

    val json = Json {
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

    inline fun <reified T> parseScryfallResponse(response: String): List<T> {
        return when (T::class) {
            Card::class -> {
                val jsonResponse = json.decodeFromString<CardResponse>(response)
                jsonResponse.data as List<T>
            }

            Ruling::class -> {
                val jsonResponse = json.decodeFromString<RulingResponse>(response)
                jsonResponse.data as List<T>
            }

            else -> throw IllegalArgumentException("Unsupported type parameter")
        }
    }
}

@Serializable
data class CardResponse(
    @SerialName("object") val type: String = "error",
    @SerialName("details") val details: String? = null,
    @SerialName("total_cards") val totalCards: Int? = 0,
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("next_page") val nextPage: String? = null,
    @SerialName("data") val data: List<Card> = listOf()
)

@Serializable
data class RulingResponse(
    @SerialName("object") val type: String = "list", @SerialName("has_more") val hasMore: Boolean = false, @SerialName("data") val data: List<Ruling> = listOf()
)

@Serializable
data class Ruling(
    @SerialName("comment") val comment: String,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("source") val source: String,
)

@Serializable
data class Card(
    @SerialName("name") val name: String,
    @SerialName("oracle_text") val oracleText: String? = null,
    @SerialName("image_uris") val imageUris: ImageUris? = null,
    @SerialName("card_faces") val cardFaces: List<CardFace>? = null,
    @SerialName("artist") val artist: String,
    @SerialName("set_name") val setName: String,
    @SerialName("prints_search_uri") val printsSearchUri: String,
    @SerialName("rulings_uri") val rulingsUri: String? = null,
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
    @SerialName("image_uris") val imageUris: ImageUris? = null, @SerialName("artist") val artist: String
)

@Serializable
data class ImageUris(
    @SerialName("small") val small: String,
    @SerialName("normal") val normal: String,
    @SerialName("large") val large: String,
    @SerialName("art_crop") val artCrop: String,

    )