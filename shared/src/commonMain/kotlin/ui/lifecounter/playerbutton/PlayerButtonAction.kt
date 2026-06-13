package ui.lifecounter.playerbutton

import domain.state.game.GameCommand
import domain.state.game.SeatId
import ui.lifecounter.GameSessionUiMapper

sealed interface PlayerButtonAction {
    data object IncrementLife : PlayerButtonAction
    data object DecrementLife : PlayerButtonAction
    data class IncrementCommanderDamage(val partner: Boolean) : PlayerButtonAction
    data class DecrementCommanderDamage(val partner: Boolean) : PlayerButtonAction
    data class SetMonarch(val monarch: Boolean) : PlayerButtonAction
    data class SetManualDeath(val dead: Boolean) : PlayerButtonAction
}

fun PlayerButtonAction.toGameCommand(
    seatId: SeatId,
    commanderState: CommanderState = CommanderState.Inactive
): GameCommand? {
    return when (this) {
        PlayerButtonAction.IncrementLife -> GameCommand.ChangeLife(seatId = seatId, delta = 1)
        PlayerButtonAction.DecrementLife -> GameCommand.ChangeLife(seatId = seatId, delta = -1)
        is PlayerButtonAction.IncrementCommanderDamage -> commanderDamageCommand(
            receiverSeatId = seatId,
            commanderState = commanderState,
            partner = partner,
            delta = 1
        )

        is PlayerButtonAction.DecrementCommanderDamage -> commanderDamageCommand(
            receiverSeatId = seatId,
            commanderState = commanderState,
            partner = partner,
            delta = -1
        )

        is PlayerButtonAction.SetMonarch -> GameCommand.SetMonarch(
            seatId = if (monarch) seatId else null
        )

        is PlayerButtonAction.SetManualDeath -> GameCommand.SetManualDeath(
            seatId = seatId,
            dead = dead
        )
    }
}

private fun commanderDamageCommand(
    receiverSeatId: SeatId,
    commanderState: CommanderState,
    partner: Boolean,
    delta: Int
): GameCommand? {
    val activeCommander = commanderState as? CommanderState.Active ?: return null
    return GameCommand.ChangeCommanderDamage(
        dealerSeatId = GameSessionUiMapper.seatIdForPlayerNumber(activeCommander.dealer.playerNum),
        receiverSeatId = receiverSeatId,
        partner = partner,
        delta = delta
    )
}
