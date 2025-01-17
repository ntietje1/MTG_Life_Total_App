package ui.dialog.customization

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.storage.IImageManager
import domain.storage.ISettingsManager
import model.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class CustomizationViewModel(
    private val initialPlayer: Player,
    val imageManager: IImageManager,
    val settingsManager: ISettingsManager
) : ViewModel() {
    private val _state = MutableStateFlow(CustomizationDialogState(initialPlayer))
    val state: StateFlow<CustomizationDialogState> = _state.asStateFlow()

    fun revertChanges() {
        setPlayer(initialPlayer)
        initialPlayer.imageString?.let { onChangeImage(it) }
        setChangeWasMade(false)
    }

    fun onImageFileSelected(file: ByteArray) {
        var copiedUri = ""
        viewModelScope.launch {
            copiedUri = imageManager.copyImageToLocalStorage(file, state.value.player.name)
        }.invokeOnCompletion {
            onChangeImage(copiedUri)
        }
    }

    open fun setPlayer(player: Player) {
        _state.value = _state.value.copy(player = player)
        setChangeNameField(TextFieldValue(player.name, selection = TextRange(player.name.length)))
    }

    fun onChangeImage(uri: String) {
        viewModelScope.launch {
            setImageUri(null)
            delay(50)
            setImageUri(
                when {
                    uri.startsWith("http") -> uri
                    uri.startsWith("/data/") -> "file://$uri"
                    else -> imageManager.getImagePath(uri)
                }
            )
        }
    }

    fun onChangeBackgroundColor(color: Color) {
        setImageUri(null)
        setBackgroundColor(color)
    }

    fun onChangeTextColor(color: Color) {
        setTextColor(color)
    }

    private fun setImageUri(uri: String?) {
        setChangeWasMade(true)
        setPlayer(state.value.player.copy(imageString = uri))
    }

    private fun setBackgroundColor(color: Color) {
        setChangeWasMade(true)
        setPlayer(state.value.player.copy(color = color))
    }

    private fun setTextColor(color: Color) {
        setChangeWasMade(true)
        setPlayer(state.value.player.copy(textColor = color))
    }

    fun showCameraWarning(value: Boolean? = null) {
        _state.value = state.value.copy(showCameraWarning = value ?: !state.value.showCameraWarning)
    }

    private fun setChangeWasMade(value: Boolean) {
        _state.value = state.value.copy(changeWasMade = value)
    }

    fun setColorChangeWasMade(value: Boolean) {
        _state.value = state.value.copy(colorChangeWasMade = value)
    }

    //TODO: show an error if an illegal name (i.e. empty) is entered
    fun setChangeNameField(value: TextFieldValue) {
        setChangeWasMade(true)
        _state.value = state.value.copy(changeNameTextField = value)
        if (value.text != state.value.player.name) setPlayer(state.value.player.copy(name = value.text))
    }

    fun setCustomizeMenuState(menuState: CustomizationMenuState) {
        _state.value = state.value.copy(customizationMenuState = menuState)
    }
}