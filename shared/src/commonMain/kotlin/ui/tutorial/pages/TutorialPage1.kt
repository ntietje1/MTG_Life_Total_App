package ui.tutorial.pages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import domain.system.NotificationManager
import org.koin.compose.koinInject


@Composable
fun TutorialPage1(
    modifier: Modifier = Modifier,
    showHint: Boolean,
    onHintDismiss: () -> Unit,
    onComplete: () -> Unit,
    notificationManager: NotificationManager = koinInject()
) {
//    val gameState = MockGameState()
//
//    var complete by remember { mutableStateOf(false) }
//
//    class MockLifeCounterViewModelPage1(
//        lifeCounterState: LifeCounterState,
//        settingsManager: ISettingsManager,
//        imageManager: IImageManager,
//        notificationManager: NotificationManager
//    ) : MockLifeCounterViewModel(
//        lifeCounterState, settingsManager, imageManager, notificationManager
//    ) {
//        override fun setMiddleButtonDialogState(value: MiddleButtonDialogState?) {
//            this.notificationManager.showNotification("Settings menu disabled", 3000)
//        }
//
//        inner class MockPlayerButtonViewModelPage1(
//            state: PlayerButtonState,
//            settingsManager: ISettingsManager,
//            imageManager: IImageManager,
//            notificationManager: NotificationManager,
//            customizationManager: PlayerCustomizationManager,
//            commanderDamageManager: CommanderDamageManager,
//            timerManager: TimerManager
//        ) : MockPlayerButtonViewModel(
//            state = state,
//            settingsManager = settingsManager,
//            imageManager = imageManager,
//            notificationManager = notificationManager,
//            customizationManager = customizationManager,
//            commanderDamageManager = commanderDamageManager,
//            timerManager = timerManager
//        ) {
//
//            private fun checkComplete() {
//                if (state.value.player.life == 20) {
//                    onComplete()
//                    complete = true
//                }
//            }
//
//            override fun incrementLife(value: Int) {
//                super.incrementLife(value)
//                if (value < 0) {
//                    checkComplete()
//                }
//            }
//
//            override fun onCommanderButtonClicked() {
//                this.notificationManager.showNotification("Commander damage disabled", 3000)
//            }
//
//            override fun onSettingsButtonClicked() {
//                this.notificationManager.showNotification("Settings disabled", 3000)
//            }
//        }
//
//        override fun generatePlayerButtonViewModel(player: Player): PlayerButtonViewModel {
//            return MockPlayerButtonViewModelPage1(
//                state = gameState.playerStates.find { it.player.playerNum == player.playerNum } ?: PlayerButtonState(player),
//                settingsManager = gameState.mockSettingsManager,
//                imageManager = gameState.mockImageManager,
//                notificationManager = this.notificationManager,
//                customizationManager = this.playerCustomizationManager,
//                commanderDamageManager = this.commanderManager,
//                timerManager = this.timerManager
//            )
//        }
//    }
//
//    TutorialScreenWrapper(
//        modifier = modifier,
//        blur = showHint,
//        step = Pair(if (complete) 1 else 0, 1),
//        instructions = if (complete) "Complete" else "Reduce a player's life total to 20"
//    ) {
//        LifeCounterScreen(
//            modifier = Modifier.fillMaxSize(),
//            viewModel = remember {
//                MockLifeCounterViewModelPage1(
//                    lifeCounterState = gameState.lifeCounterState,
//                    settingsManager = gameState.mockSettingsManager,
//                    imageManager = gameState.mockImageManager,
//                    notificationManager = notificationManager
//                )
//            },
//            goToPlayerSelectScreen = {},
//            goToTutorialScreen = {},
//            firstNavigation = false
//        )
//        if (showHint) {
//            TutorialOverlayScreen(
//                onDismiss = onHintDismiss
//            ) {
//                Column(
//                    Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    SettingsButton(
//                        modifier = Modifier.size(90.dp),
//                        mainColor = Color.White,
//                        backgroundColor = Color.Transparent,
//                        shadowEnabled = false,
//                        imageVector = vectorResource(Res.drawable.sword_icon),
//                        enabled = false
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        text = "Tap up/down on a player to adjust their life total",
//                        fontSize = 20.scaledSp,
//                        textAlign = TextAlign.Center,
//                        color = Color.White,
//                        style = defaultTextStyle(),
//                    )
//                    Spacer(modifier = Modifier.height(120.dp))
//                }
//            }
//        }
//    }
}
