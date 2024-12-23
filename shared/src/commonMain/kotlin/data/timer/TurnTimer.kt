package data.timer

data class TurnTimer(val seconds: Int, val turn: Int) {
    fun tick(): TurnTimer {
        return TurnTimer(seconds + 1, turn)
    }

    fun resetTime(): TurnTimer {
        return TurnTimer(0, turn)
    }

    fun getTimeString(): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "$minutes:${if (remainingSeconds < 10) "0" else ""}$remainingSeconds"
    }
}