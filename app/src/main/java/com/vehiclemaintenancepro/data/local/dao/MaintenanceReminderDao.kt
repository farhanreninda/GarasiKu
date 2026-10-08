package com.vehiclemaintenancepro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vehiclemaintenancepro.data.local.entity.MaintenanceReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceReminderDao {
    @Query("SELECT * FROM maintenance_reminders ORDER BY id")
    suspend fun getAllForBackup(): List<MaintenanceReminderEntity>

    @Query("DELETE FROM maintenance_reminders")
    suspend fun clearForRestore()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBackup(rows: List<MaintenanceReminderEntity>)

    @Query(
        """
        SELECT * FROM maintenance_reminders
        WHERE is_completed = 0
        ORDER BY COALESCE(due_date_epoch_day, 9223372036854775807),
                 COALESCE(due_odometer_km, 9223372036854775807)
        """,
    )
    fun observePendingReminders(): Flow<List<MaintenanceReminderEntity>>

    @Query(
        """
        SELECT * FROM maintenance_reminders
        WHERE is_completed = 0
          AND vehicle_id = :vehicleId
        ORDER BY COALESCE(due_date_epoch_day, 9223372036854775807),
                 COALESCE(due_odometer_km, 9223372036854775807)
        """,
    )
    fun observePendingRemindersForVehicle(vehicleId: Long): Flow<List<MaintenanceReminderEntity>>

    @Query(
        """
        SELECT * FROM maintenance_reminders
        WHERE is_completed = 0
        ORDER BY COALESCE(due_date_epoch_day, 9223372036854775807),
                 COALESCE(due_odometer_km, 9223372036854775807)
        """,
    )
    suspend fun getPendingReminders(): List<MaintenanceReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: MaintenanceReminderEntity): Long

    @Query("UPDATE maintenance_reminders SET is_completed = 1, updated_at_millis = :updatedAtMillis WHERE id = :reminderId")
    suspend fun completeReminder(reminderId: Long, updatedAtMillis: Long)
}
