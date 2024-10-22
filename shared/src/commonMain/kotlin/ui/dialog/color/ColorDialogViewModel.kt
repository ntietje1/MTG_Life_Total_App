package ui.dialog.color

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import theme.toHsv

class ColorDialogViewModel: ViewModel() {
    private val _state = MutableStateFlow(ColorDialogState())
    val state: StateFlow<ColorDialogState> = _state.asStateFlow()

    fun init(color: Color) {
        val values = colorToTriple(color)
        _state.value = _state.value.copy(hue = values.first, saturation = values.second, value = values.third, satValPosn = Offset.Zero)
        setNewColor(color)
    }

    fun setHue(hue: Float) {
        _state.value = _state.value.copy(hue = hue.coerceIn(0.0f, 360f))
        updateNewColor()
    }

    fun setSaturation(saturation: Float) {
        _state.value = _state.value.copy(saturation = saturation.coerceIn(0.01f, 1.0f))
        updateNewColor()
    }

    fun setValue(value: Float) {
        _state.value = _state.value.copy(value = value.coerceIn(0.01f, 1.0f))
        updateNewColor()
    }

    fun setSatValPosn(posn: Offset) {
        _state.value = _state.value.copy(satValPosn = posn)
//        updateNewColor()
    }

    private fun colorToTriple(color: Color): Triple<Float, Float, Float> {
        val hsv = color.toHsv()
        return Triple(hsv[0], hsv[1], hsv[2])
    }

    private fun updateNewColor() {
        setNewColor(calculateColor())
    }

    private fun setNewColor(color: Color) {
        _state.value = _state.value.copy(newColor = color)
    }

    fun calculateColor(): Color {
        return Color.hsv(state.value.hue, state.value.saturation, state.value.value)
    }
}