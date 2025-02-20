package domain.usecase.game

import data.GameRepository
import domain.storage.ISettingsStore
import model.Game
import model.GameWithPlayers
import model.Player
import model.Player.Companion.MAX_PLAYERS

class  NewGameUseCase(
    private val settingsManager: ISettingsStore,
    private val gameRepository: GameRepository
) {
    operator fun invoke(
        numPlayers: Int = settingsManager.defaultNumPlayers.value,
        altPlayerLayout: Boolean = settingsManager.defaultAltPlayerLayout.value,
        turnTimerEnabled: Boolean = settingsManager.turnTimer.value,
        playerGenerateFunction: (Int) -> Player
    ): GameWithPlayers {
        var game = Game(numPlayers = numPlayers, altPlayerLayout = altPlayerLayout, turnTimerEnabled = turnTimerEnabled)
        val gid = gameRepository.insertGame(game)
        game = game.copy(id = gid)
        settingsManager.setCurrentGameId(gid)
        println("Generated new game with ID: $gid")
        
        val players = List(MAX_PLAYERS) {
            playerGenerateFunction(it + 1).copy(gameId = gid)
        }
        players.forEach {
            gameRepository.insertPlayer(it)
        }
        return GameWithPlayers(game, players)
    }
}