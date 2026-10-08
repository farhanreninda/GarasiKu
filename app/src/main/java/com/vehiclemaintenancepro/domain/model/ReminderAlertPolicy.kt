package com.vehiclemaintenancepro.domain.model

import java.time.LocalDate

object ReminderAlertPolicy {
    const val DAYS_BEFORE_DUE = 7L

    fun shouldAlert(
        reminder: MaintenanceReminder,
        vehicle: Vehicle?,
        today: LocalDate = LocalDate.now(),
        settings: AppSettings = AppSettings(),
    ): Boolean {
        val dateReached = reminder.dueDate?.let { !it.isAfter(today.plusDays(settings.reminderDays(reminder.type))) } == true
        val odometerReached = reminder.dueOdometerKm?.let { due -> vehicle != null && vehicle.odometerKm >= due } == true
        return vehicle != null && vehicle.id == reminder.vehicleId && !reminder.isCompleted && (dateReached || odometerReached)
    }
}
