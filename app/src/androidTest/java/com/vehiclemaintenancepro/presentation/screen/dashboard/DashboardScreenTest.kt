package com.vehiclemaintenancepro.presentation.screen.dashboard

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.vehiclemaintenancepro.domain.model.DashboardSummary
import com.vehiclemaintenancepro.domain.model.FuelType
import com.vehiclemaintenancepro.domain.model.TransmissionType
import com.vehiclemaintenancepro.domain.model.Vehicle
import com.vehiclemaintenancepro.domain.model.VehicleType
import com.vehiclemaintenancepro.presentation.theme.VehicleMaintenanceProTheme
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue

class DashboardScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dashboardShowsEmptyVehicleState() {
        composeRule.setContent {
            VehicleMaintenanceProTheme(dynamicColor = false) {
                DashboardScreen(
                    state = DashboardUiState(
                        isLoading = false,
                        summary = emptySummary(),
                    ),
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("Belum ada kendaraan").assertIsDisplayed()
    }

    @Test
    fun dashboardShowsGasolineVehicleSummaryWithoutHealthScore() {
        composeRule.setContent {
            VehicleMaintenanceProTheme(dynamicColor = false) {
                DashboardScreen(
                    state = DashboardUiState(
                        isLoading = false,
                        summary = gasolineVehicleSummary(),
                    ),
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("Mobil", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("36.000 km").performScrollTo().assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("100/100").fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithText("Aktivitas terakhir").fetchSemanticsNodes().isEmpty())
    }

    private fun emptySummary() = DashboardSummary(
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

    private fun gasolineVehicleSummary(): DashboardSummary {
        val vehicle = Vehicle(
            id = 1L,
            vehicleType = VehicleType.Car,
            photoUri = null,
            brand = "Toyota",
            model = "Raize",
            year = 2024,
            licensePlate = "T 1819 BW",
            color = null,
            engineNumber = null,
            frameNumber = null,
            transmissionType = TransmissionType.Cvt,
            fuelType = FuelType.Gasoline,
            odometerKm = 36_000L,
            purchasePrice = null,
            purchaseDate = null,
            note = null,
            isActive = true,
        )

        return DashboardSummary(
            userName = "",
            activeVehicle = vehicle,
            vehicles = listOf(vehicle),
            nextService = null,
            nextOilChange = null,
            taxReminder = null,
            registrationReminder = null,
            insuranceReminder = null,
            monthlyExpense = 0L,
            averageFuelConsumptionKmPerLiter = null,
            nearestReminder = null,
        )
    }
}
