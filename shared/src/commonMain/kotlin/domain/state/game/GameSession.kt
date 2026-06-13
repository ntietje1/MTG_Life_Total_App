package domain.state.game

import domain.state.profile.PlayerBackground
import domain.state.profile.PlayerColors
import kotlinx.serialization.Serializable

@Serializable
data class GameSession(
    val id: GameSessionId,
    val rules: GameRules = GameRules(),
    val seats: List<GameSeat>,
    val commander: CommanderDamageMatrix = CommanderDamageMatrix(),
    val tableCounters: Map<TableCounterType, Int> = emptyMap(),
    val monarchSeatId: SeatId? = null,
    val dayNight: DayNight = DayNight.NONE,
    val version: Long = 0
) {
    init {
        require(seats.size in 1..MaxSeats) { "GameSession supports 1 to 6 seats" }
    }

    fun requireSeat(seatId: SeatId): GameSeat {
        return seats.firstOrNull { it.id == seatId }
            ?: throw IllegalArgumentException("Seat not found: ${seatId.value}")
    }

    fun updateSeat(seatId: SeatId, update: (GameSeat) -> GameSeat): GameSession {
        requireSeat(seatId)
        return copy(seats = seats.map { seat -> if (seat.id == seatId) update(seat) else seat })
    }

    fun tableCounterValue(counter: TableCounterType): Int {
        return tableCounters[counter] ?: 0
    }

    companion object {
        const val MaxSeats = 6

        fun newGame(
            id: GameSessionId,
            rules: GameRules = GameRules(),
            appearances: List<SeatAppearance>
        ): GameSession {
            require(appearances.size in 1..MaxSeats) { "GameSession supports 1 to 6 seats" }
            return GameSession(
                id = id,
                rules = rules,
                seats = appearances.mapIndexed { index, appearance ->
                    GameSeat.new(
                        id = SeatId("seat-${index + 1}"),
                        appearance = appearance,
                        startingLife = rules.startingLife
                    )
                }
            )
        }
    }
}

@Serializable
data class GameRules(
    val startingLife: Int = 40
)

@Serializable
data class GameSeat(
    val id: SeatId,
    val appearance: SeatAppearance,
    val life: TrackedInt,
    val manualDeath: Boolean = false,
    val counters: Map<CounterType, Int> = emptyMap(),
    val activeCounters: Set<CounterType> = emptySet()
) {
    fun counterValue(counter: CounterType): Int {
        return counters[counter] ?: 0
    }

    fun isDead(autoKo: Boolean, commander: CommanderDamageMatrix): Boolean {
        return manualDeath || (autoKo && (life.value <= 0 || commander.hasLethalDamage(id)))
    }

    companion object {
        fun new(id: SeatId, appearance: SeatAppearance, startingLife: Int): GameSeat {
            return GameSeat(
                id = id,
                appearance = appearance,
                life = TrackedInt(value = startingLife)
            )
        }
    }
}

@Serializable
data class SeatAppearance(
    val displayName: String,
    val colors: PlayerColors = PlayerColors(),
    val background: PlayerBackground = PlayerBackground.None,
    val sourceProfileId: PlayerProfileId? = null
)

@Serializable
data class TrackedInt(
    val value: Int,
    val recentChange: Int = 0
) {
    fun change(delta: Int): TrackedInt {
        return copy(value = value + delta, recentChange = delta)
    }

    companion object {
        val Zero = TrackedInt(0)
    }
}

@Serializable
enum class DayNight {
    NONE,
    DAY,
    NIGHT
}
