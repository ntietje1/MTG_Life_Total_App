package domain.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NumberWithRecentChange(
    val number: Int,
    val recentChange: Int
)

class RecentChangeValue(
    initialValue: NumberWithRecentChange = NumberWithRecentChange(0, 0),
    private val recentChangeDelay: Long = RECENT_CHANGE_DELAY,
    private val updateCallback: (NumberWithRecentChange) -> Unit,
) {
    constructor(
        initialValue: Int = 0,
        recentChangeDelay: Long = RECENT_CHANGE_DELAY,
        updateCallback: (NumberWithRecentChange) -> Unit
    ) : this(NumberWithRecentChange(initialValue, 0), recentChangeDelay, updateCallback)

    companion object {
        const val RECENT_CHANGE_DELAY = 1500L
    }

    private val _value = MutableStateFlow(initialValue)
    val value: StateFlow<NumberWithRecentChange> = _value.asStateFlow()

    private lateinit var scope: CoroutineScope

    private var recentChangeJob: Job? = null

    fun attach(scope: CoroutineScope): RecentChangeValue {
        this.scope = scope
        updateRecentChange()
        return this
    }

    fun increment(change: Int) {
        updateValue(
            NumberWithRecentChange(
                _value.value.number + change,
                _value.value.recentChange + change
            )
        )
        updateRecentChange()
    }

    fun set(newValue: Int) {
        val change = newValue - _value.value.number
        updateValue(NumberWithRecentChange(newValue, change))
        updateRecentChange()
    }

    private fun updateRecentChange() {
        recentChangeJob?.cancel()
        recentChangeJob = scope.launch {
            delay(recentChangeDelay)
            updateValue(NumberWithRecentChange(_value.value.number, 0))
        }
    }

    fun cancel() {
        recentChangeJob?.cancel()
        recentChangeJob = null
    }

    private fun updateValue(newValue: NumberWithRecentChange) {
        _value.value = newValue
        updateCallback(_value.value)
    }
} 