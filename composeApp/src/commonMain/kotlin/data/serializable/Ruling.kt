package data.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single ruling for a card
 */
@Serializable
data class Ruling(
    @SerialName("comment") val comment: String,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("source") val source: String,
)