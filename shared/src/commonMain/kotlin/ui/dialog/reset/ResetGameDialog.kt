package ui.dialog.reset

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ui.dialog.WarningDialog

@Composable
fun ResetGameDialog(
    onDismiss: () -> Unit,
    onChoice: (ResetGameChoice) -> Unit
) {
    var playerPreferenceReset by remember { mutableStateOf<Boolean?>(null) }

    if (playerPreferenceReset == null) {
        WarningDialog(
            onDismiss = onDismiss,
            title = "Reset Game",
            message = "Select an option to start a new game",
            optionOneMessage = "Same players",
            optionTwoMessage = "Different players",
            dismissOnOption = false,
            onOptionOne = {
                playerPreferenceReset = false
            },
            onOptionTwo = {
                playerPreferenceReset = true
            },
        )
    } else {
        WarningDialog(
            onDismiss = onDismiss,
            title = "Choose New First Player",
            message = "Select whether to skip player selection or not",
            optionOneMessage = "Select",
            optionTwoMessage = "Skip",
            dismissOnOption = false,
            onOptionOne = {
                onChoice(choiceFor(resetPlayerPreferences = playerPreferenceReset == true, chooseFirstPlayer = true))
            },
            onOptionTwo = {
                onChoice(choiceFor(resetPlayerPreferences = playerPreferenceReset == true, chooseFirstPlayer = false))
            },
        )
    }
}

fun choiceFor(
    resetPlayerPreferences: Boolean,
    chooseFirstPlayer: Boolean
): ResetGameChoice {
    return when {
        resetPlayerPreferences && chooseFirstPlayer -> ResetGameChoice.DifferentPlayersSelectFirst
        resetPlayerPreferences -> ResetGameChoice.DifferentPlayersSkipFirst
        chooseFirstPlayer -> ResetGameChoice.SamePlayersSelectFirst
        else -> ResetGameChoice.SamePlayersSkipFirst
    }
}
