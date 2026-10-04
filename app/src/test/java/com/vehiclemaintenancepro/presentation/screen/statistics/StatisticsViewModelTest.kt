package com.vehiclemaintenancepro.presentation.screen.statistics

import app.cash.turbine.test
import com.vehiclemaintenancepro.MainDispatcherRule
import com.vehiclemaintenancepro.domain.model.*
import com.vehiclemaintenancepro.domain.repository.MaintenanceRepository
import com.vehiclemaintenancepro.domain.repository.VehicleRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class StatisticsViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    @Test
    fun `statistics includes more than 200 expenses and filters the selected vehicle`() = runTest {
        val first = mockk<Vehicle> { every { id } returns 1L }
        val second = mockk<Vehicle> { every { id } returns 2L }
        val vehicles = mockk<VehicleRepository>()
        val maintenance = mockk<MaintenanceRepository>()
        val expenses = (1L..250L).map { id ->
            mockk<ActivityLog> {
                every { vehicleId } returns if (id <= 220L) 1L else 2L
                every { costAmount } returns 100L
                every { occurredAt } returns Instant.now()
            }
        }
        every { vehicles.observeVehicles() } returns flowOf(listOf(first, second))
        every { maintenance.observePendingReminders() } returns flowOf(emptyList())
        every { maintenance.observeExpensesBetween(any(), any()) } returns flowOf(expenses)
        val model = StatisticsViewModel(vehicles, maintenance)
        model.uiState.test {
            assertTrue(awaitItem().isLoading)
            val all = awaitItem()
            assertEquals(250, all.activities.size)
            assertEquals(25_000L, all.activities.sumOf { it.costAmount ?: 0L })
            model.selectVehicle(1L)
            val selected = awaitItem()
            assertEquals(220, selected.activities.size)
            assertEquals(22_000L, selected.activities.sumOf { it.costAmount ?: 0L })
            verify(exactly = 0) { maintenance.observeRecentActivity(any()) }
        }
    }

    @Test
    fun `kilometer reminder is due even when its date is in the future`() {
        val today = LocalDate.of(2026, 10, 4)
        val vehicle = mockk<Vehicle> {
            every { id } returns 1L
            every { odometerKm } returns 10_000L
        }
        val reminder = MaintenanceReminder(1L, 1L, ReminderType.Service, "Servis", today.plusDays(30), null, 10_000L, false)
        assertTrue(reminder.isDue(listOf(vehicle), today))
        assertTrue(ReminderAlertPolicy.shouldAlert(reminder, vehicle, today))
        assertFalse(ReminderAlertPolicy.shouldAlert(reminder.copy(isCompleted = true), vehicle, today))
        assertFalse(reminder.copy(dueOdometerKm = 15_000L).isDue(listOf(vehicle), today))
        assertTrue(reminder.copy(dueDate = today, dueOdometerKm = null).isDue(listOf(vehicle), today))
    }
}
