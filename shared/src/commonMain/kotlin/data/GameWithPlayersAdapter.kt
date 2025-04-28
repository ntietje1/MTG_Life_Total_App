package data

import com.hypeapps.lifelinked.db.GetGameWithPlayers
import domain.utils.toBoolean
import model.DayNightState
import model.Game
import model.Player

class GameWithPlayerAdapter(
    private val playerAdapter: PlayerAdapter
) {
    fun toGameWithPlayer(
        getGameWithPlayers: GetGameWithPlayers
    ): Pair<Game, Player> {
        println("GameWithPlayerAdapter.toGameWithPlayer")
        val game = Game(
            id = getGameWithPlayers.id,
            numPlayers = getGameWithPlayers.num_players.toInt(),
            altPlayerLayout = getGameWithPlayers.altPlayerLayout.toBoolean(),
            startTimestamp = getGameWithPlayers.start_timestamp,
            endTimestamp = getGameWithPlayers.end_timestamp,
            winnerPid = getGameWithPlayers.winner_pid,
            monarchPid = getGameWithPlayers.monarch_pid,
            dayNightState = DayNightState.entries[getGameWithPlayers.day_night.toInt()],
            playerSelectSeen = getGameWithPlayers.player_select_seen.toBoolean()
        )
        println("GameWithPlayerAdapter.toGameWithPlayer game: $game")

        val player = playerAdapter.toPlayer(
            id = getGameWithPlayers.player_id,
            gameId = game.id,
            name = getGameWithPlayers.name,
            imageString = getGameWithPlayers.image_string,
            color = getGameWithPlayers.color.toInt(),
            textColor = getGameWithPlayers.text_color.toInt(),
            playerNum = getGameWithPlayers.player_num.toInt(),
            lifeTotal = getGameWithPlayers.life_total.toInt(),
            lifeTotalRecentChange = getGameWithPlayers.life_total_recent_change.toInt(),
//            monarch = getGameWithPlayers.monarch,
            setDead = getGameWithPlayers.set_dead,
            partnerMode = getGameWithPlayers.partner_mode,
            commanderDamages = getGameWithPlayers.commander_damages,
            counters = getGameWithPlayers.counters,
            activeCounters = null // Since this is being refactored out
        )
        println("GameWithPlayerAdapter.toGameWithPlayer player: $player")

        return game to player
    }
} 