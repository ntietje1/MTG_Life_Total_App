package domain.state.game

interface GameSessionRepository {
    suspend fun loadActiveSession(): GameSession?

    suspend fun commit(mutation: GameMutation): CommitResult
}

data class GameMutation(
    val sessionId: GameSessionId,
    val expectedVersion: Long,
    val command: GameCommand,
    val resultingSession: GameSession
)

sealed interface CommitResult {
    data object Success : CommitResult

    data class Failure(val reason: String) : CommitResult
}
