package com.vehiclemaintenancepro.presentation.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import com.vehiclemaintenancepro.MainActivity
import org.junit.Rule
import org.junit.Test

class RedesignNavigationTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun homeActionsOpenFormsOnceAndKeepNavigationLabels() {
        composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText("Catat perawatan").fetchSemanticsNodes().isNotEmpty() }
        listOf("Beranda" to "Buka dashboard", "Garasi" to "Buka kendaraan", "Servis" to "Buka servis", "Biaya" to "Buka statistik", "Profil" to "Buka profil").forEach { (label, description) ->
            composeRule.onNode(hasText(label) and hasContentDescription(description)).assertIsDisplayed()
        }
        composeRule.onNodeWithText("Catat perawatan").performScrollTo().performClick()
        composeRule.onNodeWithText("Simpan aktivitas").assertIsDisplayed()
        composeRule.onNodeWithText("Batal").performClick()
        composeRule.onNodeWithContentDescription("Buka profil").performClick()
        composeRule.onNodeWithContentDescription("Buka servis").performClick()
        composeRule.onNodeWithText("Simpan aktivitas").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Buka dashboard").performClick()
        composeRule.onNodeWithText("Perbarui kilometer").performScrollTo().performClick()
        composeRule.onNodeWithText("Simpan odometer").assertIsDisplayed()
        composeRule.onNodeWithText("Batal").performClick()
        composeRule.onNodeWithContentDescription("Buka kendaraan").performClick()
        composeRule.onNode(hasText("Garasi") and hasClickAction()).assertExists()
        composeRule.onNodeWithContentDescription("Buka profil").performClick()
        composeRule.onNodeWithText("Simpan odometer").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Buka dashboard").performClick()
        composeRule.onNodeWithContentDescription("Buka pengingat servis").performClick()
        composeRule.onNodeWithContentDescription("Buka dashboard").performClick()
        composeRule.onNodeWithText("Catat perawatan").assertExists()
        composeRule.onNodeWithContentDescription("Buka pengingat servis").performClick()
        androidx.test.espresso.Espresso.pressBack()
        composeRule.onNodeWithText("Catat perawatan").assertExists()
    }
}
