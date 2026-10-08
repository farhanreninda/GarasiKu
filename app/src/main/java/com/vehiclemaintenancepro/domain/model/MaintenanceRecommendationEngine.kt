package com.vehiclemaintenancepro.domain.model

import java.time.LocalDate
import java.time.ZoneId

object MaintenanceRecommendationEngine {
    private data class Interval(val km: Long?, val months: Int?, val action: MaintenanceAction)

    // Honda PCX K1Z, Indonesian owner's manual pp. 80–81. Inspection does not reset replacement history.
    private val pcxIntervals = linkedMapOf(
        MaintenanceComponent.RoutineService to Interval(6_000, 6, MaintenanceAction.Service),
        MaintenanceComponent.EngineOil to Interval(6_000, 6, MaintenanceAction.Replace),
        MaintenanceComponent.FinalDriveOil to Interval(24_000, 24, MaintenanceAction.Replace),
        MaintenanceComponent.Cvt to Interval(12_000, 12, MaintenanceAction.Inspect),
        MaintenanceComponent.DriveBelt to Interval(24_000, 24, MaintenanceAction.Replace),
        MaintenanceComponent.SparkPlug to Interval(12_000, 12, MaintenanceAction.Replace),
        MaintenanceComponent.AirFilter to Interval(18_000, 18, MaintenanceAction.Replace),
        MaintenanceComponent.Coolant to Interval(36_000, 36, MaintenanceAction.Replace),
        MaintenanceComponent.BrakeFluid to Interval(24_000, 24, MaintenanceAction.Replace),
    )

    fun isPcx160(vehicle: Vehicle): Boolean = vehicle.vehicleType == VehicleType.Motorcycle &&
        vehicle.brand.equals("Honda", ignoreCase = true) && vehicle.model.contains("PCX", ignoreCase = true) &&
        vehicle.model.contains("160", ignoreCase = true) && vehicle.year in 2021..2024

    fun recommendationsFor(
        vehicle: Vehicle,
        activeReminders: List<MaintenanceReminder>,
        history: List<ActivityLog> = emptyList(),
        today: LocalDate = LocalDate.now(),
    ): List<MaintenanceRecommendation> {
        val records = history.filter { it.vehicleId == vehicle.id }.sortedByDescending { it.occurredAt }
        val intervals = if (isPcx160(vehicle)) pcxIntervals.toMutableMap() else linkedMapOf()
        records.asReversed().forEach { activity ->
            activity.workItems.forEach { item ->
                if (item.intervalKm != null || item.intervalMonths != null) {
                    intervals[item.component] = Interval(item.intervalKm, item.intervalMonths, item.action)
                }
            }
        }
        return intervals.map { (component, interval) ->
            val last = records.firstOrNull { activity ->
                activity.workItems.any { item -> item.component == component &&
                    (item.action == interval.action ||
                        (interval.action == MaintenanceAction.Inspect && item.action in listOf(MaintenanceAction.Service, MaintenanceAction.Replace))) }
            }
            val dueKm = interval.km?.let { km ->
                last?.odometerKm?.plus(km) ?: if (last == null) {
                    // Unrecorded work remains due; advancing the odometer must not hide a missed replacement.
                    if (component in listOf(MaintenanceComponent.RoutineService, MaintenanceComponent.EngineOil) && vehicle.odometerKm < 1_000) 1_000
                    else if (vehicle.odometerKm < km) km else (vehicle.odometerKm / km) * km
                } else null
            }
            val dueDate = interval.months?.let { months ->
                val firstService = last == null && vehicle.odometerKm < 1_000 &&
                    component in listOf(MaintenanceComponent.RoutineService, MaintenanceComponent.EngineOil)
                (last?.occurredAt?.atZone(ZoneId.systemDefault())?.toLocalDate() ?: vehicle.purchaseDate)
                    ?.plusMonths(if (firstService) 1 else months.toLong())
            }
            val custom = records.firstOrNull { log -> log.workItems.any { it.component == component &&
                (it.intervalKm != null || it.intervalMonths != null) } } != null
            MaintenanceRecommendation(
                title = if (component == MaintenanceComponent.RoutineService) "Servis berkala" else if (component == MaintenanceComponent.Cvt && interval.action == MaintenanceAction.Inspect) "Periksa CVT / drive belt" else "${interval.action.label} ${component.label}",
                description = buildString {
                    append(if (custom) "Interval pilihan pengguna: " else "Acuan Honda PCX: ")
                    append(listOfNotNull(interval.km?.let { "$it km" }, interval.months?.let { "$it bulan" }).joinToString(" / "))
                    append(". ")
                    if (last == null) append("Belum ada catatan pekerjaan ini; periksa riwayat dan kondisi.")
                    else append("Dihitung dari catatan terakhir. Berlaku saat batas KM atau waktu tercapai lebih dulu.")
                    if (last != null && (dueDate?.let { !it.isAfter(today) } == true || dueKm?.let { vehicle.odometerKm >= it } == true)) append(" Sudah jatuh tempo.")
                },
                type = if (component in listOf(MaintenanceComponent.EngineOil, MaintenanceComponent.FinalDriveOil)) ReminderType.OilChange else ReminderType.Service,
                dueOdometerKm = dueKm,
                dueDate = dueDate,
                component = component,
                lastActivityId = last?.id,
            )
        }.filterNot { recommendation ->
            activeReminders.any { reminder -> reminder.title.equals(recommendation.title, ignoreCase = true) }
        }
    }
}
