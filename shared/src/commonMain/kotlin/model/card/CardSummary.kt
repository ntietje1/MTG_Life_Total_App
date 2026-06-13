package model.card

data class CardSummary(
    val id: String,
    val name: String,
    val oracleText: String? = null,
    val art: CardArt? = null,
    val artist: String,
    val setName: String,
    val printsSearchUri: String? = null,
    val rulingsUri: String? = null
)

data class RulingSummary(
    val comment: String,
    val publishedAt: String,
    val source: String
)

fun Card.toSummary(): CardSummary {
    return CardSummary(
        id = id,
        name = name,
        oracleText = oracleText,
        art = art(),
        artist = artist,
        setName = setName,
        printsSearchUri = printsSearchUri,
        rulingsUri = rulingsUri
    )
}

fun Ruling.toSummary(): RulingSummary {
    return RulingSummary(
        comment = comment,
        publishedAt = publishedAt,
        source = source
    )
}
