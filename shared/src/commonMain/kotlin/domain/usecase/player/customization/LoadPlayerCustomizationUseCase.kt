package domain.usecase.player.customization

import domain.storage.IImageStore
import domain.storage.ISettingsStore
import model.Player

class LoadPlayerCustomizationUseCase(
    private val settingsManager: ISettingsStore,
    private val imageManager: IImageStore
) {
    fun getImagePath(imageString: String): String? {
        return when {
            imageString.startsWith("http") -> imageString
            imageString.startsWith("/data/") -> "file://$imageString"
            else -> imageManager.getImagePath(imageString)
        }
    }

    fun getFilteredPlayerProfiles(currentPlayer: Player): List<Player> {
        return mutableListOf<Player>().apply {
            addAll(settingsManager.loadPlayerPrefs().filter { it.name == "P${currentPlayer.playerNum}" })
            addAll(settingsManager.loadPlayerPrefs().filter { !it.isDefaultOrEmptyName() })
        }
    }
}