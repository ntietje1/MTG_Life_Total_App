package model.card

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CardFace(
    @SerialName("image_uris") val imageUris: ImageUris? = null, @SerialName("artist") val artist: String
)