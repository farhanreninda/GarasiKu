package com.vehiclemaintenancepro.presentation.screen.dashboard

import app.cash.turbine.test
import com.vehiclemaintenancepro.MainDispatcherRule
import com.vehiclemaintenancepro.core.common.ResultState
import com.vehiclemaintenancepro.domain.model.DashboardSummary
import com.vehiclemaintenancepro.domain.model.Vehicle
import com.vehiclemaintenancepro.domain.model.VehicleCreateRequest
import com.vehiclemaintenancepro.domain.repository.DashboardRepository
import com.vehiclemaintenancepro.domain.repository.VehicleRepository
import com.vehiclemaintenancepro.domain.usecase.ObserveDashboardSummaryUseCase
import com.vehiclemaintenancepro.domain.usecase.SetActiveVehicleUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `uiState exposes dashboard summary when repository succeeds`() = runTest {
        val repository = FakeDashboardRepository()
        val viewModel = dashboardViewModel(repository)
        val summary = dashboardSummary()

        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            repository.emit(ResultState.Success(summary))
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(summary, state.summary)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState exposes error when repository fails`() = runTest {
        val repository = FakeDashboardRepository()
        val viewModel = dashboardViewModel(repository)

        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            repository.emit(ResultState.Error(message = "database error"))
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("database error", state.errorMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun dashboardSummary() = DashboardSummary(
        userName = "",
        activeVehicle = null,
        nextService = null,
        nextOilChange = null,
        taxReminder = null,
        registrationReminder = null,
        insuranceReminder = null,
        monthlyExpense = 0L,
        averageFuelConsumptionKmPerLiter = null,
        nearestReminder = null,
    )

    private fun dashboardViewModel(repository: FakeDashboardRepository) = DashboardViewModel(
        observeDashboardSummaryUseCase = ObserveDashboardSummaryUseCase(repository),
        setActiveVehicleUseCase = SetActiveVehicleUseCase(FakeVehicleRepository()),
    )

    private class FakeDashboardRepository : DashboardRepository {
        private val states = MutableSharedFlow<ResultState<DashboardSummary>>(replay = 1)

        override fun observeDashboardSummary(): Flow<ResultState<DashboardSummary>> = states

        suspend fun emit(state: ResultState<DashboardSummary>) {
            states.emit(state)
        }
    }

    private class FakeVehicleRepository : VehicleRepository {
        override suspend fun updateDetails(vehicleId: Long, request: com.vehiclemaintenancepro.domain.model.VehicleDetailsUpdateRequest) = Unit
        override suspend fun saveAccessory(vehicleId: Long, item: com.vehiclemaintenancepro.domain.model.VehicleAccessory) = Unit
        override suspend fun deleteAccessory(vehicleId: Long, accessoryId: String) = Unit
        override fun observeVehicles(): Flow<List<Vehicle>> = emptyFlow()

        override fun observeActiveVehicle(): Flow<Vehicle?> = emptyFlow()

        override suspend fun addVehicle(request: VehicleCreateRequest): Long = 0L

        override suspend fun setActiveVehicle(vehicleId: Long) = Unit

        override suspend fun archiveVehicle(vehicleId: Long) = Unit
        override suspend fun updateOdometer(vehicleId: Long, odometerKm: Long) = Unit
    }
}

