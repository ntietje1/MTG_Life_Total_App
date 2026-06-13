package domain.game.timer

import domain.storage.TestSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TimerStateRepositoryTest {
    @Test
    fun savesLoadsAndClearsTimerState() {
        val settings = TestSettings()
        val repository = TimerStateRepository(settings)
        val state = GameTimerState(
            firstPlayer = 1,
            activePlayerIndex = 2,
            turnTimer = TurnTimer(seconds = 12, turn = 3)
        )

        repository.save(state)

        assertEquals(state, repository.load())

        repository.save(null)

        assertNull(repository.load())
        assertNull(settings.getStringOrNull(TimerStateRepository.StateKey))
    }

    @Test
    fun preservesCorruptTimerPayloadAndResetsTimerOnly() {
        val settings = TestSettings()
        settings.putString(TimerStateRepository.StateKey, "{broken")
        val repository = TimerStateRepository(settings)

        val state = repository.load()

        assertNull(state)
        assertEquals("{broken", settings.getString(TimerStateRepository.CorruptStateKey, ""))
        assertNull(settings.getStringOrNull(TimerStateRepository.StateKey))
    }
}
