package domain.usecase.player.customization

import domain.storage.ISettingsStore
import model.Player

class DeletePlayerCustomizationUseCase(
    private val settingsStore: ISettingsStore
) {
    operator fun invoke(player: Player) {
        settingsStore.deletePlayerPref(player)
    }
}