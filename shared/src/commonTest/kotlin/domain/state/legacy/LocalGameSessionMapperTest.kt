package domain.state.legacy

import androidx.compose.ui.graphics.Color
import domain.common.NumberWithRecentChange
import domain.state.game.CounterType
import domain.state.game.GameRules
import domain.state.game.GameSessionId
import domain.state.game.SeatId
import model.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import ui.lifecounter.CounterType as LegacyCounterType

class LocalGameSessionMapperTest {
    @Test
    fun mapsLegacyPlayersToGameSessionSeats() {
        val legacyPlayer = Player(
            lifeTotal = NumberWithRecentChange(number = 32, recentChange = -8),
            imageString = "local-image.png",
            color = Color(-15654349),
            textColor = Color(-12298906),
            playerNum = 2,
            name = "Nissa",
            counters = List(LegacyCounterType.entries.size) { index -> if (index == 0) 3 else 0 },
            activeCounters = listOf(LegacyCounterType.Poison),
            setDead = true
        )

        val session = LocalGameSessionMapper.fromLegacyOrFresh(
            id = GameSessionId("game-1"),
            rules = GameRules(startingLife = 40),
            legacyPlayers = listOf(legacyPlayer),
            fallbackSeatCount = 1
        )
        val seat = session.requireSeat(SeatId("seat-2"))

        assertEquals("Nissa", seat.appearance.displayName)
        assertEquals(-15654349, seat.appearance.colors.backgroundArgb)
        assertEquals(-12298906, seat.appearance.colors.textArgb)
        assertEquals("local-image.png", seat.appearance.background.fileNameForTest())
        assertEquals(32, seat.life.value)
        assertEquals(-8, seat.life.recentChange)
        assertEquals(3, seat.counterValue(CounterType.POISON))
        assertEquals(setOf(CounterType.POISON), seat.activeCounters)
        assertEquals(true, seat.manualDeath)
    }

    @Test
    fun mapsGameSessionBackToTemporaryLegacyPlayers() {
        val session = LocalGameSessionMapper.fromLegacyOrFresh(
            id = GameSessionId("game-1"),
            rules = GameRules(startingLife = 40),
            legacyPlayers = listOf(Player(playerNum = 1, name = "Ajani")),
            fallbackSeatCount = 1
        )
        val changed = session.copy(
            seats = listOf(
                session.requireSeat(SeatId("seat-1")).copy(
                    life = NumberWithRecentChange(25, -15).toTrackedInt(),
                    counters = mapOf(CounterType.ENERGY to 4),
                    activeCounters = setOf(CounterType.ENERGY)
                )
            )
        )

        val legacyPlayers = LocalGameSessionMapper.toLegacyPlayers(changed)

        assertEquals(1, legacyPlayers.size)
        assertEquals("Ajani", legacyPlayers.single().name)
        assertEquals(25, legacyPlayers.single().lifeTotal.number)
        assertEquals(-15, legacyPlayers.single().lifeTotal.recentChange)
        assertEquals(4, legacyPlayers.single().counters[LegacyCounterType.Energy.ordinal])
        assertEquals(listOf(LegacyCounterType.Energy), legacyPlayers.single().activeCounters)
    }

    @Test
    fun invalidLegacyStateFallsBackToFreshSession() {
        val session = LocalGameSessionMapper.fromLegacyOrFresh(
            id = GameSessionId("game-1"),
            rules = GameRules(startingLife = 30),
            legacyPlayers = listOf(Player(playerNum = 0, name = "Broken")),
            fallbackSeatCount = 2
        )

        assertEquals(listOf(SeatId("seat-1"), SeatId("seat-2")), session.seats.map { it.id })
        assertEquals(List(2) { 30 }, session.seats.map { it.life.value })
        assertFalse(session.seats.any { it.appearance.displayName == "Broken" })
    }
}

private fun Any.fileNameForTest(): String? {
    return when (this) {
        is domain.state.profile.PlayerBackground.LocalImage -> fileName
        else -> null
    }
}
