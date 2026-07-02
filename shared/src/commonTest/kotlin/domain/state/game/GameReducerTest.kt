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
    fun seatAppearanceChangesStayInTheGameSessionAcrossLaterStateChanges() {
        val initial = testSession(startingLife = 40)
        val editedAppearance = SeatAppearance(
            displayName = "Edited P1",
            colors = PlayerColors(backgroundArgb = -16777216, textArgb = -1118482),
            background = PlayerBackground.ProviderImage("https://images.example/edited.gif"),
            sourceProfileId = PlayerProfileId("Edited P1")
        )

        val customized = reduceGame(
            initial,
            GameCommand.SetSeatAppearance(firstSeatId, editedAppearance)
        ).session
        val lifeChanged = reduceGame(customized, GameCommand.ChangeLife(firstSeatId, -3)).session
        val reset = reduceGame(lifeChanged, GameCommand.ResetGame()).session

        assertEquals(editedAppearance, customized.requireSeat(firstSeatId).appearance)
        assertEquals(editedAppearance, lifeChanged.requireSeat(firstSeatId).appearance)
        assertEquals(editedAppearance, reset.requireSeat(firstSeatId).appearance)
    }

    @Test
    fun duplicateSeatNameIsRejectedButOtherAppearanceChangesApply() {
        val initial = testSession()
        val duplicateNameAppearance = SeatAppearance(
            displayName = "P2",
            colors = PlayerColors(backgroundArgb = 123, textArgb = 456),
            background = PlayerBackground.ProviderImage("https://images.example/new.gif")
        )

        val updated = reduceGame(
            initial,
            GameCommand.SetSeatAppearance(firstSeatId, duplicateNameAppearance)
        ).session

        val firstSeat = updated.requireSeat(firstSeatId)
        assertEquals("P1", firstSeat.appearance.displayName)
        assertEquals(duplicateNameAppearance.colors, firstSeat.appearance.colors)
        assertEquals(duplicateNameAppearance.background, firstSeat.appearance.background)
    }

    @Test
    fun duplicateSeatNamesIgnoreWhitespaceAndCase() {
        val initial = testSession()
        val duplicateNameAppearance = SeatAppearance(
            displayName = " p2 ",
            colors = PlayerColors(backgroundArgb = 123, textArgb = 456)
        )

        val updated = reduceGame(
            initial,
            GameCommand.SetSeatAppearance(firstSeatId, duplicateNameAppearance)
        ).session

        assertEquals("P1", updated.requireSeat(firstSeatId).appearance.displayName)
    }

    @Test
    fun sameSeatCanKeepItsNameWhenAppearanceChanges() {
        val initial = testSession()
        val appearance = SeatAppearance(
            displayName = "P1",
            colors = PlayerColors(backgroundArgb = 123, textArgb = 456)
        )

        val updated = reduceGame(
            initial,
            GameCommand.SetSeatAppearance(firstSeatId, appearance)
        ).session

        assertEquals(appearance, updated.requireSeat(firstSeatId).appearance)
    }

    @Test
    fun newSessionCreatesOneToSixSeatsWithStartingLife() {
        val session = GameSession.newGame(
            id = GameSessionId("game-1"),
            rules = GameRules(startingLife = 20),
            appearances = (1..6).map { seatNumber ->
                SeatAppearance.defaultForSeat(seatNumber)
            }
        )

        assertEquals(6, session.seats.size)
        assertEquals((1..6).map { SeatId("seat-$it") }, session.seats.map { it.id })
        assertEquals(List(6) { 20 }, session.seats.map { it.life.value })
    }

    @Test
    fun resetGameCanUpdateStartingLife() {
        val initial = testSession(startingLife = 40)

        val reset = reduceGame(initial, GameCommand.ResetGame(startingLife = 20)).session

        assertEquals(20, reset.rules.startingLife)
        assertEquals(List(reset.seats.size) { 20 }, reset.seats.map { it.life.value })
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
                appearances = (1..7).map {
                    SeatAppearance(
                        displayName = "P$it",
                        colors = PlayerColors(backgroundArgb = it, textArgb = -1)
                    )
                }
            )
        }
    }

    @Test
    fun seatCountCanGrowAStartedGameWithFreshDefaultSeats() {
        val changed = reduceGame(
            testSession(startingLife = 30),
            GameCommand.ChangeLife(firstSeatId, -5)
        ).session

        val resized = reduceGame(changed, GameCommand.SetSeatCount(4)).session

        assertEquals(4, resized.seats.size)
        assertEquals(25, resized.requireSeat(firstSeatId).life.value)
        assertEquals("P1", resized.requireSeat(firstSeatId).appearance.displayName)
        assertEquals("P3", resized.requireSeat(SeatId("seat-3")).appearance.displayName)
        assertEquals(30, resized.requireSeat(SeatId("seat-3")).life.value)
        assertEquals("P4", resized.requireSeat(SeatId("seat-4")).appearance.displayName)
        assertEquals(30, resized.requireSeat(SeatId("seat-4")).life.value)
    }

    @Test
    fun newSeatsUsePlayerPaletteColors() {
        val palette = PlayerColors.DefaultPalette.map { colors -> colors.backgroundArgb }
        val colored = listOf(
            GameCommand.SetSeatAppearance(
                firstSeatId,
                SeatAppearance(displayName = "P1", colors = PlayerColors.DefaultPalette[0])
            ),
            GameCommand.SetSeatAppearance(
                secondSeatId,
                SeatAppearance(displayName = "P2", colors = PlayerColors.DefaultPalette[1])
            )
        ).fold(testSession()) { session, command -> reduceGame(session, command).session }

        val resized = reduceGame(colored, GameCommand.SetSeatCount(4)).session
        val seatColors = resized.seats.map { seat -> seat.appearance.colors.backgroundArgb }
        val newSeatColors = seatColors.drop(2)

        assertEquals(seatColors.size, seatColors.distinct().size)
        assertTrue(PlayerColors().backgroundArgb !in newSeatColors)
        assertTrue(newSeatColors.all { color -> color in palette })
    }

    @Test
    fun newSeatsGetUniqueDefaultNames() {
        val renamed = reduceGame(
            testSession(),
            GameCommand.SetSeatAppearance(
                firstSeatId,
                SeatAppearance(
                    displayName = "P3",
                    colors = PlayerColors.DefaultPalette[0]
                )
            )
        ).session

        val resized = reduceGame(renamed, GameCommand.SetSeatCount(3)).session

        assertEquals("P3", resized.requireSeat(firstSeatId).appearance.displayName)
        assertEquals("P3 (2)", resized.requireSeat(SeatId("seat-3")).appearance.displayName)
    }

    @Test
    fun seatCountCanShrinkAStartedGameAndDropRemovedSeatState() {
        val initial = testSession()
        val changed = listOf(
            GameCommand.SetMonarch(secondSeatId),
            GameCommand.SetCommanderDealer(secondSeatId),
            GameCommand.ChangeCommanderDamage(secondSeatId, firstSeatId, partner = false, delta = 7),
            GameCommand.ChangeCommanderDamage(firstSeatId, secondSeatId, partner = false, delta = 9)
        ).fold(initial) { session, command -> reduceGame(session, command).session }

        val resized = reduceGame(changed, GameCommand.SetSeatCount(1)).session

        assertEquals(listOf(firstSeatId), resized.seats.map { it.id })
        assertEquals(null, resized.monarchSeatId)
        assertEquals(null, resized.commanderMode)
        assertEquals(emptyMap(), resized.commander.entries())
    }

    @Test
    fun seatCountRejectsInvalidCounts() {
        assertFailsWithMessage<IllegalArgumentException>("GameSession supports 1 to 6 seats") {
            reduceGame(testSession(), GameCommand.SetSeatCount(0))
        }
        assertFailsWithMessage<IllegalArgumentException>("GameSession supports 1 to 6 seats") {
            reduceGame(testSession(), GameCommand.SetSeatCount(7))
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
    fun repeatedLifeChangesAccumulateRecentChangeUntilCleared() {
        val initial = testSession(startingLife = 40)

        val changed = listOf(
            GameCommand.ChangeLife(firstSeatId, -1),
            GameCommand.ChangeLife(firstSeatId, -1),
            GameCommand.ChangeLife(firstSeatId, 3)
        ).fold(initial) { session, command -> reduceGame(session, command).session }
        val cleared = reduceGame(changed, GameCommand.ClearLifeRecentChange(firstSeatId)).session

        assertEquals(41, changed.requireSeat(firstSeatId).life.value)
        assertEquals(1, changed.requireSeat(firstSeatId).life.recentChange)
        assertEquals(41, cleared.requireSeat(firstSeatId).life.value)
        assertEquals(0, cleared.requireSeat(firstSeatId).life.recentChange)
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
        val reset = reduceGame(inactive, GameCommand.ResetGame()).session

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
        val reset = reduceGame(incremented, GameCommand.ResetGame()).session

        assertEquals(2, incremented.tableCounterValue(TableCounterType.STORM))
        assertEquals(0, reset.tableCounterValue(TableCounterType.STORM))
    }

    @Test
    fun tableCountersCanResetWithoutResettingSeats() {
        val initial = testSession()
        val changed = listOf(
            GameCommand.ChangeLife(firstSeatId, -5),
            GameCommand.ChangeTableCounter(TableCounterType.WHITE_MANA, 3),
            GameCommand.ChangeTableCounter(TableCounterType.STORM, 2)
        ).fold(initial) { session, command -> reduceGame(session, command).session }

        val reset = reduceGame(changed, GameCommand.ResetTableCounters).session

        assertEquals(35, reset.requireSeat(firstSeatId).life.value)
        assertEquals(0, reset.tableCounterValue(TableCounterType.WHITE_MANA))
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
    fun commanderModeStoresDealerAndPartnerMode() {
        val initial = testSession()

        val active = reduceGame(initial, GameCommand.SetCommanderDealer(firstSeatId)).session
        val partnered = reduceGame(active, GameCommand.SetCommanderPartnerMode(true)).session
        val inactive = reduceGame(partnered, GameCommand.SetCommanderDealer(null)).session

        assertEquals(CommanderMode(firstSeatId, partnerMode = false), active.commanderMode)
        assertEquals(CommanderMode(firstSeatId, partnerMode = true), partnered.commanderMode)
        assertEquals(null, inactive.commanderMode)
    }

    @Test
    fun repeatedCommanderDamageChangesAccumulateRecentChangeUntilCleared() {
        val initial = testSession()

        val changed = listOf(
            GameCommand.ChangeCommanderDamage(firstSeatId, secondSeatId, partner = false, delta = 3),
            GameCommand.ChangeCommanderDamage(firstSeatId, secondSeatId, partner = false, delta = 4),
            GameCommand.ChangeCommanderDamage(firstSeatId, secondSeatId, partner = false, delta = -2)
        ).fold(initial) { session, command -> reduceGame(session, command).session }
        val cleared = reduceGame(
            changed,
            GameCommand.ClearCommanderDamageRecentChange(firstSeatId, secondSeatId, partner = false)
        ).session

        assertEquals(5, changed.commander.damage(firstSeatId, secondSeatId, partner = false).value)
        assertEquals(5, changed.commander.damage(firstSeatId, secondSeatId, partner = false).recentChange)
        assertEquals(5, cleared.commander.damage(firstSeatId, secondSeatId, partner = false).value)
        assertEquals(0, cleared.commander.damage(firstSeatId, secondSeatId, partner = false).recentChange)
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

        val reset = reduceGame(changed, GameCommand.ResetGame()).session
        val resetSeat = reset.requireSeat(firstSeatId)

        assertEquals(40, resetSeat.life.value)
        assertEquals(0, resetSeat.life.recentChange)
        assertFalse(resetSeat.manualDeath)
        assertEquals(originalAppearance, resetSeat.appearance)
        assertEquals(null, reset.monarchSeatId)
        assertEquals(null, reset.commanderMode)
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
                SeatAppearance.defaultForSeat(1),
                SeatAppearance.defaultForSeat(2)
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
