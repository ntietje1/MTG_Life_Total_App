package domain.api

interface GifSearchClient {
    suspend fun search(query: String, limit: Int, cursor: String?): GifSearchResult
}

sealed interface GifSearchResult {
    data class Success(val page: GifPage) : GifSearchResult
    data class Failure(val reason: GifSearchFailure) : GifSearchResult
}

enum class GifSearchFailure {
    MISSING_API_KEY,
    PROVIDER_ERROR,
    MALFORMED_RESPONSE,
    NETWORK_ERROR
}

enum class GifProvider {
    KLIPY
}

data class GifPage(
    val items: List<GifAsset>,
    val nextCursor: String?
)

data class GifAsset(
    val id: String,
    val previewUrl: String,
    val fullUrl: String,
    val width: Int,
    val height: Int,
    val provider: GifProvider,
    val attributionUrl: String
)
