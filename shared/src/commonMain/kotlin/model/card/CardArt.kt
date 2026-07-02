package model.card

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CardArt(
    val small: String,
    val normal: String,
    val large: String,
    @SerialName("art_crop")
    val artCrop: String
)

fun Card.art(): CardArt? {
    return imageUris?.toCardArt()
        ?: cardFaces.orEmpty().firstNotNullOfOrNull { face -> face.imageUris?.toCardArt() }
}

private fun ImageUris.toCardArt(): CardArt {
    return CardArt(
        small = small,
        normal = normal,
        large = large,
        artCrop = artCrop
    )
}
