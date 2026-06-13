package domain.state.legacy

import domain.state.game.CommitResult
import domain.state.game.GameMutation
import domain.state.game.GameRules
import domain.state.game.GameSession
import domain.state.game.GameSessionId
import domain.state.game.GameSessionRepository
import domain.storage.ISettingsManager

class LocalGameSessionRepository(
    private val settingsManager: ISettingsManager,
    private val sessionId: GameSessionId = GameSessionId("local-active-game")
) : GameSessionRepository {
    override suspend fun loadActiveSession(): GameSession {
        return LocalGameSessionMapper.fromLegacyOrFresh(
            id = sessionId,
            rules = GameRules(startingLife = settingsManager.startingLife.value),
            legacyPlayers = settingsManager.loadPlayerStates(),
            fallbackSeatCount = settingsManager.numPlayers.value
        )
    }

    override suspend fun commit(mutation: GameMutation): CommitResult {
        return try {
            settingsManager.savePlayerStates(
                LocalGameSessionMapper.toLegacyPlayers(mutation.resultingSession)
            )
            CommitResult.Success
        } catch (error: Exception) {
            CommitResult.Failure(error.message ?: "Failed to save game session")
        }
    }
}
