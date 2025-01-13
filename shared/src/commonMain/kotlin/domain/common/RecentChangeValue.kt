package domain.common

import domain.game.AttachableManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class NumberWithRecentChange(
    val number: Int,
    val recentChange: Int
)

class RecentChangeValue(
    initialValue: NumberWithRecentChange = NumberWithRecentChange(0, 0),
    private val recentChangeDelay: Long = RECENT_CHANGE_DELAY,
    private val updateCallback: (NumberWithRecentChange) -> Unit,
): AttachableManager<CoroutineScope>() {
    companion object {
        const val RECENT_CHANGE_DELAY = 1500L
    }

    private val _value = MutableStateFlow(initialValue)
    val value: StateFlow<NumberWithRecentChange> = _value.asStateFlow()

    private var recentChangeJob: Job? = null

    override fun attach(source: CoroutineScope): RecentChangeValue {
        super.attach(source)
        updateRecentChange()
        return this
    }

    override fun detach() {
        super.detach()
        cancel()
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

    fun set(value: Int) {
        updateValue(NumberWithRecentChange(value, 0))
    }

    private fun updateRecentChange() {
        val scope = requireAttached()
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