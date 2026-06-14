package ui.dialog.middle

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.coin_icon
import lifelinked.shared.generated.resources.die_icon
import lifelinked.shared.generated.resources.heart_solid_icon
import lifelinked.shared.generated.resources.mana_icon
import lifelinked.shared.generated.resources.moon_icon
import lifelinked.shared.generated.resources.planeswalker_icon
import lifelinked.shared.generated.resources.player_count_icon
import lifelinked.shared.generated.resources.player_select_icon
import lifelinked.shared.generated.resources.reset_icon
import lifelinked.shared.generated.resources.search_icon
import lifelinked.shared.generated.resources.settings_icon_small
import lifelinked.shared.generated.resources.star_icon_small
import lifelinked.shared.generated.resources.sun_and_moon_icon
import lifelinked.shared.generated.resources.sun_icon
import org.jetbrains.compose.resources.vectorResource
import ui.components.SettingsButton
import ui.dialog.GridDialogContent
import ui.lifecounter.DayNightState

@Composable
fun MiddleButtonMenuDialog(
    modifier: Modifier = Modifier,
    dayNightState: DayNightState,
    onPlayerSelect: () -> Unit,
    onResetGame: () -> Unit,
    onStartingLife: () -> Unit,
    onToggleTheme: () -> Unit,
    onPlayerNumber: () -> Unit,
    onCounters: () -> Unit,
    onDiceRoll: () -> Unit,
    onCoinFlip: () -> Unit,
    onToggleDayNight: () -> Unit,
    onClearDayNight: () -> Unit,
    onCardSearch: () -> Unit,
    onPlanechase: () -> Unit,
    onSettings: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier) {
        val buttonSize4x3 = minOf(maxWidth / 4f, maxHeight / 3f)
        val buttonSize3x4 = minOf(maxHeight / 4f, maxWidth / 3f)

        val numColumns = remember(Unit) {
            if (buttonSize3x4 * 4 < maxHeight * 0.9f) 3 else 4
        }
        val buttonModifier = remember(Unit) {
            if (buttonSize3x4 * 4 < maxHeight * 0.9f) {
                Modifier.size(buttonSize4x3)
            } else {
                Modifier.size(buttonSize3x4)
            }
        }

        GridDialogContent(
            Modifier.fillMaxSize(),
            title = "Settings",
            columns = numColumns,
            items = listOf(
                {
                    SettingsButton(
                        modifier = buttonModifier,
                        imageVector = vectorResource(Res.drawable.player_select_icon),
                        text = "Player Select",
                        shadowEnabled = false,
                        onPress = onPlayerSelect
                    )
                },
                {
                    SettingsButton(
                        modifier = buttonModifier,
                        imageVector = vectorResource(Res.drawable.reset_icon),
                        text = "Reset Game",
                        shadowEnabled = false,
                        onPress = onResetGame
                    )
                },
                {
                    SettingsButton(
                        modifier = buttonModifier,
                        imageVector = vectorResource(Res.drawable.heart_solid_icon),
                        text = "Starting Life",
                        shadowEnabled = false,
                        onPress = onStartingLife
                    )
                },
                {
                    SettingsButton(
                        modifier = buttonModifier,
                        imageVector = vectorResource(Res.drawable.star_icon_small),
                        text = "Toggle Theme",
                        shadowEnabled = false,
                        onPress = onToggleTheme
                    )
                },
                {
                    SettingsButton(
                        modifier = buttonModifier,
                        imageVector = vectorResource(Res.drawable.player_count_icon),
                        text = "Player Number",
                        shadowEnabled = false,
                        onPress = onPlayerNumber
                    )
                },
                {
                    SettingsButton(
                        modifier = buttonModifier,
                        imageVector = vectorResource(Res.drawable.mana_icon),
                        text = "Mana & Storm",
                        shadowEnabled = false,
                        onPress = onCounters
                    )
                },
                {
                    SettingsButton(
                        modifier = buttonModifier,
                        imageVector = vectorResource(Res.drawable.die_icon),
                        text = "Dice roll",
                        shadowEnabled = false,
                        onPress = onDiceRoll
                    )
                },
                {
                    SettingsButton(
                        modifier = buttonModifier,
                        imageVector = vectorResource(Res.drawable.coin_icon),
                        text = "Coin Flip",
                        shadowEnabled = false,
                        onPress = onCoinFlip
                    )
                },
                {
                    SettingsButton(
                        modifier = buttonModifier,
                        imageVector = when (dayNightState) {
                            DayNightState.DAY -> vectorResource(Res.drawable.sun_icon)
                            DayNightState.NIGHT -> vectorResource(Res.drawable.moon_icon)
                            DayNightState.NONE -> vectorResource(Res.drawable.sun_and_moon_icon)
                        },
                        text = "Day/Night",
                        shadowEnabled = false,
                        onPress = onToggleDayNight,
                        onLongPress = {
                            onClearDayNight()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                },
                {
                    SettingsButton(
                        modifier = buttonModifier,
                        imageVector = vectorResource(Res.drawable.search_icon),
                        text = "Card Search",
                        shadowEnabled = false,
                        onPress = onCardSearch
                    )
                },
                {
                    SettingsButton(
                        modifier = buttonModifier,
                        imageVector = vectorResource(Res.drawable.planeswalker_icon),
                        text = "Planechase",
                        shadowEnabled = false,
                        onPress = onPlanechase
                    )
                },
                {
                    SettingsButton(
                        modifier = buttonModifier,
                        imageVector = vectorResource(Res.drawable.settings_icon_small),
                        text = "Settings",
                        shadowEnabled = false,
                        onPress = onSettings
                    )
                }
            )
        )
    }
}
