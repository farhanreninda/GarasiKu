package com.vehiclemaintenancepro.presentation.screen.vehicles

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.LocalIndication
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vehiclemaintenancepro.core.design.Dimens
import com.vehiclemaintenancepro.core.design.MotionTokens
import com.vehiclemaintenancepro.core.utility.Formatters
import com.vehiclemaintenancepro.domain.model.FuelType
import com.vehiclemaintenancepro.domain.model.TransmissionType
import com.vehiclemaintenancepro.domain.model.Vehicle
import com.vehiclemaintenancepro.domain.model.VehicleCatalog
import com.vehiclemaintenancepro.domain.model.VehicleCreateRequest
import com.vehiclemaintenancepro.domain.model.VehicleModelSpec
import com.vehiclemaintenancepro.domain.model.VehicleType
import com.vehiclemaintenancepro.presentation.component.AppActionRow
import com.vehiclemaintenancepro.presentation.component.FilterOptions
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.vehiclemaintenancepro.presentation.component.AppBackground
import com.vehiclemaintenancepro.presentation.component.AppLoadingState
import com.vehiclemaintenancepro.presentation.component.DateField
import com.vehiclemaintenancepro.presentation.component.EmptyPrompt
import com.vehiclemaintenancepro.presentation.component.IconBadge
import com.vehiclemaintenancepro.presentation.component.PageHeader
import com.vehiclemaintenancepro.presentation.component.PanelCard
import com.vehiclemaintenancepro.presentation.component.SectionHeader
import com.vehiclemaintenancepro.presentation.component.StatusPill
import com.vehiclemaintenancepro.presentation.component.VehicleIllustration
import java.time.LocalDate
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.FlowRow
import androidx.activity.compose.BackHandler

@Composable
fun AddVehicleDialogRoute(
    onDismiss: () -> Unit,
    viewModel: VehiclesViewModel = hiltViewModel(),
) {
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    AddVehicleDialog(
        isSaving = saveState.isSaving,
        saveError = saveState.errorMessage,
        onDismiss = { if (!saveState.isSaving) { viewModel.clearNotice(); onDismiss() } },
        onSubmit = { request ->
            viewModel.addVehicle(request, onSuccess = onDismiss)
        },
    )
}

@Composable
fun VehiclesRoute(
    viewModel: VehiclesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var detailVehicleId by rememberSaveable { mutableStateOf<Long?>(null) }
    val detailVehicle = state.vehicles.firstOrNull { it.id == detailVehicleId }
    BackHandler(detailVehicle != null) { detailVehicleId = null }

    if (detailVehicle != null) {
        VehicleOwnershipScreen(detailVehicle, saveState, onBack = { detailVehicleId = null },
            onClearNotice = viewModel::clearNotice,
            onSaveDetails = { request, done -> viewModel.updateDetails(detailVehicle.id, request, done) },
            onSaveAccessory = { item, done -> viewModel.saveAccessory(detailVehicle.id, item, done) },
            onDeleteAccessory = { id, done -> viewModel.deleteAccessory(detailVehicle.id, id, done) })
    } else VehiclesScreen(
        state = state,
        onAddVehicle = { viewModel.clearNotice(); showAddDialog = true },
        onSetActiveVehicle = { viewModel.setActiveVehicle(it) },
        onArchiveVehicle = viewModel::archiveVehicle,
        onClearNotice = viewModel::clearNotice,
        onOpenDetails = { detailVehicleId = it },
    )

    if (showAddDialog) {
        AddVehicleDialog(
            isSaving = saveState.isSaving,
            saveError = saveState.errorMessage,
            onDismiss = { if (!saveState.isSaving) showAddDialog = false },
            onSubmit = { request ->
                viewModel.addVehicle(request) { showAddDialog = false }
            },
        )
    }
}

@Composable
fun VehiclesScreen(
    state: VehiclesUiState,
    onAddVehicle: () -> Unit,
    onSetActiveVehicle: (Long) -> Unit,
    onArchiveVehicle: (Long) -> Unit,
    onClearNotice: () -> Unit,
    onOpenDetails: (Long) -> Unit = {},
) {
    var typeFilter by rememberSaveable { mutableStateOf("Semua") }
    val visibleVehicles = remember(state.vehicles, typeFilter) {
        state.vehicles.filter { typeFilter == "Semua" || it.vehicleType.name == typeFilter }
    }
    AppBackground {
        if (state.isLoading) {
            AppLoadingState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = Dimens.SpaceLg,
                    top = Dimens.SpaceLg,
                    end = Dimens.SpaceLg,
                    bottom = Dimens.SpaceLg,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    PageHeader(
                        title = "Garasi saya",
                        subtitle = "Kelola mobil & motor yang kamu pantau.",
                        actionLabel = "Tambah",
                        onAction = onAddVehicle,
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
                if (state.vehicles.isEmpty()) {
                    item {
                        EmptyPrompt(
                            icon = Icons.Rounded.DirectionsCar,
                            title = "Belum ada kendaraan",
                            message = "Tambahkan mobil atau motor pertama untuk mulai memantau servis.",
                            actionLabel = "Tambah kendaraan",
                            onAction = onAddVehicle,
                        )
                    }
                } else {
                    item {
                        FilterOptions(listOf("Semua") + VehicleType.entries.map { it.name }, typeFilter,
                            label = { key ->
                                val type = VehicleType.entries.firstOrNull { it.name == key }
                                if (type == null) "Semua (${state.vehicles.size})"
                                else "${type.displayLabel()} (${state.vehicles.count { it.vehicleType == type }})"
                            }, onSelected = { typeFilter = it }, modifier = Modifier.fillMaxWidth())
                    }
                    if (visibleVehicles.isEmpty()) {
                        item { PanelCard { Text("Tidak ada kendaraan untuk pilihan ini", style = MaterialTheme.typography.titleMedium) } }
                    }
                    items(
                        items = visibleVehicles,
                        key = { vehicle -> vehicle.id },
                    ) { vehicle ->
                        VehicleListItem(
                            vehicle = vehicle,
                            onSetActiveVehicle = { onSetActiveVehicle(vehicle.id) },
                            onArchiveVehicle = { onArchiveVehicle(vehicle.id) },
                            onOpenDetails = { onOpenDetails(vehicle.id) },
                        )
                    }
                    item {
                        PanelCard {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconBadge(Icons.Rounded.DirectionsCar, null)
                                Column(Modifier.weight(1f)) {
                                    Text("Punya kendaraan lain?", style = MaterialTheme.typography.titleSmall)
                                    Text("Simpan riwayatnya di garasi.", style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Button(onClick = onAddVehicle, modifier = Modifier.fillMaxWidth()) { Text("Tambah kendaraan") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleListItem(
    vehicle: Vehicle,
    onSetActiveVehicle: () -> Unit,
    onArchiveVehicle: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    var showHideConfirmation by remember { mutableStateOf(false) }
    var expanded by rememberSaveable(vehicle.id) { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    if (showHideConfirmation) {
        AlertDialog(
            onDismissRequest = { showHideConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.VisibilityOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            title = { Text(text = "Sembunyikan kendaraan?") },
            text = {
                Text(
                    text = "Kendaraan akan keluar dari daftar aktif. Data servis dan riwayatnya tetap tersimpan.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showHideConfirmation = false
                        onArchiveVehicle()
                    },
                ) {
                    Text(text = "Sembunyikan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHideConfirmation = false }) {
                    Text(text = "Batal")
                }
            },
        )
    }

    PanelCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                IconBadge(imageVector = vehicle.vehicleType.icon(), contentDescription = null,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(vehicle.displayName(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(vehicle.licensePlate, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                if (vehicle.isActive) StatusPill("Dipilih", containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                StatusPill(text = vehicle.vehicleType.displayLabel())
                vehicle.year?.let { StatusPill(text = it.toString()) }
            }
            Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
                FlowRow(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Odometer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Formatters.odometer(vehicle.odometerKm), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Transmisi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(vehicle.transmissionType.displayLabel(), style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            if (expanded) {
                Text("BBM: ${when (vehicle.fuelType) { FuelType.Gasoline -> "Bensin"; FuelType.Diesel -> "Diesel"; FuelType.Electric -> "Listrik"; FuelType.Hybrid -> "Hybrid"; FuelType.Lpg -> "LPG"; FuelType.Other -> "Lainnya"; FuelType.Unknown -> "Belum dicatat" }}", style = MaterialTheme.typography.bodyMedium)
                vehicle.color?.takeIf { it.isNotBlank() }?.let { Text("Warna: $it", style = MaterialTheme.typography.bodyMedium) }
                vehicle.note?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${vehicle.accessories.size} aksesori", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    androidx.compose.material3.OutlinedButton(onClick = onOpenDetails, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) { Text("Pembelian & aksesori") }
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) { Icon(Icons.Rounded.MoreVert, "Opsi kendaraan") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (!vehicle.isActive) DropdownMenuItem(text = { Text("Tampilkan") },
                            onClick = { menuOpen = false; onSetActiveVehicle() })
                        DropdownMenuItem(text = { Text(if (expanded) "Tutup detail" else "Detail kendaraan") },
                            onClick = { menuOpen = false; expanded = !expanded })
                        DropdownMenuItem(text = { Text("Sembunyikan") },
                            onClick = { menuOpen = false; showHideConfirmation = true })
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
private fun AddVehicleDialog(
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSubmit: (VehicleCreateRequest) -> Unit,
) {
    var vehicleType by rememberSaveable { mutableStateOf(VehicleType.Car) }
    var brand by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var licensePlate by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var odometer by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var taxDueDate by rememberSaveable { mutableStateOf("") }
    var registrationDueDate by rememberSaveable { mutableStateOf("") }
    var carTransmissionType by rememberSaveable { mutableStateOf<TransmissionType?>(null) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val brands = VehicleCatalog.brandsFor(vehicleType)
    val selectedBrand = brands.firstOrNull { it.name.equals(brand, ignoreCase = true) }
    val modelOptions = selectedBrand?.models.orEmpty()
    val automaticSpec = remember(vehicleType, brand, model) {
        VehicleCatalog.specFor(
            vehicleType = vehicleType,
            brand = brand,
            model = model,
        )
    }
    val selectedSpec = automaticSpec ?: if (
        vehicleType == VehicleType.Car &&
        model.isNotBlank() &&
        carTransmissionType != null
    ) {
        VehicleModelSpec(
            transmissionType = carTransmissionType ?: TransmissionType.Unknown,
            fuelType = FuelType.Gasoline,
        )
    } else {
        null
    }

    fun submitVehicle() {
        val request = buildVehicleRequest(
            vehicleType = vehicleType,
            brand = brand,
            model = model,
            licensePlate = licensePlate,
            year = year,
            odometer = odometer,
            color = color,
            note = note,
            taxDueDate = taxDueDate,
            registrationDueDate = registrationDueDate,
            vehicleSpec = selectedSpec,
            onError = { validationError = it },
        ) ?: return
        onSubmit(request)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .heightIn(max = maxHeight.coerceAtMost(690.dp)),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DialogHeader(onDismiss = onDismiss)
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
                    ) {
                        validationError?.let { error ->
                            ValidationNotice(message = error)
                        }
                        saveError?.let { ValidationNotice(message = it) }
                        VehiclePreviewPanel(
                            vehicleType = vehicleType,
                            brand = brand,
                            model = model,
                            licensePlate = licensePlate,
                            automaticSpec = selectedSpec,
                        )
                        InputSection(title = "Jenis kendaraan") {
                            VehicleTypeSelector(
                                selected = vehicleType,
                                onSelected = {
                                    vehicleType = it
                                    brand = ""
                                    model = ""
                                    carTransmissionType = null
                                },
                            )
                        }
                        InputSection(title = "Data utama") {
                            ChoicePickerField(
                                value = brand,
                                label = "Merek",
                                options = brands.map { it.name },
                                leadingIcon = vehicleType.icon(),
                                onSelected = {
                                    brand = it
                                    model = ""
                                    carTransmissionType = null
                                },
                            )
                            ChoicePickerField(
                                value = model,
                                label = "Tipe / model",
                                options = modelOptions,
                                leadingIcon = Icons.Rounded.Info,
                                enabled = brand.isNotBlank(),
                                onSelected = {
                                    model = it
                                    carTransmissionType = null
                                },
                            )
                            if (vehicleType == VehicleType.Car && automaticSpec == null) {
                                ChoicePickerField(
                                    value = carTransmissionType?.displayLabel().orEmpty(),
                                    label = "Transmisi",
                                    options = listOf(
                                        TransmissionType.Automatic.displayLabel(),
                                        TransmissionType.Manual.displayLabel(),
                                    ),
                                    leadingIcon = Icons.Rounded.Speed,
                                    enabled = model.isNotBlank(),
                                    onSelected = { selected ->
                                        carTransmissionType = when (selected) {
                                            TransmissionType.Manual.displayLabel() -> TransmissionType.Manual
                                            else -> TransmissionType.Automatic
                                        }
                                    },
                                )
                            }
                            FormTextField(
                                value = licensePlate,
                                onValueChange = { licensePlate = it.uppercase() },
                                label = "Nomor polisi",
                                leadingIcon = Icons.Rounded.DirectionsCar,
                            )
                        }
                        InputSection(title = "Detail kendaraan") {
                            VehicleDetailFields(
                                year = year,
                                onYearChange = { year = it.filter(Char::isDigit).take(4) },
                                odometer = odometer,
                                onOdometerChange = { odometer = it.filter(Char::isDigit) },
                            )
                            FormTextField(
                                value = color,
                                onValueChange = { color = it },
                                label = "Warna",
                                leadingIcon = Icons.Rounded.ColorLens,
                            )
                        }
                        InputSection(title = "Pajak dan STNK") {
                            DocumentReminderFields(
                                taxDueDate = taxDueDate,
                                onTaxDueDateChange = { taxDueDate = it },
                                registrationDueDate = registrationDueDate,
                                onRegistrationDueDateChange = { registrationDueDate = it },
                            )
                        }
                        selectedSpec?.let { spec ->
                            InputSection(title = "Spesifikasi otomatis") {
                                AutoSpecPanel(spec = spec)
                            }
                        }
                        InputSection(title = "Catatan") {
                            NoteTextField(
                                value = note,
                                onValueChange = { note = it },
                            )
                        }
                    }
                    DialogActions(
                        isSaving = isSaving,
                        onDismiss = onDismiss,
                        onSubmit = ::submitVehicle,
                    )
                }
            }
        }
    }
}

@Composable
private fun DialogHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, top = 20.dp, end = 14.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "Tambah kendaraan",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Lengkapi profil kendaraan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Tutup",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun DialogActions(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    AppActionRow(Modifier.padding(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 20.dp)) { actionModifier ->
        TextButton(onClick = onDismiss, enabled = !isSaving, modifier = actionModifier) {
            Text(text = "Batal")
        }
        Button(
            enabled = !isSaving,
            onClick = onSubmit,
            modifier = actionModifier,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(text = if (isSaving) "Menyimpan…" else "Simpan kendaraan")
        }
    }
}

@Composable
private fun ValidationNotice(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
            .padding(Dimens.SpaceMd),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun VehiclePreviewPanel(
    vehicleType: VehicleType,
    brand: String,
    model: String,
    licensePlate: String,
    automaticSpec: VehicleModelSpec?,
) {
    val title = listOf(brand, model)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Kendaraan baru" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(Dimens.SpaceMd),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(vehicleType.icon(), null, containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.primary)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = licensePlate.ifBlank { vehicleType.displayLabel() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            automaticSpec?.let { spec ->
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs)) {
                    CompactSpecPill(text = spec.transmissionType.displayLabel())
                }
            }
        }
    }
}

@Composable
private fun CompactSpecPill(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.74f))
            .padding(horizontal = Dimens.SpaceSm, vertical = 3.dp),
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 13.sp),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun InputSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        content()
    }
}

@Composable
private fun VehicleTypeSelector(
    selected: VehicleType,
    onSelected: (VehicleType) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
    ) {
        VehicleType.entries.forEach { vehicleType ->
            FormChoiceChip(
                label = vehicleType.displayLabel(),
                selected = selected == vehicleType,
                icon = vehicleType.icon(),
                onClick = { onSelected(vehicleType) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
            )
        }
    }
}

@Composable
private fun VehicleDetailFields(
    year: String,
    onYearChange: (String) -> Unit,
    odometer: String,
    onOdometerChange: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth < 360.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                YearField(value = year, onValueChange = onYearChange)
                OdometerField(value = odometer, onValueChange = onOdometerChange)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                YearField(
                    value = year,
                    onValueChange = onYearChange,
                    modifier = Modifier.weight(1f),
                )
                OdometerField(
                    value = odometer,
                    onValueChange = onOdometerChange,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DocumentReminderFields(
    taxDueDate: String,
    onTaxDueDateChange: (String) -> Unit,
    registrationDueDate: String,
    onRegistrationDueDateChange: (String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val useStackedLayout = maxWidth < 390.dp
        if (useStackedLayout) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                TaxDueDateField(value = taxDueDate, onValueChange = onTaxDueDateChange)
                RegistrationDueDateField(value = registrationDueDate, onValueChange = onRegistrationDueDateChange)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                TaxDueDateField(
                    value = taxDueDate,
                    onValueChange = onTaxDueDateChange,
                    modifier = Modifier.weight(1f),
                )
                RegistrationDueDateField(
                    value = registrationDueDate,
                    onValueChange = onRegistrationDueDateChange,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TaxDueDateField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    DateField(
        value = value,
        onValueChange = { onValueChange(it.take(10)) },
        label = "Jatuh tempo pajak",
        optional = true,
        modifier = modifier,
    )
}

@Composable
private fun RegistrationDueDateField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    DateField(
        value = value,
        onValueChange = { onValueChange(it.take(10)) },
        label = "Jatuh tempo STNK",
        optional = true,
        modifier = modifier,
    )
}

@Composable
private fun YearField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FormTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Tahun",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        leadingIcon = Icons.Rounded.CalendarToday,
        modifier = modifier,
    )
}

@Composable
private fun OdometerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FormTextField(
        value = value,
        onValueChange = onValueChange,
        label = "Odometer",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        leadingIcon = Icons.Rounded.Speed,
        modifier = modifier,
    )
}

@Composable
private fun NoteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(18.dp)
    val borderColor = if (focused) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            lineHeight = 20.sp,
        ),
        minLines = 3,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 112.dp)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, borderColor, shape)
                    .padding(horizontal = 22.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Notes,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isBlank()) {
                        Text(
                            text = "Catatan opsional",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 16.sp,
                                lineHeight = 20.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minLines: Int = 1,
    leadingIcon: ImageVector? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 18.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 20.sp),
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (singleLine) 64.dp else 112.dp),
    )
}

@Composable
private fun ChoicePickerField(
    value: String,
    label: String,
    options: List<String>,
    leadingIcon: ImageVector,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var pickerOpen by remember { mutableStateOf(false) }
    val canExpand = enabled && options.isNotEmpty()

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(text = label) },
            readOnly = true,
            enabled = enabled,
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = if (pickerOpen) 180f else 0f
                    },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .semantics { contentDescription = "Pilih $label" }
                .clip(RoundedCornerShape(18.dp))
                .clickable(
                    enabled = canExpand,
                    role = Role.Button,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { pickerOpen = true },
                ),
        )
        if (pickerOpen && canExpand) {
            ChoicePickerDialog(
                title = label,
                options = options,
                selectedOption = value,
                leadingIcon = leadingIcon,
                onDismiss = { pickerOpen = false },
                onSelected = { option ->
                    onSelected(option)
                    pickerOpen = false
                },
            )
        }
    }
}

@Composable
private fun ChoicePickerDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    leadingIcon: ImageVector,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 10.dp,
        ) {
            Column(
                modifier = Modifier.padding(top = 18.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconBadge(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pilih $title",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${options.size} pilihan tersedia",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 15.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(options, key = { it }) { option ->
                        ChoicePickerOption(
                            option = option,
                            selected = option == selectedOption,
                            onClick = { onSelected(option) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChoicePickerOption(
    option: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.secondary
                } else {
                    Color.Transparent
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = option,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.onSecondary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSecondary,
            )
        }
    }
}

@Composable
private fun AutoSpecPanel(
    spec: VehicleModelSpec,
) {
    AutoSpecCard(
        label = "Transmisi",
        value = spec.transmissionType.displayLabel(),
        icon = Icons.Rounded.Speed,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun AutoSpecCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = 58.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = Dimens.SpaceMd),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, lineHeight = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 16.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FormChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.95f
            selected -> 1f
            else -> 0.98f
        },
        animationSpec = tween(durationMillis = MotionTokens.PressMillis, easing = FastOutSlowInEasing),
        label = "form chip scale",
    )
    val shape = RoundedCornerShape(18.dp)
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.surface
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.outline
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .widthIn(min = 82.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(backgroundColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            )
            .padding(horizontal = Dimens.SpaceMd, vertical = Dimens.SpaceSm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = contentColor,
            )
            Spacer(modifier = Modifier.width(Dimens.SpaceXs))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 16.sp),
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun buildVehicleRequest(
    vehicleType: VehicleType,
    brand: String,
    model: String,
    licensePlate: String,
    year: String,
    odometer: String,
    color: String,
    note: String,
    taxDueDate: String,
    registrationDueDate: String,
    vehicleSpec: VehicleModelSpec?,
    onError: (String) -> Unit,
): VehicleCreateRequest? {
    if (brand.isBlank() || model.isBlank() || licensePlate.isBlank()) {
        onError("Merek, model, dan nomor polisi wajib diisi.")
        return null
    }

    if (vehicleSpec == null) {
        onError("Pilih transmisi kendaraan.")
        return null
    }

    val parsedYear = year.takeIf { it.isNotBlank() }?.toIntOrNull()
    if (year.isNotBlank() && parsedYear == null) {
        onError("Tahun harus berupa angka.")
        return null
    }

    val parsedTaxDueDate = if (taxDueDate.isBlank()) {
        null
    } else {
        parseOptionalDate(
            value = taxDueDate,
            fieldName = "Tanggal pajak",
            onError = onError,
        ) ?: return null
    }

    val parsedRegistrationDueDate = if (registrationDueDate.isBlank()) {
        null
    } else {
        parseOptionalDate(
            value = registrationDueDate,
            fieldName = "Tanggal STNK",
            onError = onError,
        ) ?: return null
    }

    val parsedOdometer = if (odometer.isBlank()) 0L else odometer.toLongOrNull()
    if (parsedOdometer == null || parsedOdometer < 0L) {
        onError("Odometer tidak valid. Periksa kembali nilainya.")
        return null
    }
    return VehicleCreateRequest(
        vehicleType = vehicleType,
        brand = brand,
        model = model,
        year = parsedYear,
        licensePlate = licensePlate,
        color = color,
        transmissionType = vehicleSpec.transmissionType,
        fuelType = vehicleSpec.fuelType,
        odometerKm = parsedOdometer,
        note = note,
        taxDueDate = parsedTaxDueDate,
        registrationDueDate = parsedRegistrationDueDate,
    )
}

private fun parseOptionalDate(
    value: String,
    fieldName: String,
    onError: (String) -> Unit,
): LocalDate? {
    if (value.isBlank()) return null
    return runCatching { LocalDate.parse(value) }
        .getOrElse {
            onError("$fieldName harus format yyyy-mm-dd.")
            null
        }
}

private fun Vehicle.displayName(): String = listOf(brand, model)
    .filter { it.isNotBlank() }
    .joinToString(separator = " ")
    .ifBlank { "Kendaraan" }

private fun VehicleType.icon(): ImageVector = when (this) {
    VehicleType.Car -> Icons.Rounded.DirectionsCar
    VehicleType.Motorcycle -> Icons.Rounded.TwoWheeler
}

private fun VehicleType.displayLabel(): String = when (this) {
    VehicleType.Car -> "Mobil"
    VehicleType.Motorcycle -> "Motor"
}

private fun TransmissionType.displayLabel(): String = when (this) {
    TransmissionType.Manual -> "Manual"
    TransmissionType.Automatic -> "Otomatis"
    TransmissionType.Cvt -> "Matic"
    TransmissionType.ElectricDrive -> "Listrik"
    TransmissionType.Unknown -> "-"
}
