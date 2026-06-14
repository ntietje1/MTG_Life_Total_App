package ui.dialog

import PlanechaseTutorialContent
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import domain.system.SystemManager
import org.koin.compose.koinInject
import theme.LocalDimensions
import theme.halfAlpha
import theme.scaledSp
import ui.dialog.coinflip.CoinFlipDialogContent
import ui.dialog.coinflip.CoinFlipTutorialContent
import ui.dialog.dice.DiceRollDialogContent
import ui.dialog.middle.MiddleButtonMenuDialog
import ui.dialog.navigation.DialogScaffold
import ui.dialog.planechase.ChoosePlanesDialogContent
import ui.dialog.planechase.PlaneChaseDialogContent
import ui.dialog.reset.ResetGameChoice
import ui.dialog.reset.ResetGameDialog
import ui.dialog.scryfall.ScryfallDialogContent
import ui.dialog.settings.AboutMeDialogContent
import ui.dialog.settings.SettingsDialogContent
import ui.dialog.settings.patchnotes.PatchNotesDialogContent
import ui.dialog.startinglife.StartingLifeDialogContent
import ui.lifecounter.DayNightState
import ui.lifecounter.LifeCounterModal
import ui.lifecounter.LifeCounterViewModel

typealias MiddleButtonDialogState = LifeCounterModal

@Composable
fun MiddleButtonDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: LifeCounterViewModel,
    dialogState: MiddleButtonDialogState,
    setDialogState: (MiddleButtonDialogState) -> Unit,
    onBack: () -> Unit,
    toggleTheme: () -> Unit,
    setKeepScreenOn: (Boolean) -> Unit,
    goToPlayerSelectScreen: (Boolean) -> Unit,
    triggerEnterAnimation: () -> Unit,
    setNumPlayers: (Int) -> Unit,
    setAlt4PlayerLayout: (Boolean) -> Unit,
    goToTutorialScreen: () -> Unit,
    updateTurnTimerEnabled: (Boolean) -> Unit,
) {

    val state by viewModel.state.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    val nestedBackActions = remember { mutableStateListOf<() -> Unit>() }

    fun addNestedBackAction(action: () -> Unit) {
        nestedBackActions.add(action)
    }

    fun handleBack() {
        if (nestedBackActions.isNotEmpty()) {
            nestedBackActions.removeLast().invoke()
        } else {
            onBack()
        }
    }

    AnimatedGridDialog(modifier = modifier.fillMaxSize(), onDismiss = onDismiss, onBack = ::handleBack, pages = listOf(
            Pair(
                dialogState == MiddleButtonDialogState.CoinFlip
            ) {
                CoinFlipDialogContent(modifier = modifier, goToCoinFlipTutorial = {
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
                    modifier = Modifier.fillMaxSize(),
                    selectButtonEnabled = false,
                    rulingsButtonEnabled = true,
                    addToBackStack = { _, block -> addNestedBackAction(block) },
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
                    addGoToSettingsToBackStack = {},
                    goToTutorialScreen = {
                        onDismiss()
                        goToTutorialScreen()
                    },
                    updateTurnTimerEnabled = updateTurnTimerEnabled,
                    setKeepScreenOn = setKeepScreenOn
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
                    goToPlanechaseTutorial = {
                        setDialogState(MiddleButtonDialogState.PlanarTutorial)
                    },
                    goToChoosePlanes = {
                        setDialogState(MiddleButtonDialogState.PlanarDeck)
                    },
                )
            }, Pair(
                dialogState == MiddleButtonDialogState.PlanarDeck
            ) {
                ChoosePlanesDialogContent(
                    modifier = Modifier.fillMaxSize(), addToBackStack = ::addNestedBackAction, popBackStack = ::handleBack
                )
            }, Pair(
                dialogState == MiddleButtonDialogState.PlanarTutorial
            ) {
                PlanechaseTutorialContent(
                    modifier = Modifier.fillMaxSize()
                )
            }, Pair(dialogState == MiddleButtonDialogState.Default) {
                MiddleButtonMenuDialog(
                    modifier = Modifier.fillMaxSize(),
                    dayNightState = state.dayNight,
                    onPlayerSelect = {
                        viewModel.savePlayerPrefs()
                        goToPlayerSelectScreen(false)
                        onDismiss()
                    },
                    onResetGame = { showResetDialog = true },
                    onStartingLife = { setDialogState(MiddleButtonDialogState.StartingLife) },
                    onToggleTheme = toggleTheme,
                    onPlayerNumber = { setDialogState(MiddleButtonDialogState.PlayerNumber) },
                    onCounters = { setDialogState(MiddleButtonDialogState.Counter) },
                    onDiceRoll = { setDialogState(MiddleButtonDialogState.DiceRoll) },
                    onCoinFlip = { setDialogState(MiddleButtonDialogState.CoinFlip) },
                    onToggleDayNight = { viewModel.toggleDayNight() },
                    onClearDayNight = { viewModel.setDayNight(DayNightState.NONE) },
                    onCardSearch = { setDialogState(MiddleButtonDialogState.Scryfall) },
                    onPlanechase = { setDialogState(MiddleButtonDialogState.PlaneChase) },
                    onSettings = { setDialogState(MiddleButtonDialogState.Settings) }
                )
            })
        )
    if (showResetDialog) {
        ResetGameDialog(
            onDismiss = { showResetDialog = false },
            onChoice = { choice ->
                if (choice.resetPlayerPreferences) {
                    viewModel.resetAllPrefs()
                }
                viewModel.resetGameState()
                showResetDialog = false
                onDismiss()
                if (choice.chooseFirstPlayer) {
                    goToPlayerSelectScreen(choice == ResetGameChoice.DifferentPlayersSelectFirst)
                }
            }
        )
    }
}

@Composable
fun AnimatedGridDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onBack: (() -> Unit)? = null,
    pages: List<Pair<Boolean, @Composable () -> Unit>>
) {
    val dimensions = LocalDimensions.current

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
                modifier = modifier
                    .background(
                        MaterialTheme.colorScheme.surface.halfAlpha().halfAlpha()
                    )
                    .border(
                        dimensions.borderThin, MaterialTheme.colorScheme.onPrimary.halfAlpha()
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

    DialogScaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        onDismiss = {
            onDismiss()
        },
        content = dialogContent,
        onBack = {
            if (onBack != null) {
                onBack()
            } else {
                onDismiss()
            }
        })
}

@Composable
fun GridDialogContent(
    modifier: Modifier = Modifier, title: String, columns: Int = 3, content: LazyGridScope.() -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val dimensions = LocalDimensions.current
        val padding = remember(Unit) { maxHeight / 60f }
        Column(
            Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.1f))
            Text(
                modifier = Modifier.wrapContentSize(), text = title, fontSize = dimensions.textMedium.scaledSp, color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.weight(0.015f))
            LazyVerticalGrid(
                modifier = Modifier.padding(horizontal = padding / 2f).wrapContentSize(),
                columns = GridCells.Fixed(columns),
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.Center,
                content = content
            )
            Spacer(modifier = Modifier.weight(0.15f))
        }
    }
}

@Composable
fun GridDialogContent(
    modifier: Modifier = Modifier, title: String, columns: Int = 3, items: List<@Composable () -> Unit> = emptyList()
) {
    BoxWithConstraints(modifier = modifier) {
        val dimensions = LocalDimensions.current
        val padding = remember(Unit) { maxHeight / 60f }
        Column(
            Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.1f))
            Text(
                modifier = Modifier.wrapContentSize(), text = title, fontSize = dimensions.textMedium.scaledSp, color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.weight(0.015f))
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
        }
    }
}

