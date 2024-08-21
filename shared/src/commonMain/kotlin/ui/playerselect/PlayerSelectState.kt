package ui.playerselect

data class PlayerSelectState(
    val showHelperText: HelperTextState = HelperTextState.FULL
)

enum class HelperTextState(
    val text: String
) {
    FULL("Press & hold to select 1st player"),
    FADED("Press & hold to select 1st player"),
    HIDDEN("")
}
