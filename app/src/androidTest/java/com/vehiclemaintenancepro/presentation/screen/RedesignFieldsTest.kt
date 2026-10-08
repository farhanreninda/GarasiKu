package com.vehiclemaintenancepro.presentation.screen

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.vehiclemaintenancepro.domain.model.ActivityLog
import com.vehiclemaintenancepro.domain.model.Vehicle
import com.vehiclemaintenancepro.domain.model.VehicleType
import com.vehiclemaintenancepro.domain.model.TransmissionType
import com.vehiclemaintenancepro.domain.model.FuelType
import com.vehiclemaintenancepro.presentation.component.DateField
import com.vehiclemaintenancepro.presentation.screen.statistics.StatisticsScreen
import com.vehiclemaintenancepro.presentation.screen.statistics.StatisticsUiState
import com.vehiclemaintenancepro.presentation.theme.VehicleMaintenanceProTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class RedesignFieldsTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun datePickerPreservesIsoDateAndCanClearOptionalDate() {
        val value = mutableStateOf("2026-03-07")
        composeRule.setContent { VehicleMaintenanceProTheme(darkTheme = false) {
            DateField(value.value, { value.value = it }, "Tanggal jatuh tempo", optional = true)
        } }
        composeRule.onNodeWithContentDescription("Pilih Tanggal jatuh tempo").performClick()
        composeRule.onNode(hasText("Pilih tanggal") and hasClickAction()).performClick()
        assertEquals("2026-03-07", value.value)
        composeRule.onNodeWithText("Kosongkan tanggal").performClick()
        assertEquals("", value.value)
    }

    @Test fun tappingCostBarChangesDisplayedMonthAndAmount() {
        val month = YearMonth.now().minusMonths(1)
        val vehicle = Vehicle(1, VehicleType.Car, null, "Toyota", "Raize", 2024, "QC 1234", null, null, null,
            TransmissionType.Cvt, FuelType.Gasoline, 36_000, null, null, null, true)
        val activity = ActivityLog(1, 1, "Servis", null, 150_000, month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant())
        composeRule.setContent { VehicleMaintenanceProTheme(darkTheme = false) {
            StatisticsScreen(StatisticsUiState(isLoading = false, vehicles = listOf(vehicle), activities = listOf(activity)))
        } }
        composeRule.onAllNodesWithText("Rp0")[0].assertExists()
        val label = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("id-ID")))
        composeRule.onNode(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange))
            .performScrollToNode(hasContentDescription("$label, Rp150.000"))
        composeRule.onNodeWithContentDescription("$label, Rp150.000").performClick()
        composeRule.onAllNodesWithText("Rp150.000")[0].assertExists()
        composeRule.onNode(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange))
            .performScrollToNode(hasText("Biaya $label"))
        composeRule.onNodeWithText("Biaya $label").assertExists()
    }
}
