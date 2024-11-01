package ui.tutorial.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.Player
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.placeholder_icon
import org.jetbrains.compose.resources.vectorResource
import theme.PlayerColor1
import theme.PlayerColor2
import theme.PlayerColor3
import theme.PlayerColor4
import theme.defaultTextStyle
import theme.scaledSp
import ui.SettingsButton
import ui.dialog.customization.CustomizationViewModel
import ui.lifecounter.LifeCounterScreen
import ui.lifecounter.LifeCounterState
import ui.lifecounter.playerbutton.AbstractPlayerButtonViewModel
import ui.lifecounter.playerbutton.PlayerButtonState

private class MockLifeCounterViewModelPage1(
    lifeCounterState: LifeCounterState, playerButtonViewModels: List<AbstractPlayerButtonViewModel>
) : MockLifeCounterViewModel(lifeCounterState, playerButtonViewModels) {

}

private class MockPlayerButtonViewModelPage1(
    playerButtonState: PlayerButtonState,
) : MockPlayerButtonViewModel(playerButtonState) {
    override val customizationViewmodel: CustomizationViewModel? = null

    override fun incrementLife(value: Int) {
        super.incrementLife(value)
        println("TRACKING TUTORIAL LIFE CHANGE: $value")
    }
}


@Composable
fun TutorialPage1(
    showInstructions: Boolean,
    onInstructionsDismiss: () -> Unit,
    onComplete: () -> Unit,
) {
    LifeCounterScreen(
        viewModel = MockLifeCounterViewModelPage1(
            lifeCounterState = LifeCounterState(showButtons = true, showLoadingScreen = false), playerButtonViewModels = listOf(
                MockPlayerButtonViewModelPage1(
                    PlayerButtonState(
                        Player(
                            life = 40, name = "Player 1", color = PlayerColor1
                        ),
                    )
                ), MockPlayerButtonViewModelPage1(
                    PlayerButtonState(
                        Player(
                            life = 40, name = "Player 2", color = PlayerColor2
                        ),
                    )
                ), MockPlayerButtonViewModelPage1(
                    PlayerButtonState(
                        Player(
                            life = 40, name = "Player 3", color = PlayerColor3
                        ),
                    )
                ), MockPlayerButtonViewModelPage1(
                    PlayerButtonState(
                        Player(
                            life = 40, name = "Player 4", color = PlayerColor4
                        ),
                    )
                )
            )
        ),
        toggleTheme = {},
        toggleKeepScreenOn = {},
        toggleAlt4PlayerLayout = {},
        goToPlayerSelectScreen = {},
        goToTutorialScreen = {},
        numPlayers = 4,
        alt4PlayerLayout = false,
        timerEnabled = false,
        firstNavigation = true
    )
    if (showInstructions) {
        TutorialInstructions1(onInstructionsDismiss)
    }
}

@Composable
fun TutorialInstructions1(
    onDismiss: () -> Unit
) {
        Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).pointerInput(Unit) {
                onDismiss()
            }
        ) {
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SettingsButton(
                    modifier = Modifier.size(90.dp),
                    mainColor = Color.White,
                    backgroundColor = Color.Transparent,
                    shadowEnabled = false,
                    imageVector = vectorResource(Res.drawable.placeholder_icon),
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