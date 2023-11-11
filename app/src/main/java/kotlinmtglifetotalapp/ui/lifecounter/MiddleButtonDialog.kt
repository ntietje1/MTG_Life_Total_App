import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.DialogFragment
import com.example.kotlinmtglifetotalapp.R
import com.example.kotlinmtglifetotalapp.databinding.MiddleButtonLayoutBinding
import kotlinmtglifetotalapp.ui.lifecounter.CoinFlipDialog
import kotlinmtglifetotalapp.ui.lifecounter.LifeCounterFragment
import kotlinmtglifetotalapp.ui.lifecounter.RoundedCornerDrawable
import kotlinmtglifetotalapp.ui.lifecounter.SettingsButton
import kotlinmtglifetotalapp.ui.lifecounter.playerButton.Player

/**
 * TODO: implement these features in settings
 * dice
 */
class MiddleButtonDialog : DialogFragment() {

    private var _binding: MiddleButtonLayoutBinding? = null
    private val binding get() = _binding!!

    private val middleMenu get() = binding.middleMenu
    private val parentFrag get() = parentFragment as LifeCounterFragment

    private val playerSelectButton
        get() = SettingsButton(requireContext()).apply {
            imageResource = R.drawable.player_select_icon
            text = "Player Select"
            setOnClickListener {
                (parentFragment as LifeCounterFragment).goToPlayerSelect()
            }
        }

    private val resetButton
        get() = SettingsButton(requireContext()).apply {
            imageResource = R.drawable.reset_icon
            text = "Reset game"
            setOnClickListener {
                parentFrag.resetPlayers()
                dismiss()
            }
        }

    private val changeNumPlayersButton
        get() = SettingsButton(requireContext()).apply {
            imageResource = R.drawable.player_count_icon
            text = "Player number"

            val activity = context as Activity
            setOnClickListener {

                activity.addContentView(
                    ComposeView(activity).apply {
                        setContent {
                            var showDialog by remember { mutableStateOf(true) }
                            if (showDialog) {
                                GridDialog(items = listOf(
                                    {
                                        SettingsButton(
                                            imageResource = painterResource(R.drawable.one_icon),
                                            text = "",
                                            onClick = {
                                                parentFrag.setPlayerNum(1)
                                                showDialog = false
                                            }
                                        )
                                    }, {
                                        SettingsButton(
                                            imageResource = painterResource(R.drawable.two_icon),
                                            text = "",
                                            onClick = {
                                                parentFrag.setPlayerNum(2)
                                                showDialog = false
                                            }
                                        )
                                    }, {
                                        SettingsButton(
                                            imageResource = painterResource(R.drawable.three_icon),
                                            text = "",
                                            onClick = {
                                                parentFrag.setPlayerNum(3)
                                                showDialog = false
                                            }
                                        )
                                    }, {
                                        SettingsButton(
                                            imageResource = painterResource(R.drawable.four_icon),
                                            text = "",
                                            onClick = {
                                                parentFrag.setPlayerNum(4)
                                                showDialog = false
                                            }
                                        )
                                    }, {
                                        SettingsButton(
                                            imageResource = painterResource(R.drawable.five_icon),
                                            text = "",
                                            onClick = {
                                                parentFrag.setPlayerNum(5)
                                                showDialog = false
                                            }
                                        )
                                    }, {
                                        SettingsButton(
                                            imageResource = painterResource(R.drawable.six_icon),
                                            text = "",
                                            onClick = {
                                                parentFrag.setPlayerNum(6)
                                                showDialog = false
                                            }
                                        )
                                    }
                                ), onDismiss = {
                                    showDialog = false
                                })
                            }
                        }
                    },
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )

            }
        }

    private val diceRollButton
        get() = SettingsButton(requireContext()).apply {
            imageResource = R.drawable.six_icon
            text = "Dice roll"
            setOnClickListener {

            }
        }

    private val coinFlipButton
        get() = SettingsButton(requireContext(), null).apply {
            imageResource = R.drawable.coin_icon
            text = "Coin flip"
            setOnClickListener {
                val activity = context as Activity
                activity.addContentView(
                    ComposeView(activity).apply {
                        setContent {
                            var showDialog by remember { mutableStateOf(true) }
                            if (showDialog) {
                                CoinFlipDialog(
                                    onDismiss = {
                                        showDialog = false
                                    }
                                )
                            }
                        }
                    },
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )


            }
        }

    private val changeStartingLifeButton
        get() = SettingsButton(requireContext()).apply {
            imageResource = R.drawable.forty_icon
            text = "Starting Life"

            val activity = context as Activity
            setOnClickListener {

                activity.addContentView(
                    ComposeView(activity).apply {
                        setContent {
                            var showDialog by remember { mutableStateOf(true) }
                            if (showDialog) {
                                GridDialog(items = listOf(
                                    {
                                        SettingsButton(
                                            imageResource = painterResource(id = R.drawable.forty_icon),
                                            text = "forty",
                                            onClick = {
                                                Player.startingLife = 40
                                                parentFrag.resetPlayers()
                                                showDialog = false
                                            }
                                        )
                                    }, {
                                        SettingsButton(
                                            imageResource = painterResource(id = R.drawable.thirty_icon),
                                            text = "thirty",
                                            onClick = {
                                                Player.startingLife = 30
                                                parentFrag.resetPlayers()
                                                showDialog = false
                                            }
                                        )
                                    }, {
                                        SettingsButton(
                                            imageResource = painterResource(id = R.drawable.twenty_icon),
                                            text = "twenty",
                                            onClick = {
                                                Player.startingLife = 20
                                                parentFrag.resetPlayers()
                                                showDialog = false
                                            }
                                        )
                                    }, {
                                        SettingsButton(
//                                        imageResource = painterResource(id = R.drawable.thirty_icon),
                                            text = "custom",
                                            onClick = {
                                                Player.startingLife = -1
                                                parentFrag.resetPlayers()
                                                showDialog = false
                                            }
                                        )
                                    }
                                ), onDismiss = {
                                    showDialog = false
                                })
                            }
                        }
                    },
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )

            }
        }

    private val bground
        get() = RoundedCornerDrawable.create(requireContext()).apply {
            backgroundColor = Color.DarkGray.toArgb()
//            backgroundAlpha = 0
            rippleDrawable.alpha = 0
//        rippleDrawable.setColor(ColorStateList.valueOf(Color.DKGRAY))
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MiddleButtonLayoutBinding.inflate(inflater, container, false)
        middleMenu.columnCount = 2
        middleMenu.rowCount = 3
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(bground)
        addButtons()
    }

    private fun addButtons() {
        middleMenu.addView(playerSelectButton)
        middleMenu.addView(resetButton)
        middleMenu.addView(changeNumPlayersButton)
        middleMenu.addView(diceRollButton)
        middleMenu.addView(coinFlipButton)
        middleMenu.addView(changeStartingLifeButton)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        parentFrag.toggleImageViewVis()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@Composable
fun MiddleButtonDialogComposable(parentFrag: LifeCounterFragment, onDismiss: () -> Unit) {
    val showCoinFlipDialog = remember { mutableStateOf(false) }
    val showPlayerNumberDialog = remember { mutableStateOf(false) }
    val showStartingLifeDialog = remember { mutableStateOf(false) }
    val showDiceRollDialog = remember { mutableStateOf(false)}

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
        },{
            SettingsButton(
                imageResource = painterResource(id = R.drawable.reset_icon),
                text = "Reset Game",
                onClick = {
                    parentFrag.resetPlayers()
                    onDismiss()
                }
            )
        },{
            SettingsButton(
                imageResource = painterResource(id = R.drawable.player_count_icon),
                text = "Player Number",
                onClick = {
                    showPlayerNumberDialog.value = true
//                    onDismiss()

                }
            )
        },{
            SettingsButton(
                imageResource = painterResource(R.drawable.six_icon),
                text = "Dice roll",
                onClick = {
//                    onDismiss()
                    showDiceRollDialog.value
                }
            )
        },{
            SettingsButton(
                imageResource = painterResource(R.drawable.coin_icon),
                text = "Coin Flip",
                onClick = {
//                    onDismiss()
                    showCoinFlipDialog.value = true
                }
            )
        },{
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



