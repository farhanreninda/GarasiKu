package com.vehiclemaintenancepro.domain.model

import java.time.LocalDate

object ReminderAlertPolicy {
    const val DAYS_BEFORE_DUE = 7L

    fun shouldAlert(
        reminder: MaintenanceReminder,
        vehicle: Vehicle?,
        today: LocalDate = LocalDate.now(),
    ): Boolean {
        val dateReached = reminder.dueDate?.let { !it.isAfter(today.plusDays(DAYS_BEFORE_DUE)) } == true
        val odometerReached = reminder.dueOdometerKm?.let { due -> vehicle != null && vehicle.odometerKm >= due } == true
        return !reminder.isCompleted && (dateReached || odometerReached)
    }
}
