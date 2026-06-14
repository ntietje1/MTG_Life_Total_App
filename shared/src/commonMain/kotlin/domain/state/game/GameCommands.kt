package domain.state.game

data class GameReduction(
    val session: GameSession
)

sealed interface GameCommand {
    data class ChangeLife(val seatId: SeatId, val delta: Int) : GameCommand

    data class ClearLifeRecentChange(val seatId: SeatId) : GameCommand

    data class SetManualDeath(val seatId: SeatId, val dead: Boolean) : GameCommand

    data class SetSeatAppearance(val seatId: SeatId, val appearance: SeatAppearance) : GameCommand

    data class SetMonarch(val seatId: SeatId?) : GameCommand

    data class SetCommanderDealer(val seatId: SeatId?) : GameCommand

    data class SetCommanderPartnerMode(val partnerMode: Boolean) : GameCommand

    data class ChangeSeatCounter(
        val seatId: SeatId,
        val counter: CounterType,
        val delta: Int
    ) : GameCommand

    data class SetSeatCounterActive(
        val seatId: SeatId,
        val counter: CounterType,
        val active: Boolean
    ) : GameCommand

    data class ChangeTableCounter(
        val counter: TableCounterType,
        val delta: Int
    ) : GameCommand

    data object ResetTableCounters : GameCommand

    data class ChangeCommanderDamage(
        val dealerSeatId: SeatId,
        val receiverSeatId: SeatId,
        val partner: Boolean,
        val delta: Int
    ) : GameCommand

    data class ClearCommanderDamageRecentChange(
        val dealerSeatId: SeatId,
        val receiverSeatId: SeatId,
        val partner: Boolean
    ) : GameCommand

    data object ToggleDayNight : GameCommand

    data object ResetGame : GameCommand
}

sealed interface GameEffect {
    data class PersistenceFailed(
        val mutation: GameMutation,
        val reason: String
    ) : GameEffect
}
