package domain.game

import data.GameRepository
import domain.storage.ISettingsManager
import model.Game
import model.GameWithPlayers
import model.Player
import model.Player.Companion.MAX_PLAYERS
import ui.lifecounter.DayNightState
import ui.lifecounter.playerbutton.PlayerButtonViewModel

/**
 * Manages a game state that is shared among all players
 */
class GameStateManager(
    private val settingsManager: ISettingsManager,
    private val gameRepository: GameRepository
) {
    fun toggleDayNight(currentState: DayNightState): DayNightState {
        return when (currentState) {
            DayNightState.NONE -> DayNightState.DAY
            DayNightState.DAY -> DayNightState.NIGHT
            DayNightState.NIGHT -> DayNightState.DAY
        }
    }

    fun setMonarchy(targetPlayerNum: Int, value: Boolean): (PlayerButtonViewModel) -> Unit {
        return { playerButtonViewModel ->
            playerButtonViewModel.setPlayer(
                updateMonarchy(
                    player = playerButtonViewModel.state.value.player,
                    targetPlayerNum = targetPlayerNum,
                    value = value
                )
            )
        }
    }

    private fun updateMonarchy(player: Player, targetPlayerNum: Int, value: Boolean): Player {
        return player.copy(monarch = value && player.playerNum == targetPlayerNum)
    }

//    fun loadGameState() {
//        val playerStates = settingsManager.loadPlayerStates()
//        val playerButtonViewModels = playerStates.map { PlayerButtonViewModel(it) }
//        attach(playerButtonViewModels)
//    }

    fun savePlayerState(player: Player) {
//        val playerButtonViewModels = requireAttached().value
//        settingsManager.savePlayerStates(playerButtonViewModels.map { it.state.value.player })
        return gameRepository.updatePlayer(player)
//        return 0L
    }

//    suspend fun saveGameState() {
//        val playerButtonViewModels = requireAttached().value
//        settingsManager.savePlayerStates(playerButtonViewModels.map { it.state.value.player })
//    }

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

    fun saveGameState(gameWithPlayers: GameWithPlayers) {
        println("GameStateManager.saveGameState2($gameWithPlayers)")
        gameRepository.updateGameWithPlayers(gameWithPlayers)
        println("GameStateManager.saveGameState2 done")
    }

    fun loadGameState(gameId: Long): GameWithPlayers {
        return gameRepository.getGameWithPlayers(gameId)
    }
} 