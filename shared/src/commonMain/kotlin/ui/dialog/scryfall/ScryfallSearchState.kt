package ui.dialog.scryfall

import androidx.compose.ui.text.input.TextFieldValue
import model.card.CardSummary
import model.card.RulingSummary

data class ScryfallSearchState(
    val textFieldValue: TextFieldValue = TextFieldValue(""),
    val cardResults: List<CardSummary> = listOf(),
    val rulingsResults: List<RulingSummary> = listOf(),
    val lastSearchWasError: Boolean = false,
    val rulingCard: CardSummary? = null,
    val backStackDiff: Int = 0,
    val printingsButtonEnabled: Boolean = true,
    val isSearchInProgress: Boolean = false,
    val scrollPosition: Int = 0
)
