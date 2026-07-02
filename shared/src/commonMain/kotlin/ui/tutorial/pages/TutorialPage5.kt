package ui.tutorial.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import domain.system.NotificationManager
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.down_arrow_icon
import lifelinked.shared.generated.resources.pencil_icon
import lifelinked.shared.generated.resources.settings_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.defaultTextStyle
import theme.scaledSp
import ui.components.SettingsButton
import ui.lifecounter.LifeCounterModal
import ui.lifecounter.LifeCounterScreen
import ui.lifecounter.playerbutton.PlayerButtonAction


@Composable
fun TutorialPage5(
    modifier: Modifier = Modifier,
    showHint: Boolean,
    onHintDismiss: () -> Unit,
    onComplete: () -> Unit,
    setBlurUI: (Boolean) -> Unit,
    notificationManager: NotificationManager = koinInject()
) {
    val gameState = MockGameState()

    var stepOneComplete by remember { mutableStateOf(false) }
    var stepTwoComplete by remember { mutableStateOf(false) }
    var complete by remember { mutableStateOf(false) }

    fun checkStepOneOrTwoComplete(state: ui.lifecounter.LifeCounterState) {
        when {
            state.currentModal in listOf(LifeCounterModal.PlayerNumber, LifeCounterModal.FourPlayerLayout) -> {
                stepTwoComplete = true
                notificationManager.showNotification("Next: Change the number of players", 3000)
            }

            state.currentModal != null -> {
                stepOneComplete = true
                notificationManager.showNotification("Next: Navigate to the player number menu", 3000)
            }
        }
    }

    fun checkComplete() {
        if (!complete) {
            complete = true
            onComplete()
        }
    }

    val lifeCounterViewModel = remember {
        TutorialLifeCounterController(
            gameState = gameState,
            notificationManager = notificationManager,
            shouldOpenModal = { modal ->
                modal in listOf(LifeCounterModal.PlayerNumber, LifeCounterModal.FourPlayerLayout, LifeCounterModal.Default)
            },
            blockedModalMessage = { modal ->
                when (modal) {
                    LifeCounterModal.Counter -> "Counters menu disabled"
                    LifeCounterModal.Settings -> "Settings menu disabled"
                    LifeCounterModal.StartingLife -> "Starting life menu disabled"
                    LifeCounterModal.CoinFlip -> "Coin flip menu disabled"
                    LifeCounterModal.DiceRoll -> "Dice roll menu disabled"
                    LifeCounterModal.Scryfall -> "Scryfall menu disabled"
                    LifeCounterModal.PlaneChase -> "Planar deck menu disabled"
                    else -> "Menu disabled"
                }
            },
            afterModalChanged = { state ->
                setBlurUI(state.isModalOpen)
                checkStepOneOrTwoComplete(state)
            },
            afterNumPlayersChanged = { checkComplete() },
            blockedThemeToggleMessage = "Changing theme disabled",
            blockedPlayerActionMessage = { action ->
                when (action) {
                    PlayerButtonAction.ToggleCommanderDealer -> "Commander damage disabled"
                    PlayerButtonAction.ToggleSettings -> "Settings disabled"
                    else -> null
                }
            }
        )
    }

    TutorialScreenWrapper(
        modifier = modifier,
        blur = showHint,
        step = Pair(if (complete) 3 else if (stepTwoComplete) 2 else if (stepOneComplete) 1 else 0, 3),
        instructions = if (complete) "Complete" else if (stepTwoComplete) "Change the number of players" else if (stepOneComplete) "Navigate to the player number menu" else "Open the middle settings menu",
    ) {
        LifeCounterScreen(
            modifier = modifier,
            viewModel = lifeCounterViewModel,
            goToPlayerSelectScreen = {
                notificationManager.showNotification("Player select disabled", 3000)
                lifeCounterViewModel.setShowButtons(true)
            },
            goToTutorialScreen = {},
            firstNavigation = false
        )
        if (showHint) {
            TutorialOverlayScreen(
                onDismiss = onHintDismiss
            ) {
                if (!stepOneComplete) {
                    Column(
                        Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.wrapContentSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SettingsButton(
                                modifier = Modifier.size(70.dp).rotate(-90f),
                                mainColor = Color.White,
                                backgroundColor = Color.Transparent,
                                shadowEnabled = false,
                                imageVector = vectorResource(Res.drawable.down_arrow_icon),
                                enabled = false
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SettingsButton(
                                modifier = Modifier.size(90.dp),
                                mainColor = Color.White,
                                backgroundColor = Color.Transparent,
                                shadowEnabled = false,
                                imageVector = vectorResource(Res.drawable.settings_icon),
                                enabled = false
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tap this button to open a player's settings menu",
                            fontSize = 20.scaledSp,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            style = defaultTextStyle(),
                        )
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                } else {
                    Column(
                        Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.wrapContentSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SettingsButton(
                                modifier = Modifier.size(70.dp).rotate(-90f),
                                mainColor = Color.White,
                                backgroundColor = Color.Transparent,
                                shadowEnabled = false,
                                imageVector = vectorResource(Res.drawable.down_arrow_icon),
                                enabled = false
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SettingsButton(
                                modifier = Modifier.size(90.dp),
                                mainColor = Color.White,
                                backgroundColor = Color.Transparent,
                                shadowEnabled = false,
                                imageVector = vectorResource(Res.drawable.pencil_icon),
                                enabled = false
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tap this button to open a player's customization menu",
                            fontSize = 20.scaledSp,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            style = defaultTextStyle(),
                        )
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }
    }
}
