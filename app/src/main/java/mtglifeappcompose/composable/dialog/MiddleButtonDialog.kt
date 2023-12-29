package mtglifeappcompose.composable.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mtglifeappcompose.R
import mtglifeappcompose.composable.SettingsButton
import mtglifeappcompose.data.AppViewModel
import mtglifeappcompose.data.DayNightState

private enum class MiddleButtonDialogState {
    Default, CoinFlip, PlayerNumber, FourPlayerLayout, StartingLife, DiceRoll, Counter, Settings, Scryfall
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
    val haptic = LocalHapticFeedback.current
    val viewModel: AppViewModel = viewModel()

    val enterAnimation = slideInHorizontally(TweenSpec(750, easing = LinearOutSlowInEasing)) { (-it * 1.25).toInt() }
    val exitAnimation = slideOutHorizontally(TweenSpec(750, easing = LinearOutSlowInEasing)) { (it * 1.25).toInt() }
    val dialogContent: @Composable () -> Unit = {
        Box(
            modifier = modifier.fillMaxSize(), // Use fillMaxSize to take up the entire screen
        ) {
            AnimatedVisibility(
                visible = state == MiddleButtonDialogState.CoinFlip, enter = enterAnimation, exit = exitAnimation
            ) {
                CoinFlipDialogContent(
                    Modifier.fillMaxSize(), onDismiss, coinFlipHistory
                )
            }

            AnimatedVisibility(
                visible = state == MiddleButtonDialogState.PlayerNumber, enter = enterAnimation, exit = exitAnimation
            ) {
                PlayerNumberDialogContent(
                    Modifier.fillMaxSize(),
                    onDismiss,
                    setPlayerNum,
                    resetPlayers,
                ) { state = MiddleButtonDialogState.FourPlayerLayout }
            }

            AnimatedVisibility(
                visible = state == MiddleButtonDialogState.FourPlayerLayout, enter = enterAnimation, exit = exitAnimation
            ) {
                FourPlayerLayoutContent(
                    Modifier.fillMaxSize(), onDismiss, setPlayerNum
                ) {
                    set4PlayerLayout(it)
                }
            }

            AnimatedVisibility(
                visible = state == MiddleButtonDialogState.StartingLife, enter = enterAnimation, exit = exitAnimation
            ) {
                StartingLifeDialogContent(
                    Modifier.fillMaxSize(), onDismiss, setStartingLife
                )
            }

            AnimatedVisibility(
                visible = state == MiddleButtonDialogState.DiceRoll, enter = enterAnimation, exit = exitAnimation
            ) {
                DiceRollDialogContent(Modifier.fillMaxSize(), onDismiss)
            }

            AnimatedVisibility(
                visible = state == MiddleButtonDialogState.Counter, enter = enterAnimation, exit = exitAnimation
            ) {
                CounterDialogContent(Modifier.fillMaxSize(), counters, onDismiss)
            }

            AnimatedVisibility(
                visible = state == MiddleButtonDialogState.Scryfall, enter = enterAnimation, exit = exitAnimation
            ) {
                ScryfallDialogContent(Modifier.fillMaxSize(), player = null, selectButtonEnabled = false, rulingsButtonEnabled = true)
            }

            AnimatedVisibility(
                visible = state == MiddleButtonDialogState.Settings, enter = enterAnimation, exit = exitAnimation
            ) {
                SettingsDialogContent(Modifier.fillMaxSize(), onDismiss)
            }

            AnimatedVisibility(
                visible = state == MiddleButtonDialogState.Default, enter = enterAnimation, exit = exitAnimation
            ) {
                GridDialogContent(Modifier.fillMaxSize(), items = listOf({
                    SettingsButton(
                        imageResource = painterResource(id = R.drawable.player_select_icon),
                        text = "Player Select",
                        mainColor = MaterialTheme.colorScheme.onPrimary,
                        shadowEnabled = false,
                        onPress = {
                            goToPlayerSelect()
                            onDismiss()
                        })
                }, {
                    SettingsButton(imageResource = painterResource(id = R.drawable.reset_icon), text = "Reset Game", mainColor = MaterialTheme.colorScheme.onPrimary, shadowEnabled = false, onPress = {
                        resetPlayers()
                        onDismiss()
                    })
                }, {
                    SettingsButton(imageResource = painterResource(R.drawable.forty_icon), text = "Starting Life", mainColor = MaterialTheme.colorScheme.onPrimary, shadowEnabled = false, onPress = {
                        state = MiddleButtonDialogState.StartingLife
                    })
                }, {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.moon_icon), text = "Toggle Theme", mainColor = MaterialTheme.colorScheme.onPrimary, shadowEnabled = false, onPress = toggleTheme
                    )
                }, {
                    SettingsButton(
                        imageResource = painterResource(id = R.drawable.player_count_icon),
                        text = "Player Number",
                        mainColor = MaterialTheme.colorScheme.onPrimary,
                        shadowEnabled = false,
                        onPress = {
                            state = MiddleButtonDialogState.PlayerNumber
                        })
                }, {
                    SettingsButton(imageResource = painterResource(R.drawable.mana_icon), text = "Counters", mainColor = MaterialTheme.colorScheme.onPrimary, shadowEnabled = false, onPress = {
                        state = MiddleButtonDialogState.Counter
                    })
                }, {
                    SettingsButton(imageResource = painterResource(R.drawable.six_icon), text = "Dice roll", mainColor = MaterialTheme.colorScheme.onPrimary, shadowEnabled = false, onPress = {
                        state = MiddleButtonDialogState.DiceRoll
                    })
                }, {
                    SettingsButton(imageResource = painterResource(R.drawable.coin_icon), text = "Coin Flip", mainColor = MaterialTheme.colorScheme.onPrimary, shadowEnabled = false, onPress = {
                        state = MiddleButtonDialogState.CoinFlip
                    })
                }, {
                    SettingsButton(imageResource = when (viewModel.dayNight) {
                        DayNightState.DAY -> painterResource(R.drawable.sun_icon)
                        DayNightState.NIGHT -> painterResource(R.drawable.moon_icon)
                        DayNightState.NONE -> painterResource(R.drawable.sun_and_moon_icon)
                    }, text = when (viewModel.dayNight) {
                        DayNightState.DAY -> "Day/Night"
                        DayNightState.NIGHT -> "Day/Night"
                        DayNightState.NONE -> "Day/Night"
                    }, mainColor = MaterialTheme.colorScheme.onPrimary, shadowEnabled = false, onPress = {
                        viewModel.toggleDayNight()
                    }, onLongPress = {
                        viewModel.dayNight = DayNightState.NONE
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    })
                }, {
                    SettingsButton(modifier = Modifier,
                        imageResource = painterResource(R.drawable.search_icon),
                        text = "Card Search",
                        mainColor = MaterialTheme.colorScheme.onPrimary,
                        shadowEnabled = false,
                        onPress = {
                            state = MiddleButtonDialogState.Scryfall
                        })
                }, {
                    SettingsButton(modifier = Modifier.padding(horizontal = 12.dp),
                        imageResource = painterResource(R.drawable.settings_icon),
                        text = "Settings",
                        mainColor = MaterialTheme.colorScheme.onPrimary,
                        shadowEnabled = false,
                        onPress = {
                            state = MiddleButtonDialogState.Settings
                        })
                }))
            }
        }
    }

    SettingsDialog(onDismiss = {
        onDismiss()
    }, content = dialogContent, onBack = {
        when (state) {
            MiddleButtonDialogState.Default -> onDismiss()
            MiddleButtonDialogState.FourPlayerLayout -> {
                state = MiddleButtonDialogState.PlayerNumber
            }

            else -> {
                state = MiddleButtonDialogState.Default
            }
        }
    })
}

@Composable
fun GridDialogContent(
    modifier: Modifier = Modifier, items: List<@Composable () -> Unit> = emptyList()
) {
    Box(modifier = modifier) {
        LazyVerticalGrid(modifier = Modifier
            .align(Alignment.Center)
            .wrapContentSize(), columns = GridCells.Fixed(3), content = {
            items(items.size) { index ->
                items[index]()
            }
        })
    }
}


@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onBack: () -> Unit = {},
    onConfirm: () -> Unit = {},
    exitButtonEnabled: Boolean = true,
    backButtonEnabled: Boolean = true,
    confirmButtonEnabled: Boolean = false,
    content: @Composable () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)), contentAlignment = Alignment.Center
        ) {

            Column(Modifier.fillMaxSize()) {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp), horizontalArrangement = Arrangement.End
                ) {

                    ExitButton(
                        onDismiss = onDismiss, visible = exitButtonEnabled
                    )
                }


                Box(
                    Modifier.weight(0.1f)
                ) {
                    content()
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp), horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BackButton(
                        onBack = onBack, visible = backButtonEnabled
                    )
                    ConfirmButton(
                        onConfirm = onConfirm, visible = confirmButtonEnabled
                    )
                }
            }
        }
    }
}

@Composable
fun BackButton(modifier: Modifier = Modifier, visible: Boolean, onBack: () -> Unit) {
    SettingsButton(
        modifier = modifier.rotate(180f),
        size = 100.dp,
        mainColor = MaterialTheme.colorScheme.onPrimary,
        backgroundColor = Color.Transparent,
        text = "",
        visible = visible,
        shadowEnabled = false,
        imageResource = painterResource(id = R.drawable.enter_icon),
        onTap = onBack
    )
}

@Composable
fun ConfirmButton(modifier: Modifier = Modifier, visible: Boolean, onConfirm: () -> Unit) {
    SettingsButton(
        modifier = modifier.padding(5.dp),
        size = 100.dp,
        mainColor = MaterialTheme.colorScheme.onPrimary,
        backgroundColor = Color.Transparent,
        text = "",
        visible = visible,
        shadowEnabled = false,
        imageResource = painterResource(id = R.drawable.checkmark),
        onTap = onConfirm
    )
}

@Composable
fun ExitButton(modifier: Modifier = Modifier, visible: Boolean, onDismiss: () -> Unit) {
    SettingsButton(
        modifier = modifier,
        size = 100.dp,
        mainColor = MaterialTheme.colorScheme.onPrimary,
        backgroundColor = Color.Transparent,
        text = "",
        visible = visible,
        shadowEnabled = false,
        imageResource = painterResource(id = R.drawable.x_icon),
        onTap = onDismiss
    )
}





