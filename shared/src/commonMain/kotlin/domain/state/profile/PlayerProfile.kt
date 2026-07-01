package domain.state.profile

import domain.state.game.PlayerProfileId
import domain.state.game.SeatAppearance
import kotlinx.serialization.Serializable

@Serializable
data class PlayerProfile(
    val id: PlayerProfileId,
    val displayName: String,
    val colors: PlayerColors = PlayerColors(),
    val background: PlayerBackground = PlayerBackground.None
) {
    fun toSeatAppearance(): SeatAppearance {
        return SeatAppearance(
            displayName = displayName,
            colors = colors,
            background = background,
            sourceProfileId = id
        )
    }
}

@Serializable
data class PlayerColors(
    val backgroundArgb: Int = -2894893,
    val textArgb: Int = -1
) {
    companion object {
        val DefaultPalette = listOf(
            PlayerColors(backgroundArgb = -882206),
            PlayerColors(backgroundArgb = -1160057),
            PlayerColors(backgroundArgb = -565409),
            PlayerColors(backgroundArgb = -553387),
            PlayerColors(backgroundArgb = -539553),
            PlayerColors(backgroundArgb = -12264868),
            PlayerColors(backgroundArgb = -12130341),
            PlayerColors(backgroundArgb = -10330121),
            PlayerColors(backgroundArgb = -4026628)
        )
    }
}

@Serializable
sealed interface PlayerBackground {
    @Serializable
    data object None : PlayerBackground

    @Serializable
    data class LocalImage(val fileName: String) : PlayerBackground

    @Serializable
    data class ProviderImage(val url: String) : PlayerBackground

    @Serializable
    data class CardArt(val url: String) : PlayerBackground
}
