package composable.dialog.planechase

import Platform
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.ScryfallApiRetriever
import data.SettingsManager
import data.serializable.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaneChaseViewModel(
    private val platform: Platform,
    private val settingsManager: SettingsManager
): ViewModel() {

    private val _state = MutableStateFlow(PlaneChaseState())
    val state: StateFlow<PlaneChaseState> = _state.asStateFlow()

    private val scryfallApiRetriever = ScryfallApiRetriever()

    init {
        println("Platform!!!: $platform")
        loadPlanechaseState()
        val currentPlanes = loadAllPlanes()
        searchPlanes { cards ->
            val newPlanes = cards.filter { card -> card.name !in currentPlanes.map { it.name } }
            _state.value = _state.value.copy(allPlanes = newPlanes + cards)
            settingsManager.saveAllPlanes(newPlanes + cards)
        }
    }

    private fun savePlanechaseState() {
        settingsManager.savePlanechaseState(state.value.planarDeck, state.value.planarBackStack)
    }

    private fun loadPlanechaseState() {
        val (deck, back) = settingsManager.loadPlanechaseState()
        _state.value = _state.value.copy(planarDeck = deck.toMutableStateList(), planarBackStack = back.toMutableStateList())
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
        savePlanechaseState()
    }

    fun deselectPlane(card: Card) {
        removeFromDeck(card)
        clearBackStack()
        savePlanechaseState()
    }

    fun addAllPlanarDeck(cards: List<Card>) {
        cards.forEach{
            addToTopDeck(it)
        }
        clearBackStack()
        savePlanechaseState()
    }

    fun removeAllPlanarDeck(cards: List<Card>) {
        cards.forEach {
            removeFromDeck(it)
        }
        clearBackStack()
        savePlanechaseState()
    }

    fun backPlane() {
        if (_state.value.planarDeck.isNotEmpty()) {
            println("backPlane()")
            println("backstack: ${_state.value.planarBackStack.map { it.name }}")
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

    private fun loadAllPlanes(): List<Card> {
        val allPlanes = settingsManager.loadAllPlanes()
        _state.value = _state.value.copy(allPlanes = allPlanes)
        return allPlanes
    }


    private suspend fun search(qry: String = state.value.query): List<Card> {
        return scryfallApiRetriever.parseScryfallResponse<Card>(scryfallApiRetriever.searchScryfall("t:plane $qry"))
    }

    fun searchPlanes(qry: String = state.value.query, onSearchResult: (List<Card>) -> Unit) {
        setSearchInProgress(true)
        viewModelScope.launch {
            val resultCards = search(qry)
            setSearchedPlanes(resultCards)
            onSearchResult(resultCards)
            setSearchInProgress(false)
        }
    }

    fun setQuery(value: String) {
        _state.value = _state.value.copy(query = value)
    }

    private fun setSearchInProgress(value: Boolean) {
        _state.value = _state.value.copy(searchInProgress = value)
    }

    private fun setSearchedPlanes(value: List<Card>) {
        _state.value = _state.value.copy(searchedPlanes = value)
    }

    fun onBackPress() {
        setQuery("")
        setSearchedPlanes(emptyList())
    }

    fun toggleHideUnselected(value: Boolean? = null) {
        _state.value = _state.value.copy(hideUnselected = value ?: !_state.value.hideUnselected)
    }
}