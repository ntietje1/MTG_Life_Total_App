package domain.usecase.player.state

import domain.common.NumberWithRecentChange
import model.Player
import ui.lifecounter.CounterType

class ManagePlayerCounterUseCase {
    fun incrementCounter(player: Player, counterType: CounterType, value: Int): Player {
        return player.copy(
            counters = player.counters.toMutableMap().apply {
                this[counterType] = this.getOrPut(counterType) { NumberWithRecentChange(0, 0) }.copy(
                    number = this[counterType]!!.number + value
                )
            }
        )
    }

    fun setActiveCounter(player: Player, counterType: CounterType, active: Boolean): Player {
        return player.copy(
            counters = player.counters.toMutableMap().apply {
                if (active) {
                    this[counterType] = this.getOrPut(counterType) { NumberWithRecentChange(0, 0) }
                } else {
                    this.remove(counterType)
                }
            }
        )
    }

    fun resetCounters(player: Player): Player {
        return player.copy(
            counters = mapOf()
        )
    }
}