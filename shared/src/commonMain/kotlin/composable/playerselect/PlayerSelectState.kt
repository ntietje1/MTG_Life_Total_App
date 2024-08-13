package composable.playerselect

data class PlayerSelectState(
    val showHelperText: HelperTextState = HelperTextState.FULL
)

enum class HelperTextState(
    val text: String
) {
    FULL("Tap & hold to select player"),
    FADED("Tap & hold to select player"),
    HIDDEN("")
}
