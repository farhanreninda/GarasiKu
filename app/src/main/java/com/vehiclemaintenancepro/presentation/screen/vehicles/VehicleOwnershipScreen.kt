package com.vehiclemaintenancepro.presentation.screen.vehicles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vehiclemaintenancepro.core.common.SaveState
import com.vehiclemaintenancepro.core.utility.Formatters
import com.vehiclemaintenancepro.domain.model.*
import com.vehiclemaintenancepro.presentation.component.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun VehicleOwnershipScreen(
    vehicle: Vehicle,
    saveState: SaveState,
    onBack: () -> Unit,
    onClearNotice: () -> Unit,
    onSaveDetails: (VehicleDetailsUpdateRequest, () -> Unit) -> Unit,
    onSaveAccessory: (VehicleAccessory, () -> Unit) -> Unit,
    onDeleteAccessory: (String, () -> Unit) -> Unit,
) {
    var section by rememberSaveable(vehicle.id) { mutableStateOf("Pembelian") }
    var editDetails by rememberSaveable { mutableStateOf(false) }
    var editAccessoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var addAccessory by rememberSaveable { mutableStateOf(false) }
    val editingAccessory = vehicle.accessories.firstOrNull { it.id == editAccessoryId }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Kembali ke garasi") }
                Column {
                    Text("${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.headlineSmall)
                    Text(vehicle.licensePlate, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            FilterOptions(listOf("Identitas", "Pembelian", "Aksesori"), section, { it }, { section = it }, Modifier.fillMaxWidth())
        }
        when (section) {
            "Identitas" -> item {
                PanelCard {
                    SectionHeader("Identitas kendaraan", actionLabel = "Ubah", onAction = { onClearNotice(); editDetails = true })
                    OwnershipValue("Nomor polisi", vehicle.licensePlate)
                    OwnershipValue("Nomor rangka", vehicle.frameNumber ?: "Belum dicatat")
                    OwnershipValue("Nomor mesin", vehicle.engineNumber ?: "Belum dicatat")
                    OwnershipValue("Tahun", vehicle.year?.toString() ?: "Belum dicatat")
                    OwnershipValue("Odometer terakhir", Formatters.odometer(vehicle.odometerKm))
                    vehicle.note?.takeIf { it.isNotBlank() }?.let {
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))
                        Text("Catatan kendaraan", style = MaterialTheme.typography.titleSmall)
                        Text(it, modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            "Pembelian" -> {
                item {
                    PanelCard {
                        SectionHeader("Harga pembelian", actionLabel = "Ubah", onAction = { onClearNotice(); editDetails = true })
                        Text(vehicle.purchasePrice?.let(Formatters::currency) ?: "Harga belum dicatat", style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 16.dp))
                        OwnershipValue("Harga OTR", vehicle.otrPrice?.let(Formatters::currency) ?: "Belum dicatat")
                        if (vehicle.otrPrice != null && vehicle.purchasePrice != null && vehicle.otrPrice >= vehicle.purchasePrice) {
                            OwnershipValue("Potongan harga", Formatters.currency(vehicle.otrPrice - vehicle.purchasePrice))
                        }
                        Text("Harga motor dipisahkan dari biaya servis dan aksesori.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
                    }
                }
                item {
                    PanelCard {
                        Text("Dealer & indent", style = MaterialTheme.typography.titleLarge)
                        OwnershipValue("Dealer", vehicle.dealer ?: "Belum dicatat")
                        OwnershipValue("Tanggal indent", vehicle.orderDate?.let(Formatters::date) ?: "Belum dicatat")
                        OwnershipValue("Motor datang", vehicle.purchaseDate?.let(Formatters::date) ?: "Belum dicatat")
                        if (vehicle.orderDate != null && vehicle.purchaseDate != null) {
                            OwnershipValue("Waktu tunggu", "${ChronoUnit.DAYS.between(vehicle.orderDate, vehicle.purchaseDate)} hari")
                        }
                    }
                }
            }
            "Aksesori" -> {
                item {
                    PanelCard {
                        Text("Total aksesori tercatat", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Formatters.currency(vehicle.accessories.sumOf { it.price ?: 0L }), style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 12.dp))
                        val unknown = vehicle.accessories.count { it.price == null }
                        Text("${vehicle.accessories.size} item • $unknown harga belum dicatat", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Harga yang belum diketahui tidak dihitung sebagai gratis.", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
                        Button(onClick = { onClearNotice(); addAccessory = true }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                            Icon(Icons.Outlined.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Tambah aksesori")
                        }
                    }
                }
                if (vehicle.accessories.isEmpty()) item { PanelCard { Text("Belum ada aksesori. Tambahkan nama dan harga jika diketahui.") } }
                items(vehicle.accessories, key = { it.id }) { item ->
                    PanelCard {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(item.name, style = MaterialTheme.typography.titleMedium)
                                Text(item.price?.let(Formatters::currency) ?: "Harga belum dicatat", style = MaterialTheme.typography.titleLarge,
                                    color = if (item.price == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary)
                                StatusPill(item.status.label)
                            }
                            IconButton(onClick = { onClearNotice(); editAccessoryId = item.id }) {
                                Icon(Icons.Outlined.Edit, "Ubah ${item.name}")
                            }
                        }
                    }
                }
            }
        }
    }
    if (editDetails) VehicleDetailsDialog(vehicle, saveState,
        onDismiss = { if (!saveState.isSaving) editDetails = false },
        onSave = { onSaveDetails(it) { editDetails = false } })
    if (addAccessory || editingAccessory != null) AccessoryDialog(editingAccessory, saveState,
        onDismiss = { if (!saveState.isSaving) { addAccessory = false; editAccessoryId = null } },
        onSave = { onSaveAccessory(it) { addAccessory = false; editAccessoryId = null } },
        onDelete = { id -> onDeleteAccessory(id) { editAccessoryId = null } })
}

@Composable
private fun OwnershipValue(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun VehicleDetailsDialog(vehicle: Vehicle, state: SaveState, onDismiss: () -> Unit, onSave: (VehicleDetailsUpdateRequest) -> Unit) {
    var engine by rememberSaveable { mutableStateOf(vehicle.engineNumber.orEmpty()) }
    var frame by rememberSaveable { mutableStateOf(vehicle.frameNumber.orEmpty()) }
    var otr by rememberSaveable { mutableStateOf(vehicle.otrPrice?.toString().orEmpty()) }
    var price by rememberSaveable { mutableStateOf(vehicle.purchasePrice?.toString().orEmpty()) }
    var dealer by rememberSaveable { mutableStateOf(vehicle.dealer.orEmpty()) }
    var order by rememberSaveable { mutableStateOf(vehicle.orderDate?.toString().orEmpty()) }
    var delivery by rememberSaveable { mutableStateOf(vehicle.purchaseDate?.toString().orEmpty()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AppFormDialog(onDismiss, title = { Text("Identitas & pembelian") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(engine, { engine = it }, label = { Text("Nomor mesin") }, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(frame, { frame = it }, label = { Text("Nomor rangka") }, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth())
            PriceField(otr, { otr = it }, "Harga OTR (Rp)", !state.isSaving)
            PriceField(price, { price = it }, "Harga pembelian (Rp)", !state.isSaving)
            OutlinedTextField(dealer, { dealer = it }, label = { Text("Dealer") }, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth())
            DateField(order, { order = it }, "Tanggal indent", optional = true, enabled = !state.isSaving)
            DateField(delivery, { delivery = it }, "Tanggal datang", optional = true, enabled = !state.isSaving)
            (error ?: state.errorMessage)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = {
        Button(enabled = !state.isSaving, onClick = {
            if ((otr.isNotBlank() && otr.toLongOrNull() == null) || (price.isNotBlank() && price.toLongOrNull() == null)) {
                error = "Harga terlalu besar atau tidak valid"
            } else {
                val request = VehicleDetailsUpdateRequest(engine, frame, otr.toLongOrNull(), price.toLongOrNull(), dealer,
                    order.takeIf { it.isNotBlank() }?.let(LocalDate::parse), delivery.takeIf { it.isNotBlank() }?.let(LocalDate::parse))
                val validation = runCatching { request.validate() }.exceptionOrNull()
                if (validation != null) error = validation.message else { error = null; onSave(request) }
            }
        }) { Text(if (state.isSaving) "Menyimpan…" else "Simpan pembelian") }
    }, dismissButton = { TextButton(onClick = onDismiss, enabled = !state.isSaving) { Text("Batal") } })
}

@Composable
private fun AccessoryDialog(item: VehicleAccessory?, state: SaveState, onDismiss: () -> Unit, onSave: (VehicleAccessory) -> Unit, onDelete: (String) -> Unit) {
    val id = rememberSaveable { item?.id ?: java.util.UUID.randomUUID().toString() }
    var name by rememberSaveable { mutableStateOf(item?.name.orEmpty()) }
    var price by rememberSaveable { mutableStateOf(item?.price?.toString().orEmpty()) }
    var status by rememberSaveable { mutableStateOf(item?.status ?: AccessoryStatus.Unspecified) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    AppFormDialog(onDismiss, title = { Text(if (item == null) "Tambah aksesori" else "Ubah aksesori") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Nama aksesori") }, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth())
            PriceField(price, { price = it }, "Harga aksesori (Rp)", !state.isSaving)
            Text("Kosongkan harga jika belum diketahui.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SelectionField(status.label, AccessoryStatus.entries.map { it.label }, "Status", !state.isSaving) { label -> status = AccessoryStatus.entries.first { it.label == label } }
            (error ?: state.errorMessage)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (item != null) OutlinedButton(onClick = { confirmDelete = true }, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small) { Text("Hapus aksesori", color = MaterialTheme.colorScheme.error) }
        }
    }, confirmButton = {
        Button(enabled = !state.isSaving, onClick = {
            error = when {
                name.isBlank() -> "Nama aksesori wajib diisi"
                price.isNotBlank() && price.toLongOrNull() == null -> "Harga terlalu besar atau tidak valid"
                else -> null
            }
            if (error == null) onSave(VehicleAccessory(id, name.trim(), price.toLongOrNull(), status))
        }) { Text(if (state.isSaving) "Menyimpan…" else "Simpan aksesori") }
    }, dismissButton = { TextButton(onClick = onDismiss, enabled = !state.isSaving) { Text("Batal") } })
    if (confirmDelete && item != null) AlertDialog(onDismissRequest = { if (!state.isSaving) confirmDelete = false },
        title = { Text("Hapus aksesori?") }, text = { Text(item.name) },
        confirmButton = { TextButton(enabled = !state.isSaving, onClick = { onDelete(item.id) }) { Text("Hapus") } },
        dismissButton = { TextButton(enabled = !state.isSaving, onClick = { confirmDelete = false }) { Text("Batal") } })
}

@Composable
private fun PriceField(value: String, onChange: (String) -> Unit, label: String, enabled: Boolean) {
    OutlinedTextField(value, { if (it.all(Char::isDigit)) onChange(it) }, label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), enabled = enabled, modifier = Modifier.fillMaxWidth())
}
