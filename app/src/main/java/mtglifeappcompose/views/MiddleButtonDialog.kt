package mtglifeappcompose.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mtglifeappcompose.R

/**
 * TODO: implement these features in settings
 * dice
 */

@Composable
fun MiddleButtonDialogComposable(onDismiss: () -> Unit, resetPlayers: () -> Unit, setPlayerNum: (Int) -> Unit, setStartingLife: (Int) -> Unit, goToPlayerSelect: () -> Unit) {
    val showCoinFlipDialog = remember { mutableStateOf(false) }
    val showPlayerNumberDialog = remember { mutableStateOf(false) }
    val showStartingLifeDialog = remember { mutableStateOf(false) }
    val showDiceRollDialog = remember { mutableStateOf(false) }

    // Create a lambda that represents the content of the dialog
    val dialogContent: @Composable () -> Unit = {
        when {
            showCoinFlipDialog.value -> {
                CoinFlipDialogContent(onDismiss, showCoinFlipDialog)
            }

            showPlayerNumberDialog.value -> {
                PlayerNumberDialogContent(onDismiss, showPlayerNumberDialog, setPlayerNum, resetPlayers)
            }

            showStartingLifeDialog.value -> {
                StartingLifeDialogContent(onDismiss, showStartingLifeDialog, setStartingLife)
            }

            else -> {
                GridDialogContent(items = listOf(
                    {
                        SettingsButton(
                            imageResource = painterResource(id = R.drawable.player_select_icon),
                            text = "Player Select",
                            onPress = {
                                goToPlayerSelect()
                                onDismiss()
                            }
                        )
                    }, {
                        SettingsButton(
                            imageResource = painterResource(id = R.drawable.reset_icon),
                            text = "Reset Game",
                            onPress = {
                                resetPlayers()
                                onDismiss()
                            }
                        )
                    }, {
                        SettingsButton(
                            imageResource = painterResource(id = R.drawable.player_count_icon),
                            text = "Player Number",
                            onPress = {
                                showPlayerNumberDialog.value = true
                            }
                        )
                    }, {
                        SettingsButton(
                            imageResource = painterResource(R.drawable.six_icon),
                            text = "Dice roll",
                            onPress = {
                                showDiceRollDialog.value = true
                            }
                        )
                    }, {
                        SettingsButton(
                            imageResource = painterResource(R.drawable.coin_icon),
                            text = "Coin Flip",
                            onPress = {
                                showCoinFlipDialog.value = true
                            }
                        )
                    }, {
                        SettingsButton(
                            imageResource = painterResource(R.drawable.forty_icon),
                            text = "Starting Life",
                            onPress = {
                                showStartingLifeDialog.value = true
                            }
                        )
                    }
                ))
            }
        }
    }

    SettingsDialog(
        onDismiss = {
            onDismiss()
//            showCoinFlipDialog.value = false
//            showPlayerNumberDialog.value = false
//            showStartingLifeDialog.value = false
        },
        content = dialogContent
    )
}

@Composable
fun CoinFlipDialogContent(
    onDismiss: () -> Unit,
    showCoinFlipDialog: MutableState<Boolean>
) {
    CoinFlipDialogBox()
}

@Composable
fun PlayerNumberDialogContent(
    onDismiss: () -> Unit,
    showPlayerNumberDialog: MutableState<Boolean>,
    setPlayerNum: (Int) -> Unit,
    resetPlayers: () -> Unit
) {
    GridDialogContent(items = listOf(
        {
            SettingsButton(
                imageResource = painterResource(R.drawable.one_icon),
                text = "",
                onPress = {
                    setPlayerNum(1)
                    resetPlayers()
                    onDismiss()
                    showPlayerNumberDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(R.drawable.two_icon),
                text = "",
                onPress = {
                    setPlayerNum(2)
                    resetPlayers()
                    onDismiss()
                    showPlayerNumberDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(R.drawable.three_icon),
                text = "",
                onPress = {
                    setPlayerNum(3)
                    resetPlayers()
                    onDismiss()
                    showPlayerNumberDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(R.drawable.four_icon),
                text = "",
                onPress = {
                    setPlayerNum(4)
                    resetPlayers()
                    onDismiss()
                    showPlayerNumberDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(R.drawable.five_icon),
                text = "",
                onPress = {
                    setPlayerNum(5)
                    resetPlayers()
                    onDismiss()
                    showPlayerNumberDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(R.drawable.six_icon),
                text = "",
                onPress = {
                    setPlayerNum(6)
                    resetPlayers()
                    onDismiss()
                    showPlayerNumberDialog.value = false
                }
            )
        }
    ))
}

@Composable
fun StartingLifeDialogContent(
    onDismiss: () -> Unit,
    showStartingLifeDialog: MutableState<Boolean>,
    setStartingLife: (Int) -> Unit
) {
    GridDialogContent(items = listOf(
        {
            SettingsButton(
                imageResource = painterResource(id = R.drawable.forty_icon),
                text = "",
                onPress = {
                    setStartingLife(40)
                    onDismiss()
                    showStartingLifeDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(id = R.drawable.thirty_icon),
                text = "",
                onPress = {
                    setStartingLife(30)
                    onDismiss()
                    showStartingLifeDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(id = R.drawable.twenty_icon),
                text = "",
                onPress = {
                    setStartingLife(20)
                    onDismiss()
                    showStartingLifeDialog.value = false
                }
            )
        }, {
            SettingsButton(
//                  imageResource = painterResource(id = R.drawable.thirty_icon),
                text = "custom",
                onPress = {
                    setStartingLife(-1)
                    onDismiss()
                    showStartingLifeDialog.value = false
                }
            )
        }
    ))
}

@Composable
fun GridDialogContent(
    items: List<@Composable () -> Unit> = emptyList()
) {
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 15.dp / 2),
        columns = GridCells.Fixed(2),
        content = {
            items(items.size) { index ->
                items[index]()
            }
        }

    )
}


@Composable
fun SettingsDialog(
    content: @Composable () -> Unit = {},
    onDismiss: () -> Unit = {},
    width: Dp = 300.dp,
    height: Dp = 425.dp
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.size(width, height),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(30.dp)),
                color = Color.DarkGray,
                shadowElevation = 5.dp,
            ) {
                content()
            }
        }
    }
}





