package domain.game.timer

import kotlinx.serialization.Serializable

@Serializable
data class TurnTimer(val seconds: Int, val turn: Int) {
    fun tick(): TurnTimer {
        return TurnTimer(seconds + 1, turn)
    }

    fun getTimeString(): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val paddedSeconds = remainingSeconds.toString().padStart(2, '0')

        return "$minutes:$paddedSeconds"
    }
}