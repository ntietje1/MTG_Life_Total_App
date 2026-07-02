package ui.dialog.scryfall

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.api.ScryfallClient
import domain.api.ScryfallResult
import model.card.CardSummary
import model.card.RulingSummary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScryfallSearchViewModel(
    private val scryfallClient: ScryfallClient
): ViewModel() {
    private val _state = MutableStateFlow(ScryfallSearchState())
    val state: StateFlow<ScryfallSearchState> = _state.asStateFlow()

    fun searchCards(qry: String, disablePrintingsButton: Boolean = false, onDone: suspend () -> Unit = {}) {
        viewModelScope.launch {
            clearResults()
            setIsSearchInProgress(true)
            when (val result = scryfallClient.searchCards(qry)) {
                is ScryfallResult.Failure -> {
                    setCardResults(emptyList())
                    setLastSearchWasError(true)
                }
                is ScryfallResult.Success -> {
                    setCardResults(result.value.cards)
                    setLastSearchWasError(result.value.cards.isEmpty())
                }
            }
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
            when (val result = scryfallClient.searchRulings(qry)) {
                is ScryfallResult.Failure -> {
                    setRulingsResults(emptyList())
                    setLastSearchWasError(true)
                }
                is ScryfallResult.Success -> {
                    setRulingsResults(result.value)
                    setLastSearchWasError(false)
                }
            }
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

    private fun setCardResults(cardResults: List<CardSummary>) {
        _state.value = _state.value.copy(cardResults = cardResults)
    }

    private fun setRulingsResults(rulingsResults: List<RulingSummary>) {
        _state.value = _state.value.copy(rulingsResults = rulingsResults)
    }

    private fun setLastSearchWasError(lastSearchWasError: Boolean) {
        _state.value = _state.value.copy(lastSearchWasError = lastSearchWasError)
    }

    fun setRulingCard(rulingCard: CardSummary?) {
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
