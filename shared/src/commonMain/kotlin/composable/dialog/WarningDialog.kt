package composable.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

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