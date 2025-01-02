package ui.dialog.planechase

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.input.TextFieldValue
import model.card.Card

data class PlaneChaseState(
    val planarDeck: SnapshotStateList<Card> = mutableStateListOf(),
    val planarBackStack: SnapshotStateList<Card> = mutableStateListOf(),

    val allPlanes: List<Card> = listOf(),
    val searchedPlanes: List<Card> = listOf(),
    val hideUnselected: Boolean = false,
    val query: TextFieldValue = TextFieldValue(""),
    val searchInProgress: Boolean = false,
)