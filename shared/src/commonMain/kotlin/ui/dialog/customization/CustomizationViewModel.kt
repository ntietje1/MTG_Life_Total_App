package ui.dialog.customization

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import domain.storage.IFileImageStore
import domain.storage.PreferencesRepository
import domain.storage.displayUri
import domain.state.game.PlayerProfileId
import domain.state.profile.PlayerBackground
import domain.state.profile.PlayerProfile
import domain.state.profile.PlayerProfileRepository
import model.Player
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

open class CustomizationViewModel(
    private val initialPlayer: Player,
    private val fileImageStore: IFileImageStore,
    private val profileRepository: PlayerProfileRepository,
    val preferencesRepository: PreferencesRepository,
    private val onPlayerChanged: (Player) -> Unit = {}
) : ViewModel() {
    private val _state = MutableStateFlow(CustomizationDialogState(initialPlayer))
    val state: StateFlow<CustomizationDialogState> = _state.asStateFlow()

    fun revertChanges() {
        setPlayer(initialPlayer)
        val imageUri = when (initialPlayer.background) {
            PlayerBackground.None -> initialPlayer.imageString?.let { fileImageStore.localImageUri(it) ?: it }
            else -> initialPlayer.background.displayUri(fileImageStore)
        }
        setPlayerBackground(
            background = initialPlayer.background,
            imageUri = imageUri
        )
        setChangeWasMade(false)
    }

    fun onImageFileSelected(file: ByteArray) {
        viewModelScope.launch {
            setBackground(PlayerBackground.LocalImage(fileImageStore.saveImage(file)))
        }
    }

    open fun setPlayer(player: Player) {
        _state.value = _state.value.copy(player = player)
        setChangeNameField(TextFieldValue(player.name, selection = TextRange(player.name.length)))
        onPlayerChanged(player)
    }

    fun onChangeImage(uri: String) {
        viewModelScope.launch {
            setBackground(PlayerBackground.ProviderImage(uri))
        }
    }

    fun onChangeBackgroundColor(color: Color) {
        setPlayerBackground(background = PlayerBackground.None, imageUri = null)
        setBackgroundColor(color)
    }

    fun onChangeTextColor(color: Color) {
        setTextColor(color)
    }

    private suspend fun setBackground(background: PlayerBackground) {
        setPlayerBackground(background = background, imageUri = null)
        delay(50)
        setPlayerBackground(background = background, imageUri = background.displayUri(fileImageStore))
    }

    private fun setPlayerBackground(background: PlayerBackground, imageUri: String?) {
        setChangeWasMade(true)
        setPlayer(state.value.player.copy(imageString = imageUri, background = background))
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

    fun resetRouteStack() {
        _state.value = state.value.copy(routeStack = listOf(CustomizationRoute.Default))
    }

    fun openRoute(route: CustomizationRoute) {
        _state.value = state.value.copy(routeStack = state.value.routeStack + route)
    }

    fun goBack(): Boolean {
        if (state.value.routeStack.size <= 1) return false
        _state.value = state.value.copy(routeStack = state.value.routeStack.dropLast(1))
        return true
    }

    fun loadPlayerPrefs(): List<Player> {
        val currentDefaultName = "P${state.value.player.playerNum}"
        return profileRepository.loadProfiles()
            .map { it.toPlayer(state.value.player.playerNum) }
            .sortedBy { player -> if (player.name == currentDefaultName) 0 else 1 }
    }

    fun deletePlayerPref(player: Player) {
        profileRepository.deleteProfile(PlayerProfileId(player.name))
    }

    private fun PlayerProfile.toPlayer(playerNum: Int): Player {
        return Player(
            playerNum = playerNum,
            name = displayName,
            color = Color(colors.backgroundArgb),
            textColor = Color(colors.textArgb),
            background = background,
            imageString = when (val value = background) {
                PlayerBackground.None -> null
                is PlayerBackground.LocalImage -> value.displayUri(fileImageStore)
                is PlayerBackground.ProviderImage -> value.url
                is PlayerBackground.CardArt -> value.url
            }
        )
    }
}
