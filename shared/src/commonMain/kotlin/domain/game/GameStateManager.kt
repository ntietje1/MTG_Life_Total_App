package domain.game

import data.GameRepository
import domain.storage.ISettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.DayNightState
import model.Game
import model.GameWithPlayers
import model.Player
import model.Player.Companion.MAX_PLAYERS

/**
 * Manages a game state that is shared among all players
 */
class GameStateManager(
    private val settingsManager: ISettingsManager,
    private val gameRepository: GameRepository
) {
    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState.asStateFlow()

    fun init(game: Game) {
        setGameState(game)
        settingsManager.setCurrentGameId(game.id)
        setPlayerSelectSeen(true) // If we are initializing the game, we don't need to show the player select screen again
    }

    fun setDayNight(value: DayNightState) {
        val currentGameState = gameState.value ?: return
        val updatedGameState = currentGameState.copy(dayNightState = value)
        setGameState(updatedGameState)
        gameRepository.updateDayNight(
            gameId = currentGameState.id,
            dayNightState = value
        )
    }

    fun toggleDayNight() {
        val currentGameState = gameState.value ?: return
        val currentDayNightState = currentGameState.dayNightState
        val newDayNightState = when (currentDayNightState) {
            DayNightState.NONE -> DayNightState.DAY
            DayNightState.DAY -> DayNightState.NIGHT
            DayNightState.NIGHT -> DayNightState.DAY
        }
        setDayNight(newDayNightState)
    }

    fun setMonarch(targetPlayerId: Long, value: Boolean) {
        val currentGameState = gameState.value ?: return
        val monarchPid = if (value) targetPlayerId else null
        val updatedGameState = currentGameState.copy(monarchPid = monarchPid)
        setGameState(updatedGameState)
        gameRepository.updateMonarch(
            gameId = currentGameState.id,
            monarchPid = monarchPid
        )
    }

    fun setNumPlayers(numPlayers: Int) {
        if (numPlayers < 1 || numPlayers > MAX_PLAYERS) throw IllegalArgumentException("Invalid number of players")
        val currentGameState = gameState.value ?: return
        val updatedGameState = currentGameState.copy(numPlayers = numPlayers)
        setGameState(updatedGameState)
        settingsManager.setDefaultNumPlayers(numPlayers)
        gameRepository.updateNumPlayers(numPlayers, currentGameState.id)
    }

    fun setAltPlayerLayout(altPlayerLayout: Boolean) {
        val currentGameState = gameState.value ?: return
        val updatedGameState = currentGameState.copy(altPlayerLayout = altPlayerLayout)
        setGameState(updatedGameState)
        settingsManager.setDefaultAltPlayerLayout(altPlayerLayout)
        gameRepository.updateAltPlayerLayout(altPlayerLayout, currentGameState.id)
    }

    private fun setPlayerSelectSeen(playerSelectSeen: Boolean) {
        val currentGameState = gameState.value ?: return
        val updatedGameState = currentGameState.copy(playerSelectSeen = playerSelectSeen)
        setGameState(updatedGameState)
        gameRepository.updatePlayerSelectSeen(playerSelectSeen, currentGameState.id)
    }

    private fun setGameState(game: Game) {
        _gameState.value = game
    }

    fun savePlayerState(player: Player): Long = gameRepository.updatePlayer(player)

    fun newGame(playerGenerateFunction: (Int) -> Player): GameWithPlayers {
        println("GameStateManager.newGame()")
        var game = Game(numPlayers = settingsManager.defaultNumPlayers.value)
        val gid = gameRepository.insertGame(
            game = game
        )
        game = game.copy(id = gid)
        val players = List(MAX_PLAYERS) {
            val player = playerGenerateFunction(it + 1).copy(
                gameId = gid
            )
            println("NEWGAME: $player")
            val pid = gameRepository.insertPlayer(
                player = player
            )
            player.copy(id = pid)
        }
        return GameWithPlayers(game, players)
    }

//    fun saveGame(game: Game, monarchPid: Long?) {
//        gameRepository.updateGame(game, monarchPid)
//    }

//    fun saveGameState(gameWithPlayers: GameWithPlayers) {
//        println("GameStateManager.saveGameState2($gameWithPlayers)")
////        gameRepository.updateGameWithPlayers(gameWithPlayers)
//        println("GameStateManager.saveGameState2 done")
//    }

    fun loadCurrentGameState(): GameWithPlayers? {
        val gameId = settingsManager.currentGameId.value
        return gameId?.let { gameRepository.getGameWithPlayers(it) }
    }

    fun loadGameState(gameId: Long): GameWithPlayers? {
        return gameRepository.getGameWithPlayers(gameId)
    }
} 