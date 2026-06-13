package ui.lifecounter.playerbutton

import domain.state.game.GameCommand
import domain.state.game.SeatId
import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerButtonActionTest {
    private val seatId = SeatId("seat-2")

    @Test
    fun mapsLifeActionsToGameCommands() {
        assertEquals(
            listOf(GameCommand.ChangeLife(seatId, 1)),
            PlayerButtonAction.IncrementLife.toGameCommands(seatId)
        )
        assertEquals(
            listOf(GameCommand.ChangeLife(seatId, -1)),
            PlayerButtonAction.DecrementLife.toGameCommands(seatId)
        )
    }

    @Test
    fun mapsCommanderDamageActionsToDamageAndLifeCommandsWhenCommanderDealerIsActive() {
        val commanderState = CommanderState.Active(dealer = model.Player(playerNum = 1))

        assertEquals(
            listOf(
                GameCommand.ChangeCommanderDamage(
                    dealerSeatId = SeatId("seat-1"),
                    receiverSeatId = seatId,
                    partner = true,
                    delta = 1
                ),
                GameCommand.ChangeLife(seatId, -1)
            ),
            PlayerButtonAction.IncrementCommanderDamage(partner = true).toGameCommands(seatId, commanderState)
        )
    }

    @Test
    fun commanderDamageActionHasNoCommandsWithoutActiveCommanderDealer() {
        assertEquals(
            emptyList(),
            PlayerButtonAction.IncrementCommanderDamage(partner = false).toGameCommands(seatId)
        )
    }

    @Test
    fun mapsMonarchAndDeathActionsToGameCommands() {
        assertEquals(
            listOf(GameCommand.SetMonarch(seatId)),
            PlayerButtonAction.SetMonarch(true).toGameCommands(seatId)
        )
        assertEquals(
            listOf(GameCommand.SetMonarch(null)),
            PlayerButtonAction.SetMonarch(false).toGameCommands(seatId)
        )
        assertEquals(
            listOf(GameCommand.SetManualDeath(seatId, true)),
            PlayerButtonAction.SetManualDeath(true).toGameCommands(seatId)
        )
    }
}
