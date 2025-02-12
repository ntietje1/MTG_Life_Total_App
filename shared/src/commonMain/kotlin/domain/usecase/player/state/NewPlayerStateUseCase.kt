package domain.usecase.player.state

import domain.common.NumberWithRecentChange
import domain.storage.ISettingsStore
import model.Player

class NewPlayerStateUseCase(
    private val settingsManager: ISettingsStore
) {
    operator fun invoke(playerNum: Int): Player {
        val startingLife = settingsManager.defaultStartingLife.value
        val name = "P$playerNum"
        return Player(
            lifeTotal = NumberWithRecentChange(startingLife, 0),
            name = name,
            playerNum = playerNum,
        )
    }
}