package lifelinked.composable.dialog

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hypeapps.lifelinked.R
import lifelinked.composable.SettingsButton
import lifelinked.data.AppViewModel
import lifelinked.data.DayNightState
import lifelinked.ui.theme.scaledSp

private enum class MiddleButtonDialogState {
    Default, CoinFlip, PlayerNumber, FourPlayerLayout, StartingLife, DiceRoll, Counter, Settings, Scryfall, AboutMe, PlaneChase, PlanarDeck
}

@Composable
fun MiddleButtonDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    resetPlayers: () -> Unit,
    setPlayerNum: (Int) -> Unit,
    setStartingLife: (Int) -> Unit,
    goToPlayerSelect: () -> Unit,
    coinFlipHistory: SnapshotStateList<String> = mutableStateListOf(),
    set4PlayerLayout: (value: Boolean) -> Unit,
    toggleTheme: () -> Unit,
    counters: ArrayList<MutableIntState> = arrayListOf()
) {

    var state by remember { mutableStateOf(MiddleButtonDialogState.Default) }
    val backStack = remember { mutableStateListOf<() -> Unit>(onDismiss) }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val viewModel: AppViewModel = viewModel()
    val duration = viewModel.correctAnimationDuration(450, context)


    val enterAnimation = slideInHorizontally(TweenSpec(duration, easing = LinearOutSlowInEasing)) { (-it * 1.25).toInt() }
    val exitAnimation = slideOutHorizontally(TweenSpec(duration, easing = LinearOutSlowInEasing)) { (it * 1.25).toInt() }

    @Composable
    fun FormattedAnimatedVisibility(
        visible: Boolean, content: @Composable () -> Unit
    ) {
        AnimatedVisibility(
            visible = visible, enter = enterAnimation, exit = exitAnimation
        ) {
            BoxWithConstraints(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.1f))
                    .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f)),
            ) {
                content()
            }
        }
    }

    val dialogContent: @Composable () -> Unit = {
        BackHandler {
            backStack.removeLast().invoke()
        }
        BoxWithConstraints(
            modifier = modifier.fillMaxSize(),
        ) {
            val buttonModifier = Modifier.size(min(maxWidth / 3, maxHeight / 4))
            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.CoinFlip
            ) {
                CoinFlipDialogContent(modifier, coinFlipHistory)
            }

            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.PlayerNumber
            ) {
                PlayerNumberDialogContent(
                    Modifier.fillMaxSize(),
                    onDismiss,
                    setPlayerNum,
                    resetPlayers,
                ) { state = MiddleButtonDialogState.FourPlayerLayout }
            }

            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.FourPlayerLayout
            ) {
                FourPlayerLayoutContent(
                    Modifier.fillMaxSize(), onDismiss, setPlayerNum
                ) {
                    set4PlayerLayout(it)
                }
            }

            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.StartingLife
            ) {
                StartingLifeDialogContent(
                    Modifier.fillMaxSize(), onDismiss, setStartingLife
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
                CounterDialogContent(Modifier.fillMaxSize(), counters)
            }

            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.Scryfall
            ) {
                ScryfallDialogContent(Modifier.fillMaxSize(), player = null, backStack = backStack, selectButtonEnabled = false, rulingsButtonEnabled = true)
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
                AboutMeDialogContent(Modifier.fillMaxSize(), onDismiss)
            }
            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.PlaneChase
            ) {
                PlaneChaseDialogContent(Modifier.fillMaxSize()) {
                    state = MiddleButtonDialogState.PlanarDeck
                    backStack.add { state = MiddleButtonDialogState.PlaneChase }
                }
            }
            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.PlanarDeck
            ) {
                ChoosePlanesDialogContent(Modifier.fillMaxSize(), backStack)
            }


            FormattedAnimatedVisibility(
                visible = state == MiddleButtonDialogState.Default
            ) {
                GridDialogContent(Modifier.fillMaxSize(), title = "Settings", items = listOf({
                    SettingsButton(buttonModifier, imageResource = painterResource(id = R.drawable.player_select_icon), text = "Player Select", shadowEnabled = false, onPress = {
                        goToPlayerSelect()
                        onDismiss()
                    })
                }, {
                    SettingsButton(buttonModifier, imageResource = painterResource(id = R.drawable.reset_icon), text = "Reset Game", shadowEnabled = false, onPress = {
                        resetPlayers()
                        onDismiss()
                    })
                }, {
                    SettingsButton(buttonModifier, imageResource = painterResource(R.drawable.heart_solid_icon), text = "Starting Life", shadowEnabled = false, onPress = {
                        state = MiddleButtonDialogState.StartingLife
                        backStack.add { state = MiddleButtonDialogState.Default }
                    })
                }, {
                    SettingsButton(
                        buttonModifier, imageResource = painterResource(R.drawable.star_icon_small), text = "Toggle Theme", shadowEnabled = false, onPress = toggleTheme
                    )
                }, {
                    SettingsButton(buttonModifier, imageResource = painterResource(id = R.drawable.player_count_icon), text = "Player Number", shadowEnabled = false, onPress = {
                        state = MiddleButtonDialogState.PlayerNumber
                        backStack.add { state = MiddleButtonDialogState.Default }
                    })
                }, {
                    SettingsButton(buttonModifier, imageResource = painterResource(R.drawable.mana_icon), text = "Mana & Storm", shadowEnabled = false, onPress = {
                        state = MiddleButtonDialogState.Counter
                        backStack.add { state = MiddleButtonDialogState.Default }
                    })
                }, {
                    SettingsButton(buttonModifier, imageResource = painterResource(R.drawable.six_icon), text = "Dice roll", shadowEnabled = false, onPress = {
                        state = MiddleButtonDialogState.DiceRoll
                        backStack.add { state = MiddleButtonDialogState.Default }
                    })
                }, {
                    SettingsButton(buttonModifier, imageResource = painterResource(R.drawable.coin_icon), text = "Coin Flip", shadowEnabled = false, onPress = {
                        state = MiddleButtonDialogState.CoinFlip
                        backStack.add { state = MiddleButtonDialogState.Default }
                    })
                }, {
                    SettingsButton(buttonModifier, imageResource = when (viewModel.dayNight) {
                        DayNightState.DAY -> painterResource(R.drawable.sun_icon)
                        DayNightState.NIGHT -> painterResource(R.drawable.moon_icon)
                        DayNightState.NONE -> painterResource(R.drawable.sun_and_moon_icon)
                    }, text = when (viewModel.dayNight) {
                        DayNightState.DAY -> "Day/Night"
                        DayNightState.NIGHT -> "Day/Night"
                        DayNightState.NONE -> "Day/Night"
                    }, shadowEnabled = false, onPress = {
                        viewModel.toggleDayNight()
                    }, onLongPress = {
                        viewModel.dayNight = DayNightState.NONE
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    })
                }, {
                    SettingsButton(buttonModifier, imageResource = painterResource(R.drawable.search_icon), text = "Card Search",

                        shadowEnabled = false, onPress = {
                            state = MiddleButtonDialogState.Scryfall
                            backStack.add { state = MiddleButtonDialogState.Default }
                        })
                }, {
                    SettingsButton(buttonModifier, imageResource = painterResource(R.drawable.planeswalker_icon), text = "Planechase",

                        shadowEnabled = false, onPress = {
                            state = MiddleButtonDialogState.PlaneChase
                            backStack.add { state = MiddleButtonDialogState.Default }
                        })
                }, {
                    SettingsButton(buttonModifier, imageResource = painterResource(R.drawable.settings_icon_small), text = "Settings",

                        shadowEnabled = false, onPress = {
                            state = MiddleButtonDialogState.Settings
                            backStack.add { state = MiddleButtonDialogState.Default }
                        })
                }))
            }
        }
    }

    SettingsDialog(onDismiss = {
        onDismiss()
    }, content = dialogContent, onBack = {
        backStack.removeLast().invoke()
    })
}

@Composable
fun GridDialogContent(
    modifier: Modifier = Modifier, title: String, items: List<@Composable () -> Unit> = emptyList()
) {
    Box(modifier = modifier) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier
                    .wrapContentHeight()
                    .wrapContentWidth()
                    .padding(bottom = 10.dp),
                text = title,
                fontSize = 25.scaledSp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            LazyVerticalGrid(modifier = Modifier
                .padding(horizontal = 10.dp)
                .wrapContentSize(), columns = GridCells.Fixed(3), content = {
                items(items.size) { index ->
                    items[index]()
                }
            })
            Spacer(modifier = Modifier.height(5.dp))
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
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)), contentAlignment = Alignment.Center
        ) {
            val buttonSize = maxHeight / 15f

            Column(Modifier.fillMaxSize()) {
                if (exitButtonEnabled) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(), horizontalArrangement = Arrangement.End
                    ) {

                        ExitButton(
                            Modifier.size(buttonSize), onDismiss = onDismiss, visible = exitButtonEnabled
                        )
                    }
                }
                Box(
                    Modifier.weight(0.1f)
                ) {
                    content()
                }

                if (backButtonEnabled) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(), horizontalArrangement = Arrangement.SpaceBetween
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

        backgroundColor = Color.Transparent, text = "", visible = visible, shadowEnabled = false, imageResource = painterResource(id = R.drawable.back_icon_alt), onTap = onBack
    )
}

@Composable
fun ExitButton(modifier: Modifier = Modifier, visible: Boolean, onDismiss: () -> Unit) {
    SettingsButton(
        modifier = modifier,

        backgroundColor = Color.Transparent, text = "", visible = visible, shadowEnabled = false, imageResource = painterResource(id = R.drawable.x_icon), onTap = onDismiss
    )
}





