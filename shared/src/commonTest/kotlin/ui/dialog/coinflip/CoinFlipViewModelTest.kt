package ui.dialog.coinflip

import domain.storage.PreferencesRepository
import domain.storage.TestSettings
import kotlin.test.Test
import kotlin.test.assertTrue

class CoinFlipViewModelTest {
    @Test
    fun resetClearsVisibleHistoryAndLastResult() {
        val viewModel = CoinFlipViewModel(PreferencesRepository(TestSettings()))

        viewModel.singleFlip()
        viewModel.coinControllers.single().onResult(CoinHistoryItem.HEADS)

        assertTrue(viewModel.state.value.historyString.text.isNotBlank())
        assertTrue(viewModel.state.value.lastResultString.text.isNotBlank())

        viewModel.reset()

        assertTrue(viewModel.state.value.historyString.text.isBlank())
        assertTrue(viewModel.state.value.lastResultString.text.isBlank())
    }
}
