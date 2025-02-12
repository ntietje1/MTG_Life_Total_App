package ui.dialog.startinglife

import androidx.compose.ui.text.input.TextFieldValue
import domain.storage.LocalSettingsStore

data class StartingLifeState(
    val textFieldValue: TextFieldValue = TextFieldValue(LocalSettingsStore.instance.defaultStartingLife.value.toString()),
)