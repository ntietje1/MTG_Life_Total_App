package ui.dialog.color


import PhysicsDraggable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import data.Player
import lifelinked.shared.generated.resources.Res
import lifelinked.shared.generated.resources.checkmark
import lifelinked.shared.generated.resources.x_icon
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import theme.scaledSp
import theme.toHsv
import ui.SettingsButton
import ui.dialog.customization.PlayerButtonPreview
import ui.lifecounter.playerbutton.PBState
import ui.modifier.routePointerChangesTo

@Composable
fun ColorPickerDialogContent(
    modifier: Modifier = Modifier,
    title: String,
    initialColor: Color,
    setColor: (Color) -> Unit = {},
    initialPlayer: Player,
    updatePlayerColor: (Color) -> Player,
    onDone: () -> Unit,
    viewModel: ColorDialogViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var previewPlayer by remember { mutableStateOf(initialPlayer) }

    LaunchedEffect(initialColor) {
        viewModel.init(initialColor)
    }

    LaunchedEffect(state.newColor) {
        previewPlayer = updatePlayerColor(state.newColor)
    }

    BoxWithConstraints(Modifier.wrapContentSize()) {
        val largePanelSize = remember(Unit) { min(maxWidth * 0.75f, maxHeight * 0.5f) }
        val padding = remember(Unit) { min(maxWidth / 15f, maxHeight / 25f) }
        val buttonSize = remember(Unit) { min(maxWidth / 3.5f, maxHeight / 6f) }
        val barHeight = remember(Unit) { maxWidth / 12f }
        val titleSize = remember(Unit) { (maxWidth / 40f + maxHeight / 60f).value }
        val playerButtonPreviewHeight = remember(Unit) { min(maxWidth / 2f, maxHeight / 3f) }

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

            HueBar(
                modifier = Modifier.width(largePanelSize).height(barHeight),
                color = state.newColor
            ) { value ->
                viewModel.setHue(value)
            }

            Spacer(modifier = Modifier.height(padding))

            SatBar(
                modifier = Modifier.width(largePanelSize).height(barHeight),
                color = state.newColor
            ) { value ->
                viewModel.setSaturation(value)
            }

            Spacer(modifier = Modifier.height(padding))

            ValBar(
                modifier = Modifier.width(largePanelSize).height(barHeight),
                color = state.newColor
            ) { value ->
                viewModel.setValue(value)
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
fun FloatBar(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    initialValue: Float,
    setValue: (Float) -> Unit
) {
    var barSize by remember { mutableStateOf(Size.Zero) }
    var pressOffset by remember(barSize) {
        mutableStateOf(initialValue * barSize.width)
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
    color: Color,
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

    val (hue, _, _) = color.toHsv()
    FloatBar(
        modifier = modifier,
        colors = colors,
        initialValue = hue / 360f,
        setValue = {
            setHue(it * 360f)
        }
    )
}

@Composable
fun SatBar(
    modifier: Modifier = Modifier,
    color: Color,
    setSaturation: (Float) -> Unit
) {
    val (hue, saturation, value) = color.toHsv()
    val colors = remember(hue, value) {
        listOf(
            Color.hsv(hue, 0f, 1f),
            Color.hsv(hue, 1f, 1f)
        )
    }

    FloatBar(
        modifier = modifier,
        colors = colors,
        initialValue = saturation,
        setValue = setSaturation
    )
}

@Composable
fun ValBar(
    modifier: Modifier = Modifier,
    color: Color,
    setValue: (Float) -> Unit
) {
    val (hue, saturation, value) = color.toHsv()
    val colors = remember(hue, saturation) {
        listOf(
            Color.hsv(hue, 1f, 0f),
            Color.hsv(hue, 1f, 1f)
        )
    }

    FloatBar(
        modifier = modifier,
        colors = colors,
        initialValue = value,
        setValue = setValue
    )
}
