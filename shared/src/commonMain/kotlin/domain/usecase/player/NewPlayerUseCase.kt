package domain.usecase.player

import androidx.compose.ui.graphics.Color
import domain.usecase.player.customization.ResetPlayerCustomizationUseCase
import domain.usecase.player.state.NewPlayerStateUseCase

class NewPlayerUseCase(
    private val newPlayerStateUseCase: NewPlayerStateUseCase,
    private val resetPlayerCustomizationUseCase: ResetPlayerCustomizationUseCase
) {
    operator fun invoke(playerNum: Int, usedColors: Set<Color> = setOf()) = resetPlayerCustomizationUseCase(newPlayerStateUseCase(playerNum), usedColors)
}