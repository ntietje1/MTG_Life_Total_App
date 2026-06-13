package ui.lifecounter

import domain.game.timer.TurnTimer
import domain.state.game.SeatId
import model.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LifeCounterStateTest {
    @Test
    fun showsTimerOnlyOnActivePlayerSeat() {
        val timer = TurnTimer(seconds = 12, turn = 3)
        val state = LifeCounterState(
            players = listOf(
                PlayerSeatUiState(seatId = SeatId("seat-1"), player = Player(playerNum = 1), timer = TurnTimer(1, 1)),
                PlayerSeatUiState(seatId = SeatId("seat-2"), player = Player(playerNum = 2)),
                PlayerSeatUiState(seatId = SeatId("seat-3"), player = Player(playerNum = 3), timer = TurnTimer(2, 1))
            )
        )

        val updated = state.showTimer(activePlayerIndex = 1, timer = timer)

        assertNull(updated.players[0].timer)
        assertEquals(timer, updated.players[1].timer)
        assertNull(updated.players[2].timer)
    }

    @Test
    fun clearsAllSeatTimersWhenNoActivePlayer() {
        val state = LifeCounterState(
            players = listOf(
                PlayerSeatUiState(seatId = SeatId("seat-1"), player = Player(playerNum = 1), timer = TurnTimer(1, 1)),
                PlayerSeatUiState(seatId = SeatId("seat-2"), player = Player(playerNum = 2), timer = TurnTimer(2, 1))
            )
        )

        val updated = state.showTimer(activePlayerIndex = null, timer = null)

        assertNull(updated.players[0].timer)
        assertNull(updated.players[1].timer)
    }
}
