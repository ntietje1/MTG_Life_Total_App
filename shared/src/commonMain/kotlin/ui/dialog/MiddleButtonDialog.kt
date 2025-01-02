package ui.dialog

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import di.BackHandler
import domain.system.SystemManager
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.back_icon_alt
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
import lifelinked.shared.generated.resources.x_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.scaledSp
import ui.SettingsButton
import ui.dialog.coinflip.CoinFlipDialogContent
import ui.dialog.coinflip.CoinFlipTutorialContent
import ui.dialog.planechase.ChoosePlanesDialogContent
import ui.dialog.planechase.PlaneChaseDialogContent
import ui.dialog.scryfall.ScryfallDialogContent
import ui.dialog.settings.AboutMeDialogContent
import ui.dialog.settings.SettingsDialogContent
import ui.dialog.settings.patchnotes.PatchNotesDialogContent
import ui.dialog.startinglife.StartingLifeDialogContent
import ui.lifecounter.DayNightState
import ui.lifecounter.LifeCounterViewModel

enum class MiddleButtonDialogState {
    Default, CoinFlip, CoinFlipTutorial, PlayerNumber, FourPlayerLayout, StartingLife, DiceRoll, Counter, Settings, Scryfall, PatchNotes, AboutMe, PlaneChase, PlanarDeck
}

@Composable
fun MiddleButtonDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: LifeCounterViewModel,
    dialogState: MiddleButtonDialogState,
    setDialogState: (MiddleButtonDialogState) -> Unit,
    toggleTheme: () -> Unit,
    toggleKeepScreenOn: () -> Unit,
    goToPlayerSelectScreen: (Boolean) -> Unit,
    triggerEnterAnimation: () -> Unit,
    setNumPlayers: (Int) -> Unit,
    setAlt4PlayerLayout: (Boolean) -> Unit,
    goToTutorialScreen: () -> Unit,
    updateTurnTimerEnabled: (Boolean) -> Unit,
    backHandler: BackHandler = koinInject()
) {

    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showChooseFirstPlayerDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
    ) {
        val buttonSize4x3 = minOf(maxWidth / 4f, maxHeight / 3f)
        val buttonSize3x4 = minOf(maxHeight / 4f, maxWidth / 3f)

        val numColumns = remember(Unit) {
            if (buttonSize3x4 * 4 < maxHeight * 0.9f) {
                3
            } else {
                4
            }
        }

        val buttonModifier = remember(Unit) {
            if (buttonSize3x4 * 4 < maxHeight * 0.9f) {
                Modifier.size(buttonSize4x3)
            } else {
                Modifier.size(buttonSize3x4)
            }
        }

        AnimatedGridDialog(modifier = Modifier.fillMaxSize(), onDismiss = onDismiss, backHandler = backHandler, pages = listOf(
            Pair(
                dialogState == MiddleButtonDialogState.CoinFlip
            ) {
                CoinFlipDialogContent(modifier = modifier, goToCoinFlipTutorial = {
                    backHandler.push { setDialogState(MiddleButtonDialogState.CoinFlip) }
                    setDialogState(MiddleButtonDialogState.CoinFlipTutorial)
                })
            }, Pair(
                dialogState == MiddleButtonDialogState.CoinFlipTutorial
            ) {
                CoinFlipTutorialContent(
                    modifier = modifier
                )
            }, Pair(
                dialogState == MiddleButtonDialogState.PlayerNumber
            ) {
                PlayerNumberDialogContent(modifier = Modifier.fillMaxSize(), onDismiss = onDismiss, setPlayerNum = {
                    setNumPlayers(it)
                    viewModel.resetGameState()
                    triggerEnterAnimation()
                }, resetPlayers = {
                    viewModel.resetGameState()
                    triggerEnterAnimation()
                }, show4PlayerDialog = { setDialogState(MiddleButtonDialogState.FourPlayerLayout) })
            }, Pair(
                dialogState == MiddleButtonDialogState.FourPlayerLayout
            ) {
                FourPlayerLayoutContent(modifier = Modifier.fillMaxSize(), onDismiss = onDismiss, setPlayerNum = {
                    setNumPlayers(it)
                    viewModel.resetGameState()
                    triggerEnterAnimation()
                }, setAlt4PlayerLayout = { setAlt4PlayerLayout(it) })
            }, Pair(
                dialogState == MiddleButtonDialogState.StartingLife
            ) {
                StartingLifeDialogContent(modifier = Modifier.fillMaxSize(), onDismiss = onDismiss, resetGameState = {
                    viewModel.resetGameState()
                    triggerEnterAnimation()
                })
            }, Pair(
                dialogState == MiddleButtonDialogState.DiceRoll
            ) {
                DiceRollDialogContent(Modifier.fillMaxSize())
            }, Pair(
                dialogState == MiddleButtonDialogState.Counter
            ) {
                CounterDialogContent(modifier = Modifier.fillMaxSize(),
                    counters = state.counters,
                    incrementCounter = { index, value -> viewModel.incrementCounter(index, value) },
                    resetCounters = { viewModel.resetCounters() })
            }, Pair(dialogState == MiddleButtonDialogState.Scryfall) {
                ScryfallDialogContent(
                    Modifier.fillMaxSize(),
                    selectButtonEnabled = false,
                    rulingsButtonEnabled = true,
                    addToBackStack = { _, block -> backHandler.push(block) },
                    onImageSelected = {},
                    viewModel = koinInject()
                )
            }, Pair(
                dialogState == MiddleButtonDialogState.Settings
            ) {
                SettingsDialogContent(
                    Modifier.fillMaxSize(),
                    goToPatchNotes = { setDialogState(MiddleButtonDialogState.PatchNotes) },
                    goToAboutMe = { setDialogState(MiddleButtonDialogState.AboutMe) },
                    addGoToSettingsToBackStack = { backHandler.push { setDialogState(MiddleButtonDialogState.Settings) } },
                    goToTutorialScreen = {
                        onDismiss()
                        goToTutorialScreen()
                    },
                    updateTurnTimerEnabled = updateTurnTimerEnabled,
                    toggleKeepScreenOn = toggleKeepScreenOn
                )
            }, Pair(
                dialogState == MiddleButtonDialogState.PatchNotes
            ) {
                PatchNotesDialogContent(
                    Modifier.fillMaxSize()
                )
            }, Pair(
                dialogState == MiddleButtonDialogState.AboutMe
            ) {
                AboutMeDialogContent(
                    Modifier.fillMaxSize()
                )
            }, Pair(
                dialogState == MiddleButtonDialogState.PlaneChase
            ) {
                PlaneChaseDialogContent(
                    modifier = Modifier.fillMaxSize(),
                    goToChoosePlanes = {
                        setDialogState(MiddleButtonDialogState.PlanarDeck)
                        backHandler.push { setDialogState(MiddleButtonDialogState.PlaneChase) }
                    },
                )
            }, Pair(
                dialogState == MiddleButtonDialogState.PlanarDeck
            ) {
                ChoosePlanesDialogContent(
                    modifier = Modifier.fillMaxSize(), addToBackStack = backHandler::push, popBackStack = backHandler::pop
                )
            }, Pair(
                dialogState == MiddleButtonDialogState.Default
            ) {
                GridDialogContent(
                    Modifier.fillMaxSize(), title = "Settings", columns = numColumns, items = listOf({
                        SettingsButton(modifier = buttonModifier, imageVector = vectorResource(Res.drawable.player_select_icon), text = "Player Select", shadowEnabled = false, onPress = {
                            viewModel.savePlayerStates()
                            viewModel.savePlayerPrefs()
                            goToPlayerSelectScreen(false)
                            onDismiss()
                        })
                    }, {
                        SettingsButton(modifier = buttonModifier, imageVector = vectorResource(Res.drawable.reset_icon), text = "Reset Game", shadowEnabled = false, onPress = {
                            showResetDialog = true
                        })
                    }, {
                        SettingsButton(modifier = buttonModifier, imageVector = vectorResource(Res.drawable.heart_solid_icon), text = "Starting Life", shadowEnabled = false, onPress = {
                            setDialogState(MiddleButtonDialogState.StartingLife)
                            backHandler.push { setDialogState(MiddleButtonDialogState.Default) }
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
                            setDialogState(MiddleButtonDialogState.PlayerNumber)
                            backHandler.push { setDialogState(MiddleButtonDialogState.Default) }
                        })
                    }, {
                        SettingsButton(buttonModifier, imageVector = vectorResource(Res.drawable.mana_icon), text = "Mana & Storm", shadowEnabled = false, onPress = {
                            setDialogState(MiddleButtonDialogState.Counter)
                            backHandler.push { setDialogState(MiddleButtonDialogState.Default) }
                        })
                    }, {
                        SettingsButton(buttonModifier, imageVector = vectorResource(Res.drawable.die_icon), text = "Dice roll", shadowEnabled = false, onPress = {
                            setDialogState(MiddleButtonDialogState.DiceRoll)
                            backHandler.push { setDialogState(MiddleButtonDialogState.Default) }
                        })
                    }, {
                        SettingsButton(buttonModifier, imageVector = vectorResource(Res.drawable.coin_icon), text = "Coin Flip", shadowEnabled = false, onPress = {
                            setDialogState(MiddleButtonDialogState.CoinFlip)
                            backHandler.push { setDialogState(MiddleButtonDialogState.Default) }
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
                            setDialogState(MiddleButtonDialogState.Scryfall)
                            backHandler.push { setDialogState(MiddleButtonDialogState.Default) }
                        })
                    }, {
                        SettingsButton(buttonModifier, imageVector = vectorResource(Res.drawable.planeswalker_icon), text = "Planechase", shadowEnabled = false, onPress = {
                            setDialogState(MiddleButtonDialogState.PlaneChase)
                            backHandler.push { setDialogState(MiddleButtonDialogState.Default) }
                        })
                    }, {
                        SettingsButton(buttonModifier, imageVector = vectorResource(Res.drawable.settings_icon_small), text = "Settings", shadowEnabled = false, onPress = {
                            setDialogState(MiddleButtonDialogState.Settings)
                            backHandler.push { setDialogState(MiddleButtonDialogState.Default) }
                        })
                    })
                )
            })
        )
    }
    var onReset: () -> Boolean by remember {
        mutableStateOf({
            println("This should never be called")
            false
        })
    }

    if (showResetDialog) {
        WarningDialog(
            onDismiss = { showResetDialog = false },
            title = "Reset Game",
            message = "Select an option to start a new game",
            optionOneMessage = "Same players",
            optionTwoMessage = "Different players",
            onOptionOne = {
                onReset = {
                    viewModel.resetGameState()
//                onDismiss()
                    println("resetting game, same players")
                    false
                }
                showResetDialog = false
                showChooseFirstPlayerDialog = true
            },
            onOptionTwo = {
                onReset = {
                    viewModel.resetAllPrefs()
                    viewModel.resetGameState()
                    println("resetting game, different players")
//                onDismiss()
                    true
                }
                showResetDialog = false
                showChooseFirstPlayerDialog = true
            },
        )
    }

    if (showChooseFirstPlayerDialog) {
        WarningDialog(
            onDismiss = { showChooseFirstPlayerDialog = false },
            title = "Choose New First Player",
            message = "Select whether to skip player selection or not",
            optionOneMessage = "Select",
            optionTwoMessage = "Skip",
            onOptionOne = {
                val allowChangeNumPlayers = onReset()
                showChooseFirstPlayerDialog = false
                onDismiss()
                goToPlayerSelectScreen(allowChangeNumPlayers)
            },
            onOptionTwo = {
                onReset()
                showChooseFirstPlayerDialog = false
                onDismiss()
            },
        )
    }
}

@Composable
fun AnimatedGridDialog(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, backHandler: BackHandler = koinInject(), pages: List<Pair<Boolean, @Composable () -> Unit>>
) {
    LaunchedEffect(Unit) {
        backHandler.push { onDismiss() }
    }

    val duration = (450 / SystemManager.getAnimationCorrectionFactor()).toInt()

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
                modifier = modifier.background(MaterialTheme.colorScheme.background.copy(alpha = 0.1f)).border(
                    1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)
                ),
            ) {
                content()
            }
        }
    }

    val dialogContent: @Composable () -> Unit = {
        Box {
            for (page in pages) {
                FormattedAnimatedVisibility(
                    visible = page.first
                ) {
                    page.second.invoke()
                }
            }
        }
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
    modifier: Modifier = Modifier, title: String, columns: Int = 3, content: LazyGridScope.() -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val padding = remember(Unit) { maxHeight / 60f }
        val titleSize = remember(Unit) { (maxWidth / 40f + maxHeight / 60f).value }
        Column(
            Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.1f))
//            Spacer(modifier = Modifier.height(padding * 3f))
            Text(
                modifier = Modifier.wrapContentSize(), text = title, fontSize = titleSize.scaledSp, color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.weight(0.015f))
//            Spacer(modifier = Modifier.height(padding * 2f))
//            Box(Modifier.fillMaxSize().background(color = Color.Red),
//                contentAlignment = Alignment.Center) {
            LazyVerticalGrid(
                modifier = Modifier.padding(horizontal = padding / 2f).wrapContentSize(),
                columns = GridCells.Fixed(columns),
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
            Spacer(modifier = Modifier.weight(0.15f))
//            Spacer(modifier = Modifier.height(padding / 2f))
        }
    }
}

@Composable
fun GridDialogContent(
    modifier: Modifier = Modifier, title: String, columns: Int = 3, items: List<@Composable () -> Unit> = emptyList()
) {
    BoxWithConstraints(modifier = modifier) {
        val padding = remember(Unit) { maxHeight / 60f }
        val titleSize = remember(Unit) { (maxWidth / 40f + maxHeight / 60f).value }
        Column(
            Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.1f))
//            Spacer(modifier = Modifier.height(padding * 3f))
            Text(
                modifier = Modifier.wrapContentSize(), text = title, fontSize = titleSize.scaledSp, color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.weight(0.015f))
//            Spacer(modifier = Modifier.height(padding * 2f))
//            Box(Modifier.fillMaxSize().background(color = Color.Red),
//                contentAlignment = Alignment.Center) {
            LazyVerticalGrid(modifier = Modifier.padding(horizontal = padding / 2f).wrapContentSize(),
                columns = GridCells.Fixed(columns),
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.Center,
                content = {
                    items(items.size, key = { index ->
                        items[index].hashCode()
                    }) { index ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            items[index]()
                        }
                    }
                })
            Spacer(modifier = Modifier.weight(0.15f))
//            Spacer(modifier = Modifier.height(padding / 2f))
        }
    }
}

@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier, onDismiss: () -> Unit = {}, onBack: () -> Unit = {}, exitButtonEnabled: Boolean = true, backButtonEnabled: Boolean = true, content: @Composable () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            dismissOnBackPress = false, //TODO: once backhandler works on android, set this to false
            usePlatformDefaultWidth = false,
        )
    ) {
        BoxWithConstraints(
            modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            val buttonSize = remember(Unit) { maxWidth / 6.5f }

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
        modifier = modifier, backgroundColor = Color.Transparent, text = "", visible = visible, shadowEnabled = false, imageVector = vectorResource(Res.drawable.back_icon_alt), onPress = onBack
    )
}

@Composable
fun ExitButton(modifier: Modifier = Modifier, visible: Boolean, onDismiss: () -> Unit) {
    SettingsButton(
        modifier = modifier, backgroundColor = Color.Transparent, text = "", visible = visible, shadowEnabled = false, imageVector = vectorResource(Res.drawable.x_icon), onPress = onDismiss
    )
}





