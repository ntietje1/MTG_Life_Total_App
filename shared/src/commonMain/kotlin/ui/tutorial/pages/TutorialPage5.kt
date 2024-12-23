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
import data.IImageManager
import data.ISettingsManager
import data.Player
import di.NotificationManager
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.down_arrow_icon
import lifelinked.shared.generated.resources.pencil_icon
import lifelinked.shared.generated.resources.settings_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.defaultTextStyle
import theme.scaledSp
import ui.SettingsButton
import ui.dialog.MiddleButtonDialogState
import ui.lifecounter.LifeCounterScreen
import ui.lifecounter.LifeCounterState
import ui.lifecounter.playerbutton.PlayerButtonState
import ui.lifecounter.playerbutton.PlayerButtonViewModel


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

    class MockLifeCounterViewModelPage5(
        lifeCounterState: LifeCounterState,
        settingsManager: ISettingsManager,
        imageManager: IImageManager,
        notificationManager: NotificationManager
    ) : MockLifeCounterViewModel(
        lifeCounterState, settingsManager, imageManager, notificationManager
    ) {
        private fun checkStepOneOrTwoComplete() {
            when {
                state.value.middleButtonDialogState in listOf(MiddleButtonDialogState.PlayerNumber, MiddleButtonDialogState.FourPlayerLayout) -> {
                    stepTwoComplete = true
                    notificationManager.showNotification("Next: Change the number of players", 3000)
                }

                state.value.middleButtonDialogState != null -> {
                    stepOneComplete = true
                    notificationManager.showNotification("Next: Navigate to the player number menu", 3000)
                }
            }
        }

        override fun toggleDarkTheme(value: Boolean?) {
            notificationManager.showNotification("Changing theme disabled", 3000)
        }

        override fun setMiddleButtonDialogState(value: MiddleButtonDialogState?) {
            setBlurUI(value != null)
            when (value) {
                MiddleButtonDialogState.PlayerNumber, MiddleButtonDialogState.FourPlayerLayout, MiddleButtonDialogState.Default, null -> {
                    super.setMiddleButtonDialogState(value)
                    checkStepOneOrTwoComplete()
                }

                MiddleButtonDialogState.Counter -> {
                    notificationManager.showNotification("Counters menu disabled", 3000)
                }

                MiddleButtonDialogState.Settings -> {
                    notificationManager.showNotification("Settings menu disabled", 3000)
                }

                MiddleButtonDialogState.StartingLife -> {
                    notificationManager.showNotification("Starting life menu disabled", 3000)
                }

                MiddleButtonDialogState.CoinFlip -> {
                    notificationManager.showNotification("Coin flip menu disabled", 3000)
                }

                MiddleButtonDialogState.DiceRoll -> {
                    notificationManager.showNotification("Dice roll menu disabled", 3000)
                }

                MiddleButtonDialogState.Scryfall -> {
                    notificationManager.showNotification("Scryfall menu disabled", 3000)
                }

                MiddleButtonDialogState.PlaneChase -> {
                    notificationManager.showNotification("Planar deck menu disabled", 3000)
                }

                else -> {
                    notificationManager.showNotification("Menu disabled", 3000)
                }
            }
        }

        override fun setNumPlayers(value: Int) {
            super.setNumPlayers(value)
            complete = true
            onComplete()
        }

        inner class MockPlayerButtonViewModelPage5(
            state: PlayerButtonState,
            settingsManager: ISettingsManager,
            imageManager: IImageManager,
            notificationManager: NotificationManager,
            onCommanderButtonClickedCallback: (PlayerButtonViewModel) -> Unit,
            setAllMonarchy: (Boolean) -> Unit,
            getCurrentDealer: () -> PlayerButtonViewModel?,
            updateCurrentDealerMode: (Boolean) -> Unit,
            triggerSave: () -> Unit,
            resetPlayerColor: (Player) -> Player,
            moveTimerCallback: () -> Unit
        ) : MockPlayerButtonViewModel(
            state = state,
            settingsManager = settingsManager,
            imageManager = imageManager,
            notificationManager = notificationManager,
            onCommanderButtonClickedCallback = onCommanderButtonClickedCallback,
            setAllMonarchy = setAllMonarchy,
            getCurrentDealer = getCurrentDealer,
            updateCurrentDealerMode = updateCurrentDealerMode,
            triggerSave = triggerSave,
            resetPlayerColor = resetPlayerColor,
            moveTimerCallback = moveTimerCallback
        ) {
            override fun onCommanderButtonClicked() {
                this.notificationManager.showNotification("Commander damage disabled", 3000)
            }

            override fun onSettingsButtonClicked() {
                this.notificationManager.showNotification("Settings disabled", 3000)
            }
        }

        override fun generatePlayerButtonViewModel(player: Player): PlayerButtonViewModel {
            return MockPlayerButtonViewModelPage5(
                state = gameState.playerStates.find { it.player.playerNum == player.playerNum } ?: PlayerButtonState(player),
                settingsManager = gameState.mockSettingsManager,
                imageManager = gameState.mockImageManager,
                notificationManager = this.notificationManager,
                onCommanderButtonClickedCallback = { this.onCommanderButtonClicked(it) },
                setAllMonarchy = { this.setAllMonarchy(it) },
                getCurrentDealer = { state.value.currentDealer },
                updateCurrentDealerMode = { this.setCurrentDealerIsPartnered(it) },
                triggerSave = { this.savePlayerStates() },
                resetPlayerColor = { this.resetPlayerColor(it) },
                moveTimerCallback = { this.gameTimer.moveTimer() }
            )
        }
    }

    val lifeCounterViewModel = remember {
        MockLifeCounterViewModelPage5(
            lifeCounterState = LifeCounterState(showButtons = true, showLoadingScreen = false),
            settingsManager = gameState.mockSettingsManager,
            imageManager = gameState.mockImageManager,
            notificationManager = notificationManager,
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
