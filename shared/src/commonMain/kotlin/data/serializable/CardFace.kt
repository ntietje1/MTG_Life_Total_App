package data.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single card face (for dual-faced cards)
 */
@Serializable
data class CardFace(
    @SerialName("image_uris") val imageUris: ImageUris? = null, @SerialName("artist") val artist: String
)