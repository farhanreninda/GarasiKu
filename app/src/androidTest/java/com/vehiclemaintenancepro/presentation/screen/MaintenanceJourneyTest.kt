package com.vehiclemaintenancepro.presentation.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.espresso.Espresso
import com.vehiclemaintenancepro.BuildConfig
import com.vehiclemaintenancepro.MainActivity
import androidx.room.Room
import com.vehiclemaintenancepro.core.constant.AppConstants
import com.vehiclemaintenancepro.data.local.database.VehicleMaintenanceDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class MaintenanceJourneyTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun addVehicleUpdateMileageAndRecordMaintenanceCost() {
        assumeTrue("This journey writes sample data only in the isolated QC application", BuildConfig.APPLICATION_ID.endsWith(".qc"))
        val plate = "QC ${System.currentTimeMillis() % 1_000_000}"
        val database = Room.databaseBuilder(composeRule.activity, VehicleMaintenanceDatabase::class.java, AppConstants.DATABASE_NAME)
            .addMigrations(VehicleMaintenanceDatabase.MIGRATION_1_2, VehicleMaintenanceDatabase.MIGRATION_2_3, VehicleMaintenanceDatabase.MIGRATION_3_4).build()
        val previousVehicle = runBlocking { database.vehicleDao().getActiveVehicleId() }
        try {
        composeRule.waitUntil(10_000) { composeRule.onAllNodesWithContentDescription("Tambah kendaraan").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithContentDescription("Tambah kendaraan").performClick()
        composeRule.onNodeWithContentDescription("Pilih Merek").performScrollTo().performClick()
        composeRule.onNodeWithText("Toyota").performClick()
        composeRule.onNodeWithContentDescription("Pilih Tipe / model").performScrollTo().performClick()
        composeRule.onNodeWithText("Avanza").performClick()
        if (composeRule.onAllNodesWithContentDescription("Pilih Transmisi").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithContentDescription("Pilih Transmisi").performScrollTo().performClick()
            composeRule.onNodeWithText("Otomatis").performClick()
        }
        composeRule.onNodeWithText("Nomor polisi").performScrollTo().performTextInput(plate)
        Espresso.closeSoftKeyboard()
        composeRule.onNodeWithText("Simpan kendaraan").performClick()
        waitForDialogToClose("Simpan kendaraan")
        val selector = hasText(plate) and hasClickAction()
        if (composeRule.onAllNodes(selector).fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNode(selector).performScrollTo().performClick()
        }
        composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText("Toyota Avanza / $plate").fetchSemanticsNodes().isNotEmpty() }

        composeRule.onNodeWithContentDescription("Buka profil").performClick()
        composeRule.onNodeWithText("Perbarui odometer").performScrollTo().performClick()
        composeRule.onNodeWithText("Odometer saat ini (km)").performTextReplacement("100")
        Espresso.closeSoftKeyboard()
        composeRule.onNodeWithText("Simpan odometer").performClick()
        waitForDialogToClose("Simpan odometer")
        composeRule.onNodeWithText("100 km").assertExists()

        composeRule.onNodeWithContentDescription("Buka servis").performClick()
        composeRule.onNodeWithText("Catat aktivitas").performClick()
        composeRule.onNodeWithText("Judul").performTextReplacement("Servis QC")
        composeRule.onNodeWithText("Tambahkan komponen").performScrollTo().performClick()
        composeRule.onNode(hasText("Biaya") and hasSetTextAction()).performScrollTo().performTextInput("150000")
        Espresso.closeSoftKeyboard()
        composeRule.onNodeWithText("Simpan aktivitas").performClick()
        waitForDialogToClose("Simpan aktivitas")

        composeRule.onNodeWithContentDescription("Buka statistik").performClick()
        composeRule.onNode(hasText(plate) and hasClickAction()).performClick()
        composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText("Rp150.000").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onAllNodesWithText("Rp150.000")[0].assertIsDisplayed()
        } finally {
            runBlocking {
                val dao = database.vehicleDao()
                dao.getVehicles().firstOrNull { it.licensePlate == plate }?.let { created ->
                    dao.archiveVehicle(created.id, System.currentTimeMillis())
                }
                previousVehicle?.let { id ->
                    dao.clearActiveVehicles(System.currentTimeMillis())
                    dao.markVehicleActive(id, System.currentTimeMillis())
                }
            }
            database.close()
        }
    }

    private fun waitForDialogToClose(button: String) {
        composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText(button).fetchSemanticsNodes().isEmpty() }
    }
}
