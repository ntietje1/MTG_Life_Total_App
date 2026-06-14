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
import domain.state.game.SeatId
import domain.storage.IFileImageStore
import domain.storage.PreferencesRepository
import domain.state.profile.PlayerProfileRepository
import domain.system.NotificationManager
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.down_arrow_icon
import lifelinked.shared.generated.resources.pencil_icon
import lifelinked.shared.generated.resources.settings_icon
import model.Player
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.defaultTextStyle
import theme.scaledSp
import ui.components.SettingsButton
import ui.dialog.customization.CustomizationViewModel
import ui.lifecounter.LifeCounterModal
import ui.lifecounter.LifeCounterScreen
import ui.lifecounter.LifeCounterState
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonAction


@Composable
fun TutorialPage4(
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

    class MockLifeCounterViewModelPage4(
        lifeCounterState: LifeCounterState,
        private val preferencesRepository: PreferencesRepository,
        private val profileRepository: PlayerProfileRepository,
        private val fileImageStore: IFileImageStore,
        notificationManager: NotificationManager
    ) : MockLifeCounterViewModel(
        lifeCounterState, preferencesRepository, profileRepository, fileImageStore, notificationManager
    ) {
        override fun openModal(value: LifeCounterModal) {
            showNotification("Settings menu disabled", 3000)
        }

        private fun checkStepOneComplete() {
            stepOneComplete = state.value.players.any { it.buttonState == PBState.SETTINGS }
        }

        private fun checkStepTwoComplete() {
            stepTwoComplete = state.value.players.any { it.showCustomizeMenu }
            setBlurUI(stepTwoComplete)
            if (stepTwoComplete) {
                showNotification("Next: Change the appearance of the player", 3000)
            }
        }

        override fun onPlayerButtonAction(seatId: SeatId, action: PlayerButtonAction) {
            when (action) {
                PlayerButtonAction.ToggleCommanderDealer -> {
                    showNotification("Commander damage disabled", 3000)
                }
                is PlayerButtonAction.SetMonarch -> {
                    showNotification("Monarchy disabled", 3000)
                }
                is PlayerButtonAction.SetManualDeath -> {
                    showNotification("Auto KO disabled", 3000)
                }
                PlayerButtonAction.OpenCounters -> {
                    showNotification("Counters disabled", 3000)
                }
                else -> {
                    super.onPlayerButtonAction(seatId, action)
                    if (action == PlayerButtonAction.ToggleSettings) checkStepOneComplete()
                    if (action == PlayerButtonAction.OpenCustomization) checkStepTwoComplete()
                }
            }
        }

        override fun createCustomizationViewModel(
            seatId: SeatId,
            player: Player
        ): CustomizationViewModel {
            return MockCustomizationViewModelPage4(player)
        }

        inner class MockCustomizationViewModelPage4(
            initialPlayer: Player
        ) : CustomizationViewModel(
            initialPlayer = initialPlayer,
            profileRepository = profileRepository,
            preferencesRepository = preferencesRepository,
            fileImageStore = fileImageStore,
        ) {
            override fun setPlayer(player: Player) {
                super.setPlayer(player)
                complete = true
                onComplete()
            }
        }
    }

    val lifeCounterViewModel = remember {
        MockLifeCounterViewModelPage4(
            lifeCounterState = gameState.lifeCounterState,
        preferencesRepository = gameState.mockPreferencesRepository,
        profileRepository = gameState.mockProfileRepository,
        fileImageStore = gameState.mockFileImageStore,
        notificationManager = notificationManager,
        )
    }

    TutorialScreenWrapper(
        modifier = modifier,
        blur = showHint,
        step = Pair(if (complete) 3 else if (stepTwoComplete) 2 else if (stepOneComplete) 1 else 0, 3),
        instructions = if (complete) "Complete" else if (stepTwoComplete) "Change the appearance of the player" else if (stepOneComplete) "Open the player's customization menu &\nchange their appearance" else "Open a player's settings menu",
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
