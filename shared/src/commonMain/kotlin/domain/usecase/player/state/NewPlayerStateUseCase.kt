package domain.usecase.player.state

import domain.common.NumberWithRecentChange
import domain.storage.ISettingsManager
import model.Player

class NewPlayerStateUseCase(
    private val settingsManager: ISettingsManager
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