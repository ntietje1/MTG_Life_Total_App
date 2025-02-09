package data

import androidx.compose.ui.graphics.toArgb
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.hypeapps.lifelinked.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import model.Game
import model.GameWithPlayers
import model.Player

class GameRepository(
    private val database: Database,
    private val playerAdapter: PlayerAdapter,
    private val gameWithPlayersAdapter: GameWithPlayerAdapter
) {
    private val queries = database.playerQueries

    fun getAllPlayersAsFlow(): Flow<List<Player>> {
        return queries.getAllPlayers()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { players ->
                players.map { player ->
                    playerAdapter.toPlayer(
                        id = player.id,
                        name = player.name,
                        imageString = player.image_string,
                        color = player.color.toInt(),
                        textColor = player.text_color.toInt(),
                        playerNum = player.player_num.toInt(),
                        lifeTotal = player.life_total.toInt(),
                        lifeTotalRecentChange = player.life_total_recent_change.toInt(),
                        monarch = player.monarch,
                        setDead = player.set_dead,
                        partnerMode = player.partner_mode,
                        commanderDamages = player.commander_damages,
                        counters = player.counters,
                        activeCounters = null
                    )
                }
            }
    }

    fun insertPlayer(player: Player, gameId: Long): Long {
        var playerId: Long
//        withContext(Dispatchers.IO) {
            queries.insertPlayer(
                name = player.name,
                image_string = player.imageString,
                color = player.color.toArgb().toLong(),
                text_color = player.textColor.toArgb().toLong(),
                player_num = player.playerNum.toLong(),
                life_total = player.lifeTotal.number.toLong(),
                life_total_recent_change = player.lifeTotal.recentChange.toLong(),
                monarch = player.monarch,
                set_dead = player.setDead,
                partner_mode = player.partnerMode
            )

            playerId = queries.lastInsertRowId().executeAsOne()

            // Insert commander damages
            player.commanderDamage.forEachIndexed { index, damage ->
                queries.insertCommanderDamage(
                    reciever_player_id = playerId,
                    dealer_player_id = index.toLong(),
                    damage = damage.number.toLong(),
                    recent_change = damage.recentChange.toLong()
                )
            }

            // Insert counters
            player.counters.forEachIndexed { index, value ->
                queries.insertCounter(
                    player_id = playerId,
                    counter_type = index.toLong(),
                    value_ = value.toLong()
                )
            }

            // Insert game player
            database.gameQueries.insertGamePlayer(
                game_id = gameId,
                player_id = playerId
            )
//        }
        return playerId
    }

    suspend fun deleteAllPlayers() {
        withContext(Dispatchers.IO) {
            queries.deleteAllPlayers()
        }
    }

    fun getGameWithPlayers(gameId: Long): GameWithPlayers {
        val results = database.gameQueries.getGameWithPlayers(gameId).executeAsList()
        require(results.isNotEmpty()) { "No game with id $gameId found" }
        println("GameRepository.getGameWithPlayers($gameId) results: $results")
        val game = gameWithPlayersAdapter.toGameWithPlayer(results.first()).first
        val players = results.map { row -> gameWithPlayersAdapter.toGameWithPlayer(row).second }
        return GameWithPlayers(game, players)
    }



//    fun getGameWithPlayersAsFlow(gameId: Long): Flow<GameWithPlayers> {
//        println("GameRepository.getGameWithPlayers($gameId)")
//        return database.gameQueries.getGameWithPlayers(gameId)
//            .asFlow()
//            .mapToList(Dispatchers.IO)
//            .map { results ->
//                require(results.isNotEmpty()) { "No game with id $gameId found" }
//                println("GameRepository.getGameWithPlayers($gameId) results: $results")
//                val game = gameWithPlayersAdapter.toGameWithPlayer(results.first()).first
//                println("GameRepository.getGameWithPlayers($gameId) game: $game")
//                val players = results.map { row -> gameWithPlayersAdapter.toGameWithPlayer(row).second }
//                println("GameRepository.getGameWithPlayers($gameId) players: $players")
//                GameWithPlayers(game, players)
//            }
//    }

    fun insertGame(game: Game): Long {
        var gameId: Long
//        withContext(Dispatchers.IO) {
            database.gameQueries.insertGame(
                num_players = game.numPlayers.toLong()
            )
            gameId = database.gameQueries.lastInsertRowId().executeAsOne()
//        }
        return gameId
    }

    fun updateGameWithPlayers(gameWithPlayers: GameWithPlayers) {
        val game = gameWithPlayers.game
        val players = gameWithPlayers.players
//        withContext(Dispatchers.IO) {
            println("GameRepository.updateGameWithPlayers($game)")
            database.gameQueries.updateGame(
                num_players = game.numPlayers.toLong(),
                end_timestamp = game.endTimestamp,
                winner_pid = game.winnerPid,
                id = game.id
            )
            println("GameRepository.updateGameWithPlayers done")

            database.gameQueries.removeAllGamePlayers(game.id)
            println("GameRepository.updateGameWithPlayers removeAllGamePlayers done")
            players.forEach { player ->
                val playerId = insertPlayer(player, game.id)
                database.gameQueries.insertGamePlayer(
                    game_id = game.id,
                    player_id = playerId
                )
            }
            println("GameRepository.updateGameWithPlayers insertGamePlayer done")
        }
//    }
}
