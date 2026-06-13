package domain.state.game

import domain.common.RecentChangeValue
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GameSessionStore(
    private val repository: GameSessionRepository,
    private val transientScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val recentChangeDelayMillis: Long = RecentChangeValue.RECENT_CHANGE_DELAY
) {
    private val mutex = Mutex()
    private val mutableSession = MutableStateFlow<GameSession?>(null)
    private val effectChannel = Channel<GameEffect>(capacity = Channel.BUFFERED)
    private val transientClearJobs = mutableMapOf<GameCommand, Job>()

    val session: StateFlow<GameSession?> = mutableSession.asStateFlow()
    val effects: Flow<GameEffect> = effectChannel.receiveAsFlow()

    suspend fun loadActiveSession(): GameSession? {
        val loaded = repository.loadActiveSession()
        mutableSession.value = loaded
        return loaded
    }

    suspend fun dispatchLoaded(command: GameCommand): GameReduction {
        if (mutableSession.value == null) {
            loadActiveSession()
        }
        return dispatch(command)
    }

    suspend fun dispatch(command: GameCommand): GameReduction {
        return mutex.withLock {
            val current = requireNotNull(mutableSession.value) {
                "GameSessionStore must load a session before dispatch"
            }
            val reduction = reduceGame(current, command)
            mutableSession.value = reduction.session

            val mutation = GameMutation(
                sessionId = current.id,
                expectedVersion = current.version,
                command = command,
                resultingSession = reduction.session
            )

            when (val result = repository.commit(mutation)) {
                CommitResult.Success -> Unit
                is CommitResult.Failure -> {
                    effectChannel.send(
                        GameEffect.PersistenceFailed(
                            mutation = mutation,
                            reason = result.reason
                        )
                    )
                }
            }
            scheduleTransientClear(command)

            reduction
        }
    }

    private fun scheduleTransientClear(command: GameCommand) {
        val clearCommand = when (command) {
            is GameCommand.ChangeLife -> GameCommand.ClearLifeRecentChange(command.seatId)
            is GameCommand.ChangeCommanderDamage -> GameCommand.ClearCommanderDamageRecentChange(
                dealerSeatId = command.dealerSeatId,
                receiverSeatId = command.receiverSeatId,
                partner = command.partner
            )
            else -> null
        } ?: return

        transientClearJobs.remove(clearCommand)?.cancel()
        transientClearJobs[clearCommand] = transientScope.launch {
            delay(recentChangeDelayMillis)
            dispatch(clearCommand)
        }
    }
}
