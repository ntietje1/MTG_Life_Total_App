package domain.usecase.player.state
import domain.state.game.PlayerLifeRecentChangeState
import domain.storage.ISettingsManager
import model.Player
import ui.lifecounter.CounterType

class ManagePlayerStateUseCase(
    private val settingsManager: ISettingsManager,
    private val playerLifeRecentChangeState: PlayerLifeRecentChangeState
) {
    fun resetPlayerState(player: Player): Player {
        return resetLife(player)

        //TODO: also reset using other state managers
    }

    fun setMonarchy(player: Player, value: Boolean): Player {
        return player.copy(monarch = value)
    }

    private fun resetLife(player: Player): Player {
        val startingLife = settingsManager.defaultStartingLife.value
        val lifeTotal = playerLifeRecentChangeState.setLife(player.playerNum, startingLife)
        return player.copy(lifeTotal = lifeTotal)
    }

    fun incrementLife(player: Player, value: Int): Player {
        val lifeTotal = playerLifeRecentChangeState.incrementLife(player.playerNum, value)
        return player.copy(lifeTotal = lifeTotal)
    }

    fun incrementCounter(player: Player, counterType: CounterType, value: Int): Player {
        return player //TODO: use counter usecase here
    }

    fun setActiveCounter(player: Player, counterType: CounterType, value: Boolean): Player {
        return player //TODO: use counter usecase here
    }

    fun resetCounters(player: Player): Player {
        return player //TODO: use counter usecase here
    }

    fun isPlayerDead(player: Player, autoKo: Boolean): Boolean {
        return player.setDead || (autoKo && (player.life <= 0 || player.commanderDamage.any { it.number >= 21 }))
    }

    fun toggleSetDead(player: Player, value: Boolean? = null): Player {
        return player.copy(setDead = value ?: !player.setDead)
    }
}