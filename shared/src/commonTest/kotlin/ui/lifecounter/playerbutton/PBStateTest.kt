package ui.lifecounter.playerbutton

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PBStateTest {
    @Test
    fun showsBackButtonOnlyWhenBackstackHasEntries() {
        assertFalse(PBState.SETTINGS.showsBackButton(backStackIsEmpty = true))
        assertTrue(PBState.SETTINGS.showsBackButton(backStackIsEmpty = false))
    }

    @Test
    fun hidesBackButtonForTransientSelectionStates() {
        assertFalse(PBState.SELECT_FIRST_PLAYER.showsBackButton(backStackIsEmpty = false))
        assertFalse(PBState.COMMANDER_RECEIVER.showsBackButton(backStackIsEmpty = false))
        assertFalse(PBState.COMMANDER_DEALER.showsBackButton(backStackIsEmpty = false))
    }
}
