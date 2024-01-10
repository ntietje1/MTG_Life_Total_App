package composable.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import theme.scaledSp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

/**
 * A dialog that allows the user to set the number of players
 * @param modifier the modifier for this composable
 * @param onDismiss the action to perform when the dialog is dismissed
 * @param setPlayerNum the action to perform when the number of players is set
 * @param resetPlayers the action to perform when the players are reset
 * @param show4PlayerDialog the callback to switch to the 4 player layout dialog
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun PlayerNumberDialogContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, setPlayerNum: (Int) -> Unit, resetPlayers: () -> Unit, show4PlayerDialog: () -> Unit
) {
    GridDialogContent(modifier, title = "Set number of players", items = listOf({
        SettingsButton(imageResource = painterResource("one_icon.xml"), text = "", shadowEnabled = false, onPress = {
            setPlayerNum(1)
            resetPlayers()
            onDismiss()
        })
    }, {
        SettingsButton(imageResource = painterResource("two_icon.xml"), text = "", shadowEnabled = false, onPress = {
            setPlayerNum(2)
            resetPlayers()
            onDismiss()
        })
    }, {
        SettingsButton(imageResource = painterResource("three_icon.xml"), text = "", shadowEnabled = false, onPress = {
            setPlayerNum(3)
            resetPlayers()
            onDismiss()
        })
    }, {
        SettingsButton(imageResource = painterResource("four_icon.xml"), text = "", shadowEnabled = false, onPress = {
            show4PlayerDialog()
        })
    }, {
        SettingsButton(imageResource = painterResource("five_icon.xml"), text = "", shadowEnabled = false, onPress = {
            setPlayerNum(5)
            resetPlayers()
            onDismiss()
        })
    }, {
        SettingsButton(imageResource = painterResource("six_icon.xml"), text = "", onPress = {
            setPlayerNum(6)
            resetPlayers()
            onDismiss()
        })
    }))
}

/**
 * A dialog that allows the user to select a 4 player layout
 * @param modifier the modifier for this composable
 * @param onDismiss the action to perform when the dialog is dismissed
 * @param setPlayerNum the action to perform when the number of players is set
 * @param setAlt4PlayerLayout the action to perform when the alternate 4 player layout is set
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun FourPlayerLayoutContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, setPlayerNum: (Int) -> Unit, setAlt4PlayerLayout: (value: Boolean) -> Unit
) {
    BoxWithConstraints(modifier) {
        val buttonSize = maxWidth * 0.7f
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .wrapContentHeight()
                    .wrapContentWidth()
                    .padding(bottom = 10.dp, top = 30.dp),
                text = "Select a 4 player layout",
                fontSize = 25.scaledSp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            SettingsButton(Modifier.size(buttonSize), imageResource = painterResource("default4player_icon.xml"), shadowEnabled = false, onPress = {
                setPlayerNum(4)
                setAlt4PlayerLayout(false)
                onDismiss()
            })
            SettingsButton(Modifier.size(buttonSize), imageResource = painterResource("alternate4player_icon.xml"), shadowEnabled = false, onPress = {
                setPlayerNum(4)
                setAlt4PlayerLayout(true)
                onDismiss()
            })
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}