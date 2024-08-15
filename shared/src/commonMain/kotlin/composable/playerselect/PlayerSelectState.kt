package composable.playerselect

data class PlayerSelectState(
    val showHelperText: HelperTextState = HelperTextState.FULL
)

enum class HelperTextState(
    val text: String
) {
    FULL("Press fingers down to select player"),
    FADED("Press fingers down to select player"),
    HIDDEN("")
}
