package ui.tutorial

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TutorialViewModelTest {
    @Test
    fun startsAtFirstTutorialPageWithFiveIncompleteSteps() {
        val viewModel = TutorialViewModel()

        assertEquals(0, viewModel.state.value.currentPage)
        assertEquals(5, viewModel.state.value.totalPages)
        assertEquals(List(5) { false }, viewModel.state.value.completed)
    }

    @Test
    fun changingPagesHidesHint() {
        val viewModel = TutorialViewModel()
        viewModel.showHint(true)

        viewModel.setCurrentPage(1)

        assertEquals(1, viewModel.state.value.currentPage)
        assertFalse(viewModel.state.value.showHint)
    }

    @Test
    fun successMarksCurrentPageCompleteAndHidesHint() {
        val viewModel = TutorialViewModel()
        viewModel.showHint(true)

        viewModel.setSuccess(true)

        assertTrue(viewModel.state.value.showSuccess)
        assertTrue(viewModel.state.value.completed[0])
        assertFalse(viewModel.state.value.showHint)
    }

    @Test
    fun warningCloseAndBlurStateAreExplicit() {
        val viewModel = TutorialViewModel()

        viewModel.showWarningDialog(true)
        viewModel.showCloseDialog(true)
        viewModel.setBlur(true)

        assertTrue(viewModel.state.value.showWarningDialog)
        assertTrue(viewModel.state.value.showCloseDialog)
        assertTrue(viewModel.state.value.blur)
    }
}
