package data

import com.hypeapps.lifelinked.db.GetCommanderDamages
import com.hypeapps.lifelinked.db.GetCounters
import com.hypeapps.lifelinked.db.GetGameWithPlayers
import model.DayNightState
import model.Game
import model.Player

class GameWithPlayerAdapter(
    private val playerAdapter: PlayerAdapter,
) {
    fun toGame(gameWithPlayers: GetGameWithPlayers): Game {
        println("GameWithPlayerAdapter.toGame")
        return Game(
            id = gameWithPlayers.id,
            numPlayers = gameWithPlayers.num_players.toInt(),
            startTimestamp = gameWithPlayers.start_timestamp,
            endTimestamp = gameWithPlayers.end_timestamp,
            winnerPlayerNum = gameWithPlayers.winner_player_num
        )
    }

    fun toGameWithPlayer(
        getGameWithPlayers: GetGameWithPlayers,
        commanderDamages: List<GetCommanderDamages>,
        counters: List<GetCounters>,
    ): Pair<Game, Player> {
        println("GameWithPlayerAdapter.toGameWithPlayer")
        val game = Game(
            id = getGameWithPlayers.id,
            numPlayers = getGameWithPlayers.num_players.toInt(),
            startTimestamp = getGameWithPlayers.start_timestamp,
            endTimestamp = getGameWithPlayers.end_timestamp,
            winnerPlayerNum = getGameWithPlayers.winner_player_num,
            monarchyPlayerNum = getGameWithPlayers.monarchy_player_num,
            firstPlayerNum = getGameWithPlayers.first_player_num,
            turnTimerEnabled = getGameWithPlayers.turn_timer_enabled,
            dayNightState = getGameWithPlayers.day_night_state?.let { DayNightState.valueOf(it) } ?: DayNightState.NONE,
            altPlayerLayout = getGameWithPlayers.alt_player_layout
        )
        println("GameWithPlayerAdapter.toGameWithPlayer game: $game")

        val playerNum = getGameWithPlayers.player_num
        val player = playerAdapter.toPlayer(
            gameId = game.id,
            name = getGameWithPlayers.name,
            imageString = getGameWithPlayers.image_string,
            color = getGameWithPlayers.color.toInt(),
            textColor = getGameWithPlayers.text_color.toInt(),
            playerNum = playerNum.toInt(),
            lifeTotal = getGameWithPlayers.life_total.toInt(),
            monarch = game.monarchyPlayerNum == playerNum,
            setDead = getGameWithPlayers.set_dead,
            partnerMode = getGameWithPlayers.partner_mode,
            commanderDamages = commanderDamages,
            counters = counters
        )
        println("GameWithPlayerAdapter.toGameWithPlayer player: $player")

        return game to player
    }
} 