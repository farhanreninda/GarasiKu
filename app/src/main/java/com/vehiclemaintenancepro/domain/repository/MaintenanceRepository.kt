package com.vehiclemaintenancepro.domain.repository

import com.vehiclemaintenancepro.domain.model.ActivityLog
import com.vehiclemaintenancepro.domain.model.ActivityLogCreateRequest
import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.MaintenanceReminderCreateRequest
import kotlinx.coroutines.flow.Flow

interface MaintenanceRepository {
    fun observePendingReminders(): Flow<List<MaintenanceReminder>>
    fun observePendingRemindersForVehicle(vehicleId: Long): Flow<List<MaintenanceReminder>>
    fun observeRecentActivity(limit: Int): Flow<List<ActivityLog>>
    fun observeRecentActivityForVehicle(vehicleId: Long, limit: Int): Flow<List<ActivityLog>>
    fun observeExpensesBetween(startMillis: Long, endMillis: Long): Flow<List<ActivityLog>>
    suspend fun addReminder(request: MaintenanceReminderCreateRequest): Long
    suspend fun completeReminder(reminder: MaintenanceReminder)
    suspend fun addActivity(request: ActivityLogCreateRequest): Long
}
