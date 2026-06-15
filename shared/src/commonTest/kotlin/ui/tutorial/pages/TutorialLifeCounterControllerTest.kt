package ui.tutorial.pages

import domain.state.game.SeatId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import ui.lifecounter.LifeCounterModal
import ui.lifecounter.MiddleButtonState
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonAction

class TutorialLifeCounterControllerTest {
    private val gameState = MockGameState()

    @Test
    fun tutorialControllerImplementsScreenContractWithoutProductionViewModelInheritance() {
        val controller = TutorialLifeCounterController(gameState)
        val controllerAsAny: Any = controller

        assertIs<ui.lifecounter.LifeCounterScreenController>(controller)
        assertFalse(controllerAsAny is ui.lifecounter.LifeCounterViewModel)
    }

    @Test
    fun tutorialControllerUpdatesVisibleLifeAndCommanderDamageState() {
        val controller = TutorialLifeCounterController(gameState)
        val dealerSeatId = SeatId("seat-1")
        val receiverSeatId = SeatId("seat-2")

        controller.onPlayerButtonAction(receiverSeatId, PlayerButtonAction.DecrementLife)
        controller.onPlayerButtonAction(dealerSeatId, PlayerButtonAction.ToggleCommanderDealer)
        controller.onPlayerButtonAction(receiverSeatId, PlayerButtonAction.IncrementCommanderDamage(partner = false))

        val dealer = controller.state.value.players.first { it.seatId == dealerSeatId }
        val receiver = controller.state.value.players.first { it.seatId == receiverSeatId }
        assertEquals(PBState.COMMANDER_DEALER, dealer.buttonState)
        assertEquals(MiddleButtonState.COMMANDER_EXIT, controller.state.value.middleButtonState)
        assertEquals(38, receiver.player.life)
        assertEquals(1, receiver.player.commanderDamage[0].number)
    }

    @Test
    fun tutorialControllerUsesModalStackForMiddleMenuNavigation() {
        val controller = TutorialLifeCounterController(gameState)

        controller.openModal(LifeCounterModal.Default)
        controller.openModal(LifeCounterModal.PlayerNumber)
        controller.goBackInModal()

        assertEquals(LifeCounterModal.Default, controller.state.value.currentModal)
        assertTrue(controller.state.value.isModalOpen)
    }

    @Test
    fun tutorialControllerCanBlockActionsWithoutSubclassing() {
        val controller = TutorialLifeCounterController(
            gameState = gameState,
            blockedPlayerActionMessage = { action ->
                if (action == PlayerButtonAction.ToggleSettings) "Settings disabled" else null
            }
        )
        val seatId = SeatId("seat-1")

        controller.onPlayerButtonAction(seatId, PlayerButtonAction.ToggleSettings)

        val player = controller.state.value.players.first { it.seatId == seatId }
        assertEquals(PBState.NORMAL, player.buttonState)
    }
}
