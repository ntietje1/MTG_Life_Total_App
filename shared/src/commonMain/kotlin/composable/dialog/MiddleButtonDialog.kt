package composable.dialog

import BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import composable.SettingsButton
import composable.dialog.planechase.ChoosePlanesDialogContent
import composable.dialog.planechase.PlaneChaseDialogContent
import composable.lifecounter.DayNightState
import composable.lifecounter.LifeCounterViewModel
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
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.scaledSp

private enum class MiddleButtonDialogState {
    Default, CoinFlip, PlayerNumber, FourPlayerLayout, StartingLife, DiceRoll, Counter, Settings, Scryfall, AboutMe, PlaneChase, PlanarDeck
}

@Composable
fun MiddleButtonDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: LifeCounterViewModel,
    toggleTheme: () -> Unit,
    toggleKeepScreenOn: () -> Unit,
    goToPlayerSelectScreen: () -> Unit,
    triggerEnterAnimation: () -> Unit,
    setNumPlayers: (Int) -> Unit,
    goToTutorialScreen: () -> Unit,
    backHandler: BackHandler = koinInject()
) {

    val state by viewModel.state.collectAsState()
    var middleButtonDialogState by remember { mutableStateOf(MiddleButtonDialogState.Default) }
    val haptic = LocalHapticFeedback.current
    val duration = (450 / getAnimationCorrectionFactor()).toInt()
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        backHandler.push { onDismiss() }
    }

    val enterAnimation = slideInHorizontally(
        TweenSpec(
            duration, easing = LinearOutSlowInEasing
        )
    ) { (-it * 1.25).toInt() }
    val exitAnimation = slideOutHorizontally(
        TweenSpec(
            duration, easing = LinearOutSlowInEasing
        )
    ) { (it * 1.25).toInt() }

    @Composable
    fun FormattedAnimatedVisibility(
        visible: Boolean, content: @Composable () -> Unit
    ) {
        AnimatedVisibility(
            visible = visible, enter = enterAnimation, exit = exitAnimation
        ) {
            BoxWithConstraints(
                modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background.copy(alpha = 0.1f)).border(
                    1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                ),
            ) {
                content()
            }
        }
    }

    val dialogContent: @Composable () -> Unit = {

        BoxWithConstraints(
            modifier = modifier.fillMaxSize(),
        ) {
            val buttonModifier = Modifier.size(
                min(
                    maxWidth / 3, maxHeight / 4
                )
            )
            FormattedAnimatedVisibility(
                visible = middleButtonDialogState == MiddleButtonDialogState.CoinFlip
            ) {
                CoinFlipDialogContent(
                    modifier = modifier,
                    history = state.coinFlipHistory,
                    addToHistory = { viewModel.addToCoinFlipHistory(it) },
                    resetHistory = { viewModel.resetCoinFlipHistory() },
                    fastCoinFlip = viewModel.settingsManager.fastCoinFlip
                )
            }

            FormattedAnimatedVisibility(
                visible = middleButtonDialogState == MiddleButtonDialogState.PlayerNumber
            ) {
                PlayerNumberDialogContent(modifier = Modifier.fillMaxSize(), onDismiss = onDismiss, setPlayerNum = {
                    setNumPlayers(it)
                    viewModel.resetPlayerStates()
                    triggerEnterAnimation()
                }, resetPlayers = {
                    viewModel.resetPlayerStates()
                    triggerEnterAnimation()
                }, show4PlayerDialog = { middleButtonDialogState = MiddleButtonDialogState.FourPlayerLayout })
            }

            FormattedAnimatedVisibility(
                visible = middleButtonDialogState == MiddleButtonDialogState.FourPlayerLayout
            ) {
                FourPlayerLayoutContent(modifier = Modifier.fillMaxSize(), onDismiss = onDismiss, setPlayerNum = {
                    setNumPlayers(it)
                    viewModel.resetPlayerStates()
                    triggerEnterAnimation()
                }, setAlt4PlayerLayout = { viewModel.settingsManager.alt4PlayerLayout = it })
            }

            FormattedAnimatedVisibility(
                visible = middleButtonDialogState == MiddleButtonDialogState.StartingLife
            ) {
                StartingLifeDialogContent(modifier = Modifier.fillMaxSize(), onDismiss = onDismiss, setStartingLife = {
                    viewModel.settingsManager.startingLife = it
                    viewModel.resetPlayerStates()
                    triggerEnterAnimation()
                })
            }

            FormattedAnimatedVisibility(
                visible = middleButtonDialogState == MiddleButtonDialogState.DiceRoll
            ) {
                DiceRollDialogContent(Modifier.fillMaxSize())
            }

            FormattedAnimatedVisibility(
                visible = middleButtonDialogState == MiddleButtonDialogState.Counter
            ) {
                CounterDialogContent(modifier = Modifier.fillMaxSize(),
                    counters = state.counters,
                    incrementCounter = { index, value -> viewModel.incrementCounter(index, value) },
                    resetCounters = { viewModel.resetCounters() })
            }

            FormattedAnimatedVisibility(
                visible = middleButtonDialogState == MiddleButtonDialogState.Scryfall
            ) {
                ScryfallDialogContent(
                    Modifier.fillMaxSize(),
                    selectButtonEnabled = false,
                    rulingsButtonEnabled = true,
                    addToBackStack = { backHandler.push(it) },
                    onImageSelected = {}
                )
            }

            FormattedAnimatedVisibility(
                visible = middleButtonDialogState == MiddleButtonDialogState.Settings
            ) {
                SettingsDialogContent(
                    Modifier.fillMaxSize(),
                    goToAboutMe = { middleButtonDialogState = MiddleButtonDialogState.AboutMe },
                    addGoToSettingsToBackStack = { backHandler.push { middleButtonDialogState = MiddleButtonDialogState.Settings } },
                    goToTutorialScreen = {
                        onDismiss()
                        goToTutorialScreen()
                    },
                    toggleKeepScreenOn = toggleKeepScreenOn
                )
            }
            FormattedAnimatedVisibility(
                visible = middleButtonDialogState == MiddleButtonDialogState.AboutMe
            ) {
                AboutMeDialogContent(
                    Modifier.fillMaxSize()
                )
            }
            FormattedAnimatedVisibility(
                visible = middleButtonDialogState == MiddleButtonDialogState.PlaneChase
            ) {
                PlaneChaseDialogContent(
                    modifier = Modifier.fillMaxSize(),
                    goToChoosePlanes = {
                        middleButtonDialogState = MiddleButtonDialogState.PlanarDeck
                        backHandler.push { middleButtonDialogState = MiddleButtonDialogState.PlaneChase }
                    },
                )
            }
            FormattedAnimatedVisibility(
                visible = middleButtonDialogState == MiddleButtonDialogState.PlanarDeck
            ) {
                ChoosePlanesDialogContent(
                    modifier = Modifier.fillMaxSize(),
                    addToBackStack = backHandler::push,
                    popBackStack = backHandler::pop
                )
            }


            FormattedAnimatedVisibility(
                visible = middleButtonDialogState == MiddleButtonDialogState.Default
            ) {
                GridDialogContent(
                    Modifier.fillMaxSize(), title = "Settings", items = listOf({
                        SettingsButton(modifier = buttonModifier, imageVector = vectorResource(Res.drawable.player_select_icon), text = "Player Select", shadowEnabled = false, onPress = {
                            goToPlayerSelectScreen()
                            onDismiss()
                        })
                    }, {
                        SettingsButton(modifier = buttonModifier, imageVector = vectorResource(Res.drawable.reset_icon), text = "Reset Game", shadowEnabled = false, onPress = {
                            showResetDialog = true
                        })
                    }, {
                        SettingsButton(modifier = buttonModifier, imageVector = vectorResource(Res.drawable.heart_solid_icon), text = "Starting Life", shadowEnabled = false, onPress = {
                            middleButtonDialogState = MiddleButtonDialogState.StartingLife
                            backHandler.push { middleButtonDialogState = MiddleButtonDialogState.Default }
                        })
                    }, {
                        SettingsButton(
                            buttonModifier,
                            imageVector = vectorResource(Res.drawable.star_icon_small),
                            text = "Toggle Theme",
                            shadowEnabled = false,
                            onPress = {
                                toggleTheme()
                            },
                        )
                    }, {
                        SettingsButton(buttonModifier, imageVector = vectorResource(Res.drawable.player_count_icon), text = "Player Number", shadowEnabled = false, onPress = {
                            middleButtonDialogState = MiddleButtonDialogState.PlayerNumber
                            backHandler.push { middleButtonDialogState = MiddleButtonDialogState.Default }
                        })
                    }, {
                        SettingsButton(buttonModifier, imageVector = vectorResource(Res.drawable.mana_icon), text = "Mana & Storm", shadowEnabled = false, onPress = {
                            middleButtonDialogState = MiddleButtonDialogState.Counter
                            backHandler.push { middleButtonDialogState = MiddleButtonDialogState.Default }
                        })
                    }, {
                        SettingsButton(buttonModifier, imageVector = vectorResource(Res.drawable.six_icon), text = "Dice roll", shadowEnabled = false, onPress = {
                            middleButtonDialogState = MiddleButtonDialogState.DiceRoll
                            backHandler.push { middleButtonDialogState = MiddleButtonDialogState.Default }
                        })
                    }, {
                        SettingsButton(buttonModifier, imageVector = vectorResource(Res.drawable.coin_icon), text = "Coin Flip", shadowEnabled = false, onPress = {
                            middleButtonDialogState = MiddleButtonDialogState.CoinFlip
                            backHandler.push { middleButtonDialogState = MiddleButtonDialogState.Default }
                        })
                    }, {
                        SettingsButton(buttonModifier, imageVector = when (state.dayNight) {
                            DayNightState.DAY -> vectorResource(Res.drawable.sun_icon)
                            DayNightState.NIGHT -> vectorResource(Res.drawable.moon_icon)
                            DayNightState.NONE -> vectorResource(Res.drawable.sun_and_moon_icon)
                        }, text = when (state.dayNight) {
                            DayNightState.DAY -> "Day/Night"
                            DayNightState.NIGHT -> "Day/Night"
                            DayNightState.NONE -> "Day/Night"
                        }, shadowEnabled = false, onPress = {
                            viewModel.toggleDayNight()
                        }, onLongPress = {
                            viewModel.setDayNight(DayNightState.NONE)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        })
                    }, {
                        SettingsButton(buttonModifier, imageVector = vectorResource(Res.drawable.search_icon), text = "Card Search", shadowEnabled = false, onPress = {
                            middleButtonDialogState = MiddleButtonDialogState.Scryfall
                            backHandler.push { middleButtonDialogState = MiddleButtonDialogState.Default }
                        })
                    }, {
                        SettingsButton(buttonModifier, imageVector = vectorResource(Res.drawable.planeswalker_icon), text = "Planechase", shadowEnabled = false, onPress = {
                            middleButtonDialogState = MiddleButtonDialogState.PlaneChase
                            backHandler.push { middleButtonDialogState = MiddleButtonDialogState.Default }
                        })
                    }, {
                        SettingsButton(buttonModifier, imageVector = vectorResource(Res.drawable.settings_icon_small), text = "Settings", shadowEnabled = false, onPress = {
                            middleButtonDialogState = MiddleButtonDialogState.Settings
                            backHandler.push { middleButtonDialogState = MiddleButtonDialogState.Default }
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
            message = "Select an option to start a new game",
            optionOneMessage = "Same players",
            optionTwoMessage = "Different players",
            onOptionOne = {
                viewModel.resetPlayerStates()
                showResetDialog = false
                onDismiss()
            },
            onOptionTwo = {
                viewModel.resetAllPrefs()
                viewModel.resetPlayerStates()
                showResetDialog = false
                onDismiss()
            },
        )
    }

    SettingsDialog(onDismiss = {
        onDismiss()
    }, content = dialogContent, onBack = {
        backHandler.pop()
//        backStack.removeLast().invoke()
    })
}

@Composable
fun GridDialogContent(
    modifier: Modifier = Modifier, title: String, items: List<@Composable () -> Unit> = emptyList()
) {
    Box(modifier = modifier) {
        Column(
            Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.05f))
            Text(
                modifier = Modifier.wrapContentHeight().wrapContentWidth(), text = title, fontSize = 25.scaledSp, color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.weight(0.025f))
            LazyVerticalGrid(modifier = Modifier.padding(horizontal = 10.dp).wrapContentSize(), columns = GridCells.Fixed(3), content = {
                items(items.size) { index ->
                    items[index]()
                }
            })
            Spacer(modifier = Modifier.weight(0.075f))
        }
    }
}

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
        onDismissRequest = onDismiss, properties = DialogProperties(
            dismissOnBackPress = false, //TODO: once backhandler works on android, set this to false
            usePlatformDefaultWidth = false,
        )
    ) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val buttonSize = 65.dp

            Column(Modifier.fillMaxSize()) {
                if (exitButtonEnabled) {
                    Row(
                        Modifier.fillMaxWidth().wrapContentHeight(), horizontalArrangement = Arrangement.End
                    ) {

                        ExitButton(
                            Modifier.size(buttonSize), onDismiss = onDismiss, visible = exitButtonEnabled
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
                        Modifier.fillMaxWidth().wrapContentHeight(), horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        BackButton(
                            Modifier.size(buttonSize), onBack = onBack, visible = backButtonEnabled
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun BackButton(modifier: Modifier = Modifier, visible: Boolean, onBack: () -> Unit) {
    SettingsButton(
        modifier = modifier,
        backgroundColor = Color.Transparent,
        text = "",
        visible = visible,
        shadowEnabled = false,
        imageVector = vectorResource(Res.drawable.back_icon_alt),
        onPress = onBack
    )
}

@Composable
fun ExitButton(modifier: Modifier = Modifier, visible: Boolean, onDismiss: () -> Unit) {
    SettingsButton(
        modifier = modifier,
        backgroundColor = Color.Transparent,
        text = "",
        visible = visible,
        shadowEnabled = false,
        imageVector = vectorResource(Res.drawable.x_icon),
        onPress = onDismiss
    )
}





