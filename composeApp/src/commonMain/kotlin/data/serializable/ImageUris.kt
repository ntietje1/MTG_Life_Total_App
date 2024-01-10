package data.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the image uris for a card
 */
@Serializable
data class ImageUris(
    @SerialName("small") val small: String,
    @SerialName("normal") val normal: String,
    @SerialName("large") val large: String,
    @SerialName("art_crop") val artCrop: String,
)