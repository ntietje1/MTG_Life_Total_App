package domain.system

import android.content.Context
import android.widget.Toast

actual class NotificationManager(private val context: Context) {
    private var currentToast: Toast? = null

    actual fun showNotification(message: String, duration: Long) {
        cancelCurrentNotification()
        currentToast = Toast.makeText(context, message, duration.toInt())
        currentToast?.show()
    }

    private fun cancelCurrentNotification() {
        currentToast?.cancel()
        currentToast = null
    }
}