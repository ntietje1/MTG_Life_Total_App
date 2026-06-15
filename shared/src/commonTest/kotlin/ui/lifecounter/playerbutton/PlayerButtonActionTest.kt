package ui.lifecounter.playerbutton

import domain.state.game.GameCommand
import domain.state.game.SeatId
import domain.state.game.CounterType as DomainCounterType
import kotlin.test.Test
import kotlin.test.assertEquals
import ui.lifecounter.CounterType as UiCounterType

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

    @Test
    fun mapsCounterAndPartnerModeActionsToGameCommands() {
        assertEquals(
            listOf(GameCommand.SetCommanderPartnerMode(partnerMode = true)),
            PlayerButtonAction.ToggleCommanderPartnerMode.toGameCommands(seatId)
        )
        assertEquals(
            listOf(GameCommand.SetCommanderPartnerMode(partnerMode = false)),
            PlayerButtonAction.ToggleCommanderPartnerMode.toGameCommands(
                seatId,
                CommanderState.Active(dealer = model.Player(playerNum = 1, partnerMode = true))
            )
        )
        assertEquals(
            listOf(
                GameCommand.ChangeSeatCounter(
                    seatId = seatId,
                    counter = DomainCounterType.POISON,
                    delta = 1
                )
            ),
            PlayerButtonAction.ChangeCounter(UiCounterType.Poison, 1).toGameCommands(seatId)
        )
        assertEquals(
            listOf(
                GameCommand.SetSeatCounterActive(
                    seatId = seatId,
                    counter = DomainCounterType.ENERGY,
                    active = true
                )
            ),
            PlayerButtonAction.SetCounterActive(UiCounterType.Energy, true).toGameCommands(seatId)
        )
    }

    @Test
    fun uiOnlyActionsDoNotProduceGameCommands() {
        assertEquals(emptyList(), PlayerButtonAction.ToggleSettings.toGameCommands(seatId))
        assertEquals(emptyList(), PlayerButtonAction.OpenCustomization.toGameCommands(seatId))
        assertEquals(emptyList(), PlayerButtonAction.SelectFirstPlayer.toGameCommands(seatId))
    }
}
