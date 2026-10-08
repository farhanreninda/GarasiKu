package com.vehiclemaintenancepro.data.local

import android.app.Notification
import android.app.NotificationManager
import androidx.test.platform.app.InstrumentationRegistry
import com.vehiclemaintenancepro.core.notification.ReminderNotificationScheduler
import org.junit.Assert.*
import org.junit.Test

class NotificationTestFeatureTest {
    @Test fun testNotificationChecksAppToggleAndPostsWithoutReplacingReminders() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val manager = context.getSystemService(NotificationManager::class.java)
        val reminderBefore = manager.activeNotifications.firstOrNull { it.id == 20260810 }?.postTime
        try {
            assertEquals("Pengingat dimatikan di Profil.", ReminderNotificationScheduler.sendTest(context, false))
            assertFalse(manager.activeNotifications.any { it.id == ReminderNotificationScheduler.TEST_NOTIFICATION_ID })
            assertNull(ReminderNotificationScheduler.blockedReason(context, true))
            assertTrue(ReminderNotificationScheduler.sendTest(context, true).startsWith("Tes dikirim"))
            val notification = manager.activeNotifications.single { it.id == ReminderNotificationScheduler.TEST_NOTIFICATION_ID }.notification
            assertEquals(ReminderNotificationScheduler.CHANNEL_ID, notification.channelId)
            assertEquals("Tes notifikasi GarasiKu", notification.extras.getCharSequence(Notification.EXTRA_TITLE).toString())
            assertEquals(reminderBefore, manager.activeNotifications.firstOrNull { it.id == 20260810 }?.postTime)
        } finally {
            manager.cancel(ReminderNotificationScheduler.TEST_NOTIFICATION_ID)
        }
    }
}
