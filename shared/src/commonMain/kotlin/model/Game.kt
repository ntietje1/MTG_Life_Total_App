package model

data class Game(
    val id: Long = -1,
    val numPlayers: Int,
    val altPlayerLayout: Boolean = false,
    val startTimestamp: Long = 0,
    val endTimestamp: Long? = null,
    val firstPlayerNum: Long? = null,
    val turnTimerEnabled: Boolean = false,
    val monarchyPlayerNum: Long? = null,
    val winnerPlayerNum: Long? = null,
    val dayNightState: DayNightState = DayNightState.NONE,
)

enum class DayNightState {
    NONE, DAY, NIGHT
}

data class GameWithPlayers(
    val game: Game,
    val players: List<Player>
)