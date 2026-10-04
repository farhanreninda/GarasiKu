package com.vehiclemaintenancepro.presentation.screen.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vehiclemaintenancepro.BuildConfig
import com.vehiclemaintenancepro.core.design.Dimens
import com.vehiclemaintenancepro.core.utility.Formatters
import com.vehiclemaintenancepro.presentation.component.*

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
    onOpenVehicles: () -> Unit = {},
    onOpenService: () -> Unit = {},
    odometerRequest: Int = 0,
    onOdometerRequestConsumed: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    var editingOdometer by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(odometerRequest) {
        if (odometerRequest > 0) {
            viewModel.clearNotice()
            editingOdometer = true
            onOdometerRequestConsumed()
        }
    }
    val context = LocalContext.current
    SettingsScreen(
        state = state,
        onSaveUserName = viewModel::updateUserName,
        onClearNotice = viewModel::clearNotice,
        onOpenVehicles = onOpenVehicles,
        onOpenService = onOpenService,
        onUpdateOdometer = { viewModel.clearNotice(); editingOdometer = true },
        onOpenNotifications = {
            context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
        },
    )
    val vehicle = state.vehicles.firstOrNull { it.isActive } ?: state.vehicles.firstOrNull()
    if (editingOdometer && vehicle != null) {
        var reading by rememberSaveable(vehicle.id) { mutableStateOf(vehicle.odometerKm.toString()) }
        var validation by remember { mutableStateOf<String?>(null) }
        AppFormDialog(
            onDismissRequest = { if (!saveState.isSaving) editingOdometer = false },
            title = { Text("Perbarui odometer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${vehicle.brand} ${vehicle.model} • ${vehicle.licensePlate}")
                    Text("Catatan terakhir: ${Formatters.odometer(vehicle.odometerKm)}")
                    OutlinedTextField(reading, { reading = it.filter(Char::isDigit) }, label = { Text("Odometer saat ini (km)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), enabled = !saveState.isSaving)
                    (validation ?: saveState.errorMessage)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button(enabled = !saveState.isSaving, onClick = {
                    val value = reading.toLongOrNull()
                    if (value == null || value < vehicle.odometerKm) {
                        validation = "Isi angka yang tidak lebih kecil dari catatan terakhir."
                    } else {
                        validation = null
                        viewModel.updateOdometer(vehicle.id, value) { editingOdometer = false }
                    }
                }) { Text(if (saveState.isSaving) "Menyimpan…" else "Simpan odometer") }
            },
            dismissButton = { TextButton(enabled = !saveState.isSaving, onClick = { editingOdometer = false }) { Text("Batal") } },
        )
    }
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onSaveUserName: (String) -> Unit,
    onClearNotice: () -> Unit,
    onOpenVehicles: () -> Unit = {},
    onOpenService: () -> Unit = {},
    onUpdateOdometer: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
) {
    var userName by rememberSaveable(state.userName) { mutableStateOf(state.userName) }
    var editingName by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.userName) { editingName = false }
    val vehicle = state.vehicles.firstOrNull { it.isActive } ?: state.vehicles.firstOrNull()
    AppBackground {
        if (state.isLoading) {
            AppLoadingState()
        } else {
            LazyColumn(contentPadding = PaddingValues(Dimens.SpaceLg), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg)) {
                item { PageHeader("Profil", "Identitas dan kendaraan yang kamu rawat.") }
                state.errorMessage?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
                state.notice?.let { notice ->
                    item { PanelCard { Text(notice); TextButton(onClick = onClearNotice, enabled = !state.isSaving) { Text("Tutup pesan") } } }
                }
                item {
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Profil pemilik", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(state.userName.ifBlank { "Pemilik kendaraan" }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("${state.vehicles.size} kendaraan • ${state.pendingReminderCount} pengingat belum selesai", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            TextButton(onClick = { userName = state.userName; editingName = true }, enabled = !state.isSaving, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) { Text("Ubah nama") }
                        }
                    }
                }
                item { SectionHeader("Kendaraan yang dipantau", actionLabel = "Kelola", onAction = onOpenVehicles) }
                item {
                    PanelCard {
                        if (vehicle == null) {
                            Text("Belum ada kendaraan", style = MaterialTheme.typography.titleMedium)
                            Text("Tambah kendaraan untuk mulai mencatat perawatan.", modifier = Modifier.padding(vertical = 8.dp))
                            Button(onClick = onOpenVehicles) { Text("Buka Kendaraan") }
                        } else {
                            Text("${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.titleLarge)
                            Text(vehicle.licensePlate, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Text(Formatters.odometer(vehicle.odometerKm), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            Text("Perbarui setelah berkendara agar pengingat kilometer tetap sesuai.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                            OutlinedButton(onClick = onUpdateOdometer, enabled = !state.isSaving) { Text("Perbarui odometer") }
                            TextButton(onClick = onOpenService) { Text("Kelola servis & pengingat") }
                        }
                    }
                }
                item {
                    PanelCard {
                        Text("Notifikasi perawatan", style = MaterialTheme.typography.titleMedium)
                        Text("Pemeriksaan harian untuk jadwal mendekati jatuh tempo. Izinkan notifikasi di pengaturan Android.", modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = onOpenNotifications) { Text("Atur notifikasi") }
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        Text("Data tersimpan di perangkat", style = MaterialTheme.typography.titleSmall)
                        Text("Versi ${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
    if (editingName) {
        AppFormDialog(
            onDismissRequest = { if (!state.isSaving) editingName = false },
            title = { Text("Nama profil") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(userName, { userName = it }, label = { Text("Nama pengguna") }, singleLine = true, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth())
                    state.notice?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                Button(onClick = { onSaveUserName(userName) }, enabled = !state.isSaving && userName.isNotBlank() && userName.trim() != state.userName) {
                    Text(if (state.isSaving) "Menyimpan…" else "Simpan nama")
                }
            },
            dismissButton = { TextButton(onClick = { editingName = false }, enabled = !state.isSaving) { Text("Batal") } },
        )
    }
}
