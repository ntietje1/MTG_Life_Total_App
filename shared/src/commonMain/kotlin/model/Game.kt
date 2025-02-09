package model

data class Game(
    val id: Long = -1,
    val numPlayers: Int,
    val startTimestamp: Long = 0,
    val endTimestamp: Long? = null,
    val winnerPid: Long? = null
)

data class GameWithPlayers(
    val game: Game,
    val players: List<Player>
)