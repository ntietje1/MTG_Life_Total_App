package domain.state.game

import domain.common.NumberWithRecentChange
import domain.common.RecentChangeValue
import kotlinx.coroutines.CoroutineScope
import model.Player

class PlayerLifeRecentChangeState (
    private val scope: CoroutineScope
    ) {
        private val lifeTotalTrackers = mutableMapOf<Int, RecentChangeValue>()

        fun clear() {
            lifeTotalTrackers.values.forEach { it.detach() }
            lifeTotalTrackers.clear()
        }

        fun attachLifeTracker(
            getCurrentPlayer: () -> Player,
            onUpdate: (Player) -> Unit
        ) {
            val player = getCurrentPlayer()
            lifeTotalTrackers[player.playerNum] = RecentChangeValue(
                initialValue = player.lifeTotal
            ) { newValue ->
                onUpdate(getCurrentPlayer().copy(lifeTotal = newValue))
            }.apply { attach(scope) }
        }

        fun incrementLife(playerNum: Int, value: Int): NumberWithRecentChange {
            val lifeTotalTracker = requireNotNull(lifeTotalTrackers[playerNum])
            lifeTotalTracker.increment(value)
            return lifeTotalTracker.value.value
        }

        fun setLife(playerNum: Int, value: Int): NumberWithRecentChange {
            val lifeTotalTracker = requireNotNull(lifeTotalTrackers[playerNum])
            lifeTotalTracker.set(value)
            return lifeTotalTracker.value.value
        }
    }