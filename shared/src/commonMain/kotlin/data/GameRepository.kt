package data

import androidx.compose.ui.graphics.toArgb
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.hypeapps.lifelinked.db.Database
import domain.utils.toLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import model.DayNightState
import model.Game
import model.GameWithPlayers
import model.Player

class GameRepository(
    private val database: Database,
    private val playerAdapter: PlayerAdapter,
    private val gameWithPlayersAdapter: GameWithPlayerAdapter
) {

    fun getAllPlayersAsFlow(): Flow<List<Player>> {
        return database.playerQueries.getAllPlayers()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { players ->
                players.map { player ->
                    playerAdapter.toPlayer(
                        id = player.id,
                        gameId = player.game_id,
                        name = player.name,
                        imageString = player.image_string,
                        color = player.color.toInt(),
                        textColor = player.text_color.toInt(),
                        playerNum = player.player_num.toInt(),
                        lifeTotal = player.life_total.toInt(),
                        lifeTotalRecentChange = player.life_total_recent_change.toInt(),
//                        monarch = player.monarch,
                        setDead = player.set_dead,
                        partnerMode = player.partner_mode,
                        commanderDamages = player.commander_damages,
                        counters = player.counters,
                        activeCounters = null
                    )
                }
            }
    }

    fun updatePlayer(player: Player): Long {
        try {
            database.playerQueries.updatePlayer( // Note: gameID is not updated
                id = player.id,
                name = player.name,
                image_string = player.imageString,
                color = player.color.toArgb().toLong(),
                text_color = player.textColor.toArgb().toLong(),
                player_num = player.playerNum.toLong(),
                life_total = player.lifeTotal.number.toLong(),
                life_total_recent_change = player.lifeTotal.recentChange.toLong(),
//                monarch = player.monarch,
                set_dead = player.setDead,
                partner_mode = player.partnerMode
            )

            player.commanderDamage.forEachIndexed { index, damage ->
                database.playerQueries.updateCommanderDamage(
                    reciever_player_id = player.id,
                    dealer_player_id = index.toLong(),
                    damage = damage.number.toLong(),
                    recent_change = damage.recentChange.toLong()
                )
            }

            //TODO: counters as well

//            println("DEBUG - Player updated with ID: ${player.id}")
        } catch (e: Exception) {
            println("Failed during conversion. Exception: ${e::class.simpleName}")
            println("Error message: ${e.message}")
            println("Stack trace:")
            e.printStackTrace()
            throw e
        }
        return player.id
    }

    fun insertPlayer(player: Player): Long {
        var playerId: Long
        try {
//            println("DEBUG - Inserting player: $player")
            database.playerQueries.insertPlayer(
                game_id = player.gameId,
                name = player.name,
                image_string = player.imageString,
                color = player.color.toArgb().toLong(),
                text_color = player.textColor.toArgb().toLong(),
                player_num = player.playerNum.toLong(),
                life_total = player.lifeTotal.number.toLong(),
                life_total_recent_change = player.lifeTotal.recentChange.toLong(),
//                monarch = player.monarch,
                set_dead = player.setDead,
                partner_mode = player.partnerMode
            )

            playerId = database.playerQueries.lastInsertRowId().executeAsOne()
//            println("DEBUG - Player inserted with ID: $playerId")

            // Insert commander damages
//            println("DEBUG - Commander damages:")
            player.commanderDamage.forEachIndexed { index, damage ->
                database.playerQueries.insertCommanderDamage(
                    reciever_player_id = playerId,
                    dealer_player_id = index.toLong(),
                    damage = damage.number.toLong(),
                    recent_change = damage.recentChange.toLong()
                )
            }

//            // Insert counters
//            println("DEBUG - Counters:")
//            println("Counters type: ${player.counters::class.simpleName}")
//            println("Counters content: ${player.counters}")
//            println("Counters first element type: ${player.counters.firstOrNull()?.let { it::class.simpleName }}")
//            player.counters.forEachIndexed { index, value ->
//                println("Counter $index - type: ${value::class.simpleName}, value: $value")
//                try {
//                    queries.insertCounter(
//                        player_id = playerId,
//                        counter_type = index.toLong(),
//                        value_ = value.toLong()
//                    )
//                } catch (e: Exception) {
//                    println("Failed to convert counter value: $value")
//                    println("Error: ${e.message}")
//                    throw e
//                }
//            }
        } catch (e: Exception) {
            println("Failed during conversion. Exception: ${e::class.simpleName}")
            println("Error message: ${e.message}")
            println("Stack trace:")
            e.printStackTrace()
            throw e
        }
        return playerId
    }

    suspend fun deleteAllPlayers() {
        withContext(Dispatchers.IO) {
            database.playerQueries.deleteAllPlayers()
        }
    }

    fun getGameWithPlayers(gameId: Long): GameWithPlayers? {
        val results = database.gameQueries.getGameWithPlayers(gameId).executeAsList()
        if (results.isEmpty()) {
            return null
        }
//        println("GameRepository.getGameWithPlayers($gameId) results: $results")
        println("GOT GAME WITH MONARCH: ${results.first().monarch_pid}")
        val game = gameWithPlayersAdapter.toGameWithPlayer(results.first()).first
        val players = results.map { row -> gameWithPlayersAdapter.toGameWithPlayer(row).second }
        println("GOT GAME 2 WITH MONARCH: ${game.monarchPid}")
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
            num_players = game.numPlayers.toLong(),
        )
        gameId = database.gameQueries.lastInsertRowId().executeAsOne()
//        }
        return gameId
    }

    fun updateMonarch(monarchPid: Long?, gameId: Long) {
        println("GameRepository.updateMonarch($monarchPid, $gameId)")
        database.gameQueries.updateGameMonarch(
            monarch_pid = monarchPid,
            id = gameId
        )
    }

    fun updateDayNight(dayNightState: DayNightState, gameId: Long) {
        println("GameRepository.updateDayNight($dayNightState, $gameId)")
        database.gameQueries.updateGameDayNight(
            day_night = dayNightState.ordinal.toLong(),
            id = gameId
        )
    }

    fun updateNumPlayers(numPlayers: Int, gameId: Long) {
        println("GameRepository.updateNumPlayers($numPlayers, $gameId)")
        database.gameQueries.updateGameNumPlayers(numPlayers.toLong(), gameId)
    }

    fun updateAltPlayerLayout(altPlayerLayout: Boolean, gameId: Long) {
        println("GameRepository.updateAltPlayerLayout($altPlayerLayout, $gameId)")
        database.gameQueries.updateGameAltPlayerLayout(altPlayerLayout.toLong(), gameId)
    }

    fun updatePlayerSelectSeen(playerSelectSeen: Boolean, gameId: Long) {
        println("GameRepository.updatePlayerSelectSeen($playerSelectSeen, $gameId)")
        database.gameQueries.updatePlayerSelectSeen(playerSelectSeen.toLong(), gameId)
    }

    fun insertTimer(gameId: Long, firstPlayerId: Long) {
        println("GameRepository.insertTimer($gameId, $firstPlayerId)")
        database.timerQueries.insertTurnTimer(gameId, firstPlayerId)
    }

    fun insertTurn(gameId: Long, playerId: Long, turnNumber: Int, startTimeStamp: Long, endTimeStamp: Long) {
        println("GameRepository.insertTurn($gameId, $playerId, $turnNumber, $startTimeStamp, $endTimeStamp)")
        database.timerQueries.insertTurn(
            game_id = gameId,
            player_id = playerId,
            turn_number = turnNumber.toLong(),
            start_timestamp = startTimeStamp,
            end_timestamp = endTimeStamp
        )
    }
}
