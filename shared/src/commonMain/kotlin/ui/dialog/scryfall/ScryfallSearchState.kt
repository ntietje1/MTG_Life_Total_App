package ui.dialog.scryfall

import androidx.compose.ui.text.input.TextFieldValue

data class ScryfallSearchState(
    val textFieldValue: TextFieldValue = TextFieldValue(""),
)