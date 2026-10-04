package com.vehiclemaintenancepro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vehiclemaintenancepro.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs WHERE occurred_at_millis >= :startMillis AND occurred_at_millis < :endMillis AND cost_amount IS NOT NULL ORDER BY occurred_at_millis DESC")
    fun observeExpensesBetween(startMillis: Long, endMillis: Long): Flow<List<ActivityLogEntity>>

    @Query(
        """
        SELECT * FROM activity_logs
        WHERE title NOT LIKE 'Kendaraan %'
          AND (description IS NULL OR description NOT IN ('Pengingat dibuat.', 'Pengingat ditandai selesai.'))
        ORDER BY occurred_at_millis DESC
        LIMIT :limit
        """,
    )
    fun observeRecentLogs(limit: Int): Flow<List<ActivityLogEntity>>

    @Query(
        """
        SELECT * FROM activity_logs
        WHERE vehicle_id = :vehicleId
          AND title NOT LIKE 'Kendaraan %'
          AND (description IS NULL OR description NOT IN ('Pengingat dibuat.', 'Pengingat ditandai selesai.'))
        ORDER BY occurred_at_millis DESC
        LIMIT :limit
        """,
    )
    fun observeRecentLogsForVehicle(vehicleId: Long, limit: Int): Flow<List<ActivityLogEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(cost_amount), 0) FROM activity_logs
        WHERE cost_amount IS NOT NULL
          AND occurred_at_millis >= :startMillis
          AND occurred_at_millis < :endMillis
        """,
    )
    fun observeExpenseTotalBetween(startMillis: Long, endMillis: Long): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(cost_amount), 0) FROM activity_logs
        WHERE vehicle_id = :vehicleId
          AND cost_amount IS NOT NULL
          AND occurred_at_millis >= :startMillis
          AND occurred_at_millis < :endMillis
        """,
    )
    fun observeExpenseTotalBetweenForVehicle(vehicleId: Long, startMillis: Long, endMillis: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long
}
