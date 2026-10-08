package com.vehiclemaintenancepro.presentation.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.vehiclemaintenancepro.domain.model.*
import com.vehiclemaintenancepro.presentation.screen.statistics.StatisticsScreen
import com.vehiclemaintenancepro.presentation.screen.statistics.StatisticsUiState
import com.vehiclemaintenancepro.presentation.theme.VehicleMaintenanceProTheme
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class OwnershipLedgerTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun ledgerKeepsPurchaseAccessoriesAndServiceSeparateIncludingExistingAccessoryLogs() {
        val vehicle = Vehicle(1, VehicleType.Motorcycle, null, "Honda", "PCX", 2023, "QC LEDGER", null, null, null,
            TransmissionType.Cvt, FuelType.Gasoline, 100, 1_000_000, null, null, true,
            accessories = listOf(VehicleAccessory(name = "Hugger", price = 100), VehicleAccessory(name = "Jok")))
        val logs = listOf(ActivityLog(1, 1, "Servis", null, 200, Instant.now(), category = ActivityCategory.Service),
            ActivityLog(2, 1, "Aksesori lama", null, 50, Instant.now(), category = ActivityCategory.Accessory))
        composeRule.setContent { VehicleMaintenanceProTheme(darkTheme = false) {
            StatisticsScreen(StatisticsUiState(isLoading = false, vehicles = listOf(vehicle), activities = logs))
        } }
        composeRule.onNodeWithText("Rp1.000.350").assertIsDisplayed()
        composeRule.onNodeWithText("Rp1.000.000").assertIsDisplayed()
        composeRule.onNode(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange))
            .performScrollToNode(hasText("Rp150"))
        composeRule.onNodeWithText("Rp150").assertIsDisplayed()
        composeRule.onNodeWithText("2 item • 1 harga belum dicatat").assertExists()
        composeRule.onNode(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange))
            .performScrollToNode(hasText("Rp200"))
        composeRule.onAllNodesWithText("Rp200")[0].assertIsDisplayed()
    }
}
