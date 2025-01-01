package domain.base

import kotlinx.coroutines.flow.StateFlow
import ui.lifecounter.playerbutton.PlayerButtonViewModel

abstract class AttachableManager<T> {
    protected var attachedFlow: StateFlow<T>? = null

    open fun attach(flow: StateFlow<T>) {
        if (attachedFlow != null) {
            println("WARNING: ${this::class.simpleName} is already attached, detaching previous")
            detach()
        }
        attachedFlow = flow
    }

    open fun detach() {
        attachedFlow = null
    }

    protected open fun checkAttached() {
        if (attachedFlow == null) {
            throw IllegalStateException("${this::class.simpleName} must be attached before use")
        }
    }
} 