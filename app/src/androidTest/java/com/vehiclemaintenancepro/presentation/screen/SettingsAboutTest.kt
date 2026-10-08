package com.vehiclemaintenancepro.presentation.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.vehiclemaintenancepro.BuildConfig
import com.vehiclemaintenancepro.presentation.screen.settings.SettingsScreen
import com.vehiclemaintenancepro.presentation.screen.settings.SettingsUiState
import com.vehiclemaintenancepro.presentation.theme.VehicleMaintenanceProTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsAboutTest {
    @get:Rule val composeRule = createComposeRule()
    @Test fun aboutShowsInstalledVersionAndOpensAndroidInfoAction() {
        var opened = false
        composeRule.setContent { VehicleMaintenanceProTheme(darkTheme = false) {
            SettingsScreen(SettingsUiState(isLoading = false), {}, {}, onOpenAppInfo = { opened = true })
        } }
        composeRule.onNode(hasScrollAction()).performScrollToNode(hasText("Tentang GarasiKu"))
        composeRule.onNodeWithText("Tentang GarasiKu").performClick()
        composeRule.onNodeWithText("Versi terpasang").assertIsDisplayed()
        composeRule.onNodeWithText(BuildConfig.VERSION_NAME).assertIsDisplayed()
        composeRule.onNodeWithText("Info aplikasi di Android").performClick()
        assertTrue(opened)
        composeRule.onNodeWithText("Tutup").performClick()
        composeRule.onNodeWithText("Versi terpasang").assertDoesNotExist()
    }
}
