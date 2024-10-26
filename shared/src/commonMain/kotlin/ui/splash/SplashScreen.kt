package ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import di.VersionNumber
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.middle_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.MainColor
import theme.defaultTextStyle
import theme.scaledSp

@Composable
fun SplashScreen(
    goToTutorial: () -> Unit,
    goToLifeCounter: () -> Unit,
    currentVersionNumber: VersionNumber = koinInject()
) {
    BoxWithConstraints(
        Modifier.fillMaxSize()
    ) {
        val buttonSize = remember(Unit) { maxWidth * 0.5f }
        val buttonModifier = Modifier.width(buttonSize).aspectRatio(3f).padding(buttonSize / 20f)
        val textSize = remember(Unit) { buttonSize.value * 0.08f }
        val iconSize = remember(Unit) { maxWidth * 0.4f }
        val haptic  = LocalHapticFeedback.current
        Column(
            modifier = Modifier.fillMaxSize().align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Image(
                modifier = Modifier.size(iconSize), imageVector = vectorResource(Res.drawable.middle_icon), contentScale = ContentScale.Crop, contentDescription = null
            )
            Text(
                text = "LifeLinked",
                fontSize = textSize.scaledSp*1.5f,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary,
                style = defaultTextStyle()
            )
            Spacer(modifier = Modifier.size(buttonSize / 2f))
            Button(
                modifier = buttonModifier,
                onClick = {
                    haptic.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                    goToTutorial()
                },
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "View Tutorial",
                        fontSize = textSize.scaledSp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.background,
                        style = defaultTextStyle()
                    )
                }
            }
            Button(
                modifier = buttonModifier,
                onClick = {
                    haptic.performHapticFeedback(hapticFeedbackType = HapticFeedbackType.LongPress)
                    goToLifeCounter()
                },
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = MainColor
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Go to Life Counter",
                        fontSize = textSize.scaledSp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = defaultTextStyle()
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "LifeLinked version ${currentVersionNumber.value} by Nicholas Tietje",
                fontSize = textSize.scaledSp / 1.75f,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                style = defaultTextStyle()
            )
            Spacer(modifier = Modifier.weight(0.1f))
        }
    }
}