package ui.dialog.color

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import theme.toHsv

class ColorDialogViewModel : ViewModel() {
    private val _state = MutableStateFlow(ColorDialogState())
    val state: StateFlow<ColorDialogState> = _state.asStateFlow()

    fun init(color: Color) {
        val (h, s, v) = color.toHsv()
        _state.value = _state.value.copy(oldColor = color, hue = h, saturation = s, value = v, newColor = color)
    }

    fun setHue(hue: Float, coerce: Boolean = true) {
        _state.value = _state.value.copy(hue = if (coerce) hue.coerceIn(0.00f, 360f) else hue)
        updateNewColor()
    }

    fun setSaturation(saturation: Float, coerce: Boolean = true) {
        _state.value = _state.value.copy(saturation = if (coerce) saturation.coerceIn(0.01f, 1.0f) else saturation)
        updateNewColor()
    }

    fun setValue(value: Float, coerce: Boolean = true) {
        _state.value = _state.value.copy(value = if (coerce) value.coerceIn(0.01f, 1.0f) else value)
        updateNewColor()
    }

    private fun updateNewColor() {
        _state.value = _state.value.copy(newColor = Color.hsv(
            hue = state.value.hue,
            saturation = state.value.saturation,
            value = state.value.value,
            colorSpace = ColorSpaces.Srgb
        ))
    }
}