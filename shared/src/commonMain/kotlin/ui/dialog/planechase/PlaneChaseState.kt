package ui.dialog.planechase

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.text.input.TextFieldValue
import model.card.CardSummary

data class PlaneChaseState(
    val planarDeck: SnapshotStateList<CardSummary> = mutableStateListOf(),
    val planarBackStack: SnapshotStateList<CardSummary> = mutableStateListOf(),

    val allPlanes: List<CardSummary> = listOf(),
    val searchedPlanes: List<CardSummary> = listOf(),
    val hideUnselected: Boolean = false,
    val query: TextFieldValue = TextFieldValue(""),
    val searchInProgress: Boolean = false,
)
