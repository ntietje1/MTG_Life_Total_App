package data

import data.serializable.Card
import data.serializable.CardResponse
import data.serializable.Ruling
import data.serializable.RulingResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ScryfallApiRetriever {

    val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun searchScryfall(query: String): String = withContext(Dispatchers.IO) {
        try {
            val client = HttpClient()
            val q = query.ifEmpty { " " }
            val url = if (!query.startsWith("https://api.scryfall.com/")) {
                "https://api.scryfall.com/cards/search?q=$q"
            } else {
                q
            }
            val response = client.get(url)
            return@withContext response.body<String>()
        } catch (e: Exception) {
            return@withContext "{}"
        }
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



