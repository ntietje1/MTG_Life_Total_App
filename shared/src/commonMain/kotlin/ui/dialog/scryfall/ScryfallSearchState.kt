package ui.dialog.scryfall

import androidx.compose.ui.text.input.TextFieldValue
import data.serializable.Card
import data.serializable.Ruling

data class ScryfallSearchState(
    val textFieldValue: TextFieldValue = TextFieldValue(""),
    val cardResults: List<Card> = listOf(),
    val rulingsResults: List<Ruling> = listOf(),
    val lastSearchWasError: Boolean = false,
    val rulingCard: Card? = null,
    val backStackDiff: Int = 0,
    val printingsButtonEnabled: Boolean = true,
    val isSearchInProgress: Boolean = false,
    val scrollPosition: Int = 0
)