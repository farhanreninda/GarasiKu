package com.vehiclemaintenancepro.domain.model

import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class ReminderAlertPolicyTest {
    @Test fun configuredLeadDaysApplyIndependentlyAndOdometerStillTriggers() {
        val today = LocalDate.of(2026, 10, 4)
        val vehicle = mockk<Vehicle> { every { id } returns 1L; every { odometerKm } returns 10000L }
        val settings = AppSettings(taxReminderDays = 30, serviceReminderDays = 3)
        val reminder = MaintenanceReminder(1, 1, ReminderType.Tax, "Pajak", today.plusDays(30), null, null, false)
        assertTrue(ReminderAlertPolicy.shouldAlert(reminder, vehicle, today, settings))
        assertFalse(ReminderAlertPolicy.shouldAlert(reminder.copy(dueDate = today.plusDays(31)), vehicle, today, settings))
        assertTrue(ReminderAlertPolicy.shouldAlert(reminder.copy(type = ReminderType.Registration), vehicle, today, settings))
        assertFalse(ReminderAlertPolicy.shouldAlert(reminder.copy(type = ReminderType.Service), vehicle, today, settings))
        for (type in listOf(ReminderType.Service, ReminderType.OilChange)) {
            assertTrue(ReminderAlertPolicy.shouldAlert(reminder.copy(type = type, dueDate = today.plusDays(3)), vehicle, today, settings))
            assertFalse(ReminderAlertPolicy.shouldAlert(reminder.copy(type = type, dueDate = today.plusDays(4)), vehicle, today, settings))
        }
        val sameDay = settings.copy(taxReminderDays = 0)
        assertFalse(ReminderAlertPolicy.shouldAlert(reminder.copy(dueDate = today.plusDays(1)), vehicle, today, sameDay))
        assertTrue(ReminderAlertPolicy.shouldAlert(reminder.copy(dueDate = today), vehicle, today, sameDay))
        assertTrue(ReminderAlertPolicy.shouldAlert(reminder.copy(dueDate = today.minusDays(1)), vehicle, today, sameDay))
        assertTrue(ReminderAlertPolicy.shouldAlert(reminder.copy(type = ReminderType.Service, dueOdometerKm = 10000), vehicle, today, settings))
        assertFalse(ReminderAlertPolicy.shouldAlert(reminder.copy(isCompleted = true), vehicle, today, settings))
        assertFalse(ReminderAlertPolicy.shouldAlert(reminder, null, today, settings))
        assertFalse(ReminderAlertPolicy.shouldAlert(reminder.copy(vehicleId = 2), vehicle, today, settings))
    }
}
