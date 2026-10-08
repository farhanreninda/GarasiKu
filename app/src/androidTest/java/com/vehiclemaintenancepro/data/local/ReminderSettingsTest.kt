package com.vehiclemaintenancepro.data.local

import androidx.test.platform.app.InstrumentationRegistry
import com.vehiclemaintenancepro.data.repository.SettingsRepositoryImpl
import com.vehiclemaintenancepro.domain.model.AppSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ReminderSettingsTest {
    @Test fun notificationPreferencesPersistAndRejectInvalidInput() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = SettingsRepositoryImpl(context)
        val original = repository.observeSettings().first()
        try {
            repository.updateNotificationSettings(enabled = false, taxDays = 30, serviceDays = 3)
            val restored = SettingsRepositoryImpl(context).observeSettings().first()
            assertEquals(original.copy(notificationsEnabled = false, taxReminderDays = 30, serviceReminderDays = 3), restored)
            repository.updateNotificationSettings(taxDays = 0)
            assertEquals(0, repository.observeSettings().first().taxReminderDays)
            assertEquals(3, repository.observeSettings().first().serviceReminderDays)
            try { repository.updateNotificationSettings(serviceDays = 366); fail("Invalid lead accepted") } catch (_: IllegalArgumentException) { }
            assertEquals(3, repository.observeSettings().first().serviceReminderDays)
        } finally { repository.updateSettings(original) }
    }
}
