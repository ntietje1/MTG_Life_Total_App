package ui.lifecounter

import androidx.compose.ui.graphics.Color
import domain.common.NumberWithRecentChange
import domain.state.game.CommanderDamageMatrix
import domain.state.game.CounterType
import domain.state.game.DayNight
import domain.state.game.GameRules
import domain.state.game.GameSession
import domain.state.game.GameSessionId
import domain.state.game.SeatAppearance
import domain.state.game.SeatId
import domain.state.game.TableCounterType
import domain.state.game.TrackedInt
import domain.state.profile.PlayerBackground
import domain.state.profile.PlayerColors
import domain.storage.IFileImageStore
import kotlin.test.Test
import kotlin.test.assertEquals
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonState

class GameSessionUiMapperTest {
    @Test
    fun mapsLifeCounterStateFromSessionWhilePreservingScreenOnlyState() {
        val previous = LifeCounterState(
            showButtons = true,
            showLoadingScreen = false,
            blurBackground = true,
            counters = List(7) { -1 },
            middleButtonState = MiddleButtonState.COMMANDER_EXIT
        )
        val session = testSession().copy(
            dayNight = DayNight.NIGHT,
            tableCounters = mapOf(
                TableCounterType.WHITE_MANA to 1,
                TableCounterType.BLUE_MANA to 2,
                TableCounterType.BLACK_MANA to 3,
                TableCounterType.RED_MANA to 4,
                TableCounterType.GREEN_MANA to 5,
                TableCounterType.COLORLESS_MANA to 6,
                TableCounterType.STORM to 7,
                TableCounterType.SNOW_MANA to 99
            )
        )

        val mapped = GameSessionUiMapper.mapLifeCounterState(session, previous)

        assertEquals(true, mapped.showButtons)
        assertEquals(false, mapped.showLoadingScreen)
        assertEquals(true, mapped.blurBackground)
        assertEquals(DayNightState.NIGHT, mapped.dayNight)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), mapped.counters)
        assertEquals(MiddleButtonState.COMMANDER_EXIT, mapped.middleButtonState)
    }

    @Test
    fun mapsSeatStateToExistingPlayerButtonState() {
        val receiverSeatId = SeatId("seat-2")
        val commander = CommanderDamageMatrix()
            .changeDamage(SeatId("seat-1"), receiverSeatId, partner = false, delta = 11)
            .changeDamage(SeatId("seat-1"), receiverSeatId, partner = true, delta = 4)
        val session = testSession().copy(
            monarchSeatId = receiverSeatId,
            commander = commander,
            seats = listOf(
                testSession().requireSeat(SeatId("seat-1")),
                testSession().requireSeat(receiverSeatId).copy(
                    appearance = SeatAppearance(
                        displayName = "Nissa",
                        colors = PlayerColors(backgroundArgb = -15654349, textArgb = -12298906),
                        background = PlayerBackground.LocalImage("nissa.png")
                    ),
                    life = TrackedInt(value = 24, recentChange = -3),
                    manualDeath = true,
                    counters = mapOf(CounterType.POISON to 2, CounterType.ENERGY to 5),
                    activeCounters = setOf(CounterType.POISON, CounterType.ENERGY)
                )
            )
        )
        val previous = PlayerButtonState(
            player = model.Player(playerNum = 2),
            buttonState = PBState.SETTINGS,
            showCustomizeMenu = true
        )

        val mapped = GameSessionUiMapper.mapPlayerButtonState(
            session = session,
            seatId = receiverSeatId,
            current = previous,
            fileImageStore = FakeFileImageStore(mapOf("nissa.png" to "file:///images/nissa.png"))
        )
        val player = mapped.player

        assertEquals(PBState.SETTINGS, mapped.buttonState)
        assertEquals(true, mapped.showCustomizeMenu)
        assertEquals(2, player.playerNum)
        assertEquals("Nissa", player.name)
        assertEquals("file:///images/nissa.png", player.imageString)
        assertEquals(Color(-15654349), player.color)
        assertEquals(Color(-12298906), player.textColor)
        assertEquals(NumberWithRecentChange(number = 24, recentChange = -3), player.lifeTotal)
        assertEquals(true, player.monarch)
        assertEquals(true, player.setDead)
        assertEquals(2, player.counters[CounterType.POISON.toUiCounter().ordinal])
        assertEquals(5, player.counters[CounterType.ENERGY.toUiCounter().ordinal])
        assertEquals(
            listOf(CounterType.POISON.toUiCounter(), CounterType.ENERGY.toUiCounter()),
            player.activeCounters
        )
        assertEquals(NumberWithRecentChange(number = 11, recentChange = 11), player.commanderDamage[0])
        assertEquals(NumberWithRecentChange(number = 4, recentChange = 4), player.commanderDamage[6])
    }

    @Test
    fun mapsLifeCounterUiStateWithPlayerSeatSnapshots() {
        val receiverSeatId = SeatId("seat-2")
        val commander = CommanderDamageMatrix()
            .changeDamage(SeatId("seat-1"), receiverSeatId, partner = false, delta = 7)
        val session = testSession().copy(
            commanderMode = domain.state.game.CommanderMode(SeatId("seat-1"), partnerMode = false),
            monarchSeatId = receiverSeatId,
            commander = commander,
            seats = listOf(
                testSession().requireSeat(SeatId("seat-1")),
                testSession().requireSeat(receiverSeatId).copy(
                    appearance = SeatAppearance(
                        displayName = "Nissa",
                        colors = PlayerColors(backgroundArgb = -15654349, textArgb = -12298906),
                        background = PlayerBackground.LocalImage("nissa.png")
                    ),
                    life = TrackedInt(value = 24, recentChange = -3),
                    counters = mapOf(CounterType.POISON to 2),
                    activeCounters = setOf(CounterType.POISON)
                )
            )
        )

        val mapped = GameSessionUiMapper.mapLifeCounterUiState(
            session = session,
            current = LifeCounterState(),
            fileImageStore = FakeFileImageStore(mapOf("nissa.png" to "file:///images/nissa.png"))
        )

        assertEquals(2, mapped.players.size)
        assertEquals("P1", mapped.players[0].player.name)
        assertEquals(PBState.COMMANDER_DEALER, mapped.players[0].buttonState)
        assertEquals("Nissa", mapped.players[1].player.name)
        assertEquals("file:///images/nissa.png", mapped.players[1].player.imageString)
        assertEquals(NumberWithRecentChange(number = 24, recentChange = -3), mapped.players[1].player.lifeTotal)
        assertEquals(true, mapped.players[1].player.monarch)
        assertEquals(PBState.COMMANDER_RECEIVER, mapped.players[1].buttonState)
        assertEquals(NumberWithRecentChange(number = 7, recentChange = 7), mapped.players[1].player.commanderDamage[0])
    }

    @Test
    fun mapsUiCommandsToDomainCounters() {
        assertEquals(SeatId("seat-3"), GameSessionUiMapper.seatIdForPlayerNumber(3))
        assertEquals(TableCounterType.WHITE_MANA, GameSessionUiMapper.tableCounterForIndex(0))
        assertEquals(TableCounterType.STORM, GameSessionUiMapper.tableCounterForIndex(6))
        assertEquals(CounterType.POISON, GameSessionUiMapper.domainCounterFor(CounterType.POISON.toUiCounter()))
        assertEquals(CounterType.COIN, GameSessionUiMapper.domainCounterFor(ui.lifecounter.CounterType.Coin))
    }

    @Test
    fun mapsCommanderModeToButtonState() {
        val inactive = testSession()
        val active = inactive.copy(
            commanderMode = domain.state.game.CommanderMode(SeatId("seat-1"), partnerMode = true)
        )

        assertEquals(MiddleButtonState.DEFAULT, GameSessionUiMapper.mapMiddleButtonState(inactive))
        assertEquals(PBState.NORMAL, GameSessionUiMapper.mapCommanderButtonState(inactive, SeatId("seat-1")))
        assertEquals(PBState.NORMAL, GameSessionUiMapper.mapCommanderButtonState(inactive, SeatId("seat-2")))
        assertEquals(MiddleButtonState.COMMANDER_EXIT, GameSessionUiMapper.mapMiddleButtonState(active))
        assertEquals(PBState.COMMANDER_DEALER, GameSessionUiMapper.mapCommanderButtonState(active, SeatId("seat-1")))
        assertEquals(PBState.COMMANDER_RECEIVER, GameSessionUiMapper.mapCommanderButtonState(active, SeatId("seat-2")))
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
}

private class FakeFileImageStore(
    private val localUris: Map<String, String> = emptyMap()
) : IFileImageStore {
    override suspend fun saveImage(bytes: ByteArray): String = "local-image"

    override fun localImageUri(imageId: String): String? {
        return localUris[imageId]
    }

    override fun deleteImage(imageId: String) = Unit
}

private fun CounterType.toUiCounter(): ui.lifecounter.CounterType {
    return when (this) {
        CounterType.POISON -> ui.lifecounter.CounterType.Poison
        CounterType.EXPERIENCE -> ui.lifecounter.CounterType.Experience
        CounterType.ENERGY -> ui.lifecounter.CounterType.Energy
        CounterType.COMMANDER_TAX_PRIMARY -> ui.lifecounter.CounterType.CommanderTax1
        CounterType.COMMANDER_TAX_SECONDARY -> ui.lifecounter.CounterType.CommanderTax2
        CounterType.TICKET -> ui.lifecounter.CounterType.Ticket
        CounterType.ACORN -> ui.lifecounter.CounterType.Acorn
        CounterType.WHITE_MANA -> ui.lifecounter.CounterType.WhiteMana
        CounterType.BLUE_MANA -> ui.lifecounter.CounterType.BlueMana
        CounterType.BLACK_MANA -> ui.lifecounter.CounterType.BlackMana
        CounterType.RED_MANA -> ui.lifecounter.CounterType.RedMana
        CounterType.GREEN_MANA -> ui.lifecounter.CounterType.GreenMana
        CounterType.COLORLESS_MANA -> ui.lifecounter.CounterType.ColorlessMana
        CounterType.SNOW_MANA -> ui.lifecounter.CounterType.SnowMana
        CounterType.CHAOS -> ui.lifecounter.CounterType.Chaos
        CounterType.PLANESWALKER -> ui.lifecounter.CounterType.Planeswalker
        CounterType.D20 -> ui.lifecounter.CounterType.D20
        CounterType.COIN -> ui.lifecounter.CounterType.Coin
        CounterType.BOLT -> ui.lifecounter.CounterType.Bolt
        CounterType.STAR -> ui.lifecounter.CounterType.Star
        CounterType.HEART -> ui.lifecounter.CounterType.Heart
        CounterType.SHIELD -> ui.lifecounter.CounterType.Shield
        CounterType.SWORD -> ui.lifecounter.CounterType.Sword
    }
}
