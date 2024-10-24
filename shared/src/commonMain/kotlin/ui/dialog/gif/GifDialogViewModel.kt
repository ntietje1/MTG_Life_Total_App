package ui.dialog.gif

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.api.GifApiRetriever
import data.api.MediaFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GifDialogViewModel(private val gifApiRetriever: GifApiRetriever = GifApiRetriever()) : ViewModel() {
    private val _state = MutableStateFlow(GifDialogState())
    val state: StateFlow<GifDialogState> = _state.asStateFlow()

    fun searchGifs(qry: String, amount: Int) {
        viewModelScope.launch {
            clearResults()
            setIsSearchInProgress(true)
            setLastSearchWasError(false)
//            println("searchGifs: $qry")
            val result = gifApiRetriever.searchGifs(qry, amount)
            if (result.error != null) {
                setLastSearchWasError(true)
            } else {
                result.result?.let { setGifResults(it.toSet()) }
            }
            setIsSearchInProgress(false)
        }
    }

    fun getNextGifs(amount: Int) {
        if (state.value.isSearchInProgress || state.value.additionalSearchInProgress || state.value.lastSearchWasError || state.value.gifResults.isEmpty()) { return }
        viewModelScope.launch {
//            println("getNextGifs")
            setAdditionalSearchInProgress(true)
//            val result = gifApiRetriever.getNextGifs(amount)
//            setAdditionalSearchInProgress(false)
//            println("getNextGifs result: $result")
//            setGifResults(state.value.gifResults + result)
            val result = gifApiRetriever.getNextGifs(amount)
            if (result.error != null) {
                setLastSearchWasError(true)
            } else {
                result.result?.let { setGifResults(state.value.gifResults + it) }
            }
            setAdditionalSearchInProgress(false)
        }
    }


    fun setTextFieldValue(textFieldValue: TextFieldValue) {
        _state.value = _state.value.copy(textFieldValue = textFieldValue)
    }

    private fun setGifResults(gifResults: Set<MediaFormat>) {
        _state.value = _state.value.copy(gifResults = gifResults.toSet())
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
        setGifResults(setOf())
    }
}