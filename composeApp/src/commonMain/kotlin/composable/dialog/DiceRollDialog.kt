package composable.dialog

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import composable.modifier.ShakeConfig
import composable.modifier.bounceClick
import composable.modifier.rememberShakeController
import composable.modifier.shake
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import theme.scaledSp

/**
 * A dialog that allows the user to roll dice
 * @param modifier the modifier for this composable
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun DiceRollDialogContent(
    modifier: Modifier = Modifier
) {
    GridDialogContent(modifier, title = "Tap to roll", items = listOf({
        DiceRollButton(value = 4, imageResource = painterResource("d4_icon.xml"))
    }, {
        DiceRollButton(value = 6, imageResource = painterResource("d6_icon.xml"))
    }, {
        DiceRollButton(value = 8, imageResource = painterResource("d8_icon.xml"))
    }, {
        DiceRollButton(value = 10, imageResource = painterResource("d10_icon.xml"))
    }, {
        DiceRollButton(value = 12, imageResource = painterResource("d12_icon.xml"))
    }, {
        DiceRollButton(value = 20, imageResource = painterResource("d20_icon.xml"))
    }))
}

/**
 * A button that rolls a die
 * @param value the value of the die
 * @param modifier the modifier for this composable
 * @param shape the shape of the button
 * @param backgroundColor the background color of the button
 * @param imageResource the image resource of the button
 * @param mainColor the color of the text
 * @param enabled whether the button is enabled
 * @param visible whether the button is visible
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun DiceRollButton(
    value: Int,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    backgroundColor: Color = Color.Transparent,
    imageResource: Painter = painterResource("d20_icon.xml"),
    mainColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
    visible: Boolean = true
) {

    var faceValue: Int by remember { mutableIntStateOf(value) }
    val haptic = LocalHapticFeedback.current

    val scope = rememberCoroutineScope()

    val shuffleScope = CoroutineScope(scope.coroutineContext)
    val shakeController = rememberShakeController()

    fun roll() {
        shuffleScope.launch {
            for (i in 1..5) {
                val result = (1..value).random()
                faceValue = result
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(60)
            }
        }
    }

    SettingsButton(modifier = modifier.bounceClick(amount = 0.04f).shake(shakeController),
        shape = shape,
        backgroundColor = backgroundColor,
        imageResource = imageResource,
        text = "D$value",
        mainColor = mainColor,
        shadowEnabled = false,
        enabled = enabled,
        visible = visible,
        onPress = {
            roll()
            shakeController.shake(ShakeConfig(6, translateX = 7.5f, rotate = 0.5f, rotateY = 12.5f))
        },
        overlay = {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = faceValue.toString(),
                    color = MaterialTheme.colorScheme.background,
                    fontWeight = FontWeight.Bold,
                    fontSize = maxHeight.value.scaledSp / 5,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.wrapContentHeight().fillMaxWidth().align(Alignment.Center)
                )
            }
        })
}

