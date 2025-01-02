package model.card

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CardResponse(
    @SerialName("object") val type: String = "error",
    @SerialName("details") val details: String? = null,
    @SerialName("total_cards") val totalCards: Int? = 0,
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("next_page") val nextPage: String? = null,
    @SerialName("data") val data: List<Card> = listOf()
)