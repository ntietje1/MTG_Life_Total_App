package domain.state.legacy

import domain.common.NumberWithRecentChange
import domain.game.timer.GameTimerState
import domain.state.game.GameCommand
import domain.state.game.GameMutation
import domain.state.game.GameRules
import domain.state.game.GameSession
import domain.state.game.GameSessionId
import domain.state.game.SeatAppearance
import domain.state.game.SeatId
import domain.storage.ISettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import model.Player
import model.card.Card
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LocalGameSessionRepositoryTest {
    @Test
    fun loadActiveSessionMapsLegacyPlayersAndPublishesSession() = kotlinx.coroutines.test.runTest {
        val settings = FakeSettingsManager(
            startingLifeValue = 40,
            numPlayersValue = 1,
            players = listOf(Player(playerNum = 1, name = "Jace", lifeTotal = NumberWithRecentChange(31, -9)))
        )
        val repository = LocalGameSessionRepository(settings, GameSessionId("game-1"))

        val session = repository.loadActiveSession()

        assertEquals("Jace", session.requireSeat(SeatId("seat-1")).appearance.displayName)
        assertEquals(31, session.requireSeat(SeatId("seat-1")).life.value)
    }

    @Test
    fun commitSavesLegacyPlayersAndPublishesResultingSession() = kotlinx.coroutines.test.runTest {
        val settings = FakeSettingsManager(startingLifeValue = 40, numPlayersValue = 1)
        val repository = LocalGameSessionRepository(settings, GameSessionId("game-1"))
        val initial = GameSession.newGame(
            id = GameSessionId("game-1"),
            rules = GameRules(startingLife = 40),
            appearances = listOf(SeatAppearance(displayName = "P1"))
        )
        val resultingSession = initial.copy(
            seats = listOf(initial.requireSeat(SeatId("seat-1")).copy(life = NumberWithRecentChange(36, -4).toTrackedInt())),
            version = 1
        )

        val result = repository.commit(
            GameMutation(
                sessionId = initial.id,
                expectedVersion = 0,
                command = GameCommand.ChangeLife(SeatId("seat-1"), -4),
                resultingSession = resultingSession
            )
        )

        assertIs<domain.state.game.CommitResult.Success>(result)
        assertEquals(36, settings.savedPlayers.single().lifeTotal.number)
    }
}

private class FakeSettingsManager(
    startingLifeValue: Int,
    numPlayersValue: Int,
    private val players: List<Player> = emptyList()
) : ISettingsManager {
    override val autoKo = MutableStateFlow(true)
    override fun setAutoKo(value: Boolean) { autoKo.value = value }
    override val autoSkip = MutableStateFlow(false)
    override fun setAutoSkip(value: Boolean) { autoSkip.value = value }
    override val keepScreenOn = MutableStateFlow(false)
    override fun setKeepScreenOn(value: Boolean) { keepScreenOn.value = value }
    override val cameraRollDisabled = MutableStateFlow(false)
    override fun setCameraRollDisabled(value: Boolean) { cameraRollDisabled.value = value }
    override val fastCoinFlip = MutableStateFlow(false)
    override fun setFastCoinFlip(value: Boolean) { fastCoinFlip.value = value }
    override val numPlayers = MutableStateFlow(numPlayersValue)
    override fun setNumPlayers(value: Int) { numPlayers.value = value }
    override val alt4PlayerLayout = MutableStateFlow(false)
    override fun setAlt4PlayerLayout(value: Boolean) { alt4PlayerLayout.value = value }
    override val darkTheme = MutableStateFlow(false)
    override fun setDarkTheme(value: Boolean) { darkTheme.value = value }
    override val startingLife = MutableStateFlow(startingLifeValue)
    override fun setStartingLife(value: Int) { startingLife.value = value }
    override val tutorialSkip = MutableStateFlow(false)
    override fun setTutorialSkip(value: Boolean) { tutorialSkip.value = value }
    override val lastSplashScreenShown = MutableStateFlow("0.0.0")
    override fun setLastSplashScreenShown(value: String) { lastSplashScreenShown.value = value }
    override val turnTimer = MutableStateFlow(false)
    override fun setTurnTimer(value: Boolean) { turnTimer.value = value }
    override val devMode = MutableStateFlow(false)
    override fun setDevMode(value: Boolean) { devMode.value = value }
    override val patchNotes = MutableStateFlow("")
    override fun setPatchNotes(value: String) { patchNotes.value = value }
    override val savedTimerState = MutableStateFlow<GameTimerState?>(null)
    override fun setSavedTimerState(value: GameTimerState?) { savedTimerState.value = value }

    var savedPlayers: List<Player> = emptyList()

    override fun loadPlayerStates(): List<Player> = players
    override fun savePlayerStates(players: List<Player>) { savedPlayers = players }
    override fun savePlanechaseState(allPlanes: List<Card>, planarDeck: List<Card>, planarBackStack: List<Card>) = Unit
    override fun loadPlanechaseState(): Triple<List<Card>, List<Card>, List<Card>> = Triple(emptyList(), emptyList(), emptyList())
    override fun savePlayerPref(player: Player) = Unit
    override fun deletePlayerPref(player: Player) = Unit
    override fun loadPlayerPrefs(): ArrayList<Player> = ArrayList()
}
