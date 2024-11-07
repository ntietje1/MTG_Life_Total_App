package ui.dialog.startinglife

import androidx.compose.ui.text.input.TextFieldValue
import data.SettingsManager

data class StartingLifeState(
    val textFieldValue: TextFieldValue = TextFieldValue(SettingsManager.instance.startingLife.value.toString()),
)