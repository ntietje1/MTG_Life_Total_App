package ui.dialog.gif

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.api.GifApiRetriever
import data.api.MediaFormat
import di.Platform
import di.platform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GifDialogViewModel(private val gifApiRetriever: GifApiRetriever = GifApiRetriever()) : ViewModel() {
    private val _state = MutableStateFlow(GifDialogState())
    val state: StateFlow<GifDialogState> = _state.asStateFlow()

    fun searchGifs(qry: String, amount: Int) {
        if (platform == Platform.IOS) {
            return
        }
        viewModelScope.launch {
            clearResults()
            setIsSearchInProgress(true)
//            println("searchGifs: $qry")
            val result = gifApiRetriever.searchGifs(qry, amount).toSet()
//            println("searchGifs result: $result")
            setGifResults(result)
            setIsSearchInProgress(false)
        }
    }

    fun getNextGifs(amount: Int) {
        if (state.value.isSearchInProgress || state.value.additionalSearchInProgress || state.value.gifResults.isEmpty()) { return }
        viewModelScope.launch {
//            println("getNextGifs")
            setAdditionalSearchInProgress(true)
            val result = gifApiRetriever.getNextGifs(amount)
            setAdditionalSearchInProgress(false)
//            println("getNextGifs result: $result")
            setGifResults(state.value.gifResults + result)
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

    private fun clearResults() {
        setGifResults(setOf())
    }
}