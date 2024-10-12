package ui.dialog.scryfall

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.ScryfallApiRetriever
import data.serializable.Card
import data.serializable.Ruling
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScryfallSearchViewModel(
    private val scryfallApiRetriever: ScryfallApiRetriever = ScryfallApiRetriever()
): ViewModel() {
    private val _state = MutableStateFlow(ScryfallSearchState())
    val state: StateFlow<ScryfallSearchState> = _state.asStateFlow()

    fun searchCards(qry: String, disablePrintingsButton: Boolean = false) {
        viewModelScope.launch {
            clearResults()
            setIsSearchInProgress(true)
            val result = scryfallApiRetriever.searchScryfall(qry)
            val parsedResult = scryfallApiRetriever.parseScryfallResponse<Card>(result)
            setCardResults(parsedResult)
            setLastSearchWasError(state.value.cardResults.isEmpty())
            setPrintingsButtonEnabled(!disablePrintingsButton)
            setIsSearchInProgress(false)
            incrementBackStackDiff()
        }
    }

    fun searchRulings(qry: String) {
        viewModelScope.launch {
            clearResults()
            setIsSearchInProgress(true)
            setRulingsResults(scryfallApiRetriever.parseScryfallResponse<Ruling>(scryfallApiRetriever.searchScryfall(qry)))
//            viewModel.setLastSearchWasError(state.rulingsResults.isEmpty())
            setLastSearchWasError(false)
            setIsSearchInProgress(false)
            incrementBackStackDiff()
        }
    }

    fun setScrollPosition(position: Int) {
        _state.value = _state.value.copy(scrollPosition = position)
    }

    private fun clearResults() {
        println("clearResults")
        setCardResults(listOf())
        setRulingsResults(listOf())
    }

    fun setTextFieldValue(textFieldValue: TextFieldValue) {
        _state.value = _state.value.copy(textFieldValue = textFieldValue)
    }

    private fun setCardResults(cardResults: List<Card>) {
        println("setCardResults: $cardResults")
        _state.value = _state.value.copy(cardResults = cardResults)
        println("setCardResults2: ${_state.value.cardResults}")
    }

    private fun setRulingsResults(rulingsResults: List<Ruling>) {
        _state.value = _state.value.copy(rulingsResults = rulingsResults)
    }

    private fun setLastSearchWasError(lastSearchWasError: Boolean) {
        _state.value = _state.value.copy(lastSearchWasError = lastSearchWasError)
    }

    fun setRulingCard(rulingCard: Card?) {
        _state.value = _state.value.copy(rulingCard = rulingCard)
    }

    fun incrementBackStackDiff(value: Int = 1) {
        _state.value = _state.value.copy(backStackDiff = _state.value.backStackDiff + value)
    }

    fun setPrintingsButtonEnabled(printingsButtonEnabled: Boolean) {
        _state.value = _state.value.copy(printingsButtonEnabled = printingsButtonEnabled)
    }

    private fun setIsSearchInProgress(isSearchInProgress: Boolean) {
        _state.value = _state.value.copy(isSearchInProgress = isSearchInProgress)
    }

}