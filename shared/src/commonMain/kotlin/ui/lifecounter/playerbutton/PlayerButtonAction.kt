package ui.lifecounter.playerbutton

import domain.state.game.GameCommand
import domain.state.game.SeatId
import ui.lifecounter.CounterType
import ui.lifecounter.GameSessionUiMapper

sealed interface PlayerButtonAction {
    data object IncrementLife : PlayerButtonAction
    data object DecrementLife : PlayerButtonAction
    data class IncrementCommanderDamage(val partner: Boolean) : PlayerButtonAction
    data class DecrementCommanderDamage(val partner: Boolean) : PlayerButtonAction
    data class SetMonarch(val monarch: Boolean) : PlayerButtonAction
    data class SetManualDeath(val dead: Boolean) : PlayerButtonAction
    data class SetCommanderPartnerMode(val enabled: Boolean) : PlayerButtonAction
    data class ChangeCounter(val counter: CounterType, val delta: Int) : PlayerButtonAction
    data class SetCounterActive(val counter: CounterType, val active: Boolean) : PlayerButtonAction
    data object ToggleCommanderDealer : PlayerButtonAction
    data object ToggleSettings : PlayerButtonAction
    data object PopBackStack : PlayerButtonAction
    data object OpenCounters : PlayerButtonAction
    data object OpenCounterSelection : PlayerButtonAction
    data object OpenCustomization : PlayerButtonAction
    data object CloseCustomization : PlayerButtonAction
    data object SelectFirstPlayer : PlayerButtonAction
    data object MoveTimer : PlayerButtonAction
}

fun PlayerButtonAction.toGameCommands(
    seatId: SeatId,
    commanderState: CommanderState = CommanderState.Inactive
): List<GameCommand> {
    return when (this) {
        PlayerButtonAction.IncrementLife -> listOf(GameCommand.ChangeLife(seatId = seatId, delta = 1))
        PlayerButtonAction.DecrementLife -> listOf(GameCommand.ChangeLife(seatId = seatId, delta = -1))
        is PlayerButtonAction.IncrementCommanderDamage -> commanderDamageCommand(
            receiverSeatId = seatId,
            commanderState = commanderState,
            partner = partner,
            delta = 1
        )?.let { command -> listOf(command, GameCommand.ChangeLife(seatId = seatId, delta = -1)) } ?: emptyList()

        is PlayerButtonAction.DecrementCommanderDamage -> commanderDamageCommand(
            receiverSeatId = seatId,
            commanderState = commanderState,
            partner = partner,
            delta = -1
        )?.let { command -> listOf(command, GameCommand.ChangeLife(seatId = seatId, delta = 1)) } ?: emptyList()

        is PlayerButtonAction.SetMonarch -> listOf(
            GameCommand.SetMonarch(
                seatId = if (monarch) seatId else null
            )
        )

        is PlayerButtonAction.SetManualDeath -> listOf(
            GameCommand.SetManualDeath(
                seatId = seatId,
                dead = dead
            )
        )

        is PlayerButtonAction.SetCommanderPartnerMode -> listOf(
            GameCommand.SetCommanderPartnerMode(partnerMode = enabled)
        )

        is PlayerButtonAction.ChangeCounter -> listOf(
            GameCommand.ChangeSeatCounter(
                seatId = seatId,
                counter = GameSessionUiMapper.domainCounterFor(counter),
                delta = delta
            )
        )

        is PlayerButtonAction.SetCounterActive -> listOf(
            GameCommand.SetSeatCounterActive(
                seatId = seatId,
                counter = GameSessionUiMapper.domainCounterFor(counter),
                active = active
            )
        )

        PlayerButtonAction.ToggleCommanderDealer,
        PlayerButtonAction.ToggleSettings,
        PlayerButtonAction.PopBackStack,
        PlayerButtonAction.OpenCounters,
        PlayerButtonAction.OpenCounterSelection,
        PlayerButtonAction.OpenCustomization,
        PlayerButtonAction.CloseCustomization,
        PlayerButtonAction.SelectFirstPlayer,
        PlayerButtonAction.MoveTimer -> emptyList()
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
