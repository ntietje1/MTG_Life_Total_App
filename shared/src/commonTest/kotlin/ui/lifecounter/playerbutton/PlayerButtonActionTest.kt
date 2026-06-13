package ui.lifecounter.playerbutton

import domain.state.game.GameCommand
import domain.state.game.SeatId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PlayerButtonActionTest {
    private val seatId = SeatId("seat-2")

    @Test
    fun mapsLifeActionsToGameCommands() {
        assertEquals(GameCommand.ChangeLife(seatId, 1), PlayerButtonAction.IncrementLife.toGameCommand(seatId))
        assertEquals(GameCommand.ChangeLife(seatId, -1), PlayerButtonAction.DecrementLife.toGameCommand(seatId))
    }

    @Test
    fun mapsCommanderDamageActionsToGameCommandsWhenCommanderDealerIsActive() {
        val commanderState = CommanderState.Active(dealer = model.Player(playerNum = 1))

        assertEquals(
            GameCommand.ChangeCommanderDamage(
                dealerSeatId = SeatId("seat-1"),
                receiverSeatId = seatId,
                partner = true,
                delta = 1
            ),
            PlayerButtonAction.IncrementCommanderDamage(partner = true).toGameCommand(seatId, commanderState)
        )
    }

    @Test
    fun commanderDamageActionHasNoCommandWithoutActiveCommanderDealer() {
        assertNull(PlayerButtonAction.IncrementCommanderDamage(partner = false).toGameCommand(seatId))
    }

    @Test
    fun mapsMonarchAndDeathActionsToGameCommands() {
        assertEquals(GameCommand.SetMonarch(seatId), PlayerButtonAction.SetMonarch(true).toGameCommand(seatId))
        assertEquals(GameCommand.SetMonarch(null), PlayerButtonAction.SetMonarch(false).toGameCommand(seatId))
        assertEquals(GameCommand.SetManualDeath(seatId, true), PlayerButtonAction.SetManualDeath(true).toGameCommand(seatId))
    }
}
