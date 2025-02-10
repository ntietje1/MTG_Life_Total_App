package data

import androidx.compose.ui.graphics.toArgb
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.hypeapps.lifelinked.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import model.Game
import model.GameWithPlayers
import model.Player

class GameRepository(
    private val database: Database,
    private val playerAdapter: PlayerAdapter,
    private val gameWithPlayersAdapter: GameWithPlayerAdapter
) {
    private val playerQueries = database.playerQueries
    private val gameQueries = database.gameQueries

//    fun getAllPlayersAsFlow(): Flow<List<Player>> {
//        return queries.getAllPlayers()
//            .asFlow()
//            .mapToList(Dispatchers.Default)
//            .map { players ->
//                players.map { player ->
//                    playerAdapter.toPlayer(
//                        id = player.id,
//                        name = player.name,
//                        imageString = player.image_string,
//                        color = player.color.toInt(),
//                        textColor = player.text_color.toInt(),
//                        playerNum = player.player_num.toInt(),
//                        lifeTotal = player.life_total.toInt(),
//                        lifeTotalRecentChange = player.life_total_recent_change.toInt(),
//                        monarch = player.monarch,
//                        setDead = player.set_dead,
//                        partnerMode = player.partner_mode,
//                        commanderDamages = player.commander_damages,
//                        counters = player.counters,
//                        activeCounters = null
//                    )
//                }
//            }
//    }

    fun updatePlayer(player: Player) {
        println("GameRepository.updatePlayer($player)")
//        withContext(Dispatchers.IO) {
        playerQueries.updatePlayer(
            game_id = player.gameId,
            player_num = player.playerNum.toLong(),
            name = player.name,
            image_string = player.imageString,
            color = player.color.toArgb().toLong(),
            text_color = player.textColor.toArgb().toLong(),
            life_total = player.lifeTotal.number.toLong(),
            set_dead = player.setDead,
            partner_mode = player.partnerMode
        )

        println("GameRepository.updatePlayer($player) updated player")

        player.commanderDamage.forEachIndexed { index, damage ->
            playerQueries.updateCommanderDamage(
                game_id = player.gameId,
                receiver_player_num = player.playerNum.toLong(),
                dealer_player_num = index.toLong(),
                damage = damage.number.toLong(),
            )
        }

        println("GameRepository.updatePlayer($player) updated commander damages")

        player.counters.forEachIndexed { index, value ->
            playerQueries.updateCounter(
                game_id = player.gameId,
                player_num = player.playerNum.toLong(),
                counter_type = index.toLong(),
                counter_value = value.toLong()
            )
        }

        println("GameRepository.updatePlayer($player) updated counters")
    }


    fun insertPlayer(player: Player) {
        println("GameRepository.insertPlayer($player)")
//        withContext(Dispatchers.IO) {

        playerQueries.insertPlayer(
            game_id = player.gameId,
            player_num = player.playerNum.toLong(),
            name = player.name,
            image_string = player.imageString,
            color = player.color.toArgb().toLong(),
            text_color = player.textColor.toArgb().toLong(),
            life_total = player.lifeTotal.number.toLong(),
            set_dead = player.setDead,
            partner_mode = player.partnerMode
        )
        println("GameRepository.insertPlayer($player) inserted player")

        // Insert commander damages
        player.commanderDamage.forEachIndexed { index, damage ->
            playerQueries.insertCommanderDamage(
                game_id = player.gameId,
                receiver_player_num = player.playerNum.toLong(),
                dealer_player_num = index.toLong(),
                damage = damage.number.toLong(),
            )
        }

        println("GameRepository.insertPlayer($player) inserted commander damages")

        // Insert counters
        player.counters.forEachIndexed { index, value ->
            playerQueries.insertCounter(
                game_id = player.gameId,
                player_num = player.playerNum.toLong(),
                counter_type = index.toLong(),
                counter_value = value.toLong()
            )
        }

        println("GameRepository.insertPlayer($player) done")
    }

    fun getGameWithPlayers(gameId: Long): GameWithPlayers {
        val results = database.gameQueries.getGameWithPlayers(gameId).executeAsList()
        require(results.isNotEmpty()) { "No game with id $gameId found" }

        val commanderDamages = database.gameQueries.getCommanderDamages(gameId)
            .executeAsList()
            .groupBy { it.receiver_player_num }

        val counters = database.gameQueries.getCounters(gameId)
            .executeAsList()
            .groupBy { it.player_num }

        return GameWithPlayers(
            game = gameWithPlayersAdapter.toGame(results.first()),
            players = results.map { row ->
                gameWithPlayersAdapter.toGameWithPlayer(row, commanderDamages[row.player_num]!!, counters[row.player_num]!!).second
            }
        )
    }

    fun getGameWithPlayersAsFlow(gameId: Long): Flow<GameWithPlayers> {
        return database.gameQueries.getGameWithPlayers(gameId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { results ->
                require(results.isNotEmpty()) { "No game with id $gameId found" }
                
                val commanderDamages = database.gameQueries.getCommanderDamages(gameId)
                    .executeAsList()
                    .groupBy { it.receiver_player_num }

                val counters = database.gameQueries.getCounters(gameId)
                    .executeAsList()
                    .groupBy { it.player_num }

                GameWithPlayers(
                    game = gameWithPlayersAdapter.toGame(results.first()),
                    players = results.map { row ->
                        gameWithPlayersAdapter.toGameWithPlayer(
                            row, 
                            commanderDamages[row.player_num] ?: emptyList(),
                            counters[row.player_num] ?: emptyList()
                        ).second
                    }
                )
            }
    }

    fun insertGame(game: Game): Long {
        var gameId: Long
//        withContext(Dispatchers.IO) {
        database.gameQueries.insertGame(
            num_players = game.numPlayers.toLong()
        )
        gameId = database.gameQueries.lastInsertRowId().executeAsOne()
        println("GameRepository.insertGame($game) gameId: $gameId")
//        }
        return gameId
    }

    fun updateGame(game: Game) {
        println("GameRepository.updateGame($game)")
        database.gameQueries.updateGame(
            id = game.id,
            num_players = game.numPlayers.toLong(),
            alt_player_layout = game.altPlayerLayout,
            end_timestamp = game.endTimestamp,
            winner_player_num = game.winnerPlayerNum,
            first_player_num = game.firstPlayerNum,
            turn_timer_enabled = game.turnTimerEnabled,
            monarchy_player_num = game.monarchyPlayerNum,
            day_night_state = game.dayNightState.name
        )
        println("GameRepository.updateGame($game) done")
    }
}
