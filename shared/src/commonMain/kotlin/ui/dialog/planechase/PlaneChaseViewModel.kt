package ui.dialog.planechase

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.storage.ISettingsManager
import domain.api.ScryfallApi
import model.card.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaneChaseViewModel(
    private val settingsManager: ISettingsManager
): ViewModel() {

    private val _state = MutableStateFlow(PlaneChaseState())
    val state: StateFlow<PlaneChaseState> = _state.asStateFlow()

    private val scryfallApi = ScryfallApi()

    init {
        loadPlanechaseState()
        searchPlanes { result ->
            if (result.isNotEmpty()) {
                _state.value = _state.value.copy(allPlanes = result)
                savePlanechaseState()
            }
        }
    }

    private fun shuffleDeck() {
        _state.value.planarDeck.shuffle()
    }

    fun onResetGame() {
        _state.value = _state.value.copy(planarBackStack = mutableStateListOf())
        shuffleDeck()
        savePlanechaseState()
    }

    private fun savePlanechaseState() {
        settingsManager.savePlanechaseState(
            allPlanes = state.value.allPlanes,
            planarDeck = state.value.planarDeck,
            planarBackStack = state.value.planarBackStack
        )
    }

    private fun loadPlanechaseState() {
        val (all, deck, back) = settingsManager.loadPlanechaseState()
        _state.value = _state.value.copy(
            allPlanes = all.toMutableStateList(),
            planarDeck = deck.toMutableStateList(),
            planarBackStack = back.toMutableStateList()
        )
    }

    private fun removeFromDeck(card: Card) {
        _state.value.planarDeck.remove(card)
    }

    private fun addToTopDeck(card: Card) {
        removeFromDeck(card)
        _state.value.planarDeck.add(card)
    }

    private fun addToBottomDeck(card: Card) {
        removeFromDeck(card)
        _state.value.planarDeck.add(0, card)
    }

    private fun clearBackStack() {
        _state.value.planarBackStack.clear()
    }

    private fun popDeck(): Card? {
        val card = _state.value.planarDeck.lastOrNull()
        if (card != null) { removeFromDeck(card) }
        return card
    }


    private fun pushBackStack(value: Card) {
       _state.value.planarBackStack.add(value)
    }

    private fun popBackStack(): Card? {
        val card = _state.value.planarBackStack.lastOrNull()
        if (card != null) { _state.value.planarBackStack.removeLast() }
        return card
    }

    fun selectPlane(card: Card) {
        addToTopDeck(card)
        clearBackStack()
        shuffleDeck()
        savePlanechaseState()
    }

    fun deselectPlane(card: Card) {
        removeFromDeck(card)
        clearBackStack()
        shuffleDeck()
        savePlanechaseState()
    }

    fun addAllPlanarDeck(cards: List<Card>) {
        cards.forEach{
            addToTopDeck(it)
        }
        clearBackStack()
        shuffleDeck()
        savePlanechaseState()
    }

    fun removeAllPlanarDeck(cards: List<Card>) {
        cards.forEach {
            removeFromDeck(it)
        }
        clearBackStack()
        shuffleDeck()
        savePlanechaseState()
    }

    fun backPlane() {
        if (_state.value.planarDeck.isNotEmpty()) {
            val card = popBackStack()
            card?.let { addToTopDeck(card) }
            savePlanechaseState()
        }
    }

    fun planeswalk(): Card? {
        if (state.value.planarDeck.isNotEmpty()) {
            val card = popDeck()
            card?.let {
                pushBackStack(card)
                addToBottomDeck(card)
            }
            savePlanechaseState()
            return card
        }
        return null
    }

    private suspend fun search(qry: String = state.value.query.text): List<Card> {
        return scryfallApi.searchCards("(t:plane or t:phenomenon) $qry")
    }

    fun searchPlanes(qry: String = state.value.query.text, onSearchResult: (List<Card>) -> Unit) {
        setSearchInProgress(true)
        viewModelScope.launch {
            val resultCards = search(qry)
            setSearchedPlanes(resultCards)
            onSearchResult(resultCards)
            setSearchInProgress(false)
        }
    }

    fun setQuery(value: TextFieldValue) {
        _state.value = _state.value.copy(query = value)
    }

    private fun setSearchInProgress(value: Boolean) {
        _state.value = _state.value.copy(searchInProgress = value)
    }

    private fun setSearchedPlanes(value: List<Card>) {
        _state.value = _state.value.copy(searchedPlanes = value)
    }

    fun onBackPress() {
        setQuery(TextFieldValue(""))
        setSearchedPlanes(state.value.allPlanes)
    }

    fun toggleHideUnselected(value: Boolean? = null) {
        _state.value = _state.value.copy(hideUnselected = value ?: !_state.value.hideUnselected)
    }
}