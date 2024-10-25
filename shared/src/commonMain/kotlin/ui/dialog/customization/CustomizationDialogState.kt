package ui.dialog.customization

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import data.Player

data class CustomizationDialogState(
    val player: Player,
    val customizationMenuState: CustomizationMenuState = CustomizationMenuState.DEFAULT,
    val showCameraWarning: Boolean = false,
    val showResetPrefsDialog: Boolean = false,
    val showBackgroundColorPicker: Boolean = false,
    val showTextColorPicker: Boolean = false,
    val changeNameTextField: TextFieldValue = TextFieldValue(player.name, selection = TextRange(player.name.length)),
    val changeWasMade: Boolean = false
)

enum class CustomizationMenuState {
    DEFAULT,
    LOAD_PLAYER,
    SCRYFALL_SEARCH,
    BACKGROUND_COLOR_PICKER,
    ACCENT_COLOR_PICKER,
    GIF_SEARCH
}