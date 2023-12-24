package mtglifeappcompose.composable.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.mtglifeappcompose.R
import mtglifeappcompose.composable.SettingsButton

@Composable
fun PlayerNumberDialogContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    showPlayerNumberDialog: MutableState<Boolean>,
    setPlayerNum: (Int) -> Unit,
    resetPlayers: () -> Unit
) {
    GridDialogContent(modifier, items = listOf({
        SettingsButton(imageResource = painterResource(R.drawable.one_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            shadowEnabled = false,
            onPress = {
                setPlayerNum(1)
                resetPlayers()
                onDismiss()
                showPlayerNumberDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.two_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            shadowEnabled = false,
            onPress = {
                setPlayerNum(2)
                resetPlayers()
                onDismiss()
                showPlayerNumberDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.three_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            shadowEnabled = false,
            onPress = {
                setPlayerNum(3)
                resetPlayers()
                onDismiss()
                showPlayerNumberDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.four_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            shadowEnabled = false,
            onPress = {
                setPlayerNum(4)
                resetPlayers()
                onDismiss()
                showPlayerNumberDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.five_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            shadowEnabled = false,
            onPress = {
                setPlayerNum(5)
                resetPlayers()
                onDismiss()
                showPlayerNumberDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.six_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            onPress = {
                setPlayerNum(6)
                resetPlayers()
                onDismiss()
                showPlayerNumberDialog.value = false
            })
    }))
}