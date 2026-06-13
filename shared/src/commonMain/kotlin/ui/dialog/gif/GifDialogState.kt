package ui.dialog.gif

import androidx.compose.ui.text.input.TextFieldValue
import domain.api.GifAsset

data class GifDialogState(
    val textFieldValue: TextFieldValue = TextFieldValue(""),
    val gifResults: List<GifAsset> = emptyList(),
    val isSearchInProgress: Boolean = false,
    val additionalSearchInProgress: Boolean = false,
    val scrollPosition: Int = 0,
    val lastSearchWasError: Boolean = false
)
