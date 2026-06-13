package ui.lifecounter

import androidx.compose.ui.graphics.Color
import domain.common.NumberWithRecentChange
import domain.state.game.DayNight
import domain.state.game.GameSeat
import domain.state.game.GameSession
import domain.state.game.SeatId
import domain.state.game.TableCounterType
import domain.state.game.TrackedInt
import domain.storage.IFileImageStore
import domain.storage.displayUri
import model.Player
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.PlayerButtonState
import domain.state.game.CounterType as DomainCounterType

object GameSessionUiMapper {
    fun mapLifeCounterState(session: GameSession, current: LifeCounterState): LifeCounterState {
        return current.copy(
            dayNight = session.dayNight.toUiDayNight(),
            counters = visibleTableCounters.map(session::tableCounterValue)
        )
    }

    fun mapLifeCounterUiState(
        session: GameSession,
        current: LifeCounterState,
        fileImageStore: IFileImageStore,
        autoKo: Boolean
    ): LifeCounterState {
        return mapLifeCounterState(session, current).copy(
            players = session.seats.map { seat ->
                mapPlayerSeatUiState(
                    session = session,
                    seatId = seat.id,
                    current = current.players.firstOrNull { player -> player.seatId == seat.id },
                    fileImageStore = fileImageStore,
                    autoKo = autoKo
                )
            },
            middleButtonState = mapMiddleButtonState(session)
        )
    }

    fun mapPlayerSeatUiState(
        session: GameSession,
        seatId: SeatId,
        current: PlayerSeatUiState?,
        fileImageStore: IFileImageStore,
        autoKo: Boolean
    ): PlayerSeatUiState {
        val seat = session.requireSeat(seatId)
        val commanderDealerPlayer = session.commanderMode?.let { mode ->
            session.requireSeat(mode.dealerSeatId)
                .toPlayer(session, fileImageStore)
                .copy(partnerMode = mode.partnerMode)
        }
        return PlayerSeatUiState(
            seatId = seatId,
            player = seat.toPlayer(session, fileImageStore),
            buttonState = if (session.commanderMode != null) {
                mapCommanderButtonState(session, seatId)
            } else {
                current?.buttonState ?: PBState.NORMAL
            },
            showCustomizeMenu = current?.showCustomizeMenu ?: false,
            timer = current?.timer,
            commanderState = commanderDealerPlayer?.let(ui.lifecounter.playerbutton.CommanderState::Active)
                ?: ui.lifecounter.playerbutton.CommanderState.Inactive,
            isDead = seat.isDead(autoKo = autoKo, commander = session.commander),
            backButtonVisible = current?.backButtonVisible ?: false
        )
    }

    fun mapPlayerButtonState(
        session: GameSession,
        seatId: SeatId,
        current: PlayerButtonState,
        fileImageStore: IFileImageStore
    ): PlayerButtonState {
        val seat = session.requireSeat(seatId)
        return current.copy(
            player = seat.toPlayer(session, fileImageStore)
        )
    }

    fun mapMiddleButtonState(session: GameSession): MiddleButtonState {
        return if (session.commanderMode == null) {
            MiddleButtonState.DEFAULT
        } else {
            MiddleButtonState.COMMANDER_EXIT
        }
    }

    fun mapCommanderButtonState(session: GameSession, seatId: SeatId): PBState {
        val mode = session.commanderMode ?: return PBState.NORMAL
        return if (mode.dealerSeatId == seatId) {
            PBState.COMMANDER_DEALER
        } else {
            PBState.COMMANDER_RECEIVER
        }
    }

    fun seatIdForPlayerNumber(playerNumber: Int): SeatId {
        return SeatId("seat-$playerNumber")
    }

    fun tableCounterForIndex(index: Int): TableCounterType {
        return visibleTableCounters[index]
    }

    fun domainCounterFor(counter: CounterType): DomainCounterType {
        return when (counter) {
            CounterType.Poison -> DomainCounterType.POISON
            CounterType.Experience -> DomainCounterType.EXPERIENCE
            CounterType.Energy -> DomainCounterType.ENERGY
            CounterType.CommanderTax1 -> DomainCounterType.COMMANDER_TAX_PRIMARY
            CounterType.CommanderTax2 -> DomainCounterType.COMMANDER_TAX_SECONDARY
            CounterType.Ticket -> DomainCounterType.TICKET
            CounterType.Acorn -> DomainCounterType.ACORN
            CounterType.WhiteMana -> DomainCounterType.WHITE_MANA
            CounterType.BlueMana -> DomainCounterType.BLUE_MANA
            CounterType.BlackMana -> DomainCounterType.BLACK_MANA
            CounterType.RedMana -> DomainCounterType.RED_MANA
            CounterType.GreenMana -> DomainCounterType.GREEN_MANA
            CounterType.ColorlessMana -> DomainCounterType.COLORLESS_MANA
            CounterType.SnowMana -> DomainCounterType.SNOW_MANA
            CounterType.Chaos -> DomainCounterType.CHAOS
            CounterType.Planeswalker -> DomainCounterType.PLANESWALKER
            CounterType.D20 -> DomainCounterType.D20
            CounterType.Coin -> DomainCounterType.COIN
            CounterType.Bolt -> DomainCounterType.BOLT
            CounterType.Star -> DomainCounterType.STAR
            CounterType.Heart -> DomainCounterType.HEART
            CounterType.Shield -> DomainCounterType.SHIELD
            CounterType.Sword -> DomainCounterType.SWORD
        }
    }

    private val visibleTableCounters = listOf(
        TableCounterType.WHITE_MANA,
        TableCounterType.BLUE_MANA,
        TableCounterType.BLACK_MANA,
        TableCounterType.RED_MANA,
        TableCounterType.GREEN_MANA,
        TableCounterType.COLORLESS_MANA,
        TableCounterType.STORM
    )

    private fun DayNight.toUiDayNight(): DayNightState {
        return when (this) {
            DayNight.NONE -> DayNightState.NONE
            DayNight.DAY -> DayNightState.DAY
            DayNight.NIGHT -> DayNightState.NIGHT
        }
    }

    private fun GameSeat.toPlayer(session: GameSession, fileImageStore: IFileImageStore): Player {
        return Player(
            lifeTotal = life.toNumberWithRecentChange(),
            imageString = appearance.background.displayUri(fileImageStore),
            background = appearance.background,
            color = Color(appearance.colors.backgroundArgb),
            textColor = Color(appearance.colors.textArgb),
            playerNum = id.toPlayerNumber(),
            name = appearance.displayName,
            monarch = session.monarchSeatId == id,
            commanderDamage = commanderDamageForReceiver(session),
            counters = uiCountersFromSeat(),
            activeCounters = uiActiveCountersFromSeat(),
            setDead = manualDeath
        )
    }

    private fun GameSeat.commanderDamageForReceiver(session: GameSession): List<NumberWithRecentChange> {
        val primaryDamage = (1..Player.MAX_PLAYERS).map { dealerNumber ->
            session.commander.damage(
                dealerSeatId = seatIdForPlayerNumber(dealerNumber),
                receiverSeatId = id,
                partner = false
            ).toNumberWithRecentChange()
        }
        val partnerDamage = (1..Player.MAX_PLAYERS).map { dealerNumber ->
            session.commander.damage(
                dealerSeatId = seatIdForPlayerNumber(dealerNumber),
                receiverSeatId = id,
                partner = true
            ).toNumberWithRecentChange()
        }
        return primaryDamage + partnerDamage
    }

    private fun GameSeat.uiCountersFromSeat(): List<Int> {
        val values = MutableList(CounterType.entries.size) { 0 }
        counters.forEach { (counter, value) ->
            values[counter.toUiCounter().ordinal] = value
        }
        return values
    }

    private fun GameSeat.uiActiveCountersFromSeat(): List<CounterType> {
        return DomainCounterType.entries
            .filter(activeCounters::contains)
            .map { counter -> counter.toUiCounter() }
    }

    private fun DomainCounterType.toUiCounter(): CounterType {
        return when (this) {
            DomainCounterType.POISON -> CounterType.Poison
            DomainCounterType.EXPERIENCE -> CounterType.Experience
            DomainCounterType.ENERGY -> CounterType.Energy
            DomainCounterType.COMMANDER_TAX_PRIMARY -> CounterType.CommanderTax1
            DomainCounterType.COMMANDER_TAX_SECONDARY -> CounterType.CommanderTax2
            DomainCounterType.TICKET -> CounterType.Ticket
            DomainCounterType.ACORN -> CounterType.Acorn
            DomainCounterType.WHITE_MANA -> CounterType.WhiteMana
            DomainCounterType.BLUE_MANA -> CounterType.BlueMana
            DomainCounterType.BLACK_MANA -> CounterType.BlackMana
            DomainCounterType.RED_MANA -> CounterType.RedMana
            DomainCounterType.GREEN_MANA -> CounterType.GreenMana
            DomainCounterType.COLORLESS_MANA -> CounterType.ColorlessMana
            DomainCounterType.SNOW_MANA -> CounterType.SnowMana
            DomainCounterType.CHAOS -> CounterType.Chaos
            DomainCounterType.PLANESWALKER -> CounterType.Planeswalker
            DomainCounterType.D20 -> CounterType.D20
            DomainCounterType.COIN -> CounterType.Coin
            DomainCounterType.BOLT -> CounterType.Bolt
            DomainCounterType.STAR -> CounterType.Star
            DomainCounterType.HEART -> CounterType.Heart
            DomainCounterType.SHIELD -> CounterType.Shield
            DomainCounterType.SWORD -> CounterType.Sword
        }
    }

    private fun TrackedInt.toNumberWithRecentChange(): NumberWithRecentChange {
        return NumberWithRecentChange(number = value, recentChange = recentChange)
    }

    private fun SeatId.toPlayerNumber(): Int {
        return value.removePrefix("seat-").toInt()
    }
}
