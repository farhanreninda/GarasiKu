package com.vehiclemaintenancepro.presentation.screen.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.vehiclemaintenancepro.data.backup.DataBackup
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.FileDownload
import android.Manifest
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.ui.graphics.Color
import com.vehiclemaintenancepro.domain.model.ThemeMode
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
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
    val pendingImport by viewModel.pendingImport.collectAsStateWithLifecycle()
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(viewModel::exportData)
    }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::inspectImport)
    }
    val context = LocalContext.current
    var systemNotificationsEnabled by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    val permissionPicker = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        systemNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        com.vehiclemaintenancepro.core.notification.ReminderNotificationScheduler.checkNow(context)
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        systemNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    SettingsScreen(
        state = state,
        onSaveUserName = viewModel::updateUserName,
        onThemeModeChanged = viewModel::updateThemeMode,
        systemNotificationsEnabled = systemNotificationsEnabled,
        onNotificationsEnabledChanged = { viewModel.updateNotifications(enabled = it) },
        onReminderDaysChanged = { tax, days ->
            if (tax) viewModel.updateNotifications(taxDays = days) else viewModel.updateNotifications(serviceDays = days)
        },
        onExportData = {
            viewModel.clearNotice()
            exportPicker.launch("garasiku-backup-${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))}.json")
        },
        onImportData = { viewModel.clearNotice(); importPicker.launch(arrayOf("*/*")) },
        onClearNotice = viewModel::clearNotice,
        onOpenVehicles = onOpenVehicles,
        onOpenService = onOpenService,
        onUpdateOdometer = { viewModel.clearNotice(); editingOdometer = true },
        onOpenAppInfo = {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri()))
        },
        onRequestNotificationPermission = { if (Build.VERSION.SDK_INT >= 33) permissionPicker.launch(Manifest.permission.POST_NOTIFICATIONS) },
        onOpenNotifications = {
            context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
        },
    )
    pendingImport?.let { backup ->
        ImportBackupDialog(backup, saveState.isSaving, saveState.errorMessage, viewModel::confirmImport, viewModel::cancelImport)
    }
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
    onThemeModeChanged: (ThemeMode) -> Unit = {},
    onRequestNotificationPermission: () -> Unit = {},
    systemNotificationsEnabled: Boolean = true,
    onNotificationsEnabledChanged: (Boolean) -> Unit = {},
    onReminderDaysChanged: (Boolean, Int) -> Unit = { _, _ -> },
    onOpenAppInfo: () -> Unit = {},
    onExportData: () -> Unit = {},
    onImportData: () -> Unit = {},
) {
    var userName by rememberSaveable(state.userName) { mutableStateOf(state.userName) }
    var showingAbout by rememberSaveable { mutableStateOf(false) }
    var showingReminderDetails by rememberSaveable { mutableStateOf(false) }
    var editingLead by rememberSaveable { mutableStateOf<String?>(null) }
    var editingName by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.userName) { editingName = false }
    val vehicle = state.vehicles.firstOrNull { it.isActive } ?: state.vehicles.firstOrNull()
    AppBackground {
        if (state.isLoading) {
            AppLoadingState()
        } else {
            LazyColumn(contentPadding = PaddingValues(Dimens.SpaceLg), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { PageHeader("Profil & pengaturan", "Identitas dan kendaraan yang kamu rawat.") }
                state.errorMessage?.let { error -> item { Text(error, color = MaterialTheme.colorScheme.error) } }
                state.notice?.let { notice ->
                    item { PanelCard { Text(notice); OutlinedButton(onClick = onClearNotice, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) { Text("Tutup pesan") } } }
                }
                item {
                    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                        Column(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(Color(0xFF00288E), Color(0xFF0051D5))))
                            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconBadge(Icons.Outlined.Person, null, containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.primary)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Profil pemilik", style = MaterialTheme.typography.labelMedium, color = Color.White)
                                    Text(state.userName.ifBlank { "Pemilik kendaraan" }, style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                IconButton(onClick = { userName = state.userName; editingName = true }, enabled = !state.isSaving) {
                                    Icon(Icons.Outlined.Edit, "Ubah nama", tint = Color.White)
                                }
                            }
                            Surface(color = Color(0xFF001B60).copy(alpha = 0.45f), shape = MaterialTheme.shapes.small) {
                                Text("${state.vehicles.size} kendaraan • ${state.pendingReminderCount} pengingat belum selesai",
                                    color = Color.White, modifier = Modifier.fillMaxWidth().padding(12.dp))
                            }

                        }
                    }
                }
                item {
                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = if (androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f) 1 else 2) {
                        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f).widthIn(min = 140.dp)) {
                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconBadge(Icons.Outlined.Storage, null, Modifier.size(32.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Penyimpanan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Di perangkat", style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface, modifier = Modifier.weight(1f).widthIn(min = 140.dp), onClick = onOpenService) {
                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconBadge(Icons.Outlined.EventAvailable, null, Modifier.size(32.dp), containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                                Column(Modifier.weight(1f)) {
                                    Text("Pengingat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${state.pendingReminderCount} belum selesai", style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                    }
                }
                item { SectionHeader("Kendaraan yang dipantau", actionLabel = "Kelola", onAction = onOpenVehicles) }
                item {
                    PanelCard {
                        if (vehicle == null) {
                            Text("Belum ada kendaraan", style = MaterialTheme.typography.titleMedium)
                            Text("Tambah kendaraan untuk mulai mencatat perawatan.", modifier = Modifier.padding(vertical = 8.dp))
                            Button(onClick = onOpenVehicles, modifier = Modifier.fillMaxWidth()) { Text("Buka Kendaraan") }
                        } else {
                            val largeText = androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
                            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp), maxItemsInEachRow = if (largeText) 1 else 2) {
                                Column(Modifier.weight(1f).widthIn(min = 160.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(vehicle.licensePlate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(Modifier.weight(1f), horizontalAlignment = if (largeText) Alignment.Start else Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("Odometer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(Formatters.odometer(vehicle.odometerKm), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp), maxItemsInEachRow = if (largeText) 1 else 2) {
                                OutlinedButton(onClick = onUpdateOdometer, enabled = !state.isSaving,
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.weight(1f).widthIn(min = 140.dp).heightIn(min = 48.dp)
                                        .semantics { contentDescription = "Perbarui odometer" }) { Text("Perbarui KM") }
                                FilledTonalButton(onClick = onOpenService, shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.weight(1f).widthIn(min = 140.dp).heightIn(min = 48.dp)) { Text("Servis & pengingat") }
                            }
                        }
                    }
                }
                item { SectionHeader("Pengaturan aplikasi") }
                item {
                    PanelCard {
                        SettingsSectionTitle(Icons.Outlined.Palette, "Tampilan", "Pilih tema aplikasi")
                        Spacer(Modifier.height(10.dp))
                        FilterOptions(ThemeMode.entries, state.themeMode, { it.label }, onThemeModeChanged,
                            modifier = Modifier.fillMaxWidth(), enabled = !state.isSaving)
                    }
                }
                item {
                    PanelCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f)) {
                                SettingsSectionTitle(Icons.Outlined.Notifications, "Pengingat",
                                    if (state.notificationsEnabled) "Notifikasi aktif" else "Notifikasi dimatikan")
                            }
                            Switch(state.notificationsEnabled, onNotificationsEnabledChanged, enabled = !state.isSaving,
                                modifier = Modifier.semantics { contentDescription = "Aktifkan notifikasi pengingat" })
                        }
                        Spacer(Modifier.height(6.dp))
                        ReminderLeadRow("Pajak & STNK", state.taxReminderDays, !state.isSaving) { editingLead = "tax" }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        ReminderLeadRow("Servis & ganti oli", state.serviceReminderDays, !state.isSaving) { editingLead = "service" }
                        Text("Mulai mengingatkan sebelum jatuh tempo.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                        OutlinedButton(onClick = { showingReminderDetails = true }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) { Text("Cara kerja pengingat") }
                        SettingsActionRow(Icons.Outlined.Event, "Atur jadwal pajak & servis", "Buka daftar jadwal", onClick = onOpenService)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingsActionRow(Icons.Outlined.Notifications, "Atur notifikasi", "Izin dan suara di Android", onClick = onOpenNotifications)
                        if (!systemNotificationsEnabled) {
                            Text("Izin Android belum aktif", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            if (Build.VERSION.SDK_INT >= 33) OutlinedButton(onClick = onRequestNotificationPermission, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) { Text("Izinkan notifikasi") }
                        }
                    }
                }
                item { NotificationTestPanel(state.notificationsEnabled) }
                item {
                    PanelCard {
                        SettingsSectionTitle(Icons.Outlined.Storage, "Cadangan data", "Simpan atau pulihkan catatan kendaraan")
                        Spacer(Modifier.height(8.dp))
                        SettingsActionRow(Icons.Outlined.FileUpload, "Ekspor data", "Simpan cadangan ke file JSON", !state.isSaving, onExportData)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        SettingsActionRow(Icons.Outlined.FileDownload, "Impor data", "Pulihkan file cadangan", !state.isSaving, onImportData)
                        Text("Impor akan mengganti data saat ini setelah konfirmasi.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                        if (state.isSaving) {
                            LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
                            Text("Memproses data…", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                item {
                    PanelCard {
                        SettingsActionRow(Icons.Outlined.Info, "Tentang GarasiKu",
                            "Versi ${appVersionLabel()} • ${appBuildLabel()}", onClick = { showingAbout = true })
                    }
                }
            }
        }
    }
    if (showingAbout) {
        AppFormDialog(
            onDismissRequest = { showingAbout = false },
            title = { Text("Tentang GarasiKu") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Catatan kendaraan, biaya, dan pengingat perawatan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    AboutDetail("Versi aplikasi", appVersionLabel())
                    AboutDetail("Jenis APK", appBuildLabel())
                    AboutDetail("Versi terpasang", BuildConfig.VERSION_NAME)
                    AboutDetail("Kode versi", BuildConfig.VERSION_CODE.toString())
                    AboutDetail("Penyimpanan data", "Di perangkat")
                    OutlinedButton(onClick = onOpenAppInfo, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) { Text("Info aplikasi di Android") }
                }
            },
            confirmButton = { Button(onClick = { showingAbout = false }) { Text("Tutup") } },
            dismissButton = {},
        )
    }
    if (showingReminderDetails) {
        AppFormDialog(
            onDismissRequest = { showingReminderDetails = false },
            title = { Text("Cara kerja pengingat") },
            text = { Text("Notifikasi diperiksa berkala mulai H- yang dipilih hingga jadwal ditandai selesai. Batas KM memakai odometer yang kamu catat. Android dapat menunda waktu muncul untuk menghemat baterai.", Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { Button(onClick = { showingReminderDetails = false }) { Text("Mengerti") } },
            dismissButton = {},
        )
    }
    editingLead?.let { category ->
        val tax = category == "tax"
        var days by rememberSaveable(category) { mutableStateOf((if (tax) state.taxReminderDays else state.serviceReminderDays).toString()) }
        AppFormDialog(
            onDismissRequest = { editingLead = null },
            title = { Text(if (tax) "Pengingat pajak & STNK" else "Pengingat servis & oli") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Mulai muncul berapa hari sebelum jatuh tempo? 0 berarti hari H.")
                    FilterOptions(listOf(0, 1, 3, 7, 14, 30), days.toIntOrNull() ?: -1,
                        { if (it == 0) "Hari H" else "H-$it" }, { days = it.toString() })
                    OutlinedTextField(days, { days = it.filter(Char::isDigit).take(3) }, label = { Text("Jumlah hari (0–365)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                        modifier = Modifier.fillMaxWidth(), isError = days.toIntOrNull()?.let { it !in 0..365 } == true)
                }
            },
            confirmButton = { Button(enabled = days.toIntOrNull() in 0..365, onClick = {
                onReminderDaysChanged(tax, days.toInt()); editingLead = null
            }) { Text("Simpan batas hari") } },
            dismissButton = { TextButton(onClick = { editingLead = null }) { Text("Batal") } },
        )
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

@Composable
fun ImportBackupDialog(
    backup: DataBackup,
    isSaving: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    var acknowledged by rememberSaveable(backup.exportedAtMillis) { mutableStateOf(false) }
    AppFormDialog(
        onDismissRequest = { if (!isSaving) onCancel() },
        title = { Text("Pulihkan cadangan?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Cadangan: ${java.time.Instant.ofEpochMilli(backup.exportedAtMillis).atZone(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))}")
                Text("${backup.vehicles.size} kendaraan • ${backup.activities.size} aktivitas\n${backup.reminders.size} pengingat • ${backup.accessoryCount} aksesori")
                Text("Profil, tema, dan pengaturan pengingat ikut dipulihkan. Seluruh data saat ini akan diganti. Ekspor dahulu jika ingin menyimpannya.")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(acknowledged, { acknowledged = it }, enabled = !isSaving)
                    Text("Saya setuju mengganti data di perangkat", Modifier.weight(1f))
                }
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { Button(onClick = onConfirm, enabled = acknowledged && !isSaving) { Text(if (isSaving) "Mengimpor…" else "Ganti & impor data") } },
        dismissButton = { TextButton(onClick = onCancel, enabled = !isSaving) { Text("Batal") } },
    )
}

@Composable
private fun ReminderLeadRow(label: String, days: Int, enabled: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f))
            Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(if (days == 0) "Hari H" else "H-$days", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.labelLarge)
            }
            Icon(Icons.Outlined.ChevronRight, null, Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SettingsSectionTitle(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String,
    enabled: Boolean = true, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(20.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Outlined.ChevronRight, null, Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AboutDetail(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

private fun appVersionLabel() = BuildConfig.VERSION_NAME.removeSuffix("-qc").removeSuffix("-debug")
private fun appBuildLabel() = when (BuildConfig.BUILD_TYPE) {
    "qc" -> "Pratinjau QC"
    "debug" -> "Pengembangan"
    else -> "Rilis"
}
