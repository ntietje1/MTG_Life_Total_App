package domain.state.game

import domain.state.profile.PlayerBackground
import domain.state.profile.PlayerColors
import domain.state.profile.PlayerProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GameReducerTest {
    private val firstSeatId = SeatId("seat-1")
    private val secondSeatId = SeatId("seat-2")

    @Test
    fun profileSnapshotDoesNotChangeWhenProfileChangesLater() {
        val profile = PlayerProfile(
            id = PlayerProfileId("profile-1"),
            displayName = "Chandra",
            colors = PlayerColors(backgroundArgb = -11206656, textArgb = -1),
            background = PlayerBackground.ProviderImage("https://images.example/chandra.gif")
        )
        val seat = GameSeat.new(
            id = firstSeatId,
            appearance = profile.toSeatAppearance(),
            startingLife = 40
        )

        val editedProfile = profile.copy(
            displayName = "Edited",
            colors = PlayerColors(backgroundArgb = -16777216, textArgb = -1118482)
        )

        assertEquals("Chandra", seat.appearance.displayName)
        assertNotEquals(editedProfile.toSeatAppearance(), seat.appearance)
    }

    @Test
    fun newSessionCreatesOneToSixSeatsWithStartingLife() {
        val session = GameSession.newGame(
            id = GameSessionId("game-1"),
            rules = GameRules(startingLife = 20),
            appearances = (1..6).map { seatNumber ->
                SeatAppearance(displayName = "P$seatNumber")
            }
        )

        assertEquals(6, session.seats.size)
        assertEquals((1..6).map { SeatId("seat-$it") }, session.seats.map { it.id })
        assertEquals(List(6) { 20 }, session.seats.map { it.life.value })
    }

    @Test
    fun newSessionRejectsInvalidSeatCounts() {
        val rules = GameRules(startingLife = 40)

        assertFailsWithMessage<IllegalArgumentException>("GameSession supports 1 to 6 seats") {
            GameSession.newGame(GameSessionId("empty"), rules, emptyList<SeatAppearance>())
        }
        assertFailsWithMessage<IllegalArgumentException>("GameSession supports 1 to 6 seats") {
            GameSession.newGame(
                id = GameSessionId("too-many"),
                rules = rules,
                appearances = (1..7).map { SeatAppearance(displayName = "P$it") }
            )
        }
    }

    @Test
    fun togglesDayNightFromNoneToDayToNightToDay() {
        val initial = testSession()

        val day = reduceGame(initial, GameCommand.ToggleDayNight).session
        val night = reduceGame(day, GameCommand.ToggleDayNight).session
        val dayAgain = reduceGame(night, GameCommand.ToggleDayNight).session

        assertEquals(DayNight.DAY, day.dayNight)
        assertEquals(DayNight.NIGHT, night.dayNight)
        assertEquals(DayNight.DAY, dayAgain.dayNight)
    }

    @Test
    fun monarchCanOnlyBelongToOneSeatAndCanBeCleared() {
        val initial = testSession()

        val firstMonarch = reduceGame(initial, GameCommand.SetMonarch(firstSeatId)).session
        val secondMonarch = reduceGame(firstMonarch, GameCommand.SetMonarch(secondSeatId)).session
        val cleared = reduceGame(secondMonarch, GameCommand.SetMonarch(null)).session

        assertEquals(firstSeatId, firstMonarch.monarchSeatId)
        assertEquals(secondSeatId, secondMonarch.monarchSeatId)
        assertEquals(null, cleared.monarchSeatId)
    }

    @Test
    fun lifeChangeUpdatesCurrentValueAndRecentChange() {
        val initial = testSession(startingLife = 40)

        val changed = reduceGame(initial, GameCommand.ChangeLife(firstSeatId, -7)).session
        val seat = changed.requireSeat(firstSeatId)

        assertEquals(33, seat.life.value)
        assertEquals(-7, seat.life.recentChange)
    }

    @Test
    fun manualDeathAndAutoDeathAreDomainQueries() {
        val initial = testSession(startingLife = 1)

        val manualDead = reduceGame(initial, GameCommand.SetManualDeath(firstSeatId, true)).session
        val lifeDead = reduceGame(initial, GameCommand.ChangeLife(firstSeatId, -1)).session

        assertTrue(manualDead.requireSeat(firstSeatId).isDead(autoKo = false, commander = manualDead.commander))
        assertFalse(initial.requireSeat(firstSeatId).isDead(autoKo = true, commander = initial.commander))
        assertTrue(lifeDead.requireSeat(firstSeatId).isDead(autoKo = true, commander = lifeDead.commander))
    }

    @Test
    fun keyedSeatCountersCanIncrementDeactivateAndReset() {
        val initial = testSession()

        val incremented = reduceGame(
            initial,
            GameCommand.ChangeSeatCounter(firstSeatId, CounterType.POISON, 3)
        ).session
        val active = reduceGame(
            incremented,
            GameCommand.SetSeatCounterActive(firstSeatId, CounterType.POISON, true)
        ).session
        val decremented = reduceGame(
            active,
            GameCommand.ChangeSeatCounter(firstSeatId, CounterType.POISON, -1)
        ).session
        val inactive = reduceGame(
            decremented,
            GameCommand.SetSeatCounterActive(firstSeatId, CounterType.POISON, false)
        ).session
        val reset = reduceGame(inactive, GameCommand.ResetGame).session

        assertEquals(3, incremented.requireSeat(firstSeatId).counterValue(CounterType.POISON))
        assertTrue(CounterType.POISON in active.requireSeat(firstSeatId).activeCounters)
        assertEquals(2, decremented.requireSeat(firstSeatId).counterValue(CounterType.POISON))
        assertFalse(CounterType.POISON in inactive.requireSeat(firstSeatId).activeCounters)
        assertEquals(0, reset.requireSeat(firstSeatId).counterValue(CounterType.POISON))
    }

    @Test
    fun keyedTableCountersCanIncrementAndReset() {
        val initial = testSession()

        val incremented = reduceGame(initial, GameCommand.ChangeTableCounter(TableCounterType.STORM, 2)).session
        val reset = reduceGame(incremented, GameCommand.ResetGame).session

        assertEquals(2, incremented.tableCounterValue(TableCounterType.STORM))
        assertEquals(0, reset.tableCounterValue(TableCounterType.STORM))
    }

    @Test
    fun commanderDamageIsKeyedByDealerReceiverAndPartnerSide() {
        val initial = testSession()

        val primaryDamage = reduceGame(
            initial,
            GameCommand.ChangeCommanderDamage(
                dealerSeatId = firstSeatId,
                receiverSeatId = secondSeatId,
                partner = false,
                delta = 7
            )
        ).session
        val partnerDamage = reduceGame(
            primaryDamage,
            GameCommand.ChangeCommanderDamage(
                dealerSeatId = firstSeatId,
                receiverSeatId = secondSeatId,
                partner = true,
                delta = 5
            )
        ).session

        assertEquals(7, partnerDamage.commander.damage(firstSeatId, secondSeatId, partner = false).value)
        assertEquals(5, partnerDamage.commander.damage(firstSeatId, secondSeatId, partner = true).value)
    }

    @Test
    fun commanderDamageClampsAndCanMakeASeatDead() {
        val initial = testSession()

        val highDamage = reduceGame(
            initial,
            GameCommand.ChangeCommanderDamage(firstSeatId, secondSeatId, partner = false, delta = 150)
        ).session
        val reducedDamage = reduceGame(
            highDamage,
            GameCommand.ChangeCommanderDamage(firstSeatId, secondSeatId, partner = false, delta = -200)
        ).session
        val lethalDamage = reduceGame(
            initial,
            GameCommand.ChangeCommanderDamage(firstSeatId, secondSeatId, partner = false, delta = 21)
        ).session

        assertEquals(99, highDamage.commander.damage(firstSeatId, secondSeatId, partner = false).value)
        assertEquals(0, reducedDamage.commander.damage(firstSeatId, secondSeatId, partner = false).value)
        assertTrue(lethalDamage.requireSeat(secondSeatId).isDead(autoKo = true, commander = lethalDamage.commander))
    }

    @Test
    fun resetClearsGameScopedStateButKeepsSeatAppearanceSnapshots() {
        val initial = testSession(startingLife = 40)
        val originalAppearance = initial.requireSeat(firstSeatId).appearance
        val changed = listOf(
            GameCommand.ChangeLife(firstSeatId, -11),
            GameCommand.SetMonarch(firstSeatId),
            GameCommand.ToggleDayNight,
            GameCommand.ChangeSeatCounter(firstSeatId, CounterType.ENERGY, 4),
            GameCommand.SetManualDeath(firstSeatId, true),
            GameCommand.ChangeCommanderDamage(secondSeatId, firstSeatId, partner = false, delta = 12),
            GameCommand.ChangeTableCounter(TableCounterType.STORM, 3)
        ).fold(initial) { session, command -> reduceGame(session, command).session }

        val reset = reduceGame(changed, GameCommand.ResetGame).session
        val resetSeat = reset.requireSeat(firstSeatId)

        assertEquals(40, resetSeat.life.value)
        assertEquals(0, resetSeat.life.recentChange)
        assertFalse(resetSeat.manualDeath)
        assertEquals(originalAppearance, resetSeat.appearance)
        assertEquals(null, reset.monarchSeatId)
        assertEquals(DayNight.NONE, reset.dayNight)
        assertEquals(0, resetSeat.counterValue(CounterType.ENERGY))
        assertEquals(0, reset.commander.damage(secondSeatId, firstSeatId, partner = false).value)
        assertEquals(0, reset.tableCounterValue(TableCounterType.STORM))
    }

    private fun testSession(startingLife: Int = 40): GameSession {
        return GameSession.newGame(
            id = GameSessionId("game-1"),
            rules = GameRules(startingLife = startingLife),
            appearances = listOf(
                SeatAppearance(displayName = "P1"),
                SeatAppearance(displayName = "P2")
            )
        )
    }
}

private inline fun <reified T : Throwable> assertFailsWithMessage(
    message: String,
    block: () -> Unit
) {
    val error = kotlin.test.assertFailsWith<T>(block = block)
    assertEquals(message, error.message)
}
