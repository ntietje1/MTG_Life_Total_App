package data

import data.serializable.Card
import data.serializable.CardResponse
import data.serializable.Ruling
import data.serializable.RulingResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Handles retrieving data from the Scryfall API
 */
class ScryfallApiRetriever {

    val json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Searches the Scryfall API for the given query
     * @param query The query to search for
     * @return The response from the Scryfall API
     */
    suspend fun searchScryfall(query: String): String = withContext(Dispatchers.IO) {
        val client = HttpClient()
        val url = if (!query.startsWith("https://api.scryfall.com/")) {
            "https://api.scryfall.com/cards/search?q=$query"
        } else {
            query
        }

        val response = client.get(url)

        try {
            return@withContext response.body<String>()
        } catch (e: ClientRequestException) {
            return@withContext "{}"
        } catch (e: ServerResponseException) {
            return@withContext "{}"
        }
    }

    /**
     * Parses the response from the Scryfall API into a list of the given type
     * @param T The type to parse the response into
     * @param response The response from the Scryfall API
     * @return A list of the given type
     */
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



