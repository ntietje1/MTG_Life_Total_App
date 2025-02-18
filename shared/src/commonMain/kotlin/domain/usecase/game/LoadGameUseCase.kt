package domain.usecase.game

import data.GameRepository
import model.GameWithPlayers

class LoadGameStateUseCase(
    private val repository: GameRepository
) {
    operator fun invoke(gameId: Long): GameWithPlayers {
        return repository.getGameWithPlayers(gameId).also {
            println("LoadGameStateUseCase.invoke($gameId) done, monarchId: ${it.game.monarchyPlayerNum}")
        }
    }
}