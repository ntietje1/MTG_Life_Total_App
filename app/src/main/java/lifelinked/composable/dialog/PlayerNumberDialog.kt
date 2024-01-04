package lifelinked.composable.dialog

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hypeapps.lifelinked.R
import lifelinked.composable.SettingsButton
import lifelinked.ui.theme.scaledSp

@Composable
fun PlayerNumberDialogContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, setPlayerNum: (Int) -> Unit, resetPlayers: () -> Unit, show4PlayerDialog: () -> Unit
) {
    GridDialogContent(modifier, title = "Set number of players", items = listOf({
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

@Composable
fun FourPlayerLayoutContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, setPlayerNum: (Int) -> Unit, set4PlayerDialog: (value: Boolean) -> Unit
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
            SettingsButton(Modifier.size(buttonSize), imageResource = painterResource(id = R.drawable.default4player_icon), shadowEnabled = false, onPress = {
                setPlayerNum(4)
                set4PlayerDialog(false)
                onDismiss()
            })
            SettingsButton(Modifier.size(buttonSize), imageResource = painterResource(id = R.drawable.alternate4player_icon), shadowEnabled = false, onPress = {
                setPlayerNum(4)
                set4PlayerDialog(true)
                onDismiss()
            })
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}