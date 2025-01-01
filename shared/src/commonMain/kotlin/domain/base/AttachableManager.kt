package domain.base

import kotlinx.coroutines.flow.StateFlow
import ui.lifecounter.playerbutton.PlayerButtonViewModel

abstract class AttachableManager {
    protected var playerViewModelsFlow: StateFlow<List<PlayerButtonViewModel>>? = null

    fun attach(viewModelsFlow: StateFlow<List<PlayerButtonViewModel>>) {
        if (playerViewModelsFlow != null) {
            println("WARNING: ${this::class.simpleName} is already attached, detaching previous")
            detach()
        }
        playerViewModelsFlow = viewModelsFlow
    }

    fun detach() {
        playerViewModelsFlow = null
    }

    protected fun checkAttached() {
        if (playerViewModelsFlow == null) {
            throw IllegalStateException("${this::class.simpleName} must be attached before use")
        }
    }
} 