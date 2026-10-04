package com.vehiclemaintenancepro.presentation.screen.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vehiclemaintenancepro.core.utility.Formatters
import com.vehiclemaintenancepro.domain.model.*
import com.vehiclemaintenancepro.presentation.component.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardRoute(
    onAddVehicle: () -> Unit = {},
    onOpenService: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenStatistics: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onRecordMaintenance: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(state, viewModel::retry, onAddVehicle, onOpenService, onOpenNotifications,
        viewModel::selectVehicle, onOpenStatistics, onOpenProfile, onRecordMaintenance)
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onRetry: () -> Unit,
    onAddVehicle: () -> Unit = {},
    onOpenService: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onSelectVehicle: (Long) -> Unit = {},
    onOpenStatistics: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onRecordMaintenance: () -> Unit = {},
) {
    AppBackground {
        val summary = state.summary
        when {
            state.isLoading && summary == null -> AppLoadingState()
            summary == null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Data beranda belum bisa dimuat", style = MaterialTheme.typography.titleLarge)
                    Button(onClick = onRetry) { Text("Coba lagi") }
                }
            }
            else -> {
                val vehicle = summary.activeVehicle
                val orderedVehicles = remember(summary.vehicles) { summary.vehicles.sortedBy { it.id } }
                val date = remember(LocalDate.now()) {
                    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("id-ID")))
                }
                LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    item {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(if (summary.userName.isBlank()) "Beranda" else "Halo, ${summary.userName}", style = MaterialTheme.typography.headlineSmall)
                                Text(date, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = onAddVehicle, modifier = Modifier.semantics { contentDescription = "Tambah kendaraan" }) {
                                Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            BadgedBox(badge = { if (summary.alertReminderCount > 0) Badge { Text(summary.alertReminderCount.toString()) } }) {
                                IconButton(onClick = onOpenNotifications, modifier = Modifier.semantics { contentDescription = "Buka pengingat servis" }) {
                                    Icon(Icons.Rounded.NotificationsNone, null, tint = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                    if (orderedVehicles.size > 1) {
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(orderedVehicles, key = { it.id }) { choice ->
                                    FilterChip(selected = choice.id == vehicle?.id, onClick = { onSelectVehicle(choice.id) },
                                        label = { Text(choice.licensePlate) },
                                        leadingIcon = { Icon(if (choice.vehicleType == VehicleType.Motorcycle) Icons.Rounded.TwoWheeler else Icons.Rounded.DirectionsCar, null, Modifier.size(18.dp)) })
                                }
                            }
                        }
                    }
                    if (vehicle == null) {
                        item { EmptyPrompt(Icons.Rounded.DirectionsCar, "Belum ada kendaraan", "Tambahkan kendaraan untuk mencatat servis, biaya, dan pengingatnya.", actionLabel = "Tambah kendaraan", onAction = onAddVehicle) }
                    } else {
                        item {
                            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(if (vehicle.vehicleType == VehicleType.Motorcycle) Icons.Rounded.TwoWheeler else Icons.Rounded.DirectionsCar, null, Modifier.size(24.dp))
                                        Text(if (vehicle.vehicleType == VehicleType.Motorcycle) "Motor" else "Mobil", style = MaterialTheme.typography.labelLarge)
                                    }
                                    Text("${vehicle.brand} ${vehicle.model} / ${vehicle.licensePlate}", style = MaterialTheme.typography.titleLarge)
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Odometer terakhir", style = MaterialTheme.typography.bodyMedium)
                                        Text(Formatters.odometer(vehicle.odometerKm), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                                    }
                                    TextButton(onClick = onOpenProfile, contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                                        Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Perbarui kilometer")
                                    }
                                }
                            }
                        }
                        item {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = onRecordMaintenance, shape = MaterialTheme.shapes.small) {
                                    Icon(Icons.Rounded.Add, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Catat perawatan")
                                }
                                OutlinedButton(onClick = onOpenService, shape = MaterialTheme.shapes.small) { Text("Servis & jadwal") }
                            }
                        }
                        item {
                            PanelCard(modifier = Modifier.clickable(onClickLabel = "Lihat rincian biaya", onClick = onOpenStatistics)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    IconBadge(Icons.Rounded.AccountBalanceWallet, null, containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.primary)
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Biaya bulan ini", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(Formatters.currency(summary.monthlyExpense), style = MaterialTheme.typography.titleLarge)
                                    }
                                    Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        item { SectionHeader("Perawatan & dokumen", actionLabel = "Kelola", onAction = onOpenService) }
                        item {
                            PanelCard {
                                ReminderRow("Servis", Icons.Rounded.Build, summary.nextService, onOpenService)
                                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                ReminderRow("Ganti oli", Icons.Rounded.OilBarrel, summary.nextOilChange, onOpenService)
                                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                ReminderRow("Pajak", Icons.Rounded.Event, summary.taxReminder, onOpenService)
                            }
                        }
                        if (summary.alertReminderCount > 0) {
                            item { FilledTonalButton(onClick = onOpenNotifications, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) { Text("${summary.alertReminderCount} pengingat perlu diperiksa") } }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderRow(label: String, icon: ImageVector, reminder: MaintenanceReminder?, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClickLabel = "Kelola $label", onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text(reminder?.let {
                listOfNotNull(it.dueDate?.let(Formatters::date), it.dueOdometerKm?.let(Formatters::odometer)).joinToString(" atau ").ifBlank { "Jadwal belum ditentukan" }
            } ?: "Belum ada pengingat", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
