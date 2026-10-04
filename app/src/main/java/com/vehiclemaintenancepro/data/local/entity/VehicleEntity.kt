package com.vehiclemaintenancepro.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicles",
    indices = [Index(value = ["license_plate"], unique = true)],
)
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "vehicle_type")
    val vehicleType: String,
    @ColumnInfo(name = "photo_uri")
    val photoUri: String?,
    @ColumnInfo(name = "brand")
    val brand: String,
    @ColumnInfo(name = "model")
    val model: String,
    @ColumnInfo(name = "year")
    val year: Int?,
    @ColumnInfo(name = "license_plate")
    val licensePlate: String,
    @ColumnInfo(name = "color")
    val color: String?,
    @ColumnInfo(name = "engine_number")
    val engineNumber: String?,
    @ColumnInfo(name = "frame_number")
    val frameNumber: String?,
    @ColumnInfo(name = "transmission_type")
    val transmissionType: String,
    @ColumnInfo(name = "fuel_type")
    val fuelType: String,
    @ColumnInfo(name = "odometer_km")
    val odometerKm: Long,
    @ColumnInfo(name = "purchase_price")
    val purchasePrice: Long?,
    @ColumnInfo(name = "purchase_date_epoch_day")
    val purchaseDateEpochDay: Long?,
    @ColumnInfo(name = "note")
    val note: String?,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean,
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long,
)
