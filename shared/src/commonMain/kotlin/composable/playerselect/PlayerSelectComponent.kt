package composable.playerselect

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update

/**
 * The component for the player select screen for state and navigation
 */
class PlayerSelectComponent (
    componentContext: ComponentContext,
    val goToLifeCounterScreen: () -> Unit,
    val setNumPlayers: (Int) -> Unit
): ComponentContext by componentContext {
    private val _state = MutableValue(State())
    val state: Value<State> = _state

    fun setHelperText(value: Boolean?) {
        _state.update { it.copy(showHelperText = value ?: !_state.value.showHelperText) }
    }

    data class State(val showHelperText: Boolean = true)
}