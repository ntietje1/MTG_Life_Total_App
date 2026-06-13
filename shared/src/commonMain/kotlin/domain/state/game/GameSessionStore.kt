package domain.state.game

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GameSessionStore(
    private val repository: GameSessionRepository
) {
    private val mutex = Mutex()
    private val mutableSession = MutableStateFlow<GameSession?>(null)
    private val effectChannel = Channel<GameEffect>(capacity = Channel.BUFFERED)

    val session: StateFlow<GameSession?> = mutableSession.asStateFlow()
    val effects: Flow<GameEffect> = effectChannel.receiveAsFlow()

    suspend fun loadActiveSession(): GameSession? {
        val loaded = repository.loadActiveSession()
        mutableSession.value = loaded
        return loaded
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

            reduction
        }
    }
}
