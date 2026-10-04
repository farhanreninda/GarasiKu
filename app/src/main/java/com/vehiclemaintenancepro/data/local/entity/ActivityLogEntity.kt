package com.vehiclemaintenancepro.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_logs",
    indices = [Index(value = ["vehicle_id", "occurred_at_millis"])],
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Long?,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "cost_amount")
    val costAmount: Long?,
    @ColumnInfo(name = "occurred_at_millis")
    val occurredAtMillis: Long,
    @ColumnInfo(name = "category")
    val category: String? = null,
    @ColumnInfo(name = "odometer_km")
    val odometerKm: Long? = null,
    @ColumnInfo(name = "location")
    val location: String? = null,
    @ColumnInfo(name = "work_items_json", defaultValue = "'[]'")
    val workItemsJson: String = "[]",
)
