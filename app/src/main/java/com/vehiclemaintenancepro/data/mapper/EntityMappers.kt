package com.vehiclemaintenancepro.data.mapper

import com.vehiclemaintenancepro.data.local.entity.ActivityLogEntity
import com.vehiclemaintenancepro.data.local.entity.MaintenanceReminderEntity
import com.vehiclemaintenancepro.data.local.entity.VehicleEntity
import com.vehiclemaintenancepro.domain.model.ActivityLog
import com.vehiclemaintenancepro.domain.model.ActivityLogCreateRequest
import com.vehiclemaintenancepro.domain.model.FuelType
import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.MaintenanceReminderCreateRequest
import com.vehiclemaintenancepro.domain.model.ReminderType
import com.vehiclemaintenancepro.domain.model.TransmissionType
import com.vehiclemaintenancepro.domain.model.Vehicle
import com.vehiclemaintenancepro.domain.model.VehicleCreateRequest
import com.vehiclemaintenancepro.domain.model.VehicleType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

fun VehicleEntity.toDomain(): Vehicle = Vehicle(
    id = id,
    vehicleType = vehicleType.toVehicleType(),
    photoUri = photoUri,
    brand = brand,
    model = model,
    year = year,
    licensePlate = licensePlate,
    color = color,
    engineNumber = engineNumber,
    frameNumber = frameNumber,
    transmissionType = transmissionType.toTransmissionType(),
    fuelType = fuelType.toFuelType(),
    odometerKm = odometerKm,
    purchasePrice = purchasePrice,
    purchaseDate = purchaseDateEpochDay?.let(LocalDate::ofEpochDay),
    note = note,
    isActive = isActive,
)

fun MaintenanceReminderEntity.toDomain(): MaintenanceReminder = MaintenanceReminder(
    id = id,
    vehicleId = vehicleId,
    type = type.toReminderType(),
    title = title,
    dueDate = dueDateEpochDay?.let(LocalDate::ofEpochDay),
    dueTime = dueTimeSecondOfDay?.toLong()?.let(LocalTime::ofSecondOfDay),
    dueOdometerKm = dueOdometerKm,
    isCompleted = isCompleted,
)

fun ActivityLogEntity.toDomain(): ActivityLog = ActivityLog(
    id = id,
    vehicleId = vehicleId,
    title = title,
    description = description,
    costAmount = costAmount,
    occurredAt = Instant.ofEpochMilli(occurredAtMillis),
    category = category?.let { name -> com.vehiclemaintenancepro.domain.model.ActivityCategory.entries.firstOrNull { it.name == name } },
    odometerKm = odometerKm,
    location = location,
    workItems = MaintenanceWorkItemJson.decode(workItemsJson),
)

fun VehicleCreateRequest.toEntity(
    isActive: Boolean,
    nowMillis: Long,
): VehicleEntity = VehicleEntity(
    vehicleType = vehicleType.name,
    photoUri = null,
    brand = brand.trim(),
    model = model.trim(),
    year = year,
    licensePlate = licensePlate.trim().uppercase(),
    color = color?.trim()?.takeIf { it.isNotBlank() },
    engineNumber = null,
    frameNumber = null,
    transmissionType = transmissionType.name,
    fuelType = fuelType.name,
    odometerKm = odometerKm,
    purchasePrice = null,
    purchaseDateEpochDay = null,
    note = note?.trim()?.takeIf { it.isNotBlank() },
    isActive = isActive,
    isArchived = false,
    createdAtMillis = nowMillis,
    updatedAtMillis = nowMillis,
)

fun MaintenanceReminderCreateRequest.toEntity(nowMillis: Long): MaintenanceReminderEntity = MaintenanceReminderEntity(
    vehicleId = vehicleId,
    type = type.name,
    title = title.trim(),
    dueDateEpochDay = dueDate?.toEpochDay(),
    dueTimeSecondOfDay = null,
    dueOdometerKm = dueOdometerKm,
    isCompleted = false,
    createdAtMillis = nowMillis,
    updatedAtMillis = nowMillis,
)

fun ActivityLogCreateRequest.toEntity(nowMillis: Long): ActivityLogEntity = ActivityLogEntity(
    vehicleId = vehicleId,
    title = title.trim(),
    description = description?.trim()?.takeIf { it.isNotBlank() },
    costAmount = costAmount,
    occurredAtMillis = occurredAt?.toEpochMilli() ?: nowMillis,
    category = category.name,
    odometerKm = odometerKm,
    location = location?.trim()?.takeIf { it.isNotBlank() },
    workItemsJson = MaintenanceWorkItemJson.encode(workItems),
)

private fun String.toTransmissionType(): TransmissionType = enumValueOrDefault(TransmissionType.Unknown)

private fun String.toFuelType(): FuelType = enumValueOrDefault(FuelType.Unknown)

private fun String.toReminderType(): ReminderType = enumValueOrDefault(ReminderType.Custom)

private fun String.toVehicleType(): VehicleType = enumValueOrDefault(VehicleType.Car)

private inline fun <reified T : Enum<T>> String.enumValueOrDefault(default: T): T =
    enumValues<T>().firstOrNull { it.name == this } ?: default
