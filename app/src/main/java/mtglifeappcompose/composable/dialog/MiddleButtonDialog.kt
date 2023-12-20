package mtglifeappcompose.composable.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mtglifeappcompose.R
import mtglifeappcompose.composable.SettingsButton

@Composable
fun MiddleButtonDialogComposable(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    resetPlayers: () -> Unit,
    setPlayerNum: (Int) -> Unit,
    setStartingLife: (Int) -> Unit,
    goToPlayerSelect: () -> Unit,
    coinFlipHistory: SnapshotStateList<String> = mutableStateListOf<String>(),
    toggleTheme: () -> Unit
) {
    val showCoinFlipDialog = remember { mutableStateOf(false) }
    val showPlayerNumberDialog = remember { mutableStateOf(false) }
    val showStartingLifeDialog = remember { mutableStateOf(false) }
    val showDiceRollDialog = remember { mutableStateOf(false) }
    val showDefaultDialog = remember {
        derivedStateOf {
            !showCoinFlipDialog.value && !showPlayerNumberDialog.value && !showStartingLifeDialog.value && !showDiceRollDialog.value
        }
    }

    val enterAnimation =
        slideInHorizontally(TweenSpec(750, easing = LinearOutSlowInEasing)) { (-it * 1.25).toInt() }
    val exitAnimation =
        slideOutHorizontally(TweenSpec(750, easing = LinearOutSlowInEasing)) { (it * 1.25).toInt() }
    val dialogContent: @Composable () -> Unit = {
        Box(
            modifier = modifier.fillMaxSize(), // Use fillMaxSize to take up the entire screen
        ) {
            AnimatedVisibility(
                visible = showCoinFlipDialog.value, enter = enterAnimation, exit = exitAnimation
            ) {
                CoinFlipDialogContent(
                    Modifier.fillMaxSize(), onDismiss, showCoinFlipDialog, coinFlipHistory
                )
            }

            AnimatedVisibility(
                visible = showPlayerNumberDialog.value, enter = enterAnimation, exit = exitAnimation
            ) {
                PlayerNumberDialogContent(
                    Modifier.fillMaxSize(),
                    onDismiss,
                    showPlayerNumberDialog,
                    setPlayerNum,
                    resetPlayers
                )
            }

            AnimatedVisibility(
                visible = showStartingLifeDialog.value, enter = enterAnimation, exit = exitAnimation
            ) {
                StartingLifeDialogContent(
                    Modifier.fillMaxSize(), onDismiss, showStartingLifeDialog, setStartingLife
                )
            }

            AnimatedVisibility(
                visible = showDiceRollDialog.value, enter = enterAnimation, exit = exitAnimation
            ) {
                DiceRollDialogContent(Modifier.fillMaxSize(), onDismiss, showDiceRollDialog)
            }

            AnimatedVisibility(
                visible = showDefaultDialog.value, enter = enterAnimation, exit = exitAnimation
            ) {
                GridDialogContent(Modifier.fillMaxSize(), items = listOf({
                    SettingsButton(imageResource = painterResource(id = R.drawable.player_select_icon),
                        text = "Player Select",
                        mainColor = MaterialTheme.colorScheme.onPrimary,
                        onPress = {
                            goToPlayerSelect()
                            onDismiss()
                        })
                }, {
                    SettingsButton(imageResource = painterResource(id = R.drawable.reset_icon),
                        text = "Reset Game",
                        mainColor = MaterialTheme.colorScheme.onPrimary,
                        onPress = {
                            resetPlayers()
                            onDismiss()
                        })
                }, {
                    SettingsButton(imageResource = painterResource(id = R.drawable.player_count_icon),
                        text = "Player Number",
                        mainColor = MaterialTheme.colorScheme.onPrimary,
                        onPress = {
                            showPlayerNumberDialog.value = true
                        })
                }, {
                    SettingsButton(imageResource = painterResource(R.drawable.six_icon),
                        text = "Dice roll",
                        mainColor = MaterialTheme.colorScheme.onPrimary,
                        onPress = {
                            showDiceRollDialog.value = true
                        })
                }, {
                    SettingsButton(imageResource = painterResource(R.drawable.coin_icon),
                        text = "Coin Flip",
                        mainColor = MaterialTheme.colorScheme.onPrimary,
                        onPress = {
                            showCoinFlipDialog.value = true
                        })
                }, {
                    SettingsButton(imageResource = painterResource(R.drawable.forty_icon),
                        text = "Starting Life",
                        mainColor = MaterialTheme.colorScheme.onPrimary,
                        onPress = {
                            showStartingLifeDialog.value = true
                        })
                }, {
                    SettingsButton(
                        imageResource = painterResource(R.drawable.moon_icon),
                        text = "Toggle Theme",
                        mainColor = MaterialTheme.colorScheme.onPrimary,
                        onPress = toggleTheme
                    )
                }))
            }
        }
    }

    SettingsDialog(
        onDismiss = {
            onDismiss()
        },
        content = dialogContent,
        onBack = {
            if (showCoinFlipDialog.value) {
                showCoinFlipDialog.value = false
            } else if (showPlayerNumberDialog.value) {
                showPlayerNumberDialog.value = false
            } else if (showStartingLifeDialog.value) {
                showStartingLifeDialog.value = false
            } else if (showDiceRollDialog.value) {
                showDiceRollDialog.value = false
            } else if (showDefaultDialog.value) {
                onDismiss()
            }
        },
    )
}

@Composable
fun DiceRollDialogContent(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, showDiceRollDialog: MutableState<Boolean>
) {
    GridDialogContent(modifier, items = listOf({
        DiceRollButton(value = 4, imageResource = painterResource(id = R.drawable.d4_icon))
    }, {
        DiceRollButton(value = 6, imageResource = painterResource(id = R.drawable.d6_icon))
    }, {
        DiceRollButton(value = 8, imageResource = painterResource(id = R.drawable.d8_icon))
    }, {
        DiceRollButton(
            value = 10, imageResource = painterResource(id = R.drawable.d10_icon)
        )
    }, {
        DiceRollButton(
            value = 12, imageResource = painterResource(id = R.drawable.d12_icon)
        )
    }, {
        DiceRollButton(
            value = 20, imageResource = painterResource(id = R.drawable.d20_icon)
        )
    }))
}

@Composable
fun CoinFlipDialogContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    showCoinFlipDialog: MutableState<Boolean>,
    history: SnapshotStateList<String>,
) {
    CoinFlipDialogBox(modifier, history)
}

@Composable
fun PlayerNumberDialogContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    showPlayerNumberDialog: MutableState<Boolean>,
    setPlayerNum: (Int) -> Unit,
    resetPlayers: () -> Unit
) {
    GridDialogContent(modifier, items = listOf({
        SettingsButton(imageResource = painterResource(R.drawable.one_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            onPress = {
                setPlayerNum(1)
                resetPlayers()
                onDismiss()
                showPlayerNumberDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.two_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            onPress = {
                setPlayerNum(2)
                resetPlayers()
                onDismiss()
                showPlayerNumberDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.three_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            onPress = {
                setPlayerNum(3)
                resetPlayers()
                onDismiss()
                showPlayerNumberDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.four_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            onPress = {
                setPlayerNum(4)
                resetPlayers()
                onDismiss()
                showPlayerNumberDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.five_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            onPress = {
                setPlayerNum(5)
                resetPlayers()
                onDismiss()
                showPlayerNumberDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(R.drawable.six_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            onPress = {
                setPlayerNum(6)
                resetPlayers()
                onDismiss()
                showPlayerNumberDialog.value = false
            })
    }))
}

@Composable
fun StartingLifeDialogContent(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    showStartingLifeDialog: MutableState<Boolean>,
    setStartingLife: (Int) -> Unit
) {
    GridDialogContent(modifier, items = listOf({
        SettingsButton(imageResource = painterResource(id = R.drawable.fifty_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            onPress = {
                setStartingLife(50)
                onDismiss()
                showStartingLifeDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(id = R.drawable.forty_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            onPress = {
                setStartingLife(40)
                onDismiss()
                showStartingLifeDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(id = R.drawable.thirty_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            onPress = {
                setStartingLife(30)
                onDismiss()
                showStartingLifeDialog.value = false
            })
    }, {
        SettingsButton(imageResource = painterResource(id = R.drawable.twenty_icon),
            mainColor = MaterialTheme.colorScheme.onPrimary,
            text = "",
            onPress = {
                setStartingLife(20)
                onDismiss()
                showStartingLifeDialog.value = false
            })
    }))
}

@Composable
fun GridDialogContent(
    modifier: Modifier = Modifier, items: List<@Composable () -> Unit> = emptyList()
) {
    Box(modifier = modifier) {
        LazyVerticalGrid(modifier = Modifier
            .align(Alignment.Center)
            .wrapContentSize()
            .padding(vertical = 15.dp),
            contentPadding = PaddingValues(15.dp),
            columns = GridCells.Fixed(3),
            content = {
                items(items.size) { index ->
                    items[index]()
                }
            })
    }
}


@Composable
fun SettingsDialog(
    content: @Composable () -> Unit = {}, onDismiss: () -> Unit = {}, onBack: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            dismissOnBackPress = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                shadowElevation = 5.dp,
            ) {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ExitButton(
                            onDismiss = onDismiss
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
                            .height(100.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        BackButton(
                            onBack = onBack
                        )

                    }
                }
            }
        }
    }

}

@Composable
fun BackButton(modifier: Modifier = Modifier, onBack: () -> Unit) {
    SettingsButton(
        modifier = modifier.rotate(180f),
        size = 100.dp,
        mainColor = MaterialTheme.colorScheme.onPrimary,
        backgroundColor = Color.Transparent,
        text = "",
        imageResource = painterResource(id = R.drawable.enter_icon),
        onTap = onBack
    )
}

@Composable
fun ExitButton(modifier: Modifier = Modifier, onDismiss: () -> Unit) {
    SettingsButton(
        modifier = modifier,
        size = 100.dp,
        mainColor = MaterialTheme.colorScheme.onPrimary,
        backgroundColor = Color.Transparent,
        text = "",
        imageResource = painterResource(id = R.drawable.x_icon),
        onTap = onDismiss
    )
}




