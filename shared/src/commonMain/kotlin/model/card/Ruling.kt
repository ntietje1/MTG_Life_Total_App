package model.card

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Ruling(
    @SerialName("comment") val comment: String,
    @SerialName("published_at") val publishedAt: String,
    @SerialName("source") val source: String,
)