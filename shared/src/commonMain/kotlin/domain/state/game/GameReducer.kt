package domain.state.game

fun reduceGame(
    state: GameSession,
    command: GameCommand
): GameReduction {
    return GameReduction(
        session = when (command) {
            is GameCommand.ClearCommanderDamageRecentChange -> state.clearCommanderDamageRecentChange(command)
            is GameCommand.ClearLifeRecentChange -> state.clearLifeRecentChange(command)
            is GameCommand.ChangeCommanderDamage -> state.changeCommanderDamage(command)
            is GameCommand.ChangeLife -> state.changeLife(command)
            is GameCommand.ChangeSeatCounter -> state.changeSeatCounter(command)
            is GameCommand.ChangeTableCounter -> state.changeTableCounter(command)
            is GameCommand.SetCommanderDealer -> state.setCommanderDealer(command)
            is GameCommand.SetCommanderPartnerMode -> state.setCommanderPartnerMode(command)
            is GameCommand.SetManualDeath -> state.setManualDeath(command)
            is GameCommand.SetMonarch -> state.setMonarch(command)
            is GameCommand.SetSeatAppearance -> state.setSeatAppearance(command)
            is GameCommand.SetSeatCounterActive -> state.setSeatCounterActive(command)
            is GameCommand.ResetGame -> state.resetGame(command)
            GameCommand.ResetTableCounters -> state.resetTableCounters()
            GameCommand.ToggleDayNight -> state.toggleDayNight()
        }
    )
}

private fun GameSession.changeLife(command: GameCommand.ChangeLife): GameSession {
    return updateSeat(command.seatId) { seat ->
        seat.copy(life = seat.life.change(command.delta))
    }.incrementVersion()
}

private fun GameSession.clearLifeRecentChange(command: GameCommand.ClearLifeRecentChange): GameSession {
    return updateSeat(command.seatId) { seat ->
        seat.copy(life = seat.life.clearRecentChange())
    }.incrementVersion()
}

private fun GameSession.setManualDeath(command: GameCommand.SetManualDeath): GameSession {
    return updateSeat(command.seatId) { seat ->
        seat.copy(manualDeath = command.dead)
    }.incrementVersion()
}

private fun GameSession.setSeatAppearance(command: GameCommand.SetSeatAppearance): GameSession {
    val requestedName = command.appearance.displayName
    val duplicateName = seats.any { seat ->
        seat.id != command.seatId && seat.appearance.displayName.samePlayerNameAs(requestedName)
    }
    return updateSeat(command.seatId) { seat ->
        seat.copy(
            appearance = if (duplicateName) {
                command.appearance.copy(displayName = seat.appearance.displayName)
            } else {
                command.appearance
            }
        )
    }.incrementVersion()
}

private fun String.samePlayerNameAs(other: String): Boolean {
    return trim().lowercase() == other.trim().lowercase()
}

private fun GameSession.setMonarch(command: GameCommand.SetMonarch): GameSession {
    command.seatId?.let(::requireSeat)
    return copy(monarchSeatId = command.seatId).incrementVersion()
}

private fun GameSession.setCommanderDealer(command: GameCommand.SetCommanderDealer): GameSession {
    command.seatId?.let(::requireSeat)
    return copy(
        commanderMode = command.seatId?.let { seatId ->
            CommanderMode(dealerSeatId = seatId)
        }
    ).incrementVersion()
}

private fun GameSession.setCommanderPartnerMode(command: GameCommand.SetCommanderPartnerMode): GameSession {
    val currentMode = commanderMode ?: return this
    return copy(
        commanderMode = currentMode.copy(partnerMode = command.partnerMode)
    ).incrementVersion()
}

private fun GameSession.changeSeatCounter(command: GameCommand.ChangeSeatCounter): GameSession {
    return updateSeat(command.seatId) { seat ->
        val nextValue = (seat.counterValue(command.counter) + command.delta).coerceAtLeast(0)
        val nextCounters = if (nextValue == 0) {
            seat.counters - command.counter
        } else {
            seat.counters + (command.counter to nextValue)
        }
        seat.copy(counters = nextCounters)
    }.incrementVersion()
}

private fun GameSession.setSeatCounterActive(command: GameCommand.SetSeatCounterActive): GameSession {
    return updateSeat(command.seatId) { seat ->
        seat.copy(
            activeCounters = if (command.active) {
                seat.activeCounters + command.counter
            } else {
                seat.activeCounters - command.counter
            }
        )
    }.incrementVersion()
}

private fun GameSession.changeTableCounter(command: GameCommand.ChangeTableCounter): GameSession {
    val nextValue = (tableCounterValue(command.counter) + command.delta).coerceAtLeast(0)
    val nextCounters = if (nextValue == 0) {
        tableCounters - command.counter
    } else {
        tableCounters + (command.counter to nextValue)
    }
    return copy(tableCounters = nextCounters).incrementVersion()
}

private fun GameSession.resetTableCounters(): GameSession {
    return copy(tableCounters = emptyMap()).incrementVersion()
}

private fun GameSession.changeCommanderDamage(command: GameCommand.ChangeCommanderDamage): GameSession {
    requireSeat(command.dealerSeatId)
    requireSeat(command.receiverSeatId)
    return copy(
        commander = commander.changeDamage(
            dealerSeatId = command.dealerSeatId,
            receiverSeatId = command.receiverSeatId,
            partner = command.partner,
            delta = command.delta
        )
    ).incrementVersion()
}

private fun GameSession.clearCommanderDamageRecentChange(
    command: GameCommand.ClearCommanderDamageRecentChange
): GameSession {
    requireSeat(command.dealerSeatId)
    requireSeat(command.receiverSeatId)
    return copy(
        commander = commander.clearRecentChange(
            dealerSeatId = command.dealerSeatId,
            receiverSeatId = command.receiverSeatId,
            partner = command.partner
        )
    ).incrementVersion()
}

private fun GameSession.toggleDayNight(): GameSession {
    return copy(
        dayNight = when (dayNight) {
            DayNight.NONE -> DayNight.DAY
            DayNight.DAY -> DayNight.NIGHT
            DayNight.NIGHT -> DayNight.DAY
        }
    ).incrementVersion()
}

private fun GameSession.resetGame(command: GameCommand.ResetGame): GameSession {
    val nextRules = command.startingLife?.let { startingLife ->
        rules.copy(startingLife = startingLife)
    } ?: rules
    return copy(
        rules = nextRules,
        seats = seats.map { seat ->
            GameSeat.new(
                id = seat.id,
                appearance = seat.appearance,
                startingLife = nextRules.startingLife
            )
        },
        commander = CommanderDamageMatrix(),
        commanderMode = null,
        tableCounters = emptyMap(),
        monarchSeatId = null,
        dayNight = DayNight.NONE
    ).incrementVersion()
}

private fun GameSession.incrementVersion(): GameSession {
    return copy(version = version + 1)
}
