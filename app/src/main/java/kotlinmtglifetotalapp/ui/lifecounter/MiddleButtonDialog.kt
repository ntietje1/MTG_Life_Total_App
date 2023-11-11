import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
import com.example.kotlinmtglifetotalapp.R
import kotlinmtglifetotalapp.ui.lifecounter.CoinFlipDialog
import kotlinmtglifetotalapp.ui.lifecounter.LifeCounterFragment
import kotlinmtglifetotalapp.ui.lifecounter.SettingsButton
import kotlinmtglifetotalapp.ui.lifecounter.playerButton.Player

/**
 * TODO: implement these features in settings
 * dice
 */

@Composable
fun MiddleButtonDialogComposable(parentFrag: LifeCounterFragment, onDismiss: () -> Unit) {
    val showCoinFlipDialog = remember { mutableStateOf(false) }
    val showPlayerNumberDialog = remember { mutableStateOf(false) }
    val showStartingLifeDialog = remember { mutableStateOf(false) }
    val showDiceRollDialog = remember { mutableStateOf(false) }

    GridDialog(items = listOf(
        {
            SettingsButton(
                imageResource = painterResource(id = R.drawable.player_select_icon),
                text = "Player Select",
                onClick = {
                    parentFrag.goToPlayerSelect()
                    onDismiss()
                }
            )
        }, {
            SettingsButton(
                imageResource = painterResource(id = R.drawable.reset_icon),
                text = "Reset Game",
                onClick = {
                    parentFrag.resetPlayers()
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
                    showDiceRollDialog.value
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
    ), onDismiss = {
        onDismiss()
    })

    if (showCoinFlipDialog.value) {
        CoinFlipDialog(
            onDismiss = {
                onDismiss()
                showCoinFlipDialog.value = false
            }
        )
    }

    if (showPlayerNumberDialog.value) {
        GridDialog(items = listOf(
            {
                SettingsButton(
                    imageResource = painterResource(R.drawable.one_icon),
                    text = "",
                    onClick = {
                        parentFrag.setPlayerNum(1)
                        showPlayerNumberDialog.value = false
                    }
                )
            }, {
                SettingsButton(
                    imageResource = painterResource(R.drawable.two_icon),
                    text = "",
                    onClick = {
                        parentFrag.setPlayerNum(2)
                        showPlayerNumberDialog.value = false
                    }
                )
            }, {
                SettingsButton(
                    imageResource = painterResource(R.drawable.three_icon),
                    text = "",
                    onClick = {
                        parentFrag.setPlayerNum(3)
                        showPlayerNumberDialog.value = false
                    }
                )
            }, {
                SettingsButton(
                    imageResource = painterResource(R.drawable.four_icon),
                    text = "",
                    onClick = {
                        parentFrag.setPlayerNum(4)
                        showPlayerNumberDialog.value = false
                    }
                )
            }, {
                SettingsButton(
                    imageResource = painterResource(R.drawable.five_icon),
                    text = "",
                    onClick = {
                        parentFrag.setPlayerNum(5)
                        showPlayerNumberDialog.value = false
                    }
                )
            }, {
                SettingsButton(
                    imageResource = painterResource(R.drawable.six_icon),
                    text = "",
                    onClick = {
                        parentFrag.setPlayerNum(6)
                        showPlayerNumberDialog.value = false
                    }
                )
            }
        ), onDismiss = {
            onDismiss()
            showPlayerNumberDialog.value = false
        })
    }

    if (showStartingLifeDialog.value) {
        GridDialog(items = listOf(
            {
                SettingsButton(
                    imageResource = painterResource(id = R.drawable.forty_icon),
                    text = "forty",
                    onClick = {
                        Player.startingLife = 40
                        parentFrag.resetPlayers()
                        showStartingLifeDialog.value = false
                    }
                )
            }, {
                SettingsButton(
                    imageResource = painterResource(id = R.drawable.thirty_icon),
                    text = "thirty",
                    onClick = {
                        Player.startingLife = 30
                        parentFrag.resetPlayers()
                        showStartingLifeDialog.value = false
                    }
                )
            }, {
                SettingsButton(
                    imageResource = painterResource(id = R.drawable.twenty_icon),
                    text = "twenty",
                    onClick = {
                        Player.startingLife = 20
                        parentFrag.resetPlayers()
                        showStartingLifeDialog.value = false
                    }
                )
            }, {
                SettingsButton(
//                  imageResource = painterResource(id = R.drawable.thirty_icon),
                    text = "custom",
                    onClick = {
                        Player.startingLife = -1
                        parentFrag.resetPlayers()
                        showStartingLifeDialog.value = false
                    }
                )
            }
        ), onDismiss = {
            onDismiss()
            showStartingLifeDialog.value = false
        })
    }
}

@Composable
fun GridDialog(
    items: List<@Composable () -> Unit> = emptyList(),
    onDismiss: () -> Unit = {}
) {
    SettingsDialog(
        onDismiss = onDismiss,
        content = {
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
        })
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





