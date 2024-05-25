package composable.dialog.planechase

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import data.serializable.Card

data class PlaneChaseState(
    val planarDeck: SnapshotStateList<Card> = mutableStateListOf(),
    val planarBackStack: SnapshotStateList<Card> = mutableStateListOf(),

    val allPlanes: List<Card> = listOf(),
    val searchedPlanes: List<Card> = listOf(),
    val hideUnselected: Boolean = false,
    val query: String = "",
    val searchInProgress: Boolean = false,
)