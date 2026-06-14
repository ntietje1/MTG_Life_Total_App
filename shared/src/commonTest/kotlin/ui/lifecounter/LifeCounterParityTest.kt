package ui.lifecounter

import domain.state.game.GameCommand
import domain.state.game.GameRules
import domain.state.game.GameSession
import domain.state.game.GameSessionId
import domain.state.game.SeatAppearance
import domain.state.game.SeatId
import domain.state.game.reduceGame
import domain.storage.IFileImageStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ui.lifecounter.playerbutton.PBState

class LifeCounterParityTest {
    private val firstSeatId = SeatId("seat-1")
    private val secondSeatId = SeatId("seat-2")

    @Test
    fun mapsLifeChangesIntoPlayerSeatState() {
        val session = testSession()
            .apply(GameCommand.ChangeLife(firstSeatId, -5))

        val state = mapSession(session)

        assertEquals(35, state.players[0].player.life)
        assertEquals(-5, state.players[0].player.lifeTotal.recentChange)
    }

    @Test
    fun mapsCommanderModeIntoDealerAndReceiverButtons() {
        val session = testSession()
            .apply(GameCommand.SetCommanderDealer(firstSeatId))
            .apply(GameCommand.ChangeCommanderDamage(firstSeatId, secondSeatId, partner = false, delta = 7))
            .apply(GameCommand.ChangeLife(secondSeatId, -7))

        val state = mapSession(session)

        assertEquals(MiddleButtonState.COMMANDER_EXIT, state.middleButtonState)
        assertEquals(PBState.COMMANDER_DEALER, state.players[0].buttonState)
        assertEquals(PBState.COMMANDER_RECEIVER, state.players[1].buttonState)
        assertEquals(33, state.players[1].player.life)
        assertEquals(7, state.players[1].player.commanderDamage[0].number)
    }

    @Test
    fun mapsCounterSelectionIntoVisiblePlayerCounters() {
        val session = testSession()
            .apply(GameCommand.SetSeatCounterActive(firstSeatId, domain.state.game.CounterType.POISON, true))
            .apply(GameCommand.ChangeSeatCounter(firstSeatId, domain.state.game.CounterType.POISON, 2))

        val state = mapSession(session)

        assertTrue(ui.lifecounter.CounterType.Poison in state.players[0].player.activeCounters)
        assertEquals(2, state.players[0].player.counters[ui.lifecounter.CounterType.Poison.ordinal])
    }

    @Test
    fun modalStackCoversMiddleMenuOpenNestedAndCloseFlow() {
        val stack = LifeCounterModalStack()
            .open(LifeCounterModal.Default)
            .open(LifeCounterModal.Scryfall)

        assertEquals(LifeCounterModal.Scryfall, stack.current)
        assertEquals(LifeCounterModal.Default, stack.goBack().current)
        assertEquals(null, stack.goBack().goBack().current)
    }

    private fun mapSession(session: GameSession): LifeCounterState {
        return GameSessionUiMapper.mapLifeCounterUiState(
            session = session,
            current = LifeCounterState(),
            fileImageStore = FakeFileImageStore(),
            autoKo = true
        )
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

    private fun GameSession.apply(command: GameCommand): GameSession {
        return reduceGame(this, command).session
    }

    private class FakeFileImageStore : IFileImageStore {
        override suspend fun saveImage(bytes: ByteArray): String = "image-id"
        override fun localImageUri(imageId: String): String? = null
        override fun deleteImage(imageId: String) = Unit
    }
}
