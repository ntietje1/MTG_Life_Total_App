package ui.dialog.dice

import androidx.compose.ui.text.input.TextFieldValue

data class DiceRollState(
    val textFieldValue: TextFieldValue = TextFieldValue("100"),
    val customDieValue: UInt = 100u,
    val lastResult: UInt? = null,
    val faceValue: UInt? = null,
) 