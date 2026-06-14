package ui.dialog.customization

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import model.Player

data class CustomizationDialogState(
    val player: Player,
    val routeStack: List<CustomizationRoute> = listOf(CustomizationRoute.Default),
    val showCameraWarning: Boolean = false,
    val changeNameTextField: TextFieldValue = TextFieldValue(player.name, selection = TextRange(player.name.length)),
    val changeWasMade: Boolean = false,
    val colorChangeWasMade: Boolean = false
) {
    val currentRoute: CustomizationRoute get() = routeStack.last()
}

enum class CustomizationRoute {
    Default,
    LoadPlayer,
    ScryfallSearch,
    BackgroundColorPicker,
    AccentColorPicker,
    GifSearch
}
