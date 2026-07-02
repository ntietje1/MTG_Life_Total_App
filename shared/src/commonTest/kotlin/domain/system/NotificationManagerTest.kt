package domain.system

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotificationManagerTest {
    @Test
    fun showNotificationPublishesCurrentInAppNotification() {
        val manager = NotificationManager()

        manager.showNotification("Image successfully uploaded", duration = 3000)

        val notification = manager.notification.value
        assertEquals("Image successfully uploaded", notification?.message)
        assertEquals(3000, notification?.durationMillis)
    }

    @Test
    fun dismissOnlyClearsTheCurrentNotification() {
        val manager = NotificationManager()
        manager.showNotification("First")
        val first = manager.notification.value ?: error("Missing first notification")
        manager.showNotification("Second")

        manager.dismiss(first)

        assertEquals("Second", manager.notification.value?.message)

        manager.notification.value?.let(manager::dismiss)

        assertNull(manager.notification.value)
    }
}
