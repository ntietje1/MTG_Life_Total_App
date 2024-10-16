package data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Keys(
    val tenor_api_key: String
)

class CredentialManager {
    private val client = HttpClient()

    suspend fun fetchTenorApiKey(): String {
        val url = "https://shorturl.at/qYBMz"
        val response: String = client.get(url).bodyAsText()
        val keys = Json.decodeFromString(Keys.serializer(), response)
        return keys.tenor_api_key
    }
}