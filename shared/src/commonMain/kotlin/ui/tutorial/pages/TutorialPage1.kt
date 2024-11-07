package ui.tutorial.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.IImageManager
import data.ISettingsManager
import data.Player
import di.NotificationManager
import lifelinked.shared.generated.resources.Res
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
import ui.lifecounter.playerbutton.PlayerButtonState
import ui.lifecounter.playerbutton.PlayerButtonViewModel


@Composable
fun TutorialPage1(
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
                if (state.value.player.life <= 20) {
                    onComplete()
                }
            }

            override fun incrementLife(value: Int) {
                super.incrementLife(value)
                checkComplete()
            }

            override fun incrementCommanderDamage(value: Int, partner: Boolean) {
                super.incrementCommanderDamage(value, partner)
                checkComplete()
            }

            override fun onCommanderButtonClicked() {
                this.notificationManager.showNotification("Commander damage disabled", 3000)
            }

            override fun onSettingsButtonClicked() {
                this. notificationManager.showNotification("Settings disabled", 3000)
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

    LifeCounterScreen(
        modifier = modifier,
        viewModel = remember {
            MockLifeCounterViewModelPage1(
                lifeCounterState = LifeCounterState(showButtons = true, showLoadingScreen = false),
                settingsManager = mockSettingsManagerPage1,
                imageManager = mockImageManager,
                notificationManager = notificationManager
            )
        },
        goToPlayerSelectScreen = {},
        goToTutorialScreen = {},
        firstNavigation = false
    )
    if (showHint) {
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
                    text = "Tap up/down on a player to adjust their life total",
                    fontSize = 20.scaledSp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    style = defaultTextStyle(),
                )
            }
        }
    }
}
