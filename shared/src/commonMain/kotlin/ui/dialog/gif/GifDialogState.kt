package ui.dialog.gif

import androidx.compose.ui.text.input.TextFieldValue
import data.api.MediaFormat

data class GifDialogState(
    val textFieldValue: TextFieldValue = TextFieldValue(""),
    val gifResults: Set<MediaFormat> = setOf(),
    val isSearchInProgress: Boolean = false,
    val additionalSearchInProgress: Boolean = false,
    val scrollPosition: Int = 0
)