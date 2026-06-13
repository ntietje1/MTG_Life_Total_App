package domain.state.game

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GameSessionStoreTest {
    private val firstSeatId = SeatId("seat-1")

    @Test
    fun dispatchUpdatesStateAndCommitsMutation() = runTest {
        val repository = FakeGameSessionRepository(testSession())
        val store = GameSessionStore(repository)
        store.loadActiveSession()

        store.dispatch(GameCommand.ChangeLife(firstSeatId, -3))

        assertEquals(37, store.session.value?.requireSeat(firstSeatId)?.life?.value)
        assertEquals(1, repository.commits.size)
        assertEquals(0, repository.commits.single().expectedVersion)
        assertEquals(1, repository.commits.single().resultingSession.version)
    }

    @Test
    fun dispatchLoadedLoadsTheActiveSessionWhenNeeded() = runTest {
        val repository = FakeGameSessionRepository(testSession())
        val store = GameSessionStore(repository)

        store.dispatchLoaded(GameCommand.ChangeLife(firstSeatId, -3))

        assertEquals(37, store.session.value?.requireSeat(firstSeatId)?.life?.value)
        assertEquals(1, repository.commits.size)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun lifeRecentChangeClearsAfterDelay() = runTest {
        val repository = FakeGameSessionRepository(testSession())
        val store = GameSessionStore(
            repository = repository,
            transientScope = this,
            recentChangeDelayMillis = 1_000L
        )
        store.loadActiveSession()

        store.dispatch(GameCommand.ChangeLife(firstSeatId, -3))
        advanceTimeBy(1_001L)
        advanceUntilIdle()

        assertEquals(37, store.session.value?.requireSeat(firstSeatId)?.life?.value)
        assertEquals(0, store.session.value?.requireSeat(firstSeatId)?.life?.recentChange)
        assertEquals(GameCommand.ClearLifeRecentChange(firstSeatId), repository.commits.last().command)
    }

    @Test
    fun repositoryFailureEmitsEffectAndKeepsOptimisticState() = runTest {
        val repository = FakeGameSessionRepository(testSession(), failCommits = true)
        val store = GameSessionStore(repository)
        store.loadActiveSession()

        store.dispatch(GameCommand.ChangeLife(firstSeatId, -5))

        assertEquals(35, store.session.value?.requireSeat(firstSeatId)?.life?.value)
        assertIs<GameEffect.PersistenceFailed>(store.effects.first())
    }

    @Test
    fun concurrentDispatchIsSerializedAndVersionsIncreaseMonotonically() = runTest {
        val repository = FakeGameSessionRepository(testSession())
        val store = GameSessionStore(repository)
        store.loadActiveSession()

        val first = launch { store.dispatch(GameCommand.ChangeLife(firstSeatId, -1)) }
        val second = launch { store.dispatch(GameCommand.ChangeLife(firstSeatId, -1)) }
        first.join()
        second.join()

        assertEquals(38, store.session.value?.requireSeat(firstSeatId)?.life?.value)
        assertEquals(listOf(1L, 2L), repository.commits.map { it.resultingSession.version })
    }

    @Test
    fun effectIsNotStoredAsSessionState() = runTest {
        val initial = testSession()
        val repository = FakeGameSessionRepository(initial, failCommits = true)
        val store = GameSessionStore(repository)
        store.loadActiveSession()

        store.dispatch(GameCommand.ChangeLife(firstSeatId, -2))

        assertIs<GameEffect.PersistenceFailed>(store.effects.first())
        assertEquals(38, store.session.value?.requireSeat(firstSeatId)?.life?.value)
        assertEquals(1, store.session.value?.version)
    }
}

private fun testSession(): GameSession {
    return GameSession.newGame(
        id = GameSessionId("game-1"),
        rules = GameRules(startingLife = 40),
        appearances = listOf(
            SeatAppearance(displayName = "P1"),
            SeatAppearance(displayName = "P2")
        )
    )
}

private class FakeGameSessionRepository(
    initialSession: GameSession,
    private val failCommits: Boolean = false
) : GameSessionRepository {
    private val mutableSession = MutableStateFlow<GameSession?>(initialSession)
    val commits = mutableListOf<GameMutation>()

    override suspend fun loadActiveSession(): GameSession? {
        return mutableSession.value
    }

    override suspend fun commit(mutation: GameMutation): CommitResult {
        commits += mutation
        return if (failCommits) {
            CommitResult.Failure("failed")
        } else {
            mutableSession.value = mutation.resultingSession
            CommitResult.Success
        }
    }
}
