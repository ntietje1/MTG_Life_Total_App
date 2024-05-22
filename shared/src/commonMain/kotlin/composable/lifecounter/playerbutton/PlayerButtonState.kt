package composable.lifecounter.playerbutton

import data.Player

data class PlayerButtonState(
    val player: Player,
    val buttonState: PBState = PBState.NORMAL,
    val showScryfallSearch: Boolean = false,
    val showCameraWarning: Boolean = false,
    val showFilePicker: Boolean = false,
    val showResetPrefsDialog: Boolean = false
)

enum class PBState {
    NORMAL, COMMANDER_DEALER, COMMANDER_RECEIVER, SETTINGS
}