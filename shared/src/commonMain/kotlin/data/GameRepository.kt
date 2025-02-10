package data

import androidx.compose.ui.graphics.toArgb
import com.hypeapps.lifelinked.db.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
        queries.updatePlayer(
            game_id = player.gameId,
            player_num = player.playerNum.toLong(),
            name = player.name,
            image_string = player.imageString,
            color = player.color.toArgb().toLong(),
            text_color = player.textColor.toArgb().toLong(),
            life_total = player.lifeTotal.number.toLong(),
            monarch = player.monarch,
            set_dead = player.setDead,
            partner_mode = player.partnerMode
        )

        println("GameRepository.updatePlayer($player) updated player")

        player.commanderDamage.forEachIndexed { index, damage ->
            queries.updateCommanderDamage(
                game_id = player.gameId,
                receiver_player_num = player.playerNum.toLong(),
                dealer_player_num = index.toLong(),
                damage = damage.number.toLong(),
            )
        }

        println("GameRepository.updatePlayer($player) updated commander damages")

        player.counters.forEachIndexed { index, value ->
            queries.updateCounter(
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

        queries.insertPlayer(
            game_id = player.gameId,
            player_num = player.playerNum.toLong(),
            name = player.name,
            image_string = player.imageString,
            color = player.color.toArgb().toLong(),
            text_color = player.textColor.toArgb().toLong(),
            life_total = player.lifeTotal.number.toLong(),
            monarch = player.monarch,
            set_dead = player.setDead,
            partner_mode = player.partnerMode
        )
        println("GameRepository.insertPlayer($player) inserted player")

        // Insert commander damages
        player.commanderDamage.forEachIndexed { index, damage ->
            queries.insertCommanderDamage(
                game_id = player.gameId,
                receiver_player_num = player.playerNum.toLong(),
                dealer_player_num = index.toLong(),
                damage = damage.number.toLong(),
            )
        }

        println("GameRepository.insertPlayer($player) inserted commander damages")

        // Insert counters
        player.counters.forEachIndexed { index, value ->
            queries.insertCounter(
                game_id = player.gameId,
                player_num = player.playerNum.toLong(),
                counter_type = index.toLong(),
                counter_value = value.toLong()
            )
        }

        println("GameRepository.insertPlayer($player) done")
    }

    suspend fun deleteAllPlayers() {
        withContext(Dispatchers.IO) {
            queries.deleteAllPlayers()
        }
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
        println("GameRepository.insertGame($game) gameId: $gameId")
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
            winner_player_num = game.winnerPlayerNum,
            id = game.id
        )
        println("GameRepository.updateGameWithPlayers updated game")
        players.forEach {
            updatePlayer(it)
        }
        println("GameRepository.updateGameWithPlayers done")
    }
//    }
}
