package domain.state.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ui.lifecounter.playerbutton.PlayerButtonViewModel

class MonarchyState(
    private val scope: CoroutineScope
) {
    fun observeMonarchChanges(
        players: StateFlow<List<PlayerButtonViewModel>>
    ) {
        scope.launch {
            players.value.forEach { viewModel ->
                viewModel.state.collect { state ->
                    if (state.player.monarch) {
                        players.value
                            .filter { it != viewModel }
                            .forEach { it.setMonarchy(false) } //TODO: this currently relies on setmonarchy being exposed from the viewmodel, should change this
                    }
                }
            }
        }
    }
}