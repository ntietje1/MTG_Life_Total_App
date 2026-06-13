package domain.state.game

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class GameSessionId(val value: String)

@Serializable
@JvmInline
value class SeatId(val value: String)

@Serializable
@JvmInline
value class PlayerProfileId(val value: String)
