package data

data class TurnTimer(val seconds: Int, val turn: Int) {
    fun tick(): TurnTimer {
        return TurnTimer(seconds + 1, turn)
    }

    fun nextTurn(): TurnTimer {
        return TurnTimer(seconds, turn + 1)
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