package domain.system

expect class NotificationManager {
    fun showNotification(message: String, duration: Long = 2000L)
}