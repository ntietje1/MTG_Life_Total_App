package composable.playerselect

import com.arkivanov.decompose.ComponentContext

/**
 * The component for the player select screen for state and navigation
 */
class PlayerSelectComponent (
    componentContext: ComponentContext,
    val goToLifeCounterScreen: () -> Unit,
    val setNumPlayers: (Int) -> Unit
): ComponentContext by componentContext {

}