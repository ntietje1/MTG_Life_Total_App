package composable.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import composable.lifecounter.LifeCounterComponent
import data.SettingsManager
import data.SettingsManager.startingLife
import getAnimationCorrectionFactor
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.back_icon_alt
import lifelinked.shared.generated.resources.coin_icon
import lifelinked.shared.generated.resources.heart_solid_icon
import lifelinked.shared.generated.resources.mana_icon
import lifelinked.shared.generated.resources.moon_icon
import lifelinked.shared.generated.resources.planeswalker_icon
import lifelinked.shared.generated.resources.player_count_icon
import lifelinked.shared.generated.resources.player_select_icon
import lifelinked.shared.generated.resources.reset_icon
import lifelinked.shared.generated.resources.search_icon
import lifelinked.shared.generated.resources.settings_icon_small
import lifelinked.shared.generated.resources.six_icon
import lifelinked.shared.generated.resources.star_icon_small
import lifelinked.shared.generated.resources.sun_and_moon_icon
import lifelinked.shared.generated.resources.sun_icon
import lifelinked.shared.generated.resources.x_icon
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.vectorResource
import theme.scaledSp

/**
 * The possible states of the middle button dialog
 */
private enum class MiddleButtonDialogState {
    Default, CoinFlip, PlayerNumber, FourPlayerLayout, StartingLife, DiceRoll, Counter, Settings, Scryfall, AboutMe, PlaneChase, PlanarDeck
}

/**
 * A dialog that allows the user to quickly interact with settings or move to other screens
 * @param modifier the modifier for this composable
 * @param onDismiss the action to perform when the dialog is dismissed
 * @param component the LifeCounterComponent
 * @param toggleTheme the action to perform when the theme is toggled
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun MiddleButtonDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    component: LifeCounterComponent,
    toggleTheme: () -> Unit
) {

    var state by remember { mutableStateOf(MiddleButtonDialogState.Default) }
    val backStack = remember { mutableStateListOf(onDismiss) }
    val haptic = LocalHapticFeedback.current
    val duration = (450 / getAnimationCorrectionFactor()).toInt()
    var showResetDialog by remember { mutableStateOf(false) }

    val enterAnimation = slideInHorizontally(
        TweenSpec(
            duration,
            easing = LinearOutSlowInEasing
        )
    ) { (-it * 1.25).toInt() }
    val exitAnimation = slideOutHorizontally(
        TweenSpec(
            duration,
            easing = LinearOutSlowInEasing
        )
    ) { (it * 1.25).toInt() }

    @Composable
    fun FormattedAnimatedVisibility(
        visible: Boolean, content: @Composable () -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = enterAnimation,
            exit = exitAnimation
        ) {
            BoxWithConstraints(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.1f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                    ),
            ) {
                content()
            }
        }
    }

    val dialogContent: @Composable () -> Unit = {
//        BackHandler {
//            backStack.removeLast().invoke()
//        }
        BoxWithConstraints(
            modifier = modifier.fillMaxSize(),
        ) {
            val buttonModifier = Modifier.size(
                min(
                    maxWidth / 3,
                    maxHeight / 4
                )
            )
            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.CoinFlip
            ) {
                CoinFlipDialogContent(
                    modifier = modifier,
                    history = component.coinFlipHistory
                )
            }

            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.PlayerNumber
            ) {
                PlayerNumberDialogContent(
                    modifier = Modifier.fillMaxSize(),
                    onDismiss = onDismiss,
                    setPlayerNum = {
                        component.setNumPlayers(it)
                        component.resetPlayerStates()
                                   },
                    resetPlayers = { component.resetPlayerStates() },
                    show4PlayerDialog = { state = MiddleButtonDialogState.FourPlayerLayout }
                )
            }

            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.FourPlayerLayout
            ) {
                FourPlayerLayoutContent(
                    modifier = Modifier.fillMaxSize(),
                    onDismiss = onDismiss,
                    setPlayerNum = {
                        component.setNumPlayers(it)
                        component.resetPlayerStates()
                                   },
                    setAlt4PlayerLayout = { SettingsManager.alt4PlayerLayout = it }
                )
            }

            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.StartingLife
            ) {
                StartingLifeDialogContent(
                    modifier = Modifier.fillMaxSize(),
                    onDismiss = onDismiss,
                    setStartingLife = {
                        startingLife = it
                        component.resetPlayerStates()
                    }
                )
            }

            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.DiceRoll
            ) {
                DiceRollDialogContent(Modifier.fillMaxSize())
            }

            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.Counter
            ) {
                CounterDialogContent(
                    modifier = Modifier.fillMaxSize(),
                    counters = component.counters
                )
            }

            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.Scryfall
            ) {
                ScryfallDialogContent(
                    Modifier.fillMaxSize(),
                    player = null,
                    backStack = backStack,
                    selectButtonEnabled = false,
                    rulingsButtonEnabled = true
                )
            }

            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.Settings
            ) {
                SettingsDialogContent(Modifier.fillMaxSize(),
                    goToAboutMe = { state = MiddleButtonDialogState.AboutMe },
                    addGoToSettingsToBackStack = { backStack.add { state = MiddleButtonDialogState.Settings } })
            }
            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.AboutMe
            ) {
                AboutMeDialogContent(
                    Modifier.fillMaxSize()
                )
            }
            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.PlaneChase
            ) {
                PlaneChaseDialogContent(
                    modifier = Modifier.fillMaxSize(),
                    component = component,
                    goToChoosePlanes = {
                        state = MiddleButtonDialogState.PlanarDeck
                        backStack.add { state = MiddleButtonDialogState.PlaneChase }
                    }
                )
            }
            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.PlanarDeck
            ) {
                val choosePlanesActions = remember { ChoosePlanesActions(planarDeck = component.planarDeck, backStack = backStack, planarBackStack = component.planarBackStack) }
                ChoosePlanesDialogContent(
                    modifier = Modifier.fillMaxSize(),
                    actions = choosePlanesActions
                )
            }


            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.Default
            ) {
                GridDialogContent(
                    Modifier.fillMaxSize(),
                    title = "Settings",
                    items = listOf(
                        {
                            SettingsButton(
                                modifier = buttonModifier,
                                imageVector = vectorResource(Res.drawable.player_select_icon),
                                text = "Player Select",
                                shadowEnabled = false,
                                onPress = {
                                    component.goToPlayerSelectScreen()
                                    onDismiss()
                                })
                        },
                        {
                            SettingsButton(
                                modifier = buttonModifier,
                               imageVector = vectorResource(Res.drawable.reset_icon),
                                text = "Reset Game",
                                shadowEnabled = false,
                                onPress = {
                                    showResetDialog = true
                                })
                        },
                        {
                            SettingsButton(
                                modifier = buttonModifier,
                               imageVector = vectorResource(Res.drawable.heart_solid_icon),
                                text = "Starting Life",
                                shadowEnabled = false,
                                onPress = {
                                    state = MiddleButtonDialogState.StartingLife
                                    backStack.add { state = MiddleButtonDialogState.Default }
                                })
                        },
                        {
                            SettingsButton(
                                buttonModifier,
                               imageVector = vectorResource(Res.drawable.star_icon_small),
                                text = "Toggle Theme",
                                shadowEnabled = false,
                                onPress = {
                                    toggleTheme()
                                },
                            )
                        },
                        {
                            SettingsButton(
                                buttonModifier,
                               imageVector = vectorResource(Res.drawable.player_count_icon),
                                text = "Player Number",
                                shadowEnabled = false,
                                onPress = {
                                    state = MiddleButtonDialogState.PlayerNumber
                                    backStack.add { state = MiddleButtonDialogState.Default }
                                })
                        },
                        {
                            SettingsButton(
                                buttonModifier,
                               imageVector = vectorResource(Res.drawable.mana_icon),
                                text = "Mana & Storm",
                                shadowEnabled = false,
                                onPress = {
                                    state = MiddleButtonDialogState.Counter
                                    backStack.add { state = MiddleButtonDialogState.Default }
                                })
                        },
                        {
                            SettingsButton(
                                buttonModifier,
                               imageVector = vectorResource(Res.drawable.six_icon),
                                text = "Dice roll",
                                shadowEnabled = false,
                                onPress = {
                                    state = MiddleButtonDialogState.DiceRoll
                                    backStack.add { state = MiddleButtonDialogState.Default }
                                })
                        },
                        {
                            SettingsButton(
                                buttonModifier,
                               imageVector = vectorResource(Res.drawable.coin_icon),
                                text = "Coin Flip",
                                shadowEnabled = false,
                                onPress = {
                                    state = MiddleButtonDialogState.CoinFlip
                                    backStack.add { state = MiddleButtonDialogState.Default }
                                })
                        },
                        {
                            SettingsButton(buttonModifier,
                                imageVector = when (component.dayNight) {
                                    LifeCounterComponent.DayNightState.DAY -> vectorResource(Res.drawable.sun_icon)
                                    LifeCounterComponent.DayNightState.NIGHT -> vectorResource(Res.drawable.moon_icon)
                                    LifeCounterComponent.DayNightState.NONE -> vectorResource(Res.drawable.sun_and_moon_icon)
                                },
                                text = when (component.dayNight) {
                                    LifeCounterComponent.DayNightState.DAY -> "Day/Night"
                                    LifeCounterComponent.DayNightState.NIGHT -> "Day/Night"
                                    LifeCounterComponent.DayNightState.NONE -> "Day/Night"
                                },
                                shadowEnabled = false,
                                onPress = {
                                    component.toggleDayNight()
                                },
                                onLongPress = {
                                    component.dayNight = LifeCounterComponent.DayNightState.NONE
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                })
                        },
                        {
                            SettingsButton(buttonModifier,
                               imageVector = vectorResource(Res.drawable.search_icon),
                                text = "Card Search",
                                shadowEnabled = false,
                                onPress = {
                                    state = MiddleButtonDialogState.Scryfall
                                    backStack.add { state = MiddleButtonDialogState.Default }
                                })
                        },
                        {
                            SettingsButton(buttonModifier,
                               imageVector = vectorResource(Res.drawable.planeswalker_icon),
                                text = "Planechase",
                                shadowEnabled = false,
                                onPress = {
                                    state = MiddleButtonDialogState.PlaneChase
                                    backStack.add { state = MiddleButtonDialogState.Default }
                                })
                        },
                        {
                            SettingsButton(buttonModifier,
                               imageVector = vectorResource(Res.drawable.settings_icon_small),
                                text = "Settings",
                                shadowEnabled = false,
                                onPress = {
                                    state = MiddleButtonDialogState.Settings
                                    backStack.add { state = MiddleButtonDialogState.Default }
                                })
                        })
                )
            }
        }
    }

    if (showResetDialog) {
        WarningDialog(
            onDismiss = { showResetDialog = false },
            title = "Reset Game",
            message = "Would you like to start a new game with the same players, or reset all customizations as well?",
            optionOneMessage = "New game, same players",
            optionTwoMessage = "New game, different players",
            onOptionOne = {
                component.resetPlayerStates()
                showResetDialog = false
                onDismiss()
            },
            onOptionTwo = {
                component.resetCustomizations()
                component.resetPlayerStates()
                showResetDialog = false
                onDismiss()
            },
        )
    }

    SettingsDialog(
        onDismiss = {
            onDismiss()
        },
        content = dialogContent,
        onBack = {
            backStack.removeLast().invoke()
        })
}

/**
 * A generic dialog for displaying a grid of buttons
 * @param modifier the modifier for this composable
 * @param title the title of the dialog
 * @param items the composable items to display in the grid
 */
@Composable
fun GridDialogContent(
    modifier: Modifier = Modifier, title: String, items: List<@Composable () -> Unit> = emptyList()
) {
    Box(modifier = modifier) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.05f))
            Text(
                modifier = Modifier
                    .wrapContentHeight()
                    .wrapContentWidth(),
                text = title,
                fontSize = 25.scaledSp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.weight(0.025f))
            LazyVerticalGrid(modifier = Modifier
                .padding(horizontal = 10.dp)
                .wrapContentSize(),
                columns = GridCells.Fixed(3),
                content = {
                    items(items.size) { index ->
                        items[index]()
                    }
                })
            Spacer(modifier = Modifier.weight(0.075f))
        }
    }
}

/**
 * A base dialog for displaying settings within
 * @param modifier the modifier for this composable
 * @param onDismiss the action to perform when the dialog is dismissed
 * @param onBack the action to perform when the back button is pressed
 * @param exitButtonEnabled whether the exit button is enabled
 * @param backButtonEnabled whether the back button is enabled
 * @param content the content of the dialog
 */
@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onBack: () -> Unit = {},
    exitButtonEnabled: Boolean = true,
    backButtonEnabled: Boolean = true,
    content: @Composable () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            val buttonSize = maxHeight / 15f

            Column(Modifier.fillMaxSize()) {
                if (exitButtonEnabled) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.End
                    ) {

                        ExitButton(
                            Modifier.size(buttonSize),
                            onDismiss = onDismiss,
                            visible = exitButtonEnabled
                        )
                    }
                }
                Box(
                    Modifier.weight(0.1f).padding(5.dp)
                ) {
                    content()
                }

                if (backButtonEnabled) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BackButton(
                            Modifier.size(buttonSize),
                            onBack = onBack,
                            visible = backButtonEnabled
                        )
                    }
                }

            }
        }
    }
}

/**
 * Back button for use in dialogs
 * @param modifier the modifier for this composable
 * @param visible whether the button is visible
 * @param onBack the action to perform when the button is pressed
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun BackButton(modifier: Modifier = Modifier, visible: Boolean, onBack: () -> Unit) {
    SettingsButton(
        modifier = modifier,
        backgroundColor = Color.Transparent,
        text = "",
        visible = visible,
        shadowEnabled = false,
       imageVector = vectorResource(Res.drawable.back_icon_alt),
        onTap = onBack
    )
}

/**
 * Exit button for use in dialogs
 * @param modifier the modifier for this composable
 * @param visible whether the button is visible
 * @param onDismiss the action to perform when the button is pressed
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun ExitButton(modifier: Modifier = Modifier, visible: Boolean, onDismiss: () -> Unit) {
    SettingsButton(
        modifier = modifier,

        backgroundColor = Color.Transparent,
        text = "",
        visible = visible,
        shadowEnabled = false,
       imageVector = vectorResource(Res.drawable.x_icon),
        onTap = onDismiss
    )
}





