package data.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a list of rulings for a card
 */
@Serializable
data class RulingResponse(
    @SerialName("object") val type: String = "list", @SerialName("has_more") val hasMore: Boolean = false, @SerialName("data") val data: List<Ruling> = listOf()
)