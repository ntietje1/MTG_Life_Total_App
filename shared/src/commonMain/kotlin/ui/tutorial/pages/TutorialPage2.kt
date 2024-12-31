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
import lifelinked.shared.generated.resources.commander_solid_icon
import lifelinked.shared.generated.resources.down_arrow_icon
import lifelinked.shared.generated.resources.sword_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.defaultTextStyle
import theme.scaledSp
import ui.SettingsButton
import ui.dialog.MiddleButtonDialogState
import ui.lifecounter.LifeCounterScreen
import ui.lifecounter.LifeCounterState
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonState
import ui.lifecounter.playerbutton.PlayerButtonViewModel


@Composable
fun TutorialPage2(
    modifier: Modifier = Modifier,
    showHint: Boolean,
    onHintDismiss: () -> Unit,
    onComplete: () -> Unit,
    notificationManager: NotificationManager = koinInject()
) {
    val gameState = MockGameState()

    var stepOneComplete by remember { mutableStateOf(false) }
    var complete by remember { mutableStateOf(false) }

    class MockLifeCounterViewModelPage2(
        lifeCounterState: LifeCounterState,
        settingsManager: ISettingsManager,
        imageManager: IImageManager,
        notificationManager: NotificationManager
    ) : MockLifeCounterViewModel(
        lifeCounterState, settingsManager, imageManager, notificationManager
    ) {
        override fun setMiddleButtonDialogState(value: MiddleButtonDialogState?) {
            this.notificationManager.showNotification("Settings menu disabled", 3000)
        }

        private fun checkStepOneComplete() {
            stepOneComplete = playerButtonViewModels.any { it.state.value.buttonState == PBState.COMMANDER_DEALER }
        }

        inner class MockPlayerButtonViewModelPage2(
            state: PlayerButtonState,
            settingsManager: ISettingsManager,
            imageManager: IImageManager,
            notificationManager: NotificationManager,
            setMonarchy: (Boolean) -> Unit,
            triggerSave: () -> Unit,
            resetPlayerColor: (Player) -> Player,
            moveTimerCallback: () -> Unit
        ) : MockPlayerButtonViewModel(
            state = state,
            settingsManager = settingsManager,
            imageManager = imageManager,
            notificationManager = notificationManager,
            setMonarchy = setMonarchy,
            triggerSave = triggerSave,
            resetPlayerColor = resetPlayerColor,
            moveTimerCallback = moveTimerCallback
        ) {

            private fun checkComplete() {
                if (state.value.player.commanderDamage.any { it >= 21 }) {
                    onComplete()
                    complete = true
                }
            }

            override fun onCommanderButtonClicked() {
                super.onCommanderButtonClicked()
                checkStepOneComplete()
            }

            override fun popBackStack() {
                super.popBackStack()
                checkStepOneComplete()
            }

            override fun incrementCommanderDamage(value: Int, partner: Boolean) {
                super.incrementCommanderDamage(value, partner)
                checkComplete()
            }


            override fun onSettingsButtonClicked() {
                this.notificationManager.showNotification("Settings disabled", 3000)
            }
        }

        override fun generatePlayerButtonViewModel(player: Player): PlayerButtonViewModel {
            return MockPlayerButtonViewModelPage2(
                state = gameState.playerStates.find { it.player.playerNum == player.playerNum } ?: PlayerButtonState(player),
                settingsManager = gameState.mockSettingsManager,
                imageManager = gameState.mockImageManager,
                notificationManager = this.notificationManager,
                setMonarchy = { this.setMonarchy(player.playerNum, it) },
                triggerSave = { this.savePlayerStates() },
                resetPlayerColor = { this.resetPlayerColor(it) },
                moveTimerCallback = { this.gameStateManager.moveTimer() }
            )
        }
    }

    val lifeCounterViewModel = remember {
        MockLifeCounterViewModelPage2(
            lifeCounterState = LifeCounterState(showButtons = true, showLoadingScreen = false),
            settingsManager = gameState.mockSettingsManager,
            imageManager = gameState.mockImageManager,
            notificationManager = notificationManager,
        )
    }

    TutorialScreenWrapper(
        modifier = modifier,
        blur = showHint,
        step = Pair(if (complete) 2 else if (stepOneComplete) 1 else 0, 2),
        instructions = if (complete) "Complete" else if (stepOneComplete) "Deal 21 commander damage to a player" else "Open a player's commander damage menu",
    ) {
        LifeCounterScreen(
            modifier = modifier,
            viewModel = lifeCounterViewModel,
            goToPlayerSelectScreen = {},
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
                                imageVector = vectorResource(Res.drawable.commander_solid_icon),
                                enabled = false
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tap this button to deal commander damage as that player",
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
                        SettingsButton(
                            modifier = Modifier.size(100.dp),
                            mainColor = Color.White,
                            backgroundColor = Color.Transparent,
                            shadowEnabled = false,
                            imageVector = vectorResource(Res.drawable.sword_icon),
                            enabled = false
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Increment a player's commander damage in the same way as life total",
                            fontSize = 20.scaledSp,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            style = defaultTextStyle(),
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        Text(
                            text = "Damage dealt here also applies to the player's life total",
                            fontSize = 20.scaledSp,
                            textAlign = TextAlign.Center,
                            color = Color.White,
                            style = defaultTextStyle(),
                        )
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }
            }
        }
    }
}
