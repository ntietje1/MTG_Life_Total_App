package domain.usecase.player.state
import domain.state.game.CommanderDamageState
import domain.state.game.MonarchyState
import domain.state.game.PlayerLifeRecentChangeState
import domain.storage.ISettingsStore
import model.Player

class ManagePlayerStateUseCase(
    private val settingsManager: ISettingsStore,
    private val playerLifeRecentChangeState: PlayerLifeRecentChangeState,
    private val monarchyState: MonarchyState,
    private val commanderDamageState: CommanderDamageState,
    private val managePlayerCounterUseCase: ManagePlayerCounterUseCase
) {
    fun resetPlayerState(player: Player): Player {
        var updatedPlayer = resetLife(player)
        updatedPlayer = commanderDamageState.resetCommanderDamage(updatedPlayer)
        updatedPlayer = managePlayerCounterUseCase.resetCounters(updatedPlayer)
        return updatedPlayer
    }

    fun setMonarchy(player: Player, value: Boolean): Player {
        if (value) {
            monarchyState.setMonarchState(player.playerNum)
        } else {
            monarchyState.setMonarchState(null)
        }
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

    fun isPlayerDead(player: Player, autoKo: Boolean): Boolean {
        return player.setDead || (autoKo && (player.life <= 0 || player.commanderDamage.any { it.number >= 21 }))
    }

    fun toggleSetDead(player: Player, value: Boolean? = null): Player {
        return player.copy(setDead = value ?: !player.setDead)
    }
}