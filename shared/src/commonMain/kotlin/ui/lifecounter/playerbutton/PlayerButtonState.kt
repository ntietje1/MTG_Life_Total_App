package ui.lifecounter.playerbutton

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import data.Player

data class PlayerButtonState(
    val player: Player,
    val buttonState: PBState = PBState.NORMAL,
    val showScryfallSearch: Boolean = false,
    val showCameraWarning: Boolean = false,
    val showResetPrefsDialog: Boolean = false,
    val showBackgroundColorPicker: Boolean = false,
    val showTextColorPicker: Boolean = false,
    val showChangeNameField: Boolean = false,
    val changeNameTextField: TextFieldValue = TextFieldValue(player.name, selection = TextRange(player.name.length)),
    val backStack: List<() -> Unit> = listOf()
)

enum class PBState {
    NORMAL,
    COMMANDER_DEALER,
    COMMANDER_RECEIVER,
    SETTINGS_DEFAULT,
    SETTINGS_CUSTOMIZE,
    SETTINGS_BACKGROUND_COLOR_PICKER,
    SETTINGS_TEXT_COLOR_PICKER,
    SETTINGS_LOAD_PLAYER,
    COUNTERS_VIEW,
    COUNTERS_SELECT
}