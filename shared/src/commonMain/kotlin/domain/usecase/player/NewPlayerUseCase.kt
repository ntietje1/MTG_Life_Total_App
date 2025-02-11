package domain.usecase.player

import domain.usecase.player.state.NewPlayerStateUseCase

class NewPlayerUseCase(
    private val newPlayerStateUseCase: NewPlayerStateUseCase
) {
    operator fun invoke(playerNum: Int) = newPlayerStateUseCase(playerNum)
}