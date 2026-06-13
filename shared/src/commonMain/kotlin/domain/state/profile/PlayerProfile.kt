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
)

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
