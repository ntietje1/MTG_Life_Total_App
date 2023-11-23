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
import mtglifeappcompose.dialogs.CoinFlipDialogBox
import com.example.mtglifeappcompose.views.SettingsButton

/**
 * TODO: implement these features in settings
 * dice
 */

@Composable
fun MiddleButtonDialogComposable(onDismiss: () -> Unit) {
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
                PlayerNumberDialogContent(onDismiss, showPlayerNumberDialog)
            }

            showStartingLifeDialog.value -> {
                StartingLifeDialogContent(onDismiss, showStartingLifeDialog)
            }

            else -> {
                GridDialogContent(items = listOf(
                    {
                        SettingsButton(
                            imageResource = painterResource(id = R.drawable.player_select_icon),
                            text = "Player Select",
                            onClick = {
//                                parentFrag.goToPlayerSelect()
                                onDismiss()
                            }
                        )
                    }, {
                        SettingsButton(
                            imageResource = painterResource(id = R.drawable.reset_icon),
                            text = "Reset Game",
                            onClick = {
//                                parentFrag.resetPlayers()
                                onDismiss()
                            }
                        )
                    }, {
                        SettingsButton(
                            imageResource = painterResource(id = R.drawable.player_count_icon),
                            text = "Player Number",
                            onClick = {
                                showPlayerNumberDialog.value = true
                            }
                        )
                    }, {
                        SettingsButton(
                            imageResource = painterResource(R.drawable.six_icon),
                            text = "Dice roll",
                            onClick = {
                                showDiceRollDialog.value = true
                            }
                        )
                    }, {
                        SettingsButton(
                            imageResource = painterResource(R.drawable.coin_icon),
                            text = "Coin Flip",
                            onClick = {
                                showCoinFlipDialog.value = true
                            }
                        )
                    }, {
                        SettingsButton(
                            imageResource = painterResource(R.drawable.forty_icon),
                            text = "Starting Life",
                            onClick = {
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
    showPlayerNumberDialog: MutableState<Boolean>
) {
    GridDialogContent(items = listOf(
        {
            SettingsButton(
                imageResource = painterResource(R.drawable.one_icon),
                text = "",
                onClick = {
//                    parentFrag.setPlayerNum(1)
                    onDismiss()
                    showPlayerNumberDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(R.drawable.two_icon),
                text = "",
                onClick = {
//                    parentFrag.setPlayerNum(2)
                    onDismiss()
                    showPlayerNumberDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(R.drawable.three_icon),
                text = "",
                onClick = {
//                    parentFrag.setPlayerNum(3)
                    onDismiss()
                    showPlayerNumberDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(R.drawable.four_icon),
                text = "",
                onClick = {
//                    parentFrag.setPlayerNum(4)
                    onDismiss()
                    showPlayerNumberDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(R.drawable.five_icon),
                text = "",
                onClick = {
//                    parentFrag.setPlayerNum(5)
                    onDismiss()
                    showPlayerNumberDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(R.drawable.six_icon),
                text = "",
                onClick = {
//                    parentFrag.setPlayerNum(6)
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
    showStartingLifeDialog: MutableState<Boolean>
) {
    GridDialogContent(items = listOf(
        {
            SettingsButton(
                imageResource = painterResource(id = R.drawable.forty_icon),
                text = "",
                onClick = {
//                    Player.startingLife = 40
//                    parentFrag.resetPlayers()
                    onDismiss()
                    showStartingLifeDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(id = R.drawable.thirty_icon),
                text = "",
                onClick = {
//                    Player.startingLife = 30
//                    parentFrag.resetPlayers()
                    onDismiss()
                    showStartingLifeDialog.value = false
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(id = R.drawable.twenty_icon),
                text = "",
                onClick = {
//                    Player.startingLife = 20
//                    parentFrag.resetPlayers()
                    onDismiss()
                    showStartingLifeDialog.value = false
                }
            )
        }, {
            SettingsButton(
//                  imageResource = painterResource(id = R.drawable.thirty_icon),
                text = "custom",
                onClick = {
//                    Player.startingLife = -1
//                    parentFrag.resetPlayers()
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




