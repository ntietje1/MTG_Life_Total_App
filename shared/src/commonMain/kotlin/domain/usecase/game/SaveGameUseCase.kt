package domain.usecase.game

import data.GameRepository
import model.Game

class SaveGameUseCase(
    private val gameRepository: GameRepository
) {
    operator fun invoke(game: Game) {
        gameRepository.updateGame(game)
    }
}