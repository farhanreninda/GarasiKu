package com.vehiclemaintenancepro.presentation.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.runtime.mutableStateOf
import com.vehiclemaintenancepro.presentation.screen.service.AddActivityDialog
import com.vehiclemaintenancepro.presentation.screen.service.ServiceScreen
import com.vehiclemaintenancepro.presentation.screen.service.ServiceUiState
import com.vehiclemaintenancepro.domain.model.*
import com.vehiclemaintenancepro.presentation.screen.settings.SettingsScreen
import com.vehiclemaintenancepro.presentation.screen.settings.SettingsUiState
import com.vehiclemaintenancepro.presentation.screen.statistics.StatisticsScreen
import com.vehiclemaintenancepro.presentation.screen.statistics.StatisticsUiState
import com.vehiclemaintenancepro.presentation.theme.VehicleMaintenanceProTheme
import java.time.Instant
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MaintenanceScreensTest {
    @get:Rule val composeRule = createComposeRule()

    @Test fun maintenanceHistoryFiltersCategoriesAndShowsComponentForecasts() {
        val pcx = vehicle().copy(vehicleType = VehicleType.Motorcycle, brand = "Honda", model = "PCX 160 CBS", year = 2023, odometerKm = 19_200)
        val activity = ActivityLog(1, 1, "Servis PCX", null, 124_552, Instant.parse("2026-03-07T05:00:00Z"),
            ActivityCategory.Service, 18_333, "Dewa Motor Plered", listOf(MaintenanceWorkItem(MaintenanceComponent.EngineOil, MaintenanceAction.Replace, "Shell 10W-40")))
        composeRule.setContent {
            VehicleMaintenanceProTheme(darkTheme = false) {
                ServiceScreen(ServiceUiState(isLoading = false, vehicles = listOf(pcx), activeVehicle = pcx, activities = listOf(activity)),
                    {}, {}, {}, {}, {}, {})
            }
        }
        composeRule.onNodeWithText("Lokasi: Dewa Motor Plered").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Rincian pekerjaan (1)").performScrollTo().performClick()
        composeRule.onNodeWithText("Shell 10W-40").performScrollTo().assertIsDisplayed()
        composeRule.onNode(hasText("Ganti oli") and hasClickAction()).performScrollTo().performClick()
        composeRule.onNodeWithText("Belum ada catatan untuk kategori ini").assertExists()
        composeRule.onNode(hasText("Servis") and hasClickAction()).performClick()
        composeRule.onNodeWithText("Servis PCX").assertExists()
        composeRule.onNodeWithText("Jadwal").performScrollTo().performClick()
        composeRule.onNodeWithText("24.333 km").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun activityFormDisablesDuplicateSubmitAndPreservesInputOnError() {
        val saving = mutableStateOf(false)
        val error = mutableStateOf<String?>(null)
        var submitted = ""
        composeRule.setContent {
            VehicleMaintenanceProTheme(darkTheme = false) {
                AddActivityDialog(saving.value, error.value, vehicle(), onDismiss = {}, onSubmit = {
                    submitted = it.title
                    saving.value = true
                })
            }
        }
        composeRule.onNodeWithText("Judul").performTextReplacement("Ganti oli QC")
        androidx.test.espresso.Espresso.closeSoftKeyboard()
        composeRule.onNodeWithText("Tambahkan komponen").performScrollTo().performClick()
        composeRule.onNodeWithText("Simpan aktivitas").performClick()
        assertEquals("Ganti oli QC", submitted)
        composeRule.onNodeWithText("Menyimpan…").assertIsNotEnabled()
        composeRule.runOnIdle { saving.value = false; error.value = "Data belum tersimpan. Coba lagi." }
        composeRule.onNodeWithText("Ganti oli QC").assertExists()
        composeRule.onNodeWithText("Data belum tersimpan. Coba lagi.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Simpan aktivitas").assertIsEnabled()
    }

    @Test
    fun statisticsHasVehicleFiltersAndMaintenanceActions() {
        var selection: Long? = null
        var openedService = false
        composeRule.setContent {
            VehicleMaintenanceProTheme(darkTheme = false) {
                StatisticsScreen(
                    StatisticsUiState(isLoading = false, vehicles = listOf(vehicle()), activities = listOf(ActivityLog(1L, 1L, "Servis", "", 150_000L, Instant.now()))),
                    onSelectVehicle = { selection = it },
                    onOpenService = { openedService = true },
                )
            }
        }
        composeRule.onAllNodesWithText("Rp150.000")[0].assertIsDisplayed()
        composeRule.onNodeWithText("QC 1234").performClick()
        assertEquals(1L, selection)
        composeRule.onNodeWithText("Catat biaya di Servis").performScrollTo().performClick()
        assertTrue(openedService)
    }

    @Test
    fun profileSavesEditedNameAndOpensOdometerUpdate() {
        var saved = ""
        var openedOdometer = false
        composeRule.setContent {
            VehicleMaintenanceProTheme(darkTheme = false) {
                SettingsScreen(
                    SettingsUiState(isLoading = false, userName = "Pemilik", vehicles = listOf(vehicle())),
                    onSaveUserName = { saved = it },
                    onClearNotice = {},
                    onUpdateOdometer = { openedOdometer = true },
                )
            }
        }
        composeRule.onNodeWithText("Ubah nama").performClick()
        composeRule.onNodeWithText("Nama pengguna").performTextReplacement("Nama baru")
        composeRule.onNodeWithText("Simpan nama").performClick()
        assertEquals("Nama baru", saved)
        composeRule.onNodeWithText("Batal").performClick()
        composeRule.onNodeWithText("Perbarui odometer").performScrollTo().performClick()
        assertTrue(openedOdometer)
    }

    @Test
    fun emptyStatisticsOffersAddVehicle() {
        var added = false
        composeRule.setContent {
            VehicleMaintenanceProTheme(darkTheme = false) {
                StatisticsScreen(StatisticsUiState(isLoading = false), onAddVehicle = { added = true })
            }
        }
        composeRule.onNodeWithText("Tambah kendaraan").performClick()
        assertTrue(added)
    }

    private fun vehicle() = Vehicle(1L, VehicleType.Car, null, "Toyota", "Raize", 2024, "QC 1234", null, null, null, TransmissionType.Cvt, FuelType.Gasoline, 36_000L, null, null, null, true)
}
