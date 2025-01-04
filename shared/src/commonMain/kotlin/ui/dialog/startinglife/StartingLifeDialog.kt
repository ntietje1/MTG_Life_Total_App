package ui.dialog.startinglife

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.enter_icon
import lifelinked.shared.generated.resources.forty_icon
import lifelinked.shared.generated.resources.thirty_icon
import lifelinked.shared.generated.resources.twenty_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.LocalDimensions
import theme.scaledSp
import ui.components.SettingsButton
import ui.components.TextFieldWithButton
import ui.dialog.GridDialogContent

@Composable
fun StartingLifeDialogContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, resetGameState: (Int) -> Unit, viewModel: StartingLifeViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val dimensions = LocalDimensions.current

    BoxWithConstraints(modifier) {
        val textFieldHeight = remember(Unit) { maxWidth / 9f + 30.dp }
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(textFieldHeight))
            GridDialogContent(Modifier.wrapContentSize().weight(1.0f), title = "Set starting life total", items = listOf({
                SettingsButton(imageVector = vectorResource(Res.drawable.forty_icon), text = "", shadowEnabled = false, onPress = {
                    viewModel.setStartingLife(40)
                    resetGameState(40)
                    onDismiss()
                })
            }, {
                SettingsButton(imageVector = vectorResource(Res.drawable.thirty_icon), text = "", shadowEnabled = false, onPress = {
                    viewModel.setStartingLife(30)
                    resetGameState(30)
                    onDismiss()
                })
            }, {
                SettingsButton(imageVector = vectorResource(Res.drawable.twenty_icon), text = "", shadowEnabled = false, onPress = {
                    viewModel.setStartingLife(20)
                    resetGameState(20)
                    onDismiss()
                })
            }))

            TextFieldWithButton(
                modifier = Modifier.fillMaxWidth(0.8f).height(textFieldHeight)
                    .border(
                        dimensions.borderThin, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f), RoundedCornerShape(15)
                    ), value = state.textFieldValue, onValueChange = {
                    viewModel.setTextFieldValue(it)
                }, label = "Custom Starting Life", keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                ), keyboardActions = KeyboardActions(onDone = {
                    viewModel.parseStartingLife()?.let {
                        viewModel.setStartingLife(it)
                        resetGameState(it)
                        onDismiss()
                    }
                })
            ) {
                SettingsButton(modifier = Modifier.fillMaxSize(), imageVector = vectorResource(Res.drawable.enter_icon), shadowEnabled = false, onPress = {
                    viewModel.parseStartingLife()?.let {
                        viewModel.setStartingLife(it)
                        resetGameState(it)
                        onDismiss()
                    }
                })
            }
            Spacer(Modifier.weight(0.5f))
        }
    }
}