package model

data class Game(
    val id: Long = -1,
    val numPlayers: Int,
    val altPlayerLayout: Boolean = false,
    val startTimestamp: Long = 0,
    val endTimestamp: Long? = null,
    val winnerPid: Long? = null,
    val monarchPid: Long? = null,
    val dayNightState: DayNightState = DayNightState.NONE,
    val playerSelectSeen: Boolean = false,
)

enum class DayNightState {
    NONE, DAY, NIGHT
}


data class GameWithPlayers(
    val game: Game,
    val players: List<Player>
)