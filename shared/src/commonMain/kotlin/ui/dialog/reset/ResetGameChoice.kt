package ui.dialog.reset

enum class ResetGameChoice(
    val resetPlayerPreferences: Boolean,
    val chooseFirstPlayer: Boolean
) {
    SamePlayersSelectFirst(
        resetPlayerPreferences = false,
        chooseFirstPlayer = true
    ),
    SamePlayersSkipFirst(
        resetPlayerPreferences = false,
        chooseFirstPlayer = false
    ),
    DifferentPlayersSelectFirst(
        resetPlayerPreferences = true,
        chooseFirstPlayer = true
    ),
    DifferentPlayersSkipFirst(
        resetPlayerPreferences = true,
        chooseFirstPlayer = false
    )
}
