package composable.dialog

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.SettingsManager
import theme.blendWith
import theme.scaledSp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

/**
 * A dialog that allows the user to set global settings and view contact information
 * @param modifier the modifier for this composable
 * @param goToAboutMe the action to perform when the user selects the "About Me" option
 * @param addGoToSettingsToBackStack callback to add the "Settings" destination to the back stack
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun SettingsDialogContent(
    modifier: Modifier = Modifier, goToAboutMe: () -> Unit, addGoToSettingsToBackStack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

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
                        initialState = SettingsManager.rotatingMiddleButton,
                        toggle = { SettingsManager.rotatingMiddleButton = it },
                        icon = painterResource("d20_icon.xml")
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Fast Coin Flip",
                        initialState = SettingsManager.fastCoinFlip,
                        toggle = { SettingsManager.fastCoinFlip = it },
                        icon = painterResource("coin_icon.xml")
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Disable Camera Roll",
                        initialState = SettingsManager.cameraRollDisabled,
                        toggle = { SettingsManager.cameraRollDisabled = it },
                        icon = painterResource("invisible_icon.xml")
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Auto KO",
                        initialState = SettingsManager.autoKo,
                        toggle = { SettingsManager.autoKo = it },
                        icon = painterResource("skull_icon.xml")
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Auto Skip Player Select",
                        initialState = SettingsManager.autoSkip,
                        toggle = { SettingsManager.autoSkip = it },
                        icon = painterResource("player_select_icon.xml")
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Keep Screen On (requires restart)",
                        initialState = SettingsManager.keepScreenOn,
                        toggle = { SettingsManager.keepScreenOn = it },
                        icon = painterResource("sun_icon.xml")
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
                        text = "Submit Feedback", additionalText = "", icon = painterResource("thumbsup_icon.xml")
                    ) {
                        val url = "https://forms.gle/2rPor1Dvm2EtAkgo6"
                        uriHandler.openUri(url)
                    }
                    SettingsDialogButton(
                        text = "Write a Review", additionalText = "", icon = painterResource("star_icon_small.xml")
                    ) {
                        val url = "https://play.google.com/store/apps/details?id=com.hypeapps.lifelinked"
                        uriHandler.openUri(url)
                    }
                    SettingsDialogButton(
                        text = "Email", additionalText = "HypePixelSoftware@gmail.com", icon = painterResource("email_icon.xml")
                    ) {
                        val url = "mailto:hypepixelsoftware@gmail.com"
                        uriHandler.openUri(url)
                    }
                    SettingsDialogButton(
                        text = "About Me", additionalText = "", icon = painterResource("change_name_icon.xml")
                    ) {
                        goToAboutMe()
                        addGoToSettingsToBackStack()
                    }
                    SettingsDialogButton(
                        text = "Buy me a Coffee", additionalText = "", icon = painterResource("coffee_icon.xml")
                    ) {
                        val url = "https://venmo.com/u/Nicholas-Tietje"
                        uriHandler.openUri(url)
                    }
                    SettingsDialogButton(
                        text = "Privacy Policy", additionalText = "", icon = painterResource("visible_icon.xml")
                    ) {
                        val url = "https://sites.google.com/view/lifelinked-privacy-policy/"
                        uriHandler.openUri(url)
                    }
                }
            }

            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 10.dp),
                    text = "LifeLinked v1.0.3 by Nicholas Tietje",
                    textAlign = TextAlign.Center, style = TextStyle(
                        fontSize = 14.scaledSp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                )
            }
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 2.5.dp, bottom = 15.dp),
                    text = "Card art & information powered by the Scryfall API",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontSize = 14.scaledSp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    )
                )
            }

        }
    }
}

/**
 * Header composable
 * @param modifier the modifier for this composable
 * @param text the text to display
 */
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
                fontSize = 14.scaledSp, color = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}

/**
 * A settings button with a toggle
 * @param modifier the modifier for this composable
 * @param text the text to display
 * @param initialState the initial state of the toggle
 * @param toggle the action to perform when the toggle is toggled
 * @param icon the icon to display
 */
@Composable
fun SettingsDialogButtonWithToggle(
    modifier: Modifier = Modifier,
    text: String = "placeholder text",
    initialState: Boolean,
    toggle: (Boolean) -> Unit,
    icon: Painter
) {
    val haptic = LocalHapticFeedback.current
    val isChecked = remember { mutableStateOf(initialState) }

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
                .size(50.dp),
            shadowEnabled = false,
            imageResource = icon,
            enabled = false
        )
        Text(
            modifier = Modifier.fillMaxWidth(0.8f),
            text = text,
            style = TextStyle(
                fontSize = 16.scaledSp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            modifier = Modifier.padding(end = 30.dp),
            checked = isChecked.value,
            onCheckedChange = { checked ->
                isChecked.value = checked
                toggle(checked)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            colors = SwitchDefaults.colors(
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

/**
 * A settings button with an additional text field
 * @param modifier the modifier for this composable
 * @param text the text to display
 * @param additionalText the additional text to display
 * @param icon the icon to display
 * @param onTap the action to perform when the button is tapped
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun SettingsDialogButton(
    modifier: Modifier = Modifier,
    text: String = "placeholder text",
    additionalText: String = "additional",
    icon: Painter = painterResource("placeholder_icon.xml"),
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
                fontSize = 16.scaledSp, color = MaterialTheme.colorScheme.onPrimary
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.padding(end = 30.dp), text = additionalText, style = TextStyle(
                fontSize = 16.scaledSp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        )
    }
}