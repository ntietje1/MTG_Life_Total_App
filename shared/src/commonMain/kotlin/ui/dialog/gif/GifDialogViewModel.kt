package ui.dialog.gif

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.api.GifAsset
import domain.api.GifSearchClient
import domain.api.GifSearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GifDialogViewModel(private val gifSearchClient: GifSearchClient) : ViewModel() {
    private val _state = MutableStateFlow(GifDialogState())
    val state: StateFlow<GifDialogState> = _state.asStateFlow()
    private var nextCursor: String? = null
    private var lastQuery: String? = null

    fun searchGifs(qry: String, amount: Int) {
        viewModelScope.launch {
            clearResults()
            setIsSearchInProgress(true)
            setLastSearchWasError(false)
            when (val result = gifSearchClient.search(qry, amount, cursor = null)) {
                is GifSearchResult.Failure -> setLastSearchWasError(true)
                is GifSearchResult.Success -> {
                    nextCursor = result.page.nextCursor
                    lastQuery = qry
                    setGifResults(result.page.items)
                }
            }
            setIsSearchInProgress(false)
        }
    }

    fun getNextGifs(amount: Int) {
        val query = lastQuery ?: return
        val cursor = nextCursor ?: return
        if (state.value.isSearchInProgress || state.value.additionalSearchInProgress || state.value.lastSearchWasError || state.value.gifResults.isEmpty()) { return }
        viewModelScope.launch {
            setAdditionalSearchInProgress(true)
            when (val result = gifSearchClient.search(query, amount, cursor)) {
                is GifSearchResult.Failure -> setLastSearchWasError(true)
                is GifSearchResult.Success -> {
                    nextCursor = result.page.nextCursor
                    setGifResults(state.value.gifResults + result.page.items)
                }
            }
            setAdditionalSearchInProgress(false)
        }
    }


    fun setTextFieldValue(textFieldValue: TextFieldValue) {
        _state.value = _state.value.copy(textFieldValue = textFieldValue)
    }

    private fun setGifResults(gifResults: List<GifAsset>) {
        _state.value = _state.value.copy(gifResults = gifResults)
    }


    private fun setIsSearchInProgress(isSearchInProgress: Boolean) {
        _state.value = _state.value.copy(isSearchInProgress = isSearchInProgress)
    }

    private fun setAdditionalSearchInProgress(additionalSearchInProgress: Boolean) {
        _state.value = _state.value.copy(additionalSearchInProgress = additionalSearchInProgress)
    }

    fun setScrollPosition(scrollPosition: Int) {
        _state.value = _state.value.copy(scrollPosition = scrollPosition)
    }

    private fun setLastSearchWasError(lastSearchWasError: Boolean) {
        _state.value = _state.value.copy(lastSearchWasError = lastSearchWasError)
    }

    private fun clearResults() {
        nextCursor = null
        lastQuery = null
        setGifResults(emptyList())
    }
}
