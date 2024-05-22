package data.serializable

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Card(
    @SerialName("name") val name: String,
    @SerialName("oracle_text") val oracleText: String? = null,
    @SerialName("image_uris") val imageUris: ImageUris? = null,
    @SerialName("card_faces") val cardFaces: List<CardFace>? = null,
    @SerialName("artist") val artist: String,
    @SerialName("set_name") val setName: String,
    @SerialName("prints_search_uri") val printsSearchUri: String,
    @SerialName("rulings_uri") val rulingsUri: String? = null,
) {
    fun getUris(): ImageUris {
        return when {
            imageUris != null -> imageUris
            cardFaces != null -> cardFaces[0].imageUris!!
            else -> throw Exception("Error parsing imageuri for card $name")
        }
    }
}