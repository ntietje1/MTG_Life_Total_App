package mtglifeappcompose.composable.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.mtglifeappcompose.R
import mtglifeappcompose.composable.SettingsButton

@Composable
fun StartingLifeDialogContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    showStartingLifeDialog: MutableState<Boolean>,
    setStartingLife: (Int) -> Unit
) {
    GridDialogContent(modifier, items = listOf({
        SettingsButton(imageResource = painterResource(id = R.drawable.fifty_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            shadowEnabled = false,
            onPress = {
                setStartingLife(50)
                onDismiss()
                showStartingLifeDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(id = R.drawable.forty_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            shadowEnabled = false,
            onPress = {
                setStartingLife(40)
                onDismiss()
                showStartingLifeDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(id = R.drawable.thirty_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            shadowEnabled = false,
            onPress = {
                setStartingLife(30)
                onDismiss()
                showStartingLifeDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(id = R.drawable.twenty_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            shadowEnabled = false,
            onPress = {
                setStartingLife(20)
                onDismiss()
                showStartingLifeDialog.value = false
            })
    }))
}