package ui.lifecounter

import domain.game.timer.TurnTimer
import domain.state.game.SeatId
import model.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ui.lifecounter.playerbutton.PBState

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

    @Test
    fun opensAndClosesPlayerSettingsWithBackStackState() {
        val state = stateWithSeats(1)
        val seatId = SeatId("seat-1")

        val opened = state.openPlayerSettings(seatId)
        val closed = opened.openPlayerSettings(seatId)

        assertEquals(PBState.SETTINGS, opened.players.single().buttonState)
        assertEquals(listOf(PBState.NORMAL), opened.players.single().buttonBackStack)
        assertTrue(opened.players.single().backButtonVisible)
        assertEquals(PBState.NORMAL, closed.players.single().buttonState)
        assertEquals(emptyList(), closed.players.single().buttonBackStack)
        assertFalse(closed.players.single().backButtonVisible)
    }

    @Test
    fun pushesAndPopsNestedPlayerButtonStates() {
        val state = stateWithSeats(1)
        val seatId = SeatId("seat-1")

        val counters = state
            .openPlayerSettings(seatId)
            .openPlayerCounters(seatId)
            .openPlayerCounterSelection(seatId)
        val poppedToCounters = counters.popPlayerButtonBackStack(seatId)
        val poppedToSettings = poppedToCounters.popPlayerButtonBackStack(seatId)

        assertEquals(PBState.COUNTERS_SELECT, counters.players.single().buttonState)
        assertEquals(listOf(PBState.NORMAL, PBState.SETTINGS, PBState.COUNTERS_VIEW), counters.players.single().buttonBackStack)
        assertEquals(PBState.COUNTERS_VIEW, poppedToCounters.players.single().buttonState)
        assertEquals(PBState.SETTINGS, poppedToSettings.players.single().buttonState)
    }

    @Test
    fun firstPlayerPromptCanBeClearedThroughBackStack() {
        val state = stateWithSeats(2)

        val prompted = state.promptForFirstPlayer()
        val cleared = prompted.clearFirstPlayerPrompt()

        assertEquals(listOf(PBState.SELECT_FIRST_PLAYER, PBState.SELECT_FIRST_PLAYER), prompted.players.map { it.buttonState })
        assertEquals(listOf(PBState.NORMAL, PBState.NORMAL), cleared.players.map { it.buttonState })
        assertTrue(cleared.players.all { it.buttonBackStack.isEmpty() })
    }

    private fun stateWithSeats(count: Int): LifeCounterState {
        return LifeCounterState(
            players = (1..count).map { playerNumber ->
                PlayerSeatUiState(
                    seatId = SeatId("seat-$playerNumber"),
                    player = Player(playerNum = playerNumber)
                )
            }
        )
    }
}
