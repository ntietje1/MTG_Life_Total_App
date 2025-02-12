package domain.usecase.player.customization

import domain.storage.ISettingsStore
import model.Player

class SavePlayerCustomizationUseCase(
    private val settingsStore: ISettingsStore
) {
    operator fun invoke(player: Player) {
        settingsStore.savePlayerPref(player)
        println("Saved player: ${player.name} imageUri: ${player.imageString}")
    }
}