package model.card

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RulingResponse(
    @SerialName("object") val type: String = "list", @SerialName("has_more") val hasMore: Boolean = false, @SerialName("data") val data: List<Ruling> = listOf()
)