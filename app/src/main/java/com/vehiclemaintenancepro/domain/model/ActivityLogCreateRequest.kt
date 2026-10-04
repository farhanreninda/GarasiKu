package com.vehiclemaintenancepro.domain.model

data class ActivityLogCreateRequest(
    val vehicleId: Long?,
    val title: String,
    val description: String?,
    val costAmount: Long?,
    val category: ActivityCategory = ActivityCategory.Service,
    val odometerKm: Long? = null,
    val location: String? = null,
    val occurredAt: java.time.Instant? = null,
    val workItems: List<MaintenanceWorkItem> = emptyList(),
)
