package ui.tutorial.pages

import domain.state.game.SeatId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import theme.PlayerColor9
import ui.lifecounter.LifeCounterModal
import ui.lifecounter.MiddleButtonState
import ui.lifecounter.playerbutton.CommanderState
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
        val receiverCommanderState = assertIs<CommanderState.Active>(receiver.commanderState)
        assertEquals(PBState.COMMANDER_DEALER, dealer.buttonState)
        assertEquals(MiddleButtonState.COMMANDER_EXIT, controller.state.value.middleButtonState)
        assertEquals(38, receiver.player.life)
        assertEquals(1, receiver.player.commanderDamage[0].number)
        assertEquals(1, receiver.player.commanderDamage[receiverCommanderState.getDealerIndex(partner = false)].number)
    }

    @Test
    fun tutorialCommanderDamageModePublishesActiveDealerForVisibleDamageControls() {
        val controller = TutorialLifeCounterController(gameState)
        val dealerSeatId = SeatId("seat-1")
        val receiverSeatId = SeatId("seat-2")

        controller.onPlayerButtonAction(dealerSeatId, PlayerButtonAction.ToggleCommanderDealer)

        val dealer = controller.state.value.players.first { it.seatId == dealerSeatId }
        val receiver = controller.state.value.players.first { it.seatId == receiverSeatId }
        val receiverCommanderState = receiver.commanderState

        assertIs<CommanderState.Active>(dealer.commanderState)
        assertIs<CommanderState.Active>(receiverCommanderState)
        assertEquals(dealer.player.playerNum, receiverCommanderState.dealer.playerNum)
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
    fun tutorialControllerReportsMiddleModalOpenAndClosedState() {
        val reportedModalVisibility = mutableListOf<Boolean>()
        val controller = TutorialLifeCounterController(
            gameState = gameState,
            afterModalChanged = { state -> reportedModalVisibility += state.isModalOpen }
        )

        controller.openModal(LifeCounterModal.Default)
        controller.closeModal()

        assertEquals(listOf(true, false), reportedModalVisibility)
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

    @Test
    fun tutorialControllerReportsCustomizationCloseWithClosedState() {
        val seatId = SeatId("seat-1")
        val reportedCustomizationVisibility = mutableListOf<Boolean>()
        val controller = TutorialLifeCounterController(
            gameState = gameState,
            afterPlayerAction = { _, action, state ->
                if (action == PlayerButtonAction.OpenCustomization || action == PlayerButtonAction.CloseCustomization) {
                    reportedCustomizationVisibility += state.players.any { it.showCustomizeMenu }
                }
            }
        )

        controller.onPlayerButtonAction(seatId, PlayerButtonAction.OpenCustomization)
        controller.onPlayerButtonAction(seatId, PlayerButtonAction.CloseCustomization)

        assertEquals(listOf(true, false), reportedCustomizationVisibility)
    }

    @Test
    fun tutorialControllerAppliesCustomizationChangesToPlayerButtonOnClose() {
        val seatId = SeatId("seat-1")
        val controller = TutorialLifeCounterController(gameState)

        controller.onPlayerButtonAction(seatId, PlayerButtonAction.OpenCustomization)
        val customizationViewModel = requireNotNull(controller.customizationViewModelFor(seatId))
        customizationViewModel.setPlayer(
            customizationViewModel.state.value.player.copy(
                name = "Tutorial Player",
                color = PlayerColor9,
            )
        )

        controller.onPlayerButtonAction(seatId, PlayerButtonAction.CloseCustomization)

        val player = controller.state.value.players.first { it.seatId == seatId }
        assertEquals("Tutorial Player", player.player.name)
        assertEquals(PlayerColor9, player.player.color)
        assertFalse(player.showCustomizeMenu)
    }
}
