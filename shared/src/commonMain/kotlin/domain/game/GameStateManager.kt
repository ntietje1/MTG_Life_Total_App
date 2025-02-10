package domain.game

import data.GameRepository
import domain.storage.ISettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import model.Game
import model.GameWithPlayers
import model.Player
import model.Player.Companion.MAX_PLAYERS
import ui.lifecounter.playerbutton.PlayerButtonViewModel
import kotlin.coroutines.coroutineContext

/**
 * Manages a game state that is shared among all players
 */
class GameStateManager(
    private val settingsManager: ISettingsManager,
    private val gameRepository: GameRepository
) {

    suspend fun attachMonarchyObserver(playerButtonViewModels:  StateFlow<List<PlayerButtonViewModel>>) {
        playerButtonViewModels.value.forEach { viewModel ->
            CoroutineScope(coroutineContext).launch {
                viewModel.state.collect { state ->
                    if (state.player.monarch) {
                        playerButtonViewModels.value
                            .filter { it != viewModel }
                            .forEach { it.setMonarchy(false) }
                    }
                }
            }
        }
    }

    fun newGame(numPlayers: Int, playerGenerateFunction: (Int) -> Player): GameWithPlayers {
        var game = Game(numPlayers = numPlayers)
        val gid = gameRepository.insertGame(
            game = game
        )
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

    fun saveGame(game: Game) {
        gameRepository.updateGame(game)
    }

    fun loadGameState(gameId: Long): GameWithPlayers {
        return gameRepository.getGameWithPlayers(gameId)
    }
} 