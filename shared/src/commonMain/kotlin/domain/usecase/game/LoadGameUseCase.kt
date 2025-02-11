package domain.usecase.game

import data.GameRepository
import model.GameWithPlayers

class LoadGameStateUseCase(
    private val gameRepository: GameRepository
) {
    operator fun invoke(gameId: Long): GameWithPlayers {
        return gameRepository.getGameWithPlayers(gameId)
    }
}