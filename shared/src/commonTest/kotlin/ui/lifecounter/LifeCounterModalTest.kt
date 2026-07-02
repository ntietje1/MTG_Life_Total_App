package ui.lifecounter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LifeCounterModalTest {
    @Test
    fun openAddsModalToStack() {
        val stack = LifeCounterModalStack()
            .open(LifeCounterModal.Default)

        assertEquals(LifeCounterModal.Default, stack.current)
        assertFalse(stack.canGoBack)
    }

    @Test
    fun nestedModalCanGoBackToPreviousModal() {
        val stack = LifeCounterModalStack()
            .open(LifeCounterModal.Default)
            .open(LifeCounterModal.StartingLife)

        assertEquals(LifeCounterModal.StartingLife, stack.current)
        assertTrue(stack.canGoBack)
        assertEquals(LifeCounterModal.Default, stack.goBack().current)
    }

    @Test
    fun backFromRootClosesStack() {
        val stack = LifeCounterModalStack()
            .open(LifeCounterModal.Default)

        assertNull(stack.goBack().current)
    }

    @Test
    fun replaceChangesCurrentModalWithoutAddingHistory() {
        val stack = LifeCounterModalStack()
            .open(LifeCounterModal.Default)
            .replace(LifeCounterModal.Settings)

        assertEquals(LifeCounterModal.Settings, stack.current)
        assertFalse(stack.canGoBack)
    }
}
