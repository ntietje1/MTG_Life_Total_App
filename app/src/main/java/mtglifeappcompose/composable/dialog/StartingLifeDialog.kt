package mtglifeappcompose.composable.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mtglifeappcompose.R
import mtglifeappcompose.composable.SettingsButton

@Composable
fun StartingLifeDialogContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, setStartingLife: (Int) -> Unit
) {
    var customLife by remember { mutableStateOf("") }
    BoxWithConstraints(modifier) {
        val maxHeight = maxHeight
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(maxHeight / 6f))
            GridDialogContent(Modifier.wrapContentSize(), items = listOf({
                SettingsButton(imageResource = painterResource(id = R.drawable.forty_icon), mainColor = MaterialTheme.colorScheme.onPrimary, text = "", shadowEnabled = false, onPress = {
                    setStartingLife(40)
                    onDismiss()
                })
            }, {
                SettingsButton(imageResource = painterResource(id = R.drawable.thirty_icon), mainColor = MaterialTheme.colorScheme.onPrimary, text = "", shadowEnabled = false, onPress = {
                    setStartingLife(30)
                    onDismiss()
                })
            }, {
                SettingsButton(imageResource = painterResource(id = R.drawable.twenty_icon), mainColor = MaterialTheme.colorScheme.onPrimary, text = "", shadowEnabled = false, onPress = {
                    setStartingLife(20)
                    onDismiss()
                })
            }))
            Box(
                modifier = Modifier.wrapContentSize()
            ) {
                fun customSetStartLife() {
                    val life = customLife.toIntOrNull()
                    if (life != null) {
                        setStartingLife(life)
                        onDismiss()
                    }
                }
                TextField(value = customLife, onValueChange = { customLife = it }, label = {
                    Text(
                        "Custom Starting Life", color = MaterialTheme.colorScheme.onPrimary, fontSize = 15.sp
                    )
                }, textStyle = TextStyle(fontSize = 20.sp), singleLine = true, colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    disabledTextColor = MaterialTheme.colorScheme.onPrimary,
                    focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    disabledLabelColor = MaterialTheme.colorScheme.onPrimary,
                    cursorColor = MaterialTheme.colorScheme.onPrimary,
                    selectionColors = TextSelectionColors(
                        handleColor = MaterialTheme.colorScheme.onPrimary,
                        backgroundColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    ),
                    focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                    disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.2f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ), keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number, autoCorrect = false, capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Done
                ), keyboardActions = KeyboardActions(onDone = { customSetStartLife() }), modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(80.dp)
                    .padding(top = 20.dp)
                    .padding(horizontal = 5.dp)
                )
                SettingsButton(
                    Modifier
                        .align(Alignment.CenterEnd)
                        .padding(top = 20.dp, end = 5.dp)
                        .size(50.dp),
                    imageResource = painterResource(id = R.drawable.enter_icon),
                    shadowEnabled = false,
                    mainColor = MaterialTheme.colorScheme.onPrimary,
                    onPress = { customSetStartLife() })
            }
        }
    }
}