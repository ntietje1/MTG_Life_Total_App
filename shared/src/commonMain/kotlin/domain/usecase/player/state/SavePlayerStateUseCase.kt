package domain.usecase.player.state

import data.GameRepository
import model.Player
import ui.lifecounter.CounterType

class SavePlayerStateUseCase(
    private val gameRepository: GameRepository
) {
    operator fun invoke(player: Player) {
        gameRepository.updatePlayer(player)
    }

    fun saveCounter(player: Player, counterType: CounterType) {
        gameRepository.updateCounter(player.gameId, player.playerNum, counterType.name, player.counters[counterType]!!.number)
    }
}