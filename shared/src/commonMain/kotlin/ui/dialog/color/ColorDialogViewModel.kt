package ui.dialog.color

import androidx.compose.ui.graphics.Color
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

    fun setHue(hue: Float) {
        _state.value = _state.value.copy(hue = hue.coerceIn(0.0f, 360f))
        updateNewColor()
    }

    fun setSaturation(saturation: Float) {
        _state.value = _state.value.copy(saturation = saturation.coerceIn(0.05f, 1.0f))
        updateNewColor()
    }

    fun setValue(value: Float) {
        _state.value = _state.value.copy(value = value.coerceIn(0.05f, 1.0f))
        updateNewColor()
    }

    private fun updateNewColor() {
        _state.value = _state.value.copy(newColor = Color.hsv(state.value.hue, state.value.saturation, state.value.value))
    }
}