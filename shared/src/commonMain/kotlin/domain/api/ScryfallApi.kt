package domain.api

import model.card.Card
import model.card.CardResponse
import model.card.Ruling
import model.card.RulingResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class ScryfallApi(private val client: HttpClient = HttpClient()) {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun searchRulings(query: String): List<Ruling> {
        val response = searchScryfall(query)
        return parseScryfallResponse(response)
    }

    suspend fun searchCards(query: String): List<Card> {
        val response = searchScryfall(query)
        return parseScryfallResponse(response)
    }

    private suspend fun searchScryfall(query: String): String = withContext(Dispatchers.IO) {
        try {
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

    private inline fun <reified T> parseScryfallResponse(response: String): List<T> {
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



