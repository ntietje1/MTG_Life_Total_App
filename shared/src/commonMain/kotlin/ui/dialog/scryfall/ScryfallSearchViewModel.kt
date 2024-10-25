package ui.dialog.scryfall

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.api.ScryfallApiRetriever
import data.serializable.Card
import data.serializable.Ruling
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScryfallSearchViewModel(
    private val scryfallApiRetriever: ScryfallApiRetriever = ScryfallApiRetriever()
): ViewModel() {
    private val _state = MutableStateFlow(ScryfallSearchState())
    val state: StateFlow<ScryfallSearchState> = _state.asStateFlow()

    fun searchCards(qry: String, disablePrintingsButton: Boolean = false, onDone: suspend () -> Unit = {}) {
        viewModelScope.launch {
            clearResults()
            setIsSearchInProgress(true)
            val parsedResult = scryfallApiRetriever.searchCards(qry)
            setCardResults(parsedResult)
            setLastSearchWasError(state.value.cardResults.isEmpty())
            setPrintingsButtonEnabled(!disablePrintingsButton)
            setIsSearchInProgress(false)
            incrementBackStackDiff()
        }.invokeOnCompletion {
            viewModelScope.launch {
                delay(10)
                onDone()
            }
        }
    }

    fun searchRulings(qry: String, onDone: suspend () -> Unit = {}) {
        viewModelScope.launch {
            clearResults()
            setIsSearchInProgress(true)
            setRulingsResults(scryfallApiRetriever.searchRulings(qry))
//            viewModel.setLastSearchWasError(state.rulingsResults.isEmpty())
            setLastSearchWasError(false)
            setIsSearchInProgress(false)
            incrementBackStackDiff()
        }.invokeOnCompletion {
            viewModelScope.launch {
                delay(10)
                onDone()
            }
        }
    }

    fun setScrollPosition(position: Int) {
        _state.value = _state.value.copy(scrollPosition = position)
    }

    private fun clearResults() {
        setCardResults(listOf())
        setRulingsResults(listOf())
    }

    fun setTextFieldValue(textFieldValue: TextFieldValue) {
        _state.value = _state.value.copy(textFieldValue = textFieldValue)
    }

    private fun setCardResults(cardResults: List<Card>) {
        _state.value = _state.value.copy(cardResults = cardResults)
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