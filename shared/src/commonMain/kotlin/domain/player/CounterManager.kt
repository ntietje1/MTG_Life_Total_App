package domain.player

import data.Player
import ui.lifecounter.CounterType

class CounterManager {
    fun incrementCounter(player: Player, counterType: CounterType, value: Int): Player {
        return player.copy(
            counters = player.counters.toMutableList().apply {
                this[counterType.ordinal] += value
            }
        )
    }

    fun setActiveCounters(player: Player, counterType: CounterType, active: Boolean): Player {
        return player.copy(activeCounters = player.activeCounters.toMutableList().apply {
            if (active) {
                add(counterType)
            } else {
                remove(counterType)
            }
        })
    }
} 