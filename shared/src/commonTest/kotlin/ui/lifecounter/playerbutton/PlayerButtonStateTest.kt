package ui.lifecounter.playerbutton

import model.Player
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerButtonStateTest {
    @Test
    fun showsBackButtonOnlyWhenBackstackHasEntries() {
        val state = PlayerButtonState(player = Player(playerNum = 1), buttonState = PBState.SETTINGS)

        assertFalse(state.showsBackButton(backStackIsEmpty = true))
        assertTrue(state.showsBackButton(backStackIsEmpty = false))
    }

    @Test
    fun hidesBackButtonForTransientSelectionStates() {
        val player = Player(playerNum = 1)

        assertFalse(PlayerButtonState(player, PBState.SELECT_FIRST_PLAYER).showsBackButton(backStackIsEmpty = false))
        assertFalse(PlayerButtonState(player, PBState.COMMANDER_RECEIVER).showsBackButton(backStackIsEmpty = false))
        assertFalse(PlayerButtonState(player, PBState.COMMANDER_DEALER).showsBackButton(backStackIsEmpty = false))
    }
}
