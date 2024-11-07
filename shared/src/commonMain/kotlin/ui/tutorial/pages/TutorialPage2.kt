package ui.tutorial.pages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.IImageManager
import data.ISettingsManager
import data.Player
import di.NotificationManager
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.commander_solid_icon
import lifelinked.shared.generated.resources.sword_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.PlayerColor2
import theme.PlayerColor5
import theme.PlayerColor6
import theme.PlayerColor7
import theme.defaultTextStyle
import theme.scaledSp
import ui.SettingsButton
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

    val playerStates = listOf(
        Player(
            life = 40, name = "Player 1", color = PlayerColor7, playerNum = 1
        ),
        Player(
            life = 40, name = "Player 2", color = PlayerColor2, playerNum = 2
        ),
        Player(
            life = 40, name = "Player 3", color = PlayerColor5, playerNum = 3
        ),
        Player(
            life = 40, name = "Player 4", color = PlayerColor6, playerNum = 4
        ),
    )

    val mockSettingsManagerPage1 = MockSettingsManager(
        autoKo = false,
        numPlayers = 4,
        alt4PlayerLayout = false,
        startingLife = 40,
        turnTimer = false,
        playerStates = playerStates,
        planarDeck = emptyList(),
        planarBackStack = emptyList(),
        playerPrefs = arrayListOf()
    )

    val mockImageManager = MockImageManager()

    class MockLifeCounterViewModelPage1(
        lifeCounterState: LifeCounterState,
        settingsManager: ISettingsManager,
        imageManager: IImageManager,
        notificationManager: NotificationManager
    ) : MockLifeCounterViewModel(
        lifeCounterState, settingsManager, imageManager, notificationManager
    ) {
        override fun openMiddleButtonDialog(value: Boolean) {
            this.notificationManager.showNotification("Settings menu disabled", 3000)
        }

        inner class MockPlayerButtonViewModelPage1(
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

            private fun checkComplete() {
                if (state.value.player.commanderDamage.any { it >= 21 }) {
                    onComplete()
                }
            }

            override fun receiveCommanderDamage(index: Int, value: Int) {
                super.receiveCommanderDamage(index, value)
                checkComplete()
            }


            override fun onSettingsButtonClicked() {
                this.notificationManager.showNotification("Settings disabled", 3000)
            }
        }

        override fun generatePlayerButtonViewModel(player: Player): PlayerButtonViewModel {
            return MockPlayerButtonViewModelPage1(
                state = PlayerButtonState(player),
                settingsManager = mockSettingsManagerPage1,
                imageManager = mockImageManager,
                notificationManager = this.notificationManager,
                onCommanderButtonClickedCallback = { this.onCommanderButtonClicked(it) },
                setAllMonarchy = { this.setAllMonarchy(it) },
                getCurrentDealer = { state.value.currentDealer },
                updateCurrentDealerMode = { this.setCurrentDealerIsPartnered(it) },
                triggerSave = { this.savePlayerStates() },
                resetPlayerColor = { this.resetPlayerColor(it) },
                moveTimerCallback = { this.moveTimer() }
            )
        }
    }

    val lifeCounterViewModel = remember {
        MockLifeCounterViewModelPage1(
            lifeCounterState = LifeCounterState(showButtons = true, showLoadingScreen = false),
            settingsManager = mockSettingsManagerPage1,
            imageManager = mockImageManager,
            notificationManager = notificationManager,
        )
    }

    LifeCounterScreen(
        modifier = modifier,
        viewModel = lifeCounterViewModel,
        goToPlayerSelectScreen = {},
        goToTutorialScreen = {},
        firstNavigation = false
    )
    if (showHint) {
        if (!lifeCounterViewModel.playerButtonViewModels.any { it.state.value.buttonState == PBState.COMMANDER_DEALER }) {
            TutorialOverlayScreen(
                onDismiss = onHintDismiss
            ) {
                val circleSize = 65.dp
                val xOffset = (-149.5).dp
                val yOffset = 58.5.dp
                Canvas(modifier = Modifier.size(circleSize).align(Alignment.Center).offset(x = xOffset, y = yOffset)) {
                    drawCircle(
                        color = Color.Red, style = Stroke(width = circleSize.value * 0.2f), radius = circleSize.value * 1.3f
                    )
                }
                val iconSize = circleSize * 0.55f
                SettingsButton(
                    modifier = Modifier.size(iconSize).align(Alignment.Center).offset(x = xOffset, y = yOffset).rotate(90f),
                    mainColor = Color.White,
                    backgroundColor = Color.Transparent,
                    shadowEnabled = false,
                    imageVector = vectorResource(Res.drawable.commander_solid_icon),
                    enabled = false
                )

                Column(
                    Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SettingsButton(
                        modifier = Modifier.size(90.dp),
                        mainColor = Color.White,
                        backgroundColor = Color.Transparent,
                        shadowEnabled = false,
                        imageVector = vectorResource(Res.drawable.commander_solid_icon),
                        enabled = false
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Tap the commander button on a player to deal commander damage as that player",
                        fontSize = 20.scaledSp,
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        style = defaultTextStyle(),
                    )
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        } else {
            TutorialOverlayScreen(
                onDismiss = onHintDismiss
            ) {
                Column(
                    Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SettingsButton(
                        modifier = Modifier.size(90.dp),
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
                        text = "Damage dealt here will also apply to the player's life total",
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
