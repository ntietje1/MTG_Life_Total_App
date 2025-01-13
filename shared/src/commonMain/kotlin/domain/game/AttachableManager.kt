package domain.game

import kotlinx.coroutines.flow.StateFlow

interface Attachable<T> {
    fun attach(source: T): Attachable<T>
    fun detach()
}

abstract class AttachableManager<T> : Attachable<T> {
    private var attached: T? = null

    override fun attach(source: T): AttachableManager<T> {
        if (attached != null) {
            println("WARNING: ${this::class.simpleName} is already attached, detaching previous")
            detach()
        }
        attached = source
        return this
    }

    override fun detach() {
        attached = null
    }

    protected open fun requireAttached(): T {
        return attached ?: throw IllegalStateException("${this::class.simpleName} must be attached before use")
    }
}

abstract class AttachableFlowManager<T> : AttachableManager<StateFlow<T>>()