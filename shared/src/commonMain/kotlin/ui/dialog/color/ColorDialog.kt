package ui.dialog.color


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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.min
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.checkmark
import lifelinked.shared.generated.resources.x_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.scaledSp
import theme.toHsv
import ui.SettingsButton
import ui.dialog.SettingsDialog
import ui.modifier.routePointerChangesTo

@Composable
fun ColorDialog(
    modifier: Modifier = Modifier, onDismiss: () -> Unit, initialColor: Color, setColor: (Color) -> Unit
) {
    SettingsDialog(
        modifier = modifier,
        backButtonEnabled = false,
        onDismiss = onDismiss
    ) {
        ColorPickerDialogContent(modifier = Modifier.fillMaxSize(), title = "remove this", initialColor = initialColor, setColor = setColor, onDone = onDismiss)
    }
}

//TODO: color picker doesn't work when tapping, only dragging
@Composable
fun ColorPickerDialogContent(
    modifier: Modifier = Modifier,
    title: String,
    initialColor: Color,
    setColor: (Color) -> Unit = {},
    onDone: () -> Unit,
    viewModel: ColorDialogViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init(initialColor)
    }

    BoxWithConstraints(Modifier.wrapContentSize()) {
        val largePanelSize = remember(Unit) { min(maxWidth * 0.75f, maxHeight * 0.5f) }
        val padding = remember(Unit) { min(maxWidth / 15f, maxHeight / 25f) }
        val buttonSize = remember(Unit) { min(maxWidth / 3.5f, maxHeight / 6f) }
        val barHeight = remember(Unit) { maxWidth / 12f }
        val titleSize = remember(Unit) { (maxWidth / 40f + maxHeight / 60f).value }

        Column(
            modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(padding * 0.5f))

            Text(
                modifier = Modifier.wrapContentSize(), text = title, fontSize = titleSize.scaledSp, color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(padding * 1.5f))

            SatValPanel(
                modifier = Modifier.size(largePanelSize),
                hue = state.hue,
                initialSaturation = initialColor.toHsv()[1],
                initialValue = initialColor.toHsv()[2],
                posn = state.satValPosn,
                setPosn = { posn -> viewModel.setSatValPosn(posn) },
            ) { sat, value ->
                viewModel.setSaturation(sat)
                viewModel.setValue(value)
            }

            Spacer(modifier = Modifier.height(padding))

            HueBar(
                modifier = Modifier.width(largePanelSize).height(barHeight),
                initialHue = initialColor.toHsv()[0]
            ) { hue ->
                viewModel.setHue(hue)
            }

            Spacer(modifier = Modifier.height(padding))

            Row(
                modifier = Modifier.wrapContentSize(), horizontalArrangement = Arrangement.Center
            ) {
                Column(Modifier.wrapContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    SettingsButton(
                        modifier = Modifier.size(buttonSize).padding(bottom = padding / 2f),
                        imageVector = vectorResource(Res.drawable.x_icon),
                        shape = RoundedCornerShape(10),
                        shadowEnabled = false,
                        backgroundColor = initialColor,
                        mainColor = if (initialColor.luminance() > 0.5f) Color.Black else Color.White,
                        textSizeMultiplier = 1.5f,
                        onTap = {
                            onDone()
                        }
                    )
                    Text(text = "Cancel", color = MaterialTheme.colorScheme.onPrimary, fontSize = titleSize.scaledSp / 2f)
                }

                Spacer(modifier = Modifier.width(padding))
                Column(Modifier.wrapContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                    SettingsButton(
                        modifier = Modifier.size(buttonSize).padding(bottom = padding / 2f),
                        imageVector = vectorResource(Res.drawable.checkmark),
                        shape = RoundedCornerShape(10),
                        shadowEnabled = false,
                        backgroundColor = state.newColor,
                        mainColor = if (state.newColor.luminance() > 0.5f) Color.Black else Color.White,
                        textSizeMultiplier = 1.5f,
                        onTap = {
                            setColor(state.newColor)
                            onDone()
                        }
                    )
                    Text(text = "Confirm", color = MaterialTheme.colorScheme.onPrimary, fontSize = titleSize.scaledSp / 2f)
                }
            }

            Spacer(modifier = Modifier.height(titleSize.dp * 2f))
        }
    }
}

@Composable
fun HueBar(
    modifier: Modifier = Modifier,
    initialHue: Float = 0f,
    setHue: (Float) -> Unit
) {
    var pressOffset by remember { mutableStateOf( 0f) }
    var hueBarWidth by remember { mutableStateOf(0f) }

    LaunchedEffect(initialHue, hueBarWidth) {
        if (pressOffset == 0.0f) {
            val initialOffsetX = (initialHue / 360f) * hueBarWidth
            pressOffset = initialOffsetX
            setHue(initialHue)
        }
    }

    BoxWithConstraints(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val x = change.position.x
                        pressOffset = x
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                hueBarWidth = size.width
                val selectedHue = pressOffset * 360f / size.width
                setHue(selectedHue)

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
                    center = Offset(pressOffset, size.height / 2),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun SatValPanel(
    modifier: Modifier = Modifier,
    hue: Float = 0f,
    initialSaturation: Float = 1f,
    initialValue: Float = 1f,
    posn: Offset = Offset(0f, 0f),
    setPosn: (Offset) -> Unit = {},
    setSatVal: (Float, Float) -> Unit
) {
//    println("GOT POSN: $posn")
    BoxWithConstraints(
        Modifier.wrapContentSize()
    ) {
        val maxWidth = maxWidth
        val maxHeight = maxHeight
        fun updatePosn(x: Float, y: Float) {
            setPosn(Offset(x, y))
            val sat = x / (maxWidth.value*1.15f) / 2
            val value = 1f - y / (maxHeight.value*1.30f)
            setSatVal(sat, value)
        }
//        val size = Size(maxWidth.value, maxHeight.value)
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clip(RoundedCornerShape(5))
                .pointerInput(Unit) {
                    routePointerChangesTo(
                        onDown = {
                            updatePosn(it.position.x, it.position.y)
                        },
                        onMove = { pointerInputChange, _ ->
                            updatePosn(pointerInputChange.position.x, pointerInputChange.position.y)
                        }
                    )
                }
        ) {
            LaunchedEffect(initialValue, initialSaturation, posn) {
                if (posn == Offset.Zero) {
                    val x = initialSaturation * maxWidth.value*2*1.15f
                    val y = (1f - initialValue) * maxHeight.value*1.30f
                    setPosn(Offset(x, y))
                }
            }
            val satShader = remember(hue) {
                Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.hsv(hue, 1f, 1f)),
                    startX = 0f,
                    endX = maxWidth.value*2
                )
            }
            val valShader = remember {
                Brush.verticalGradient(
                    colors = listOf(Color.White, Color.Black),
                    startY = 0f,
                    endY = maxHeight.value*1.30f
                )
            }

            Canvas(
                modifier = modifier
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
                    color = Color.White, radius = 8.dp.toPx(), center = posn, style = Stroke(
                        width = 2.dp.toPx()
                    )
                )

                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = posn,
                )
            }
        }
    }
}