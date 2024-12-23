package ui.dialog.color


import PhysicsDraggable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import data.Player
import data.Player.Companion.allPlayerColors
import org.koin.compose.koinInject
import theme.scaledSp
import theme.toHsv
import ui.dialog.customization.PlayerButtonPreview
import ui.lifecounter.playerbutton.PBState
import ui.modifier.routePointerChangesTo

@Composable
fun ColorPickerDialogContent(
    modifier: Modifier = Modifier,
    title: String,
    initialColor: Color,
    initialPlayer: Player,
    updateColor: (Color) -> Player,
    viewModel: ColorDialogViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var previewPlayer by remember { mutableStateOf(initialPlayer) }

    LaunchedEffect(initialColor) {
        if (state.oldColor == Color.Unspecified) {
            viewModel.init(initialColor)
        }
    }

    LaunchedEffect(state.newColor) {
        if (state.newColor != Color.Unspecified) {
            previewPlayer = updateColor(state.newColor)
        }
    }

    BoxWithConstraints(Modifier.wrapContentSize()) {
        val barWidth = remember(Unit) { min(maxWidth * 0.75f, maxHeight * 0.5f) }
        val padding = remember(Unit) { min(maxWidth / 15f, maxHeight / 25f) }
//        val buttonSize = remember(Unit) { min(maxWidth / 3.5f, maxHeight / 6f) }
        val barHeight = remember(Unit) { maxWidth / 12f }
        val titleSize = remember(Unit) { (maxWidth / 40f + maxHeight / 60f).value }
        val playerButtonPreviewHeight = remember(Unit) { min(maxWidth / 2f, maxHeight / 3.35f) }

        Column(
            modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(padding * 0.5f))

            Text(
                modifier = Modifier.wrapContentSize(), text = title, fontSize = titleSize.scaledSp, color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(modifier = Modifier.height(padding))

            PhysicsDraggable(
                Modifier.height(playerButtonPreviewHeight).aspectRatio(1.75f)
            ) {
                PlayerButtonPreview(
                    modifier = Modifier.fillMaxSize(),
                    name = previewPlayer.name,
                    lifeNumber = 40,
                    state = PBState.NORMAL,
                    isDead = false,
                    imageUri = null,
                    backgroundColor = previewPlayer.color,
                    accentColor = previewPlayer.textColor,
                )
            }

            Spacer(modifier = Modifier.height(padding))

            ColorGrid(
                modifier = Modifier.width(barWidth).height(barHeight * 2.5f).align(Alignment.CenterHorizontally),
                colors = listOf(state.oldColor, Color.White, Color.Black) + allPlayerColors,
                selectedColor = state.newColor,
                onColorSelected = { color ->
                    val (h, s, v) = color.toHsv()
                    viewModel.setHue(h, false)
                    viewModel.setSaturation(s, false)
                    viewModel.setValue(v, false)
                    updateColor(state.newColor)
                }
            )

            Spacer(modifier = Modifier.height(padding))

            HueBar(
                modifier = Modifier.width(barWidth).height(barHeight),
                initialHue = state.hue
            ) { value ->
                viewModel.setHue(value)
                updateColor(state.newColor)
            }

            Spacer(modifier = Modifier.height(padding))

            SatBar(
                modifier = Modifier.width(barWidth).height(barHeight),
                initialSaturation = state.saturation,
                hue = state.hue
            ) { value ->
                viewModel.setSaturation(value)
                updateColor(state.newColor)
            }

            Spacer(modifier = Modifier.height(padding))

            ValBar(
                modifier = Modifier.width(barWidth).height(barHeight),
                initialValue = state.value,
                hue = state.hue
            ) { value ->
                viewModel.setValue(value)
                updateColor(state.newColor)
            }

            Spacer(modifier = Modifier.height(padding * 3f))
        }
    }
}

@Composable
fun FloatBar(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    initialValue: Float,
    setValue: (Float) -> Unit
) {
    var barSize by remember { mutableStateOf(Size.Zero) }
    var pressOffset by remember(barSize, initialValue) {
        mutableStateOf(initialValue * barSize.width)
    }

    LaunchedEffect(barSize, initialValue) {
        pressOffset = initialValue * barSize.width
    }

    BoxWithConstraints(modifier = modifier) {
        val pointerStrokeSize = (3.dp + maxWidth / 100).value

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .pointerInput(Unit) {
                    routePointerChangesTo(
                        onMove = { change, _ ->
                            pressOffset = change.position.x.coerceIn(barSize.height / 8f, barSize.width - barSize.height / 8f)
                            setValue(pressOffset / barSize.width)
                        },
                        onDown = {
                            pressOffset = it.position.x.coerceIn(barSize.height / 8f, barSize.width - barSize.height / 8f)
                            setValue(pressOffset / barSize.width)
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                barSize = size

                // Draw the gradient
                drawRect(
                    brush = Brush.horizontalGradient(colors),
                    size = size
                )

                // Draw the selector
                drawCircle(
                    color = Color.White,
                    radius = size.height / 2 - pointerStrokeSize / 2,
                    center = Offset(pressOffset, size.height / 2),
                    style = Stroke(width = pointerStrokeSize)
                )
            }
        }
    }
}

@Composable
fun HueBar(
    modifier: Modifier = Modifier,
    initialHue: Float,
    setHue: (Float) -> Unit
) {
    val colors = remember {
        listOf(
            Color.hsv(0f, 1f, 1f),    // Red
            Color.hsv(60f, 1f, 1f),   // Yellow
            Color.hsv(120f, 1f, 1f),  // Green
            Color.hsv(180f, 1f, 1f),  // Cyan
            Color.hsv(240f, 1f, 1f),  // Blue
            Color.hsv(300f, 1f, 1f),  // Magenta
            Color.hsv(360f, 1f, 1f)   // Red
        )
    }

    FloatBar(
        modifier = modifier,
        colors = colors,
        initialValue = initialHue / 360f,
        setValue = {
            setHue(it * 360f)
        }
    )
}

@Composable
fun SatBar(
    modifier: Modifier = Modifier,
    initialSaturation: Float,
    hue: Float,
    setSaturation: (Float) -> Unit
) {
    val colors = remember(hue) {
        listOf(
            Color.hsv(hue, 0f, 1f),
            Color.hsv(hue, 1f, 1f)
        )
    }

    FloatBar(
        modifier = modifier,
        colors = colors,
        initialValue = initialSaturation,
        setValue = setSaturation
    )
}

@Composable
fun ValBar(
    modifier: Modifier = Modifier,
    initialValue: Float,
    hue: Float,
    setValue: (Float) -> Unit
) {
    val colors = remember(hue) {
        listOf(
            Color.hsv(hue, 1f, 0f),
            Color.hsv(hue, 1f, 1f)
        )
    }

    FloatBar(
        modifier = modifier,
        colors = colors,
        initialValue = initialValue,
        setValue = setValue
    )
}

@Composable
fun ColorGrid(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = 2
    val columns = 6

    BoxWithConstraints(modifier) {
        val padding = remember(Unit) { (min(maxWidth / columns, maxHeight / rows) / 5) }
        val circleSize = remember(Unit) {
            (min(maxWidth / columns, maxHeight / rows) - padding)
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(padding)
        ) {
            for (row in 0 until rows) {
                Row(
                    Modifier.wrapContentSize().align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(padding)
                ) {
                    for (col in 0 until columns) {
                        val index = row * columns + col
                        if (index < colors.size) {
                            ColorCircle(
                                modifier = Modifier.size(circleSize),
                                color = colors[index],
                                isSelected = colors[index] == selectedColor,
                                onClick = { onColorSelected(colors[index]) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(color)
                .then(
                    if (isSelected) {
                        Modifier.border(maxHeight / 15f, MaterialTheme.colorScheme.onSurface, CircleShape)
                    } else {
                        Modifier
                    }
                )
                .clickable(onClick = onClick)
        )
    }
}
