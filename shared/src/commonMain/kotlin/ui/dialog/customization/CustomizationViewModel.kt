package ui.dialog.customization

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.usecase.player.customization.DeletePlayerCustomizationUseCase
import domain.usecase.player.customization.LoadPlayerCustomizationUseCase
import domain.usecase.player.customization.ManagePlayerCustomizationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import model.Player

open class CustomizationViewModel(
    private val initialPlayer: Player,
    private val managePlayerCustomizationUseCase: ManagePlayerCustomizationUseCase,
    private val deletePlayerCustomizationUseCase: DeletePlayerCustomizationUseCase,
    private val loadPlayerCustomizationUseCase: LoadPlayerCustomizationUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(CustomizationDialogState(initialPlayer))
    val state: StateFlow<CustomizationDialogState> = _state.asStateFlow()

    fun getPlayerProfiles() = loadPlayerCustomizationUseCase.getFilteredPlayerProfiles(state.value.player)
    fun deletePlayerProfile(player: Player) = deletePlayerCustomizationUseCase(player)

    fun revertChanges() {
        setPlayer(initialPlayer)
        initialPlayer.imageString?.let { setImageUri(it) }
        setChangeWasMade(false)
    }

    open fun setPlayer(player: Player) {
        _state.value = _state.value.copy(player = player)
        setChangeNameField(TextFieldValue(player.name, selection = TextRange(player.name.length)))
    }

    fun onImageSelected(uri: String) {
        val locatedUri = loadPlayerCustomizationUseCase.getImagePath(uri)
        setImageUri(locatedUri)
        setChangeWasMade(true)
    }

    fun onImageFileSelected(file: ByteArray) {
        viewModelScope.launch {
            val updatedPlayer = managePlayerCustomizationUseCase.updatePlayerImageBytes(state.value.player, file)
            setPlayer(updatedPlayer)
            val imagePath = loadPlayerCustomizationUseCase.getImagePath(updatedPlayer.imageString!!)
            setImageUri(imagePath)
            setChangeWasMade(true)
        }
    }

    fun onChangeBackgroundColor(color: Color) {
        setImageUri(null)
        setBackgroundColor(color)
        setChangeWasMade(true)
    }

    fun onChangeTextColor(color: Color) {
        setTextColor(color)
        setChangeWasMade(true)
    }

    private fun setImageUri(uri: String?) {
        if (uri == state.value.player.imageString) return
        val updatedPlayer = managePlayerCustomizationUseCase.updatePlayerImageUri(
            state.value.player,
            uri
        )
        setPlayer(updatedPlayer)
    }

    private fun setBackgroundColor(color: Color) {
        if (color == state.value.player.color) return
        val updatedPlayer = managePlayerCustomizationUseCase.updatePlayerBackgroundColor(
            state.value.player,
            color
        )
        setPlayer(updatedPlayer)
    }

    private fun setTextColor(color: Color) {
        if (color == state.value.player.textColor) return
        val updatedPlayer = managePlayerCustomizationUseCase.updatePlayerAccentColor(
            state.value.player,
            color
        )
        setPlayer(updatedPlayer)
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