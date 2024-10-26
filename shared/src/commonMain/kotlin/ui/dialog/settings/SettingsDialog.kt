package ui.dialog.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.SettingsManager
import di.Platform
import di.VersionNumber
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.change_name_icon
import lifelinked.shared.generated.resources.coffee_icon
import lifelinked.shared.generated.resources.coin_icon
import lifelinked.shared.generated.resources.d20_icon
import lifelinked.shared.generated.resources.email_icon
import lifelinked.shared.generated.resources.invisible_icon
import lifelinked.shared.generated.resources.player_select_icon
import lifelinked.shared.generated.resources.reset_icon
import lifelinked.shared.generated.resources.skull_icon
import lifelinked.shared.generated.resources.star_icon_small
import lifelinked.shared.generated.resources.sun_icon
import lifelinked.shared.generated.resources.thumbsup_icon
import lifelinked.shared.generated.resources.timer_icon
import lifelinked.shared.generated.resources.visible_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.MainColorLight
import theme.defaultTextStyle
import theme.scaledSp
import ui.SettingsButton

@Composable
fun SettingsDialogContent(
    modifier: Modifier = Modifier,
    goToAboutMe: () -> Unit,
    goToPatchNotes: () -> Unit,
    addGoToSettingsToBackStack: () -> Unit,
    goToTutorialScreen: () -> Unit,
    toggleKeepScreenOn: () -> Unit,
    updateTurnTimerEnabled: (Boolean) -> Unit,
    settingsManager: SettingsManager = koinInject(),
    platform: Platform = koinInject(),
    version: VersionNumber = koinInject()
) {
    val uriHandler = LocalUriHandler.current

    BoxWithConstraints(modifier) {
        val buttonHeight = remember(Unit) { 10.dp + maxWidth / 10f }
        val textSize = remember(Unit) { (maxWidth / 30f).value }
        val smallPadding = remember(Unit) { maxWidth / 50f }
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                SettingsDialogHeader(
                    modifier = Modifier.fillMaxWidth().height(buttonHeight),
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
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "Fast Coin Flip",
                        initialState = settingsManager.fastCoinFlip,
                        toggle = { settingsManager.fastCoinFlip = it },
                        icon = vectorResource(Res.drawable.coin_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "Disable Camera Roll",
                        initialState = settingsManager.cameraRollDisabled,
                        toggle = { settingsManager.cameraRollDisabled = it },
                        icon = vectorResource(Res.drawable.invisible_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "Auto KO",
                        initialState = settingsManager.autoKo,
                        toggle = { settingsManager.autoKo = it },
                        icon = vectorResource(Res.drawable.skull_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "Auto Skip Player Select",
                        initialState = settingsManager.autoSkip,
                        toggle = { settingsManager.autoSkip = it },
                        icon = vectorResource(Res.drawable.player_select_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "Keep Screen On",
                        initialState = settingsManager.keepScreenOn,
                        toggle = {
                            toggleKeepScreenOn()
                            settingsManager.keepScreenOn = it
                        },
                        icon = vectorResource(Res.drawable.sun_icon)
                    )
                    SettingsDialogButtonWithToggle(
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "Turn Timer",
                        initialState = settingsManager.turnTimer.value,
                        toggle = {
                            settingsManager.setTurnTimer(it)
//                            settingsManager.turnTimer = it
                            updateTurnTimerEnabled(it)
                        },
                        icon = vectorResource(Res.drawable.timer_icon)
                    )
                    SettingsDialogButton(
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "Patch Notes", icon = vectorResource(Res.drawable.d20_icon),
                    ) {
                        goToPatchNotes()
                        addGoToSettingsToBackStack()
                    }
                    SettingsDialogButton(
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "View Tutorial Again", icon = vectorResource(Res.drawable.reset_icon),
                    ) {
                        goToTutorialScreen()
                    }
                }
            }
            item {
                SettingsDialogHeader(
                    modifier = Modifier.fillMaxWidth().height(buttonHeight),
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
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "Submit Feedback", icon = vectorResource(Res.drawable.thumbsup_icon)
                    ) {
                        val url = "https://forms.gle/2rPor1Dvm2EtAkgo6"
                        uriHandler.openUri(url)
                    }
                    SettingsDialogButton(
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "Write a Review", icon = vectorResource(Res.drawable.star_icon_small)
                    ) {
                        val url = platform.appStoreListing
                        uriHandler.openUri(url)
                    }
                    SettingsDialogButton(
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "Email", icon = vectorResource(Res.drawable.email_icon)
                    ) {
                        val url = "mailto:hypepixelsoftware@gmail.com"
                        uriHandler.openUri(url)
                    }
                    SettingsDialogButton(
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "About Me", icon = vectorResource(Res.drawable.change_name_icon)
                    ) {
                        goToAboutMe()
                        addGoToSettingsToBackStack()
                    }
                    if (platform != Platform.IOS) {
                        SettingsDialogButton(
                            modifier = Modifier.fillMaxWidth().height(buttonHeight),
                            text = "Buy me a Coffee", icon = vectorResource(Res.drawable.coffee_icon)
                        ) {
                            val url = "https://www.buymeacoffee.com/hypepixel"
                            uriHandler.openUri(url)
                        }
                    }
                    SettingsDialogButton(
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "Privacy Policy", icon = vectorResource(Res.drawable.visible_icon)
                    ) {
                        val url = "https://sites.google.com/view/lifelinked/privacy-policy"
                        uriHandler.openUri(url)
                    }
                }
            }

            if (settingsManager.devMode && false) {
                item {
                    SettingsDialogHeader(
                        modifier = Modifier.fillMaxWidth().height(buttonHeight),
                        text = "SECRET OPTIONS"
                    )
                }
                item {
                    Column(
                        Modifier
                            .wrapContentSize()
                            .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                    ) {
                       //TODO:
                        // dvd logo bouncing

                    }
                }

            }

            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = smallPadding, end = smallPadding, top = smallPadding),
                    text = "LifeLinked v${version.value} by Nicholas Tietje",
                    fontSize = textSize.scaledSp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    style = defaultTextStyle()
                )
            }
            item {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = smallPadding, end = smallPadding, top = smallPadding / 4f, bottom = smallPadding * 1.5f),
                    text = "Card art & information powered by the Scryfall API",
                    fontSize = textSize.scaledSp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    style = defaultTextStyle()
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
    BoxWithConstraints(
        modifier
            .background(Color.White.copy(alpha = 0.0f)), contentAlignment = Alignment.BottomStart
    ) {
        val textSize = remember(Unit) { (maxWidth / 24f).value }
        val padding = remember(Unit) { maxWidth / 35f }
        Text(
            modifier = Modifier.padding(padding),
            text = text,
            fontSize = textSize.scaledSp,
            color = MaterialTheme.colorScheme.onPrimary,
            style = defaultTextStyle()
        )
    }
}

@Composable
fun SettingsDialogButton(
    modifier: Modifier = Modifier,
    text: String = "placeholder text",
    icon: ImageVector,
    hapticEnabled: Boolean = true,
    onTap: () -> Unit = {},
) {
    SettingsDialogButtonWithToggle(
        modifier = modifier,
        text = text,
        onTap = onTap,
        hapticEnabled = hapticEnabled,
        toggleVisible = false,
        initialState = false,
        toggle = {},
        icon = icon
    )
}

@Composable
fun SettingsDialogButtonWithToggle(
    modifier: Modifier = Modifier,
    text: String = "placeholder text",
    onTap: () -> Unit = {},
    hapticEnabled: Boolean = false,
    toggleVisible: Boolean = true,
    initialState: Boolean = false,
    icon: ImageVector,
    toggle: (Boolean) -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val isChecked = remember { mutableStateOf(initialState) }

    BoxWithConstraints(Modifier.wrapContentSize()) {
        val iconSize = remember(Unit) { 10.dp + maxHeight / 3f }
        val padding = remember(Unit) { 5.dp + maxWidth / 35f }
        val textSize = remember(Unit) { 7 + (maxWidth / 32f).value }
        val toggleScale = remember(Unit) { (maxWidth.value + 5) / 500.dp.value }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.2f))
                .border(0.5.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        onTap()
                        if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    })
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(Modifier.wrapContentSize().padding(horizontal = padding / 2f, vertical = padding / 4f)) {
                SettingsButton(
                    modifier = Modifier
                        .size(iconSize),
                    shadowEnabled = false,
                    imageVector = icon,
                    enabled = false
                )
            }
            Text(
                modifier = Modifier.fillMaxWidth(0.8f).padding(start = padding / 4f),
                text = text,
                fontSize = textSize.scaledSp,
                color = MaterialTheme.colorScheme.onPrimary,
                style = defaultTextStyle()
            )
            Spacer(modifier = Modifier.weight(1f))
            if (toggleVisible) {
                Switch(
                    modifier = Modifier.scale(toggleScale).padding(end = padding),
                    checked = isChecked.value,
                    onCheckedChange = { checked ->
                        isChecked.value = checked
                        toggle(checked)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MainColorLight.copy(alpha = 0.5f),
                        checkedTrackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        uncheckedTrackColor = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
                        checkedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        uncheckedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    )
                )
            }
        }
    }
}