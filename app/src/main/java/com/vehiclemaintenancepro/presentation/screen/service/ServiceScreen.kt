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

    LaunchedEffect(addReminderRequest) {
        if (addReminderRequest > 0) {
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
        onAddReminder = { viewModel.clearNotice(); showReminderDialog = true },
        onAddActivity = { viewModel.clearNotice(); showActivityDialog = true },
        onCompleteReminder = viewModel::completeReminder,
        onCreateRecommendationReminder = { viewModel.addReminder(it) },
        onClearNotice = viewModel::clearNotice,
    )

    val activeVehicle = state.activeVehicle
    if (showReminderDialog && activeVehicle != null) {
        AddReminderDialog(
            isSaving = saveState.isSaving,
            saveError = saveState.errorMessage,
            vehicle = activeVehicle,
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
    val uriHandler = LocalUriHandler.current
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
            val visibleActivities = remember(state.activities, categoryFilter) { state.activities.filter { categoryFilter == "Semua" || it.category?.label == categoryFilter } }
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Dimens.SpaceLg,
                    top = Dimens.SpaceLg,
                    end = Dimens.SpaceLg,
                    bottom = Dimens.Space2Xl,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
            ) {
            item {
                PageHeader(
                    title = "Servis",
                    subtitle = "Catatan perawatan dan jadwal berikutnya.",
                )
            }
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
                    PrimaryTabRow(selectedTabIndex = if (section == "Riwayat") 0 else 1,
                        containerColor = MaterialTheme.colorScheme.background) {
                        listOf("Riwayat", "Jadwal").forEach { label ->
                            Tab(selected = section == label, onClick = { section = label }, text = { Text(label) })
                        }
                    }
                }
                if (section == "Jadwal") {
                    item {
                        if (MaintenanceRecommendationEngine.isPcx160(activeVehicle)) {
                            Text("Estimasi mengikuti catatan tiap komponen. KM atau tanggal yang tercapai lebih dulu menjadi batas perawatan.",
                                style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = { uriHandler.openUri("https://www.wahanahonda.com/assets/upload/buku_manual/honda-pcx.pdf") }) {
                                Text("Acuan Honda PCX • halaman 80–81")
                            }
                        } else {
                            Text("Acuan model ini belum tersedia. Atur interval tiap komponen saat mencatat perawatan sesuai buku kendaraan.",
                                style = MaterialTheme.typography.bodyMedium)
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
                item { ChoiceRow(listOf("Semua") + ActivityCategory.entries.map { it.label }, categoryFilter, { it }) { categoryFilter = it } }
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
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            androidx.compose.material3.OutlinedButton(
                onClick = onAddReminder,
                shape = MaterialTheme.shapes.small,
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(Dimens.SpaceSm))
                Text(text = "Pengingat")
            }
            Button(onClick = onAddActivity, shape = MaterialTheme.shapes.small) {
                Text(text = "Catat aktivitas")
            }
        }
    }
}

@Composable
private fun RecommendationItem(
    recommendation: MaintenanceRecommendation,
    onCreateReminder: () -> Unit,
) {
    PanelCard(containerColor = MaterialTheme.colorScheme.surface) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
            Text(text = recommendation.title, style = MaterialTheme.typography.titleMedium)
            Text(text = recommendation.description, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm), verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                recommendation.dueOdometerKm?.let { km -> StatusPill(text = Formatters.odometer(km)) }
                recommendation.dueDate?.let { date -> StatusPill(text = Formatters.date(date)) }
                if (recommendation.dueOdometerKm == null && recommendation.dueDate == null) Text("Lengkapi tanggal pekerjaan terakhir")
            }
            TextButton(onClick = onCreateReminder, enabled = recommendation.dueOdometerKm != null || recommendation.dueDate != null) { Text("Buat pengingat") }
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
            Text(activity.timeLabel(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            StatusPill(text = activity.odometerKm?.let(Formatters::odometer) ?: "KM belum dicatat",
                containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
        }
        Text(text = "Lokasi: ${activity.location ?: "Belum dicatat"}", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        activity.costAmount?.let { cost ->
            Text(Formatters.currency(cost), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        if (activity.workItems.isNotEmpty()) {
            Text(text = activity.workItems.joinToString(" • ") { it.component.label },
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { expanded = !expanded }, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
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
private fun AddReminderDialog(
    isSaving: Boolean,
    saveError: String?,
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onSubmit: (MaintenanceReminderCreateRequest) -> Unit,
) {
    var type by rememberSaveable { mutableStateOf(ReminderType.Service) }
    var title by rememberSaveable { mutableStateOf(type.defaultTitle()) }
    var dueDate by rememberSaveable { mutableStateOf(LocalDate.now().plusMonths(1).toString()) }
    var dueOdometer by rememberSaveable { mutableStateOf("") }
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
