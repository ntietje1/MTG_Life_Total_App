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

class MockLifeCounterViewModelTest {
    private val gameState = MockGameState()

    @Test
    fun tutorialControllerImplementsScreenContractWithoutProductionViewModelInheritance() {
        val controller = TestMockLifeCounterViewModel(gameState)
        val controllerAsAny: Any = controller

        assertIs<ui.lifecounter.LifeCounterScreenController>(controller)
        assertFalse(controllerAsAny is ui.lifecounter.LifeCounterViewModel)
    }

    @Test
    fun tutorialControllerUpdatesVisibleLifeAndCommanderDamageState() {
        val controller = TestMockLifeCounterViewModel(gameState)
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
        val controller = TestMockLifeCounterViewModel(gameState)

        controller.openModal(LifeCounterModal.Default)
        controller.openModal(LifeCounterModal.PlayerNumber)
        controller.goBackInModal()

        assertEquals(LifeCounterModal.Default, controller.state.value.currentModal)
        assertTrue(controller.state.value.isModalOpen)
    }
}

private class TestMockLifeCounterViewModel(
    gameState: MockGameState
) : MockLifeCounterViewModel(
    lifeCounterState = gameState.lifeCounterState,
    preferencesRepository = gameState.mockPreferencesRepository,
    profileRepository = gameState.mockProfileRepository,
    fileImageStore = gameState.mockFileImageStore,
    notificationManager = null,
)
