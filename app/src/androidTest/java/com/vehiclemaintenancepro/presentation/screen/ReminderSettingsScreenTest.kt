package com.vehiclemaintenancepro.presentation.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.vehiclemaintenancepro.presentation.screen.settings.SettingsScreen
import com.vehiclemaintenancepro.presentation.screen.settings.SettingsUiState
import com.vehiclemaintenancepro.presentation.theme.VehicleMaintenanceProTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ReminderSettingsScreenTest {
    @get:Rule val composeRule = createComposeRule()
    @Test fun leadPickerSupportsPresetsCustomValuesAndCancel() {
        var saved: Pair<Boolean, Int>? = null
        composeRule.setContent { VehicleMaintenanceProTheme {
            SettingsScreen(SettingsUiState(isLoading = false, taxReminderDays = 30, serviceReminderDays = 3), {}, {},
                onReminderDaysChanged = { tax, days -> saved = tax to days })
        } }
        composeRule.onNodeWithText("Pajak & STNK").performScrollTo().performClick()
        composeRule.onNodeWithText("H-14").performClick()
        composeRule.onNodeWithText("Simpan batas hari").performClick()
        assertEquals(true to 14, saved)
        saved = null
        composeRule.onNodeWithText("Servis & ganti oli").performScrollTo().performClick()
        composeRule.onNodeWithText("Jumlah hari (0–365)").performTextReplacement("366")
        composeRule.onNodeWithText("Simpan batas hari").assertIsNotEnabled()
        composeRule.onNodeWithText("Jumlah hari (0–365)").performTextReplacement("21")
        composeRule.onNodeWithText("Simpan batas hari").performClick()
        assertEquals(false to 21, saved)
        saved = null
        composeRule.onNodeWithText("Pajak & STNK").performScrollTo().performClick()
        composeRule.onNodeWithText("Batal").performClick()
        assertNull(saved)
    }
}
