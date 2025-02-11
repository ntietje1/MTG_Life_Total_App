package domain.usecase.player.state

import data.GameRepository
import model.Player

class SavePlayerStateUseCase(
    private val gameRepository: GameRepository
) {
    operator fun invoke(player: Player) {
        gameRepository.updatePlayer(player)
    }
}