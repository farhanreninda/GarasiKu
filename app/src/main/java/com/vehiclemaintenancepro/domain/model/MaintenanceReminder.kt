package com.vehiclemaintenancepro.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class MaintenanceReminder(
    val id: Long,
    val vehicleId: Long,
    val type: ReminderType,
    val title: String,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val dueOdometerKm: Long?,
    val isCompleted: Boolean,
)
