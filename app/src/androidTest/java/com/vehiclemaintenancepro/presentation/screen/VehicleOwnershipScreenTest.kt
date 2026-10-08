package com.vehiclemaintenancepro.presentation.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.vehiclemaintenancepro.core.common.SaveState
import com.vehiclemaintenancepro.domain.model.*
import com.vehiclemaintenancepro.presentation.screen.vehicles.VehicleOwnershipScreen
import com.vehiclemaintenancepro.presentation.theme.VehicleMaintenanceProTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class VehicleOwnershipScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun purchaseAndAccessorySectionsShowActualTotalsAndMissingPrices() {
        var saved: VehicleAccessory? = null
        val vehicle = Vehicle(1, VehicleType.Motorcycle, null, "Honda", "PCX 160 CBS", 2023, "QC OWNERSHIP", null,
            "QCENGINE0001", "QCFRAME0000000001", TransmissionType.Cvt, FuelType.Gasoline, 19_537, 31_775_000,
            LocalDate.of(2023, 7, 13), null, true, 32_075_000, "Daya Motor", LocalDate.of(2023, 5, 27),
            listOf(VehicleAccessory(id = "hugger", name = "Hugger", price = 84_676), VehicleAccessory(id = "footrest", name = "Pijakan kaki PCX")))
        composeRule.setContent { VehicleMaintenanceProTheme(darkTheme = false) {
            VehicleOwnershipScreen(vehicle, SaveState(), {}, {}, { _, _ -> }, { item, done -> saved = item; done() }, { _, _ -> })
        } }
        composeRule.onNodeWithText("Rp31.775.000").assertIsDisplayed()
        composeRule.onNodeWithText("Rp300.000").performScrollTo().assertIsDisplayed()
        composeRule.onNode(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange))
            .performScrollToNode(hasText("47 hari"))
        composeRule.onNodeWithText("47 hari").assertIsDisplayed()
        composeRule.onNode(SemanticsMatcher.keyIsDefined(androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange))
            .performScrollToNode(hasText("Aksesori") and hasClickAction())
        composeRule.onNode(hasText("Aksesori") and hasClickAction()).performClick()
        composeRule.onNodeWithText("2 item • 1 harga belum dicatat").assertIsDisplayed()
        composeRule.onNodeWithText("Tambah aksesori").performClick()
        composeRule.onNodeWithText("Simpan aksesori").performClick()
        composeRule.onNodeWithText("Nama aksesori wajib diisi").assertExists()
        assertNull(saved)
        composeRule.onNodeWithText("Nama aksesori").performTextInput("Sarung jok Arvi")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        composeRule.onNodeWithText("Simpan aksesori").performClick()
        assertEquals("Sarung jok Arvi", saved!!.name)
        assertNull(saved.price)
    }
}
