package com.vehiclemaintenancepro.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_reminders",
    indices = [Index(value = ["vehicle_id", "type", "is_completed"])],
)
data class MaintenanceReminderEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "due_date_epoch_day")
    val dueDateEpochDay: Long?,
    @ColumnInfo(name = "due_time_second_of_day")
    val dueTimeSecondOfDay: Int?,
    @ColumnInfo(name = "due_odometer_km")
    val dueOdometerKm: Long?,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long,
)
