package com.vehiclemaintenancepro.presentation.screen.service

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vehiclemaintenancepro.core.design.Dimens
import com.vehiclemaintenancepro.core.utility.Formatters
import com.vehiclemaintenancepro.domain.model.ActivityLog
import com.vehiclemaintenancepro.domain.model.ActivityLogCreateRequest
import com.vehiclemaintenancepro.domain.model.ActivityCategory
import com.vehiclemaintenancepro.domain.model.MaintenanceWorkItem
import com.vehiclemaintenancepro.data.mapper.MaintenanceWorkItemJson
import com.vehiclemaintenancepro.domain.model.MaintenanceRecommendation
import com.vehiclemaintenancepro.domain.model.MaintenanceRecommendationEngine
import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.MaintenanceReminderCreateRequest
import com.vehiclemaintenancepro.domain.model.ReminderType
import com.vehiclemaintenancepro.domain.model.Vehicle
import com.vehiclemaintenancepro.domain.model.VehicleType
import com.vehiclemaintenancepro.presentation.component.AppActionRow
import com.vehiclemaintenancepro.presentation.component.FilterOptions
import com.vehiclemaintenancepro.presentation.component.AppBackground
import com.vehiclemaintenancepro.presentation.component.AppLoadingState
import com.vehiclemaintenancepro.presentation.component.AppFormDialog
import com.vehiclemaintenancepro.presentation.component.DateField
import com.vehiclemaintenancepro.presentation.component.EmptyPrompt
import com.vehiclemaintenancepro.presentation.component.IconBadge
import com.vehiclemaintenancepro.presentation.component.PageHeader
import com.vehiclemaintenancepro.presentation.component.PanelCard
import com.vehiclemaintenancepro.presentation.component.SectionHeader
import com.vehiclemaintenancepro.presentation.component.StatusPill
import com.vehiclemaintenancepro.presentation.component.VehicleIllustration
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ServiceRoute(
    addReminderRequest: Int,
    onAddVehicle: () -> Unit,
    viewModel: ServiceViewModel = hiltViewModel(),
    addActivityRequest: Int = 0,
    onActivityRequestConsumed: () -> Unit = {},
    onReminderRequestConsumed: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    var showReminderDialog by rememberSaveable { mutableStateOf(false) }
    var showActivityDialog by rememberSaveable { mutableStateOf(false) }
    var reminderDraft by rememberSaveable(stateSaver = androidx.compose.runtime.saveable.Saver<MaintenanceReminderCreateRequest?, List<String>>(
        save = { draft -> draft?.let { listOf(it.vehicleId.toString(), it.type.name, it.title, it.dueDate?.toString().orEmpty(), it.dueOdometerKm?.toString().orEmpty()) } ?: emptyList() },
        restore = { fields -> if (fields.isEmpty()) null else MaintenanceReminderCreateRequest(fields[0].toLong(), ReminderType.valueOf(fields[1]), fields[2],
            fields[3].takeIf(String::isNotEmpty)?.let(LocalDate::parse), fields[4].toLongOrNull()) },
    )) { mutableStateOf<MaintenanceReminderCreateRequest?>(null) }

    LaunchedEffect(addReminderRequest) {
        if (addReminderRequest > 0) {
            reminderDraft = null
            showReminderDialog = true
            onReminderRequestConsumed()
        }
    }
    LaunchedEffect(addActivityRequest) {
        if (addActivityRequest > 0) {
            viewModel.clearNotice()
            showActivityDialog = true
            onActivityRequestConsumed()
        }
    }

    ServiceScreen(
        state = state,
        onAddVehicle = onAddVehicle,
        onAddReminder = { viewModel.clearNotice(); reminderDraft = null; showReminderDialog = true },
        onAddActivity = { viewModel.clearNotice(); showActivityDialog = true },
        onCompleteReminder = viewModel::completeReminder,
        onCreateRecommendationReminder = { viewModel.clearNotice(); reminderDraft = it; showReminderDialog = true },
        onClearNotice = viewModel::clearNotice,
    )

    val activeVehicle = state.activeVehicle
    if (showReminderDialog && activeVehicle != null) {
        AddReminderDialog(
            isSaving = saveState.isSaving,
            saveError = saveState.errorMessage,
            vehicle = activeVehicle,
            initialRequest = reminderDraft,
            lastOilReading = state.activities.filter { it.vehicleId == activeVehicle.id }.sortedByDescending { it.occurredAt }
                .firstOrNull { log -> log.workItems.any { it.component == com.vehiclemaintenancepro.domain.model.MaintenanceComponent.EngineOil &&
                    it.action == com.vehiclemaintenancepro.domain.model.MaintenanceAction.Replace } }?.odometerKm,
            onDismiss = { if (!saveState.isSaving) showReminderDialog = false },
            onSubmit = { request ->
                viewModel.addReminder(request) { showReminderDialog = false }
            },
        )
    }

    if (showActivityDialog && activeVehicle != null) {
        AddActivityDialog(
            isSaving = saveState.isSaving,
            saveError = saveState.errorMessage,
            vehicle = activeVehicle,
            onDismiss = { if (!saveState.isSaving) showActivityDialog = false },
            onSubmit = { request ->
                viewModel.addActivity(request) { showActivityDialog = false }
            },
        )
    }
}

@Composable
fun ServiceScreen(
    state: ServiceUiState,
    onAddVehicle: () -> Unit,
    onAddReminder: () -> Unit,
    onAddActivity: () -> Unit,
    onCompleteReminder: (MaintenanceReminder) -> Unit,
    onCreateRecommendationReminder: (MaintenanceReminderCreateRequest) -> Unit,
    onClearNotice: () -> Unit,
) {
    var section by rememberSaveable { mutableStateOf("Riwayat") }
    var categoryFilter by rememberSaveable { mutableStateOf("Semua") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val uriHandler = LocalUriHandler.current
    var showingReference by remember { mutableStateOf(false) }
    if (showingReference) AlertDialog(
        onDismissRequest = { showingReference = false },
        title = { Text("Acuan perawatan PCX 160") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Buku PCX K1Z halaman 80–81: setelah servis awal, oli mesin 6.000 km/6 bulan; busi 12.000 km/12 bulan; filter udara 18.000 km/18 bulan; drive belt 24.000 km/24 bulan; oli gardan dan minyak rem 24.000 km/2 tahun; coolant 36.000 km/3 tahun.")
            Text("Ini acuan pabrikan. Kondisi pemakaian dapat membutuhkan perawatan lebih cepat. Untuk oli 2.000 km, pilih target pribadi saat membuat pengingat. Interval khusus juga bisa disimpan saat mencatat pekerjaan.")
            Text("Target dihitung dari pekerjaan terakhir yang sesuai. Tanpa riwayat, target awal perlu dikonfirmasi. Batas kilometer atau waktu yang tercapai lebih dulu berlaku.")
        } },
        confirmButton = { TextButton(onClick = { uriHandler.openUri("https://www.wahanahonda.com/assets/upload/buku_manual/honda-pcx.pdf") }) { Text("Buku PCX") } },
        dismissButton = { TextButton(onClick = { showingReference = false }) { Text("Tutup") } },
    )
    AppBackground {
        if (state.isLoading) {
            AppLoadingState()
        } else {
            val activeVehicle = state.activeVehicle
            val estimates = remember(activeVehicle, state.activities) {
                activeVehicle?.let { vehicle ->
                    MaintenanceRecommendationEngine.recommendationsFor(
                        vehicle = vehicle,
                        activeReminders = emptyList(),
                        history = state.activities,
                    )
                }.orEmpty()
            }
            val recommendations = remember(estimates, state.reminders) { estimates.filterNot { estimate -> state.reminders.any { it.title.equals(estimate.title, ignoreCase = true) } } }
            val visibleActivities = remember(state.activities, categoryFilter, searchQuery) {
                val query = searchQuery.trim()
                state.activities.filter { activity ->
                    (categoryFilter == "Semua" || activity.category?.label == categoryFilter) &&
                        (query.isBlank() || listOfNotNull(activity.title, activity.location, activity.description,
                            activity.workItems.joinToString(" ") { "${it.component.label} ${it.description}" })
                            .any { it.contains(query, ignoreCase = true) })
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Dimens.SpaceLg,
                    top = Dimens.SpaceLg,
                    end = Dimens.SpaceLg,
                    bottom = Dimens.SpaceLg,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
            state.notice?.let { notice ->
                item {
                    NoticeCard(
                        message = notice,
                        onDismiss = onClearNotice,
                    )
                }
            }
            state.errorMessage?.let { error ->
                item {
                    NoticeCard(
                        message = error,
                        onDismiss = onClearNotice,
                    )
                }
            }

            if (!state.isLoading && activeVehicle == null) {
                item {
                    EmptyPrompt(
                        icon = Icons.Rounded.DirectionsCar,
                        title = "Pilih kendaraan dulu",
                        message = "Reminder dan aktivitas servis membutuhkan kendaraan aktif.",
                        actionLabel = "Tambah kendaraan",
                        onAction = onAddVehicle,
                    )
                }
            } else if (activeVehicle != null) {
                item {
                    ActiveServiceVehicleCard(
                        vehicle = activeVehicle,
                        onAddReminder = onAddReminder,
                        onAddActivity = onAddActivity,
                    )
                }
                item {
                    FilterOptions(listOf("Riwayat", "Jadwal"), section, { it }, { section = it }, Modifier.fillMaxWidth())
                }
                if (section == "Jadwal") {
                    item {
                        PanelCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("Interval & acuan", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                    Text(if (MaintenanceRecommendationEngine.isPcx160(activeVehicle)) "PCX 160 • bisa disesuaikan" else "Gunakan buku kendaraanmu",
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (MaintenanceRecommendationEngine.isPcx160(activeVehicle)) IconButton(onClick = { showingReference = true }) {
                                    Icon(Icons.Rounded.Info, "Acuan & pilihan interval", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                if (recommendations.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Rekomendasi perawatan")
                    }
                    items(
                        items = recommendations,
                        key = { recommendation -> recommendation.title },
                    ) { recommendation ->
                        RecommendationItem(
                            recommendation = recommendation,
                            currentOdometerKm = activeVehicle.odometerKm,
                            onCreateReminder = {
                                onCreateRecommendationReminder(
                                    recommendation.toReminderRequest(vehicleId = activeVehicle.id),
                                )
                            },
                        )
                    }
                }
                item {
                    SectionHeader(
                        title = "Pengingatmu",
                        actionLabel = "Tambah",
                        onAction = onAddReminder,
                    )
                }
                if (state.reminders.isEmpty()) {
                    item {
                        PanelCard {
                            Text(
                                text = "Belum ada pengingat",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                } else {
                    items(
                        items = state.reminders,
                        key = { reminder -> reminder.id },
                    ) { reminder ->
                        ReminderItem(
                            reminder = reminder,
                            onCompleteReminder = { onCompleteReminder(reminder) },
                        )
                    }
                }
                }
                if (section == "Riwayat") {
                item {
                    OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true,
                        label = { Text("Cari riwayat servis") },
                        leadingIcon = { Icon(Icons.Rounded.Search, null) },
                        trailingIcon = { if (searchQuery.isNotBlank()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Rounded.Close, "Hapus pencarian") } },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant))
                }

                if (searchQuery.isBlank() && categoryFilter == "Semua") {
                    val next = estimates.firstOrNull { it.dueOdometerKm != null || it.dueDate != null }
                    if (next != null) {
                        item {
                            PanelCard {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Estimasi • ${next.title}", style = MaterialTheme.typography.titleSmall)
                                        Text(listOfNotNull(next.dueOdometerKm?.let(Formatters::odometer), next.dueDate?.let(Formatters::date)).joinToString(" • "),
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (next.lastActivityId != null && (next.dueDate?.let { !it.isAfter(LocalDate.now()) } == true || next.dueOdometerKm?.let { it <= activeVehicle.odometerKm } == true)) {
                                        StatusPill("Jatuh tempo", containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer)
                                    }
                                }
                                androidx.compose.material3.OutlinedButton(onClick = { section = "Jadwal" }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) { Text("Lihat jadwal") }
                            }
                        }
                    }
                }
                item { FilterOptions(listOf("Semua") + ActivityCategory.entries.map { it.label }, categoryFilter, { it }, { categoryFilter = it }, Modifier.fillMaxWidth()) }
                item {
                    SectionHeader(
                        title = "Riwayat perawatan",
                        actionLabel = "Catat",
                        onAction = onAddActivity,
                    )
                }
                if (visibleActivities.isEmpty()) {
                    item {
                        PanelCard {
                            Text(
                                text = "Belum ada catatan untuk kategori ini",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                } else {
                    items(
                        items = visibleActivities,
                        key = { activity -> activity.id },
                    ) { activity ->
                        ActivityItem(activity = activity, estimates = estimates)
                    }
                }
                }
            }
            }
        }
    }
}

@Composable
private fun ActiveServiceVehicleCard(
    vehicle: Vehicle,
    onAddReminder: () -> Unit,
    onAddActivity: () -> Unit,
) {
    PanelCard(containerColor = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(vehicle.vehicleType.icon(), null, containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.primary)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
            ) {
                Text(
                    text = vehicle.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${vehicle.licensePlate} • ${Formatters.odometer(vehicle.odometerKm)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(Dimens.SpaceMd))
        AppActionRow { actionModifier ->
            androidx.compose.material3.OutlinedButton(
                modifier = actionModifier,
                onClick = onAddReminder,
                shape = MaterialTheme.shapes.small,
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                Text(text = "Pengingat")
            }
            Button(onClick = onAddActivity, modifier = actionModifier, shape = MaterialTheme.shapes.small) {
                Text(text = "Catat aktivitas")
            }
        }
    }
}

@Composable
private fun RecommendationItem(
    recommendation: MaintenanceRecommendation,
    currentOdometerKm: Long,
    onCreateReminder: () -> Unit,
) {
    var expanded by rememberSaveable(recommendation.title) { mutableStateOf(false) }
    val overdue = recommendation.lastActivityId != null &&
        (recommendation.dueDate?.let { !it.isAfter(LocalDate.now()) } == true ||
            recommendation.dueOdometerKm?.let { it <= currentOdometerKm } == true)
    PanelCard(containerColor = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            IconBadge(if (recommendation.type == ReminderType.OilChange) Icons.Rounded.Speed else Icons.Rounded.Build, null)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(recommendation.title, style = MaterialTheme.typography.titleMedium)
                Text(if (recommendation.lastActivityId == null) "Konfirmasi riwayat & target" else if (overdue) "Jatuh tempo • perlu perhatian" else "Target perawatan berikutnya",
                    style = MaterialTheme.typography.bodySmall, color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            recommendation.dueOdometerKm?.let { StatusPill(Formatters.odometer(it)) }
            recommendation.dueDate?.let { StatusPill(Formatters.date(it)) }
        }
        if (expanded) Text(recommendation.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        AppActionRow { actionModifier ->
            androidx.compose.material3.OutlinedButton(onClick = { expanded = !expanded }, modifier = actionModifier, shape = MaterialTheme.shapes.small) {
                Text(if (expanded) "Tutup detail" else "Detail interval")
            }
            FilledTonalButton(onClick = onCreateReminder, modifier = actionModifier, shape = MaterialTheme.shapes.small,
                enabled = recommendation.dueOdometerKm != null || recommendation.dueDate != null) { Text("Buat pengingat") }
        }
    }
}

@Composable
private fun ReminderItem(
    reminder: MaintenanceReminder,
    onCompleteReminder: () -> Unit,
) {
    PanelCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            verticalAlignment = Alignment.Top,
        ) {
            IconBadge(
                imageVector = Icons.Rounded.Event,
                contentDescription = null,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = reminder.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                StatusPill(text = reminder.type.displayLabel())
                Text(
                    text = reminder.dueLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onCompleteReminder) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Selesai",
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun ActivityItem(activity: ActivityLog, estimates: List<MaintenanceRecommendation>) {
    var expanded by rememberSaveable(activity.id) { mutableStateOf(false) }
    PanelCard {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StatusPill(activity.category?.label ?: "Catatan lama", containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
        }
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
        Text(text = "Lokasi: ${activity.location ?: "Belum dicatat"}", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.surfaceVariant) {
            FlowRow(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(activity.timeLabel(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(activity.odometerKm?.let(Formatters::odometer) ?: "KM belum dicatat", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                activity.costAmount?.let { Text(Formatters.currency(it), style = MaterialTheme.typography.titleSmall) }
            }
        }
        if (activity.workItems.isNotEmpty()) {
            Text(text = activity.workItems.joinToString(" • ") { it.component.label },
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            androidx.compose.material3.OutlinedButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) {
                Text(if (expanded) "Tutup rincian" else "Rincian pekerjaan (${activity.workItems.size})")
            }
        }
        if (expanded || activity.workItems.isEmpty()) {
        activity.description?.let { description ->
            Text(
                text = description,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        activity.workItems.forEach { item ->
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
                Text(text = "${item.component.label} • ${item.action.label}", style = MaterialTheme.typography.titleSmall)
                if (item.description.isNotBlank()) Text(text = item.description, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                estimates.firstOrNull { it.component == item.component && it.lastActivityId == activity.id }?.let { estimate ->
                    val targets = listOfNotNull(estimate.dueOdometerKm?.let(Formatters::odometer), estimate.dueDate?.let(Formatters::date))
                    Text(text = "Berikutnya: ${targets.joinToString(" atau ").ifBlank { "lengkapi KM/tanggal" }}",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        }
        }
    }
}

@Composable
private fun NoticeCard(
    message: String,
    onDismiss: () -> Unit,
) {
    PanelCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBadge(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            TextButton(onClick = onDismiss) {
                Text(text = "Tutup")
            }
        }
    }
}

@Composable
internal fun AddReminderDialog(
    isSaving: Boolean,
    saveError: String?,
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onSubmit: (MaintenanceReminderCreateRequest) -> Unit,
    initialRequest: MaintenanceReminderCreateRequest? = null,
    lastOilReading: Long? = null,
) {
    var type by rememberSaveable { mutableStateOf(initialRequest?.type ?: ReminderType.Service) }
    var title by rememberSaveable { mutableStateOf(initialRequest?.title ?: type.defaultTitle()) }
    var dueDate by rememberSaveable { mutableStateOf(if (initialRequest != null) initialRequest.dueDate?.toString().orEmpty() else LocalDate.now().plusMonths(1).toString()) }
    var dueOdometer by rememberSaveable { mutableStateOf(initialRequest?.dueOdometerKm?.toString().orEmpty()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AppFormDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Tambah pengingat") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            ) {
                validationError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                saveError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
                Text(
                    text = vehicle.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (initialRequest != null) {
                    Text("Periksa target sebelum menyimpan. KM dan tanggal dapat diubah sesuai pemakaian.", style = MaterialTheme.typography.bodySmall)
                    if (initialRequest.title.contains("Oli mesin", ignoreCase = true) && lastOilReading != null) {
                        Text("Oli terakhir: ${Formatters.odometer(lastOilReading)}", style = MaterialTheme.typography.bodySmall)
                        AppActionRow { actionModifier ->
                            androidx.compose.material3.OutlinedButton(modifier = actionModifier, shape = MaterialTheme.shapes.small, onClick = { dueOdometer = (lastOilReading + 2_000).toString(); dueDate = "" }) { Text("Pilihan 2.000 km") }
                            androidx.compose.material3.OutlinedButton(modifier = actionModifier, shape = MaterialTheme.shapes.small, onClick = { dueOdometer = initialRequest.dueOdometerKm?.toString().orEmpty(); dueDate = initialRequest.dueDate?.toString().orEmpty() }) { Text("Acuan pabrikan") }
                        }
                        Text("Pilihan 2.000 km memakai batas KM saja; tambahkan tanggal jika diperlukan.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                ChoiceRow(
                    values = ReminderType.entries,
                    selected = type,
                    label = ReminderType::displayLabel,
                    onSelected = {
                        type = it
                        title = it.defaultTitle()
                    },
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = "Judul") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                DateField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = "Tanggal jatuh tempo",
                    enabled = !isSaving,
                    optional = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = dueOdometer,
                    onValueChange = { dueOdometer = it.filter(Char::isDigit) },
                    label = { Text(text = "Odometer jatuh tempo") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    val request = buildReminderRequest(
                        vehicleId = vehicle.id,
                        type = type,
                        title = title,
                        dueDate = dueDate,
                        dueOdometer = dueOdometer,
                        onError = { validationError = it },
                    ) ?: return@Button
                    onSubmit(request)
                },
            ) {
                Text(text = if (isSaving) "Menyimpan…" else "Simpan pengingat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(text = "Batal")
            }
        },
    )
}

@Composable
internal fun AddActivityDialog(
    isSaving: Boolean,
    saveError: String?,
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onSubmit: (ActivityLogCreateRequest) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("Servis rutin") }
    var description by rememberSaveable { mutableStateOf("") }
    var cost by rememberSaveable { mutableStateOf("") }
    var categoryName by rememberSaveable { mutableStateOf(ActivityCategory.Service.name) }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var odometer by rememberSaveable { mutableStateOf(vehicle.odometerKm.toString()) }
    var location by rememberSaveable { mutableStateOf("") }
    var workJson by rememberSaveable { mutableStateOf("[]") }
    val workItems = remember(workJson) { MaintenanceWorkItemJson.decode(workJson) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AppFormDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Catat perawatan") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            ) {
                validationError?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                saveError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
                Text(
                    text = vehicle.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = "Judul") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(text = "Kategori", style = MaterialTheme.typography.labelLarge)
                ChoiceRow(values = ActivityCategory.entries, selected = ActivityCategory.valueOf(categoryName),
                    label = { it.label }, onSelected = { categoryName = it.name })
                DateField(value = date, onValueChange = { date = it }, label = "Tanggal perawatan",
                    modifier = Modifier.fillMaxWidth(), enabled = !isSaving)
                OutlinedTextField(value = odometer, onValueChange = { odometer = it.filter(Char::isDigit) },
                    label = { Text("KM saat perawatan") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), enabled = !isSaving)
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Lokasi / bengkel") },
                    modifier = Modifier.fillMaxWidth(), enabled = !isSaving)
                MaintenanceWorkEditor(items = workItems, enabled = !isSaving,
                    onChange = { workJson = MaintenanceWorkItemJson.encode(it) })
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = "Catatan tambahan") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it.filter(Char::isDigit) },
                    label = { Text(text = "Biaya") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isSaving,
                onClick = {
                    if (title.isBlank()) {
                        validationError = "Judul aktivitas wajib diisi."
                        return@Button
                    }
                    if (cost.isNotBlank() && cost.toLongOrNull() == null) {
                        validationError = "Biaya terlalu besar. Periksa kembali nilainya."
                        return@Button
                    }
                    val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
                    if (parsedDate == null || parsedDate.isAfter(LocalDate.now())) {
                        validationError = "Isi tanggal valid, paling lambat hari ini."
                        return@Button
                    }
                    if (odometer.isNotBlank() && odometer.toLongOrNull() == null) {
                        validationError = "KM tidak valid."
                        return@Button
                    }
                    if (workItems.isEmpty()) {
                        validationError = "Tambahkan minimal satu komponen pekerjaan."
                        return@Button
                    }
                    onSubmit(
                        ActivityLogCreateRequest(
                            vehicleId = vehicle.id,
                            title = title,
                            description = description,
                            costAmount = cost.takeIf { it.isNotBlank() }?.toLongOrNull(),
                            category = ActivityCategory.valueOf(categoryName),
                            odometerKm = odometer.toLongOrNull(),
                            location = location,
                            occurredAt = parsedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                            workItems = workItems,
                        ),
                    )
                },
            ) {
                Text(text = if (isSaving) "Menyimpan…" else "Simpan aktivitas")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(text = "Batal")
            }
        },
    )
}

@Composable
internal fun <T> ChoiceRow(
    values: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
    ) {
        values.forEach { value ->
            FilterChip(
                selected = value == selected,
                onClick = { onSelected(value) },
                label = { Text(text = label(value)) },
            )
        }
    }
}

private fun buildReminderRequest(
    vehicleId: Long,
    type: ReminderType,
    title: String,
    dueDate: String,
    dueOdometer: String,
    onError: (String) -> Unit,
): MaintenanceReminderCreateRequest? {
    if (title.isBlank()) {
        onError("Judul reminder wajib diisi.")
        return null
    }

    val parsedDate = if (dueDate.isBlank()) {
        null
    } else {
        runCatching { LocalDate.parse(dueDate) }.getOrNull()
    }
    if (dueDate.isNotBlank() && parsedDate == null) {
        onError("Format tanggal harus yyyy-mm-dd.")
        return null
    }

    val parsedOdometer = dueOdometer.takeIf { it.isNotBlank() }?.toLongOrNull()
    if (parsedDate == null && parsedOdometer == null) {
        onError("Isi tanggal atau odometer jatuh tempo.")
        return null
    }

    return MaintenanceReminderCreateRequest(
        vehicleId = vehicleId,
        type = type,
        title = title,
        dueDate = parsedDate,
        dueOdometerKm = parsedOdometer,
    )
}

private fun MaintenanceRecommendation.toReminderRequest(vehicleId: Long): MaintenanceReminderCreateRequest =
    MaintenanceReminderCreateRequest(
        vehicleId = vehicleId,
        type = type,
        title = title,
        dueDate = dueDate,
        dueOdometerKm = dueOdometerKm,
    )

private fun Vehicle.displayName(): String = listOf(brand, model)
    .filter { it.isNotBlank() }
    .joinToString(separator = " ")
    .ifBlank { "Kendaraan" }

private fun VehicleType.icon(): androidx.compose.ui.graphics.vector.ImageVector = when (this) {
    VehicleType.Car -> Icons.Rounded.DirectionsCar
    VehicleType.Motorcycle -> Icons.Rounded.TwoWheeler
}

private fun VehicleType.displayLabel(): String = when (this) {
    VehicleType.Car -> "Mobil"
    VehicleType.Motorcycle -> "Motor"
}

private fun MaintenanceReminder.dueLabel(): String {
    return listOfNotNull(dueOdometerKm?.let(Formatters::odometer), dueDate?.let(Formatters::date))
        .joinToString(" atau ").ifBlank { "Jadwal belum ditentukan" }
}

private fun ActivityLog.timeLabel(): String {
    val formatter = DateTimeFormatter
        .ofPattern("dd MMM yyyy", Locale.Builder().setLanguage("id").setRegion("ID").build())
    return occurredAt
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

private fun ReminderType.displayLabel(): String = when (this) {
    ReminderType.Service -> "Servis"
    ReminderType.OilChange -> "Oli"
    ReminderType.Tax -> "Pajak"
    ReminderType.Registration -> "STNK"
    ReminderType.Insurance -> "Asuransi"
    ReminderType.Custom -> "Custom"
}

private fun ReminderType.defaultTitle(): String = when (this) {
    ReminderType.Service -> "Servis rutin"
    ReminderType.OilChange -> "Ganti oli"
    ReminderType.Tax -> "Bayar pajak"
    ReminderType.Registration -> "Perpanjang STNK"
    ReminderType.Insurance -> "Perpanjang asuransi"
    ReminderType.Custom -> "Pengingat baru"
}
