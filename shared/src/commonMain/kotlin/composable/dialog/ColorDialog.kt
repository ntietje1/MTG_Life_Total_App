package composable.dialog


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.checkmark
import lifelinked.shared.generated.resources.x_icon
import org.jetbrains.compose.resources.vectorResource
import theme.toHsv

/**
 * A dialog that allows the user to select a color
 * @param modifier the modifier for this composable
 * @param onDismiss the action to perform when the dialog is dismissed
 * @param initialColor the initial color of the dialog
 * @param setColor the callback to perform when the color is set
 */
@Composable
fun ColorDialog(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, initialColor: Color, setColor: (Color) -> Unit
) {
    SettingsDialog(
        modifier = modifier,
        backButtonEnabled = false,
        onDismiss = onDismiss
    ) {
        ColorSelector(initialColor = initialColor, setColor = setColor, onDismiss = onDismiss)
    }
}

/**
 * A color selector composable that allows the user to select a hue, saturation, and value
 * @param initialColor the initial color of the dialog
 * @param setColor the callback to perform when the color is set
 * @param onDismiss the action to perform when the dialog is dismissed
 */
@Composable
fun ColorSelector(initialColor: Color = Color.Red, setColor: (Color) -> Unit = {}, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {

        val hsv = remember {
            val hsv = initialColor.toHsv()

            mutableStateOf(
                Triple(hsv[0].coerceIn(0.0f, 360f), hsv[1], hsv[2])
            )
        }

        val backgroundColor = remember {
            derivedStateOf {
                Color.hsv(hsv.value.first.coerceIn(0.0f, 360f) , hsv.value.second.coerceIn(0.0f, 1.0f), hsv.value.third.coerceIn(0.0f, 1.0f))
            }
        }

        SatValPanel(hue = hsv.value.first.coerceIn(0.0f, 360f)) { sat, value ->
            hsv.value = Triple(hsv.value.first.coerceIn(0.0f, 360f), sat.coerceIn(0.0f, 1.0f), value.coerceIn(0.0f, 1.0f))
            println("hue = ${hsv.value.first}, sat = $sat, value = $value")
        }

        Spacer(modifier = Modifier.height(32.dp))

        HueBar { hue ->
            hsv.value = Triple(hue.coerceIn(0.0f, 360f), hsv.value.second.coerceIn(0.0f, 1.0f), hsv.value.third.coerceIn(0.0f, 1.0f))
            println("hue = $hue, sat = ${hsv.value.second}, value = ${hsv.value.third}")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.wrapContentSize(), horizontalArrangement = Arrangement.Center
        ) {
            Column(Modifier.wrapContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                SettingsButton(
                    modifier = Modifier.size(100.dp),
                    imageVector = vectorResource(Res.drawable.x_icon),
                    shape = RoundedCornerShape(10),
                    shadowEnabled = false,
                    backgroundColor = initialColor,
                    mainColor = if (initialColor.luminance() > 0.5f) Color.Black else Color.White,
                    textSizeMultiplier = 1.5f,
                    onTap = {
                        setColor(initialColor)
                        onDismiss()
                    }
                )
                Text(text = "Cancel", color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(modifier = Modifier.width(32.dp))
            Column(Modifier.wrapContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                SettingsButton(
                    modifier = Modifier.size(100.dp),
                    imageVector = vectorResource(Res.drawable.checkmark),
                    shape = RoundedCornerShape(10),
                    shadowEnabled = false,
                    backgroundColor = backgroundColor.value,
                    mainColor = if (backgroundColor.value.luminance() > 0.5f) Color.Black else Color.White,
                    textSizeMultiplier = 1.5f,
                    onTap = {
                        setColor(backgroundColor.value)
                        onDismiss()
                    }
                )
                Text(text = "Confirm", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

/**
 * A hue bar composable that allows the user to select a hue
 * @param setColor the callback to perform when the color is set
 */
@Composable
fun HueBar(setColor: (Float) -> Unit) {
    val pressOffset = remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(modifier = Modifier
        .width(300.dp)
        .height(40.dp)
    ) {
        val maxWidth = maxWidth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val x = change.position.x
                        pressOffset.value = Offset(x, 0f)
                    }
                }
        ) {

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val selectedHue = pressOffset.value.x * 360f / size.width
                setColor(selectedHue)

                val hueColors = List(size.width.toInt()) { index ->
                    val hue = 360f * index / size.width.toInt()
                    Color.hsv(hue, 1f, 1f)
                }

                val rectWidth = size.width / hueColors.size
                for (i in hueColors.indices) {
                    drawRect(
                        color = hueColors[i],
                        topLeft = Offset(i * rectWidth, 0f),
                        size = Size(rectWidth, size.height)
                    )
                }

                drawCircle(
                    color = Color.White,
                    radius = size.height / 2,
                    center = Offset(pressOffset.value.x, size.height / 2),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

/**
 * A saturation and value panel composable that allows the user to select a saturation and value
 * @param hue the hue of the color
 * @param setSatVal the callback to perform when the saturation and value are set
 */
@Composable
fun SatValPanel(
    hue: Float, setSatVal: (Float, Float) -> Unit
) {
    BoxWithConstraints(
        Modifier.wrapContentSize()
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        val pressPosn = remember {
            mutableStateOf(Offset.Zero)
        }
    Box(
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures { position, change ->
                    val x = position.position.x
                    val y = position.position.y
                    pressPosn.value = Offset(x, y)
                    val sat = x / maxWidth.value
                    val value = 1f - y / maxHeight.value
                    setSatVal(sat, value)
                }
            }
    ) {
        val color = Color.hsv(hue, 1f, 1f)
        val satShader = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, color),
            startX = 0f,
            endX = maxWidth.value
        )
        val valShader = Brush.verticalGradient(
            colors = listOf(Color.White, Color.Black),
            startY = 0f,
            endY = maxHeight.value
        )

        Canvas(
            modifier = Modifier
                .size(300.dp)
        ) {

            drawRect(
                color = Color.White,
                topLeft = Offset.Zero,
                size = size
            )

            drawRect(
                brush = valShader,
                topLeft = Offset.Zero,
                size = size,
            )
            drawRect(
                brush = satShader,
                topLeft = Offset.Zero,
                size = size,
                blendMode = BlendMode.Multiply
            )

            drawCircle(
                color = Color.White, radius = 8.dp.toPx(), center = pressPosn.value, style = Stroke(
                    width = 2.dp.toPx()
                )
            )

            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = pressPosn.value,
            )
        }
        }
    }
}