package mtglifeappcompose.composable.dialog

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SettingsDialogContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit
) {
    BoxWithConstraints(modifier) {
        Text(modifier = Modifier.align(Alignment.Center), text = "To be implemented!")

        /**
         * TODO: Implement SettingsDialogContent
         * - Add a SettingsButton for each of the following:
         *  - GENERAL -
         *  - Rotating middle button
         *  - Self commander damage
         *  - Fast coin flip
         *  - Disable camera roll
         *  - Disable haptic feedback
         *  - Disable screen timeout
         *
         *  -
         *  - CONTACT -
         *  - Submit Feedback
         *  - Write a Review
         *  - Buy me a coffee
         *  - About me
         */

    }
}