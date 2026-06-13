package domain.state.game

import kotlinx.serialization.Serializable

private const val CommanderDamageMinimum = 0
private const val CommanderDamageMaximum = 99
private const val CommanderDamageLethal = 21

@Serializable
data class CommanderDamageMatrix(
    private val damage: Map<CommanderDamageKey, TrackedInt> = emptyMap()
) {
    fun damage(dealerSeatId: SeatId, receiverSeatId: SeatId, partner: Boolean): TrackedInt {
        return damage[CommanderDamageKey(dealerSeatId, receiverSeatId, partner)] ?: TrackedInt.Zero
    }

    fun changeDamage(
        dealerSeatId: SeatId,
        receiverSeatId: SeatId,
        partner: Boolean,
        delta: Int
    ): CommanderDamageMatrix {
        val key = CommanderDamageKey(dealerSeatId, receiverSeatId, partner)
        val current = damage[key] ?: TrackedInt.Zero
        val nextValue = (current.value + delta).coerceIn(CommanderDamageMinimum, CommanderDamageMaximum)
        val actualDelta = nextValue - current.value
        val nextDamage = if (nextValue == CommanderDamageMinimum) {
            damage - key
        } else {
            damage + (key to TrackedInt(value = nextValue, recentChange = current.recentChange + actualDelta))
        }
        return copy(damage = nextDamage)
    }

    fun clearRecentChange(
        dealerSeatId: SeatId,
        receiverSeatId: SeatId,
        partner: Boolean
    ): CommanderDamageMatrix {
        val key = CommanderDamageKey(dealerSeatId, receiverSeatId, partner)
        val current = damage[key] ?: return this
        return copy(damage = damage + (key to current.clearRecentChange()))
    }

    fun hasLethalDamage(receiverSeatId: SeatId): Boolean {
        return damage.any { (key, value) ->
            key.receiverSeatId == receiverSeatId && value.value >= CommanderDamageLethal
        }
    }
}

@Serializable
data class CommanderDamageKey(
    val dealerSeatId: SeatId,
    val receiverSeatId: SeatId,
    val partner: Boolean
)
