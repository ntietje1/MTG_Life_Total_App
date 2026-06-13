package domain.game.timer

import domain.storage.PreferencesRepository
import domain.storage.TestSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TimerManagerTest {
    @Test
    fun promptsForFirstPlayerAndPublishesActiveTimerThroughHost() = runTest {
        val preferencesRepository = PreferencesRepository(TestSettings()).also {
            it.setNumPlayers(2)
        }
        val manager = TimerManager(
            timerStateRepository = TimerStateRepository(TestSettings()),
            preferencesRepository = preferencesRepository
        )
        val host = FakeTimerManagerHost(playerCount = 2)
        manager.attach(host)

        manager.onTimerEnabledChange(true)
        manager.handleFirstPlayerSelection(1)

        assertEquals(1, host.promptCount)
        assertEquals(1, host.clearFirstPlayerPromptCount)
        assertEquals(1, host.activePlayerIndex)
        assertNotNull(host.activeTimer)
        manager.detach()
    }

    @Test
    fun skipsDeadPlayersWhenMovingTimer() = runTest {
        val preferencesRepository = PreferencesRepository(TestSettings()).also {
            it.setNumPlayers(3)
        }
        val manager = TimerManager(
            timerStateRepository = TimerStateRepository(TestSettings()),
            preferencesRepository = preferencesRepository
        )
        val host = FakeTimerManagerHost(playerCount = 3, deadPlayers = setOf(2))
        manager.attach(host)

        manager.onTimerEnabledChange(true)
        manager.handleFirstPlayerSelection(1)
        manager.moveTimer()

        assertEquals(0, host.activePlayerIndex)
        manager.detach()
    }
}

private class FakeTimerManagerHost(
    override val playerCount: Int,
    private val deadPlayers: Set<Int> = emptySet()
) : TimerManagerHost {
    var promptCount = 0
        private set
    var clearFirstPlayerPromptCount = 0
        private set
    var activePlayerIndex: Int? = null
        private set
    var activeTimer: TurnTimer? = null
        private set

    override fun isPlayerDead(index: Int): Boolean {
        return index in deadPlayers
    }

    override fun promptForFirstPlayer() {
        promptCount += 1
    }

    override fun clearFirstPlayerPrompt() {
        clearFirstPlayerPromptCount += 1
    }

    override fun showTimer(activePlayerIndex: Int?, timer: TurnTimer?) {
        this.activePlayerIndex = activePlayerIndex
        activeTimer = timer
    }
}
