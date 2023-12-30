package mtglifeappcompose.composable.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.mtglifeappcompose.R
import mtglifeappcompose.composable.SettingsButton

@Composable
fun PlayerNumberDialogContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, setPlayerNum: (Int) -> Unit, resetPlayers: () -> Unit, show4PlayerDialog: () -> Unit
) {
    GridDialogContent(modifier, items = listOf({
        SettingsButton(imageResource = painterResource(R.drawable.one_icon), text = "", shadowEnabled = false, onPress = {
            setPlayerNum(1)
            resetPlayers()
            onDismiss()
        })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.two_icon), text = "", shadowEnabled = false, onPress = {
            setPlayerNum(2)
            resetPlayers()
            onDismiss()
        })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.three_icon), text = "", shadowEnabled = false, onPress = {
            setPlayerNum(3)
            resetPlayers()
            onDismiss()
        })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.four_icon), text = "", shadowEnabled = false, onPress = {
            show4PlayerDialog()
//                setPlayerNum(4)
//                resetPlayers()
//                onDismiss()
        })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.five_icon), text = "", shadowEnabled = false, onPress = {
            setPlayerNum(5)
            resetPlayers()
            onDismiss()
        })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.six_icon), text = "", onPress = {
            setPlayerNum(6)
            resetPlayers()
            onDismiss()
        })
    }))
}