package domain.usecase.game

import data.GameRepository
import domain.storage.ISettingsStore
import model.Game
import model.GameWithPlayers
import model.Player
import model.Player.Companion.MAX_PLAYERS

class NewGameUseCase(
    private val settingsManager: ISettingsStore,
    private val gameRepository: GameRepository
) {
    operator fun invoke(
        numPlayers: Int, 
        playerGenerateFunction: (Int) -> Player
    ): GameWithPlayers {
        var game = Game(numPlayers = numPlayers)
        val gid = gameRepository.insertGame(game)
        game = game.copy(id = gid)
        settingsManager.setCurrentGameId(gid)
        
        val players = List(MAX_PLAYERS) {
            playerGenerateFunction(it + 1).copy(gameId = gid)
        }
        players.forEach {
            gameRepository.insertPlayer(it)
        }
        return GameWithPlayers(game, players)
    }
}