package domain.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Backstack {
    private var operations = listOf<() -> Unit>()
    private val _isEmpty = MutableStateFlow(true)
    val isEmpty: StateFlow<Boolean> = _isEmpty.asStateFlow()

    fun push(operation: () -> Unit) {
        operations = operations + operation
        _isEmpty.value = false
    }

    fun pop(): () -> Unit {
        if (operations.isEmpty()) throw IllegalStateException("Cannot pop from empty backstack")
        val operation = operations.last()
        operations = operations.dropLast(1)
        _isEmpty.value = operations.isEmpty()
        return operation
    }

    fun clear() {
        operations = listOf()
        _isEmpty.value = true
    }
}
