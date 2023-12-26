package mtglifeappcompose.composable.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.mtglifeappcompose.R
import mtglifeappcompose.composable.SettingsButton

@Composable
fun FourPlayerLayoutContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, setPlayerNum: (Int) -> Unit, set4PlayerDialog: (value: Boolean) -> Unit
) {
    BoxWithConstraints(modifier) {
        val buttonSize = maxWidth * 0.8f
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceAround, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SettingsButton(size = buttonSize, imageResource = painterResource(id = R.drawable.default4player_icon), mainColor = MaterialTheme.colorScheme.onPrimary, shadowEnabled = false, onPress = {
                setPlayerNum(4)
                set4PlayerDialog(false)
                onDismiss()
            })
            SettingsButton(size = buttonSize,
                imageResource = painterResource(id = R.drawable.alternate4player_icon),
                mainColor = MaterialTheme.colorScheme.onPrimary,
                shadowEnabled = false,
                onPress = {
                    setPlayerNum(4)
                    set4PlayerDialog(true)
                    onDismiss()
                })
        }
    }
}