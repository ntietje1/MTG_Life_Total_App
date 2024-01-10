package composable.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * A generic warning dialog with two options
 * @param title the title of the dialog
 * @param message the message of the dialog
 * @param optionOneEnabled whether the first option is enabled
 * @param optionTwoEnabled whether the second option is enabled
 * @param optionOneMessage the message of the first option
 * @param optionTwoMessage the message of the second option
 * @param onOptionOne the action to perform when the first option is selected
 * @param onOptionTwo the action to perform when the second option is selected
 * @param onDismiss the action to perform when the dialog is dismissed
 */
@Composable
fun WarningDialog(
    title: String,
    message: String,
    optionOneEnabled: Boolean = true,
    optionTwoEnabled: Boolean = true,
    optionOneMessage: String = "Confirm",
    optionTwoMessage: String = "Dismiss",
    onOptionOne: () -> Unit = {},
    onOptionTwo: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {

    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(message)
        },
        confirmButton = {
            if (optionOneEnabled) {
                TextButton(onClick = {
                    onDismiss()
                    onOptionOne()
                }) {
                    Text(optionOneMessage, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        dismissButton = {
            if (optionTwoEnabled) {
                TextButton(onClick = {
                    onDismiss()
                    onOptionTwo()
                }) {
                    Text(optionTwoMessage, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    )
}