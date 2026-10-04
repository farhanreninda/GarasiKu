package com.vehiclemaintenancepro.data.repository

import androidx.room.withTransaction
import com.vehiclemaintenancepro.data.local.dao.ActivityLogDao
import com.vehiclemaintenancepro.data.local.dao.MaintenanceReminderDao
import com.vehiclemaintenancepro.data.local.database.VehicleMaintenanceDatabase
import com.vehiclemaintenancepro.data.local.entity.ActivityLogEntity
import com.vehiclemaintenancepro.data.mapper.toDomain
import com.vehiclemaintenancepro.data.mapper.toEntity
import com.vehiclemaintenancepro.domain.model.ActivityLog
import com.vehiclemaintenancepro.domain.model.ActivityLogCreateRequest
import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.MaintenanceReminderCreateRequest
import com.vehiclemaintenancepro.domain.repository.MaintenanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MaintenanceRepositoryImpl @Inject constructor(
    private val database: VehicleMaintenanceDatabase,
    private val reminderDao: MaintenanceReminderDao,
    private val activityLogDao: ActivityLogDao,
) : MaintenanceRepository {
    override fun observePendingRemindersForVehicle(vehicleId: Long): Flow<List<MaintenanceReminder>> = reminderDao
        .observePendingRemindersForVehicle(vehicleId)
        .map { reminders -> reminders.map { it.toDomain() } }

    override fun observeRecentActivityForVehicle(vehicleId: Long, limit: Int): Flow<List<ActivityLog>> = activityLogDao
        .observeRecentLogsForVehicle(vehicleId, limit)
        .map { logs -> logs.map { it.toDomain() } }

    override fun observeExpensesBetween(startMillis: Long, endMillis: Long): Flow<List<ActivityLog>> = activityLogDao
        .observeExpensesBetween(startMillis, endMillis)
        .map { logs -> logs.map { it.toDomain() } }
    override fun observePendingReminders(): Flow<List<MaintenanceReminder>> = reminderDao
        .observePendingReminders()
        .map { reminders -> reminders.map { it.toDomain() } }

    override fun observeRecentActivity(limit: Int): Flow<List<ActivityLog>> = activityLogDao
        .observeRecentLogs(limit = limit)
        .map { logs -> logs.map { it.toDomain() } }

    override suspend fun addReminder(request: MaintenanceReminderCreateRequest): Long = database.withTransaction {
        val nowMillis = System.currentTimeMillis()
        val reminderId = reminderDao.insertReminder(request.toEntity(nowMillis = nowMillis))
        activityLogDao.insertLog(
            ActivityLogEntity(
                vehicleId = request.vehicleId,
                title = request.title.trim(),
                description = "Pengingat dibuat.",
                costAmount = null,
                occurredAtMillis = nowMillis,
            ),
        )
        reminderId
    }

    override suspend fun completeReminder(reminder: MaintenanceReminder) {
        database.withTransaction {
            val nowMillis = System.currentTimeMillis()
            reminderDao.completeReminder(
                reminderId = reminder.id,
                updatedAtMillis = nowMillis,
            )
            activityLogDao.insertLog(
                ActivityLogEntity(
                    vehicleId = reminder.vehicleId,
                    title = "${reminder.title} selesai",
                    description = "Pengingat ditandai selesai.",
                    costAmount = null,
                    occurredAtMillis = nowMillis,
                ),
            )
        }
    }

    override suspend fun addActivity(request: ActivityLogCreateRequest): Long {
        require(request.title.isNotBlank())
        require(request.odometerKm == null || request.odometerKm >= 0)
        require(request.costAmount == null || request.costAmount >= 0)
        require(request.workItems.map { it.component }.distinct().size == request.workItems.size)
        require(request.workItems.all { (it.intervalKm == null || it.intervalKm > 0) && (it.intervalMonths == null || it.intervalMonths in 1..1200) })
        val nowMillis = System.currentTimeMillis()
        require(request.occurredAt == null || request.occurredAt.toEpochMilli() <= nowMillis)
        return activityLogDao.insertLog(request.toEntity(nowMillis = nowMillis))
    }
}
