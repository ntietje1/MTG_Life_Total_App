package ui.lifecounter.playerbutton

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import data.Player
import data.TurnTimer

data class PlayerButtonState(
    val player: Player,
    val buttonState: PBState = PBState.NORMAL,
    val showScryfallSearch: Boolean = false,
    val showCameraWarning: Boolean = false,
    val showResetPrefsDialog: Boolean = false,
    val showBackgroundColorPicker: Boolean = false,
    val showTextColorPicker: Boolean = false,
    val showChangeNameField: Boolean = false,
    val showCustomizeMenu: Boolean = false,
    val customizationMenuState: CustomizationMenuState = CustomizationMenuState.DEFAULT,
    val changeNameTextField: TextFieldValue = TextFieldValue(player.name, selection = TextRange(player.name.length)),
    val backStack: List<() -> Unit> = listOf(),
    val timer: TurnTimer? = null
)

enum class CustomizationMenuState {
    DEFAULT,
    LOAD_PLAYER,
    SCRYFALL_SEARCH,
    BACKGROUND_COLOR_PICKER,
    ACCENT_COLOR_PICKER,
    GIF_SEARCH
}

enum class PBState {
    NORMAL,
    COMMANDER_DEALER,
    COMMANDER_RECEIVER,
    SETTINGS,
//    SETTINGS_CUSTOMIZE,
//    SETTINGS_BACKGROUND_COLOR_PICKER,
//    SETTINGS_TEXT_COLOR_PICKER,
//    SETTINGS_LOAD_PLAYER,
    COUNTERS_VIEW,
    COUNTERS_SELECT,
    SELECT_FIRST_PLAYER
}