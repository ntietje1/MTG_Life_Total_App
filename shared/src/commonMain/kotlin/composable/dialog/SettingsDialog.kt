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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.SettingsManager
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.change_name_icon
import lifelinked.shared.generated.resources.coffee_icon
import lifelinked.shared.generated.resources.coin_icon
import lifelinked.shared.generated.resources.email_icon
import lifelinked.shared.generated.resources.invisible_icon
import lifelinked.shared.generated.resources.placeholder_icon
import lifelinked.shared.generated.resources.player_select_icon
import lifelinked.shared.generated.resources.skull_icon
import lifelinked.shared.generated.resources.star_icon_small
import lifelinked.shared.generated.resources.sun_icon
import lifelinked.shared.generated.resources.thumbsup_icon
import lifelinked.shared.generated.resources.visible_icon
import org.jetbrains.compose.resources.vectorResource
import theme.blendWith
import theme.scaledSp

/**
 * A dialog that allows the user to set global settings and view contact information
 * @param modifier the modifier for this composable
 * @param goToAboutMe the action to perform when the user selects the "About Me" option
 * @param addGoToSettingsToBackStack callback to add the "Settings" destination to the back stack
 */
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
                        text = "Fast Coin Flip",
                        initialState = SettingsManager.fastCoinFlip,
                        toggle = { SettingsManager.fastCoinFlip = it },
                        icon = vectorResource(Res.drawable.coin_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Disable Camera Roll",
                        initialState = SettingsManager.cameraRollDisabled,
                        toggle = { SettingsManager.cameraRollDisabled = it },
                        icon = vectorResource(Res.drawable.invisible_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Auto KO",
                        initialState = SettingsManager.autoKo,
                        toggle = { SettingsManager.autoKo = it },
                        icon = vectorResource(Res.drawable.skull_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Auto Skip Player Select",
                        initialState = SettingsManager.autoSkip,
                        toggle = { SettingsManager.autoSkip = it },
                        icon = vectorResource(Res.drawable.player_select_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        text = "Keep Screen On (requires restart)",
                        initialState = SettingsManager.keepScreenOn,
                        toggle = { SettingsManager.keepScreenOn = it },
                        icon = vectorResource(Res.drawable.sun_icon)
                    )
//                    SettingsDialogButton(
//                        text = "View Tutorial Again", additionalText = "", icon = vectorResource(Res.drawable.reset_icon)
//                    ) {
////                        SettingsManager.tutorialCompleted = false
//                    }
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
                        text = "Submit Feedback", additionalText = "", icon = vectorResource(Res.drawable.thumbsup_icon)
                    ) {
                        val url = "https://forms.gle/2rPor1Dvm2EtAkgo6"
                        uriHandler.openUri(url)
                    }
                    SettingsDialogButton(
                        text = "Write a Review", additionalText = "", icon = vectorResource(Res.drawable.star_icon_small)
                    ) {
                        val url = "https://play.google.com/store/apps/details?id=com.hypeapps.lifelinked"
                        uriHandler.openUri(url)
                    }
                    SettingsDialogButton(
                        text = "Email", additionalText = "HypePixelSoftware@gmail.com", icon = vectorResource(Res.drawable.email_icon)
                    ) {
                        val url = "mailto:hypepixelsoftware@gmail.com"
                        uriHandler.openUri(url)
                    }
                    SettingsDialogButton(
                        text = "About Me", additionalText = "", icon = vectorResource(Res.drawable.change_name_icon)
                    ) {
                        goToAboutMe()
                        addGoToSettingsToBackStack()
                    }
                    SettingsDialogButton(
                        text = "Buy me a Coffee", additionalText = "", icon = vectorResource(Res.drawable.coffee_icon)
                    ) {
                        val url = "https://www.buymeacoffee.com/hypepixel"
                        uriHandler.openUri(url)
                    }
                    SettingsDialogButton(
                        text = "Privacy Policy", additionalText = "", icon = vectorResource(Res.drawable.visible_icon)
                    ) {
                        val url = "https://sites.google.com/view/lifelinked/privacy-policy"
                        uriHandler.openUri(url)
                    }
                }
            }

            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 10.dp),
                    text = "LifeLinked v1.4 by Nicholas Tietje",
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
    icon: ImageVector
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
            imageVector = icon,
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
@Composable
fun SettingsDialogButton(
    modifier: Modifier = Modifier,
    text: String = "placeholder text",
    additionalText: String = "additional",
    icon: ImageVector = vectorResource(Res.drawable.placeholder_icon),
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
                .size(50.dp), shadowEnabled = false, imageVector = icon, enabled = false
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