package domain.usecase.player.state

import model.Player
import ui.lifecounter.CounterType

class ManagePlayerCounterUseCase {
    fun incrementCounter(player: Player, counterType: CounterType, value: Int): Player {
        return player.copy(counters = player.counters.toMutableList().apply {
            this[counterType.ordinal] += value
        })
    }

    fun setActiveCounter(player: Player, counterType: CounterType, active: Boolean): Player {
        return player.copy(activeCounters = player.activeCounters.toMutableList().apply {
            if (active) {
                add(counterType)
            } else {
                remove(counterType)
            }
        })
    }

    fun resetCounters(player: Player): Player {
        return player.copy(
            counters = List(CounterType.entries.size) { 0 },
            activeCounters = listOf()
        )
    }
}