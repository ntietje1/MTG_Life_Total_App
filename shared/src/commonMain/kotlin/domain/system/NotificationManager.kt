package domain.system

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationManager {
    private var nextId = 0L
    private val _notification = MutableStateFlow<InAppNotification?>(null)
    val notification: StateFlow<InAppNotification?> = _notification.asStateFlow()

    fun showNotification(message: String, duration: Long = 2000L) {
        nextId += 1
        _notification.value = InAppNotification(
            id = nextId,
            message = message,
            durationMillis = duration
        )
    }

    fun dismiss(notification: InAppNotification) {
        if (_notification.value?.id == notification.id) {
            _notification.value = null
        }
    }
}

data class InAppNotification(
    val id: Long,
    val message: String,
    val durationMillis: Long
)
