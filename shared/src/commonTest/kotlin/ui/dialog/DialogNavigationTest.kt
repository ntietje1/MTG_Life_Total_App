package ui.dialog

import kotlin.test.Test
import kotlin.test.assertEquals
import ui.dialog.reset.ResetGameChoice
import ui.dialog.reset.choiceFor

class DialogNavigationTest {
    @Test
    fun resetDifferentPlayersAndSelectFirstPlayerHasExplicitChoice() {
        val choice = choiceFor(resetPlayerPreferences = true, chooseFirstPlayer = true)

        assertEquals(ResetGameChoice.DifferentPlayersSelectFirst, choice)
    }

    @Test
    fun resetSamePlayersAndSkipFirstPlayerHasExplicitChoice() {
        val choice = choiceFor(resetPlayerPreferences = false, chooseFirstPlayer = false)

        assertEquals(ResetGameChoice.SamePlayersSkipFirst, choice)
    }
}
