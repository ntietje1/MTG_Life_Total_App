package ui.lifecounter

import domain.game.timer.TurnTimer
import domain.state.game.SeatId
import model.Player
import ui.dialog.COUNTER_DIALOG_ENTRIES
import ui.lifecounter.playerbutton.PBState
import ui.lifecounter.playerbutton.showsBackButton

data class LifeCounterState(
    val showButtons: Boolean = false,
    val showLoadingScreen: Boolean = true,
    val blurBackground: Boolean = false,
    val dayNight: DayNightState = DayNightState.NONE,
    val coinFlipHistory: List<String> = emptyList(),
    val counters: List<Int> = List(COUNTER_DIALOG_ENTRIES) { 0 },
    val players: List<PlayerSeatUiState> = emptyList(),
    val modalStack: LifeCounterModalStack = LifeCounterModalStack.Empty,
    val middleButtonState: MiddleButtonState = MiddleButtonState.DEFAULT,
) {
    val currentModal: LifeCounterModal? get() = modalStack.current
    val isModalOpen: Boolean get() = currentModal != null
}

enum class MiddleButtonState {
    DEFAULT, COMMANDER_EXIT
}

enum class DayNightState {
    NONE, DAY, NIGHT
}

fun LifeCounterState.showTimer(activePlayerIndex: Int?, timer: TurnTimer?): LifeCounterState {
    return copy(
        players = players.mapIndexed { index, player ->
            player.copy(timer = if (index == activePlayerIndex) timer else null)
        }
    )
}

fun LifeCounterState.setPlayerButtonState(seatId: SeatId, buttonState: PBState): LifeCounterState {
    return updatePlayer(seatId) { player ->
        player.withButtonState(
            buttonState = buttonState,
            buttonBackStack = if (buttonState == PBState.COMMANDER_RECEIVER) emptyList() else player.buttonBackStack
        )
    }
}

fun LifeCounterState.openPlayerSettings(seatId: SeatId): LifeCounterState {
    val player = players.firstOrNull { it.seatId == seatId } ?: return this
    return if (player.buttonState == PBState.NORMAL) {
        pushPlayerButtonState(seatId, PBState.SETTINGS)
    } else {
        closePlayerMenu(seatId)
    }
}

fun LifeCounterState.openPlayerCounters(seatId: SeatId): LifeCounterState {
    return pushPlayerButtonState(seatId, PBState.COUNTERS_VIEW)
}

fun LifeCounterState.openPlayerCounterSelection(seatId: SeatId): LifeCounterState {
    return pushPlayerButtonState(seatId, PBState.COUNTERS_SELECT)
}

fun LifeCounterState.popPlayerButtonBackStack(seatId: SeatId): LifeCounterState {
    return updatePlayer(seatId) { player ->
        val previousState = player.buttonBackStack.lastOrNull() ?: return@updatePlayer player
        player.withButtonState(
            buttonState = previousState,
            buttonBackStack = player.buttonBackStack.dropLast(1)
        )
    }
}

fun LifeCounterState.closePlayerMenu(seatId: SeatId): LifeCounterState {
    return updatePlayer(seatId) { player ->
        player.withButtonState(buttonState = PBState.NORMAL, buttonBackStack = emptyList())
            .copy(showCustomizeMenu = false)
    }
}

fun LifeCounterState.promptForFirstPlayer(): LifeCounterState {
    return copy(
        players = players.map { player ->
            player.withButtonState(
                buttonState = PBState.SELECT_FIRST_PLAYER,
                buttonBackStack = player.buttonBackStack + player.buttonState
            )
        }
    )
}

fun LifeCounterState.clearFirstPlayerPrompt(): LifeCounterState {
    return players
        .filter { player -> player.buttonState == PBState.SELECT_FIRST_PLAYER }
        .fold(this) { current, player -> current.popPlayerButtonBackStack(player.seatId) }
}

fun LifeCounterState.setAllPlayerButtonStates(buttonState: PBState): LifeCounterState {
    return copy(
        players = players.map { player ->
            player.withButtonState(buttonState = buttonState, buttonBackStack = emptyList())
                .copy(showCustomizeMenu = false)
        }
    )
}

fun LifeCounterState.openPlayerCustomization(seatId: SeatId): LifeCounterState {
    return updatePlayer(seatId) { player -> player.copy(showCustomizeMenu = true) }
}

fun LifeCounterState.closePlayerCustomization(seatId: SeatId): LifeCounterState {
    return closePlayerMenu(seatId)
}

private fun LifeCounterState.pushPlayerButtonState(seatId: SeatId, buttonState: PBState): LifeCounterState {
    return updatePlayer(seatId) { player ->
        player.withButtonState(
            buttonState = buttonState,
            buttonBackStack = player.buttonBackStack + player.buttonState
        )
    }
}

private fun LifeCounterState.updatePlayer(
    seatId: SeatId,
    update: (PlayerSeatUiState) -> PlayerSeatUiState
): LifeCounterState {
    return copy(players = players.map { player -> if (player.seatId == seatId) update(player) else player })
}

private fun PlayerSeatUiState.withButtonState(
    buttonState: PBState,
    buttonBackStack: List<PBState>
): PlayerSeatUiState {
    return copy(
        buttonState = buttonState,
        buttonBackStack = buttonBackStack,
        backButtonVisible = buttonState.showsBackButton(backStackIsEmpty = buttonBackStack.isEmpty())
    )
}
