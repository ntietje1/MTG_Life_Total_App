package domain.state.game

import androidx.compose.ui.graphics.Color
import domain.common.NumberWithRecentChange
import domain.state.profile.PlayerBackground
import domain.state.profile.PlayerColors
import domain.storage.PreferencesRepository
import domain.storage.TestSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import ui.lifecounter.CounterType as LegacyCounterType

class SavedGameRepositoryTest {
    @Test
    fun commitsAndLoadsVersionedGameSession() = kotlinx.coroutines.test.runTest {
        val settings = TestSettings()
        val repository = SavedGameRepository(
            settings = settings,
            preferencesRepository = testPreferences()
        )
        val session = testSession().copy(
            commander = CommanderDamageMatrix()
                .changeDamage(SeatId("seat-1"), SeatId("seat-2"), partner = false, delta = 7),
            commanderMode = CommanderMode(SeatId("seat-1"), partnerMode = true),
            tableCounters = mapOf(TableCounterType.STORM to 3),
            monarchSeatId = SeatId("seat-2"),
            dayNight = DayNight.DAY,
            version = 12
        )

        val result = repository.commit(testMutation(session))
        val loaded = repository.loadActiveSession()

        assertIs<CommitResult.Success>(result)
        assertNotNull(settings.getStringOrNull(SavedGameRepository.ActiveGameKey))
        assertEquals(session, loaded)
    }

    @Test
    fun storesCountersByStableKey() = kotlinx.coroutines.test.runTest {
        val settings = TestSettings()
        val repository = SavedGameRepository(
            settings = settings,
            preferencesRepository = testPreferences()
        )
        val session = testSession().copy(
            seats = listOf(
                testSession().requireSeat(SeatId("seat-1")).copy(
                    counters = mapOf(CounterType.POISON to 3),
                    activeCounters = setOf(CounterType.POISON)
                ),
                testSession().requireSeat(SeatId("seat-2"))
            ),
            tableCounters = mapOf(TableCounterType.STORM to 2)
        )

        repository.commit(testMutation(session))

        val savedJson = settings.getString(SavedGameRepository.ActiveGameKey, "")
        assertTrue(savedJson.contains("POISON"))
        assertTrue(savedJson.contains("STORM"))
        assertFalse(savedJson.contains("\"counters\":["))
    }

    @Test
    fun migratesLegacyPlayerStateToSavedGamePayload() = kotlinx.coroutines.test.runTest {
        val settings = TestSettings()
        val preferences = testPreferences(startingLife = 40, numPlayers = 1)
        val legacyPlayer = Player(
            playerNum = 1,
            name = "Jace",
            lifeTotal = NumberWithRecentChange(31, -9),
            color = Color(-123),
            textColor = Color(-456),
            counters = List(LegacyCounterType.entries.size) { index ->
                if (index == LegacyCounterType.Poison.ordinal) 4 else 0
            },
            activeCounters = listOf(LegacyCounterType.Poison),
            setDead = true
        )
        settings.putString("playerStates", Json.encodeToString(listOf(legacyPlayer)))
        val repository = SavedGameRepository(settings = settings, preferencesRepository = preferences)

        val loaded = repository.loadActiveSession()

        assertNotNull(loaded)
        assertEquals("Jace", loaded.requireSeat(SeatId("seat-1")).appearance.displayName)
        assertEquals(31, loaded.requireSeat(SeatId("seat-1")).life.value)
        assertEquals(4, loaded.requireSeat(SeatId("seat-1")).counterValue(CounterType.POISON))
        assertEquals(true, loaded.requireSeat(SeatId("seat-1")).manualDeath)
        assertNotNull(settings.getStringOrNull(SavedGameRepository.ActiveGameKey))
    }

    @Test
    fun corruptSavedGameIsPreservedAndFreshSessionLoads() = kotlinx.coroutines.test.runTest {
        val settings = TestSettings()
        settings.putString(SavedGameRepository.ActiveGameKey, "{broken")
        val repository = SavedGameRepository(
            settings = settings,
            preferencesRepository = testPreferences(startingLife = 30, numPlayers = 2)
        )

        val loaded = repository.loadActiveSession()

        assertEquals("{broken", settings.getString(SavedGameRepository.CorruptActiveGameKey, ""))
        assertNull(settings.getStringOrNull(SavedGameRepository.ActiveGameKey))
        assertNotNull(loaded)
        assertEquals(listOf(SeatId("seat-1"), SeatId("seat-2")), loaded.seats.map { it.id })
        assertEquals(List(2) { 30 }, loaded.seats.map { it.life.value })
        assertEquals(
            PlayerColors.DefaultPalette.take(2),
            loaded.seats.map { seat -> seat.appearance.colors }
        )
        assertFalse(loaded.seats.any { seat -> seat.appearance.colors == PlayerColors() })
    }

    @Test
    fun freshSessionUsesPaletteColors() = kotlinx.coroutines.test.runTest {
        val repository = SavedGameRepository(
            settings = TestSettings(),
            preferencesRepository = testPreferences(numPlayers = 4)
        )

        val loaded = repository.loadActiveSession()

        assertEquals(
            PlayerColors.DefaultPalette.take(4),
            loaded.seats.map { seat -> seat.appearance.colors }
        )
        assertFalse(loaded.seats.any { seat -> seat.appearance.colors == PlayerColors() })
    }

    @Test
    fun missingDtoFieldsUseDefaults() = kotlinx.coroutines.test.runTest {
        val settings = TestSettings()
        settings.putString(
            SavedGameRepository.ActiveGameKey,
            """
            {
              "version":1,
              "id":"local-active-game",
              "rules":{"startingLife":40},
              "seats":[{"id":"seat-1","displayName":"P1","lifeValue":22}]
            }
            """.trimIndent()
        )
        val repository = SavedGameRepository(
            settings = settings,
            preferencesRepository = testPreferences()
        )

        val loaded = repository.loadActiveSession()

        assertNotNull(loaded)
        val seat = loaded.requireSeat(SeatId("seat-1"))
        assertEquals("P1", seat.appearance.displayName)
        assertEquals(22, seat.life.value)
        assertEquals(0, seat.life.recentChange)
        assertEquals(PlayerColors.DefaultPalette[0], seat.appearance.colors)
        assertEquals(PlayerBackground.None, seat.appearance.background)
        assertEquals(0, loaded.version)
    }

    private fun testSession(): GameSession {
        return GameSession.newGame(
            id = GameSessionId("game-1"),
            rules = GameRules(startingLife = 40),
            appearances = listOf(
                SeatAppearance(
                    displayName = "P1",
                    colors = PlayerColors.DefaultPalette[0],
                    background = PlayerBackground.LocalImage("p1.png")
                ),
                SeatAppearance(
                    displayName = "P2",
                    colors = PlayerColors.DefaultPalette[1],
                    background = PlayerBackground.CardArt("https://img.example/card.jpg")
                )
            )
        )
    }

    private fun testMutation(session: GameSession): GameMutation {
        return GameMutation(
            sessionId = session.id,
            expectedVersion = session.version - 1,
            command = GameCommand.ChangeLife(SeatId("seat-1"), 1),
            resultingSession = session
        )
    }

    private fun testPreferences(startingLife: Int = 40, numPlayers: Int = 2): PreferencesRepository {
        return PreferencesRepository(TestSettings()).also {
            it.setStartingLife(startingLife)
            it.setNumPlayers(numPlayers)
        }
    }
}
