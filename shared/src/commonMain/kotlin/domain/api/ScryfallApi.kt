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
        return json.decodeFromString<RulingResponse>(response).data
    }

    suspend fun searchCards(query: String): List<Card> {
        val response = searchScryfall(query)
        return json.decodeFromString<CardResponse>(response).data
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

}



