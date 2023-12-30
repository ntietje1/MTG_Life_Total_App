package mtglifeappcompose.composable.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mtglifeappcompose.R
import mtglifeappcompose.composable.SettingsButton
import mtglifeappcompose.data.AppViewModel
import mtglifeappcompose.ui.theme.blendWith

@Composable
fun SettingsDialogContent(
    modifier: Modifier = Modifier, goToAboutMe: () -> Unit, addGoToSettingsToBackStack: () -> Unit
) {
    val viewModel: AppViewModel = viewModel()

    BoxWithConstraints(modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SettingsDialogHeader(
                    text = "GENERAL"
                )
            }
            item {
                Column(
                    Modifier
                        .wrapContentSize()
                        .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                ) {
                    SettingsDialogButtonWithToggle(
                        text = "Rotating Middle Button",
                        initialState = viewModel.rotatingMiddleButton,
                        toggle = { viewModel.toggleRotatingMiddleButton(it) },
                        icon = painterResource(id = R.drawable.placeholder_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Fast Coin Flip", initialState = viewModel.fastCoinFlip, toggle = { viewModel.toggleFastCoinFlip(it) }, icon = painterResource(id = R.drawable.placeholder_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Disable Camera Roll",
                        initialState = viewModel.cameraRollDisabled,
                        toggle = { viewModel.toggleCameraRollEnabled(it) },
                        icon = painterResource(id = R.drawable.placeholder_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Commander Damage Causes Life Loss",
                        initialState = viewModel.commanderDamageCausesLifeLoss,
                        toggle = { viewModel.toggleCommanderDamageCausesLifeLoss(it) },
                        icon = painterResource(id = R.drawable.placeholder_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Auto KO", initialState = viewModel.autoKo, toggle = { viewModel.toggleAutoKo(it) }, icon = painterResource(id = R.drawable.placeholder_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Keep Screen On (requires restart)",
                        initialState = viewModel.keepScreenOn,
                        toggle = { viewModel.toggleKeepScreenOn(it) },
                        icon = painterResource(id = R.drawable.placeholder_icon)
                    )
                }
            }
            item {
                SettingsDialogHeader(
                    text = "CONTACT"
                )
            }
            item {
                Column(
                    Modifier
                        .wrapContentSize()
                        .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                ) {
                    SettingsDialogButton(
                        text = "Submit Feedback", additionalText = "", icon = painterResource(id = R.drawable.placeholder_icon)
                    )
                    SettingsDialogButton(
                        text = "Write a Review", additionalText = "", icon = painterResource(id = R.drawable.placeholder_icon)
                    )
                    SettingsDialogButton(text = "About Me", additionalText = "", icon = painterResource(id = R.drawable.placeholder_icon), onTap = {
                        goToAboutMe()
                        addGoToSettingsToBackStack()
                    })
                    SettingsDialogButton(
                        text = "Buy me a Coffee", additionalText = "", icon = painterResource(id = R.drawable.placeholder_icon)
                    )
                }
            }

            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 10.dp), text = "<APP NAME> v1.0.0 by Nicholas Tietje", textAlign = TextAlign.Center, style = TextStyle(
                        fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                )
            }
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.5.dp),
                    text = "Card art & information powered by the Scryfall API",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                )
            }

        }
    }
}

@Composable
fun SettingsDialogHeader(
    modifier: Modifier = Modifier,
    text: String = "placeholder text",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.White.copy(alpha = 0.0f)), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Start
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp), text = text, style = TextStyle(
                fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}

@Composable
fun SettingsDialogButtonWithToggle(
    modifier: Modifier = Modifier, text: String = "placeholder text", initialState: Boolean, toggle: (Boolean) -> Unit, icon: Painter = painterResource(id = R.drawable.placeholder_icon)
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.White.copy(alpha = 0.2f))
            .border(0.5.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        SettingsButton(
            modifier = Modifier
                .padding(horizontal = 7.5.dp)
                .size(50.dp), shadowEnabled = false, imageResource = icon, enabled = false
        )
        Text(
            modifier = Modifier.fillMaxWidth(0.8f), text = text, style = TextStyle(
                fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            modifier = Modifier.padding(end = 30.dp), checked = initialState, onCheckedChange = {
                toggle(it)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }, colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary.blendWith(Color.Magenta).copy(alpha = 0.5f),
                checkedTrackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                uncheckedTrackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                checkedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                uncheckedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            )
        )
    }
}

@Composable
fun SettingsDialogButton(
    modifier: Modifier = Modifier,
    text: String = "placeholder text",
    additionalText: String = "additional",
    icon: Painter = painterResource(id = R.drawable.placeholder_icon),
    onTap: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.White.copy(alpha = 0.2f))
            .border(0.5.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    onTap()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                })
            }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start
    ) {
        SettingsButton(
            modifier = Modifier
                .padding(horizontal = 7.5.dp)
                .size(50.dp), shadowEnabled = false, imageResource = icon, enabled = false
        )
        Text(
            modifier = Modifier, text = text, style = TextStyle(
                fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.padding(end = 30.dp), text = additionalText, style = TextStyle(
                fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        )
    }
}