package ui.dialog

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.min
import ui.components.SettingsButton
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.alternate4player_icon
import lifelinked.shared.generated.resources.default4player_icon
import lifelinked.shared.generated.resources.five_icon
import lifelinked.shared.generated.resources.four_icon
import lifelinked.shared.generated.resources.one_icon
import lifelinked.shared.generated.resources.six_icon
import lifelinked.shared.generated.resources.three_icon
import lifelinked.shared.generated.resources.two_icon
import org.jetbrains.compose.resources.vectorResource
import theme.LocalDimensions
import theme.scaledSp

@Composable
fun PlayerNumberDialogContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    setPlayerNumAndExit: (Int) -> Unit,
    show4PlayerDialog: () -> Unit
) {
    GridDialogContent(modifier, title = "Set number of players", items = listOf({
        SettingsButton(imageVector = vectorResource(Res.drawable.one_icon), text = "", shadowEnabled = false, onPress = {
            setPlayerNumAndExit(1)
            onDismiss()
        })
    }, {
        SettingsButton(imageVector = vectorResource(Res.drawable.two_icon), text = "", shadowEnabled = false, onPress = {
            setPlayerNumAndExit(2)
            onDismiss()
        })
    }, {
        SettingsButton(imageVector = vectorResource(Res.drawable.three_icon), text = "", shadowEnabled = false, onPress = {
            setPlayerNumAndExit(3)
            onDismiss()
        })
    }, {
        SettingsButton(imageVector = vectorResource(Res.drawable.four_icon), text = "", shadowEnabled = false, onPress = {
            show4PlayerDialog()
        })
    }, {
        SettingsButton(imageVector = vectorResource(Res.drawable.five_icon), text = "", shadowEnabled = false, onPress = {
            setPlayerNumAndExit(5)
            onDismiss()
        })
    }, {
        SettingsButton(imageVector = vectorResource(Res.drawable.six_icon), text = "", onPress = {
            setPlayerNumAndExit(6)
            onDismiss()
        })
    }))
}

@Composable
fun FourPlayerLayoutContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, setPlayerNumAndExit: (Int) -> Unit, setAltPlayerLayout: (value: Boolean) -> Unit
) {
    BoxWithConstraints(modifier) {
        val buttonSize = remember(Unit) { min(maxWidth * 0.7f, maxHeight * 0.5f) }
        val dimensions = LocalDimensions.current
        Column(
            modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .wrapContentHeight()
                    .wrapContentWidth()
                    .padding(top = dimensions.paddingMedium * 2),
                text = "Select a 4 player layout",
                fontSize = dimensions.textMedium.scaledSp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            SettingsButton(Modifier.size(buttonSize), imageVector = vectorResource(Res.drawable.default4player_icon), shadowEnabled = false, onPress = {
                setPlayerNumAndExit(4)
                setAltPlayerLayout(false)
                onDismiss()
            })
            SettingsButton(Modifier.size(buttonSize), imageVector = vectorResource(Res.drawable.alternate4player_icon), shadowEnabled = false, onPress = {
                setPlayerNumAndExit(4)
                setAltPlayerLayout(true)
                onDismiss()
            })
            Spacer(modifier = Modifier.height(dimensions.paddingMedium))
        }
    }
}