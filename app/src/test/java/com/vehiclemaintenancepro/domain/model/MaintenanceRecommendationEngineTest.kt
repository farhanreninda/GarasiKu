package com.vehiclemaintenancepro.domain.model

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class MaintenanceRecommendationEngineTest {
    private val today = LocalDate.of(2026, 10, 4)
    private fun vehicle(model: String = "PCX 160 CBS", brand: String = "Honda") = Vehicle(
        1, VehicleType.Motorcycle, null, brand, model, 2023, "T 5774 JC", null, null, null,
        TransmissionType.Cvt, FuelType.Gasoline, 19_200, null, null, null, true,
    )
    private fun record(id: Long, date: String, km: Long?, vararg items: MaintenanceWorkItem) = ActivityLog(
        id, 1, "Perawatan", null, null, LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault()).toInstant(),
        ActivityCategory.Service, km, "Bengkel", items.toList(),
    )

    @Test fun `different components use different intervals and overdue dates stay overdue`() {
        val last = record(1, "2026-03-07", 18_333,
            MaintenanceWorkItem(MaintenanceComponent.EngineOil, MaintenanceAction.Replace),
            MaintenanceWorkItem(MaintenanceComponent.FinalDriveOil, MaintenanceAction.Replace),
            MaintenanceWorkItem(MaintenanceComponent.Cvt, MaintenanceAction.Service))
        val results = MaintenanceRecommendationEngine.recommendationsFor(vehicle(), emptyList(), listOf(last), today).associateBy { it.component }
        assertEquals(24_333L, results.getValue(MaintenanceComponent.EngineOil).dueOdometerKm)
        assertEquals(LocalDate.of(2026, 9, 7), results.getValue(MaintenanceComponent.EngineOil).dueDate)
        assertEquals(42_333L, results.getValue(MaintenanceComponent.FinalDriveOil).dueOdometerKm)
        assertEquals(LocalDate.of(2028, 3, 7), results.getValue(MaintenanceComponent.FinalDriveOil).dueDate)
        assertEquals(30_333L, results.getValue(MaintenanceComponent.Cvt).dueOdometerKm)
        assertEquals(18_000L, results.getValue(MaintenanceComponent.AirFilter).dueOdometerKm)
    }

    @Test fun `inspection does not reset replacement and older complete history is used`() {
        val replace = record(1, "2024-01-01", 8_000, MaintenanceWorkItem(MaintenanceComponent.SparkPlug, MaintenanceAction.Replace))
        val inspect = record(2, "2026-01-01", 18_000, MaintenanceWorkItem(MaintenanceComponent.SparkPlug, MaintenanceAction.Inspect))
        val unrelated = (3L..50L).map { record(it, "2026-02-01", null, MaintenanceWorkItem(MaintenanceComponent.Other, MaintenanceAction.Repair)) }
        val result = MaintenanceRecommendationEngine.recommendationsFor(vehicle(), emptyList(), unrelated + inspect + replace, today)
            .first { it.component == MaintenanceComponent.SparkPlug }
        assertEquals(20_000L, result.dueOdometerKm)
        assertEquals(1L, result.lastActivityId)
    }

    @Test fun `custom interval works for an unsupported model without assuming Honda rules`() {
        val item = MaintenanceWorkItem(MaintenanceComponent.EngineOil, MaintenanceAction.Replace, intervalKm = 2_000, intervalMonths = 3)
        val history = listOf(record(1, "2026-03-07", 18_333, item))
        val result = MaintenanceRecommendationEngine.recommendationsFor(vehicle("NMAX", "Yamaha"), emptyList(), history, today).single()
        assertEquals(20_333L, result.dueOdometerKm)
        assertEquals(LocalDate.of(2026, 6, 7), result.dueDate)
        assertTrue(MaintenanceRecommendationEngine.recommendationsFor(vehicle("NMAX", "Yamaha"), emptyList()).isEmpty())
    }

    @Test fun `missing mileage does not manufacture a kilometer baseline`() {
        val history = listOf(record(1, "2026-03-07", null, MaintenanceWorkItem(MaintenanceComponent.EngineOil, MaintenanceAction.Replace)))
        val result = MaintenanceRecommendationEngine.recommendationsFor(vehicle(), emptyList(), history, today).first { it.component == MaintenanceComponent.EngineOil }
        assertNull(result.dueOdometerKm)
        assertEquals(LocalDate.of(2026, 9, 7), result.dueDate)
    }

    @Test fun `other vehicles history cannot reset the selected vehicle schedule`() {
        val history = listOf(record(1, "2026-03-07", 18_333, MaintenanceWorkItem(MaintenanceComponent.EngineOil, MaintenanceAction.Replace)).copy(vehicleId = 2))
        val result = MaintenanceRecommendationEngine.recommendationsFor(vehicle(), emptyList(), history, today).first { it.component == MaintenanceComponent.EngineOil }
        assertNull(result.lastActivityId)
        assertTrue(result.description.contains("Belum ada catatan"))
    }

    @Test fun `new PCX starts with the first service milestone`() {
        val newVehicle = vehicle().copy(odometerKm = 100, purchaseDate = LocalDate.of(2026, 9, 10))
        val result = MaintenanceRecommendationEngine.recommendationsFor(newVehicle, emptyList(), today = today)
            .first { it.component == MaintenanceComponent.EngineOil }
        assertEquals(1_000L, result.dueOdometerKm)
        assertEquals(LocalDate.of(2026, 10, 10), result.dueDate)
    }
}
