package com.vehiclemaintenancepro.domain.model

import java.time.Instant

data class ActivityLog(
    val id: Long,
    val vehicleId: Long?,
    val title: String,
    val description: String?,
    val costAmount: Long?,
    val occurredAt: Instant,
    val category: ActivityCategory? = null,
    val odometerKm: Long? = null,
    val location: String? = null,
    val workItems: List<MaintenanceWorkItem> = emptyList(),
)
