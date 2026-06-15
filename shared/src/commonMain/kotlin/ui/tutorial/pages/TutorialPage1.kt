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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import domain.system.NotificationManager
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.sword_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.defaultTextStyle
import theme.scaledSp
import ui.components.SettingsButton
import ui.lifecounter.LifeCounterScreen
import ui.lifecounter.playerbutton.PlayerButtonAction


@Composable
fun TutorialPage1(
    modifier: Modifier = Modifier,
    showHint: Boolean,
    onHintDismiss: () -> Unit,
    onComplete: () -> Unit,
    notificationManager: NotificationManager = koinInject()
) {
    val gameState = MockGameState()

    var complete by remember { mutableStateOf(false) }

    fun checkComplete(state: ui.lifecounter.LifeCounterState) {
        if (!complete && state.players.any { it.player.life == 20 }) {
            onComplete()
            complete = true
        }
    }

    TutorialScreenWrapper(
        modifier = modifier,
        blur = showHint,
        step = Pair(if (complete) 1 else 0, 1),
        instructions = if (complete) "Complete" else "Reduce a player's life total to 20"
    ) {
        LifeCounterScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = remember {
                TutorialLifeCounterController(
                    gameState = gameState,
                    notificationManager = notificationManager,
                    shouldOpenModal = { false },
                    blockedModalMessage = { "Settings menu disabled" },
                    blockedPlayerActionMessage = { action ->
                        when (action) {
                            PlayerButtonAction.ToggleCommanderDealer -> "Commander damage disabled"
                            PlayerButtonAction.ToggleSettings -> "Settings disabled"
                            else -> null
                        }
                    },
                    afterPlayerAction = { _, action, state ->
                        if (action == PlayerButtonAction.DecrementLife) {
                            checkComplete(state)
                        }
                    }
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
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}
