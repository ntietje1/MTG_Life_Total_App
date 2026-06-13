package domain.state.legacy

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import domain.common.NumberWithRecentChange
import domain.state.game.CounterType
import domain.state.game.GameRules
import domain.state.game.GameSeat
import domain.state.game.GameSession
import domain.state.game.GameSessionId
import domain.state.game.SeatAppearance
import domain.state.game.SeatId
import domain.state.game.TrackedInt
import domain.state.profile.PlayerBackground
import domain.state.profile.PlayerColors
import model.Player
import ui.lifecounter.CounterType as LegacyCounterType

object LocalGameSessionMapper {
    fun fromLegacyOrFresh(
        id: GameSessionId,
        rules: GameRules,
        legacyPlayers: List<Player>,
        fallbackSeatCount: Int
    ): GameSession {
        val validPlayers = legacyPlayers.takeIf(::hasValidPlayers)
        if (validPlayers == null) {
            return freshSession(id, rules, fallbackSeatCount)
        }

        return GameSession(
            id = id,
            rules = rules,
            seats = validPlayers.map(::legacyPlayerToSeat)
        )
    }

    fun toLegacyPlayers(session: GameSession): List<Player> {
        return session.seats.map { seat ->
            val playerNumber = seat.id.value.removePrefix("seat-").toIntOrNull() ?: 1
            Player(
                lifeTotal = seat.life.toLegacyNumberWithRecentChange(),
                imageString = when (val background = seat.appearance.background) {
                    is PlayerBackground.LocalImage -> background.fileName
                    is PlayerBackground.ProviderImage -> background.url
                    is PlayerBackground.CardArt -> background.url
                    PlayerBackground.None -> null
                },
                color = Color(seat.appearance.colors.backgroundArgb),
                textColor = Color(seat.appearance.colors.textArgb),
                playerNum = playerNumber,
                name = seat.appearance.displayName,
                counters = legacyCountersFromSeat(seat),
                activeCounters = seat.activeCounters.map(::toLegacyCounter),
                setDead = seat.manualDeath
            )
        }
    }

    private fun hasValidPlayers(players: List<Player>): Boolean {
        return players.isNotEmpty() &&
            players.size <= GameSession.MaxSeats &&
            players.all { it.playerNum in 1..GameSession.MaxSeats }
    }

    private fun freshSession(id: GameSessionId, rules: GameRules, seatCount: Int): GameSession {
        val safeSeatCount = seatCount.coerceIn(1, GameSession.MaxSeats)
        return GameSession.newGame(
            id = id,
            rules = rules,
            appearances = (1..safeSeatCount).map { seatNumber ->
                SeatAppearance(displayName = "P$seatNumber")
            }
        )
    }

    private fun legacyPlayerToSeat(player: Player): GameSeat {
        return GameSeat(
            id = SeatId("seat-${player.playerNum}"),
            appearance = SeatAppearance(
                displayName = player.name,
                colors = PlayerColors(
                    backgroundArgb = player.color.toArgb(),
                    textArgb = player.textColor.toArgb()
                ),
                background = player.imageString?.let(PlayerBackground::LocalImage) ?: PlayerBackground.None
            ),
            life = player.lifeTotal.toTrackedInt(),
            manualDeath = player.setDead,
            counters = domainCountersFromLegacy(player.counters),
            activeCounters = player.activeCounters.map(::toDomainCounter).toSet()
        )
    }

    private fun domainCountersFromLegacy(counters: List<Int>): Map<CounterType, Int> {
        return CounterType.entries.mapNotNull { counter: CounterType ->
            val legacyCounter = toLegacyCounter(counter)
            val value = counters.getOrNull(legacyCounter.ordinal) ?: 0
            if (value == 0) null else counter to value
        }.toMap()
    }

    private fun legacyCountersFromSeat(seat: GameSeat): List<Int> {
        val counters = MutableList(LegacyCounterType.entries.size) { 0 }
        seat.counters.forEach { (counter, value) ->
            val legacyCounter = toLegacyCounter(counter)
            counters[legacyCounter.ordinal] = value
        }
        return counters
    }

    private fun toDomainCounter(counter: LegacyCounterType): CounterType {
        return when (counter) {
            LegacyCounterType.Poison -> CounterType.POISON
            LegacyCounterType.Experience -> CounterType.EXPERIENCE
            LegacyCounterType.Energy -> CounterType.ENERGY
            LegacyCounterType.CommanderTax1 -> CounterType.COMMANDER_TAX_PRIMARY
            LegacyCounterType.CommanderTax2 -> CounterType.COMMANDER_TAX_SECONDARY
            LegacyCounterType.Ticket -> CounterType.TICKET
            LegacyCounterType.Acorn -> CounterType.ACORN
            LegacyCounterType.WhiteMana -> CounterType.WHITE_MANA
            LegacyCounterType.BlueMana -> CounterType.BLUE_MANA
            LegacyCounterType.BlackMana -> CounterType.BLACK_MANA
            LegacyCounterType.RedMana -> CounterType.RED_MANA
            LegacyCounterType.GreenMana -> CounterType.GREEN_MANA
            LegacyCounterType.ColorlessMana -> CounterType.COLORLESS_MANA
            LegacyCounterType.SnowMana -> CounterType.SNOW_MANA
            LegacyCounterType.Chaos -> CounterType.CHAOS
            LegacyCounterType.Planeswalker -> CounterType.PLANESWALKER
            LegacyCounterType.D20 -> CounterType.D20
            LegacyCounterType.Coin -> CounterType.COIN
            LegacyCounterType.Bolt -> CounterType.BOLT
            LegacyCounterType.Star -> CounterType.STAR
            LegacyCounterType.Heart -> CounterType.HEART
            LegacyCounterType.Shield -> CounterType.SHIELD
            LegacyCounterType.Sword -> CounterType.SWORD
        }
    }

    private fun toLegacyCounter(counter: CounterType): LegacyCounterType {
        return when (counter) {
            CounterType.POISON -> LegacyCounterType.Poison
            CounterType.EXPERIENCE -> LegacyCounterType.Experience
            CounterType.ENERGY -> LegacyCounterType.Energy
            CounterType.COMMANDER_TAX_PRIMARY -> LegacyCounterType.CommanderTax1
            CounterType.COMMANDER_TAX_SECONDARY -> LegacyCounterType.CommanderTax2
            CounterType.TICKET -> LegacyCounterType.Ticket
            CounterType.ACORN -> LegacyCounterType.Acorn
            CounterType.WHITE_MANA -> LegacyCounterType.WhiteMana
            CounterType.BLUE_MANA -> LegacyCounterType.BlueMana
            CounterType.BLACK_MANA -> LegacyCounterType.BlackMana
            CounterType.RED_MANA -> LegacyCounterType.RedMana
            CounterType.GREEN_MANA -> LegacyCounterType.GreenMana
            CounterType.COLORLESS_MANA -> LegacyCounterType.ColorlessMana
            CounterType.SNOW_MANA -> LegacyCounterType.SnowMana
            CounterType.CHAOS -> LegacyCounterType.Chaos
            CounterType.PLANESWALKER -> LegacyCounterType.Planeswalker
            CounterType.D20 -> LegacyCounterType.D20
            CounterType.COIN -> LegacyCounterType.Coin
            CounterType.BOLT -> LegacyCounterType.Bolt
            CounterType.STAR -> LegacyCounterType.Star
            CounterType.HEART -> LegacyCounterType.Heart
            CounterType.SHIELD -> LegacyCounterType.Shield
            CounterType.SWORD -> LegacyCounterType.Sword
        }
    }
}

fun NumberWithRecentChange.toTrackedInt(): TrackedInt {
    return TrackedInt(value = number, recentChange = recentChange)
}

fun TrackedInt.toLegacyNumberWithRecentChange(): NumberWithRecentChange {
    return NumberWithRecentChange(number = value, recentChange = recentChange)
}
