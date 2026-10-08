package com.vehiclemaintenancepro.presentation.screen.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
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
                    Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Coba lagi") }
                }
            }
            else -> {
                val vehicle = summary.activeVehicle
                val orderedVehicles = remember(summary.vehicles) { summary.vehicles.sortedBy { it.id } }
                val date = remember(LocalDate.now()) {
                    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.forLanguageTag("id-ID")))
                }
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(date, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium)
                                Text(if (summary.userName.isBlank()) "Halo, pemilik kendaraan" else "Halo, ${summary.userName}", style = MaterialTheme.typography.headlineMedium)
                            }
                            FilledIconButton(onClick = onAddVehicle, shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.semantics { contentDescription = "Tambah kendaraan" }) {
                                Icon(Icons.Rounded.Add, null)
                            }
                        }
                    }
                    if (orderedVehicles.size > 1) {
                        item {
                            FilterOptions(orderedVehicles.map { it.id }, vehicle?.id ?: orderedVehicles.first().id,
                                { id -> orderedVehicles.first { it.id == id }.licensePlate }, onSelectVehicle)
                        }
                    }
                    if (vehicle == null) {
                        item { EmptyPrompt(Icons.Rounded.DirectionsCar, "Belum ada kendaraan", "Tambahkan kendaraan untuk mencatat servis, biaya, dan pengingatnya.", actionLabel = "Tambah kendaraan", onAction = onAddVehicle) }
                    } else {
                        item {
                            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        StatusPill(if (vehicle.isActive) "Utama" else "Dipilih", containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.primary)
                                        Text(vehicle.licensePlate, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Column(Modifier.weight(1f)) {
                                            Text("${vehicle.brand} ${vehicle.model}", style = MaterialTheme.typography.headlineSmall)
                                            Text(listOfNotNull(if (vehicle.vehicleType == VehicleType.Motorcycle) "Motor" else "Mobil", vehicle.year?.toString()).joinToString(" • "),
                                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        IconBadge(if (vehicle.vehicleType == VehicleType.Motorcycle) Icons.Rounded.TwoWheeler else Icons.Rounded.DirectionsCar,
                                            null, Modifier.size(56.dp), containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.primary)
                                    }
                                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceVariant) {
                                        FlowRow(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                            Column {
                                                Text("Odometer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(Formatters.odometer(vehicle.odometerKm), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                            Column {
                                                Text("Servis berikutnya", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(summary.nextService?.let { it.dueOdometerKm?.let(Formatters::odometer) ?: it.dueDate?.let(Formatters::date) }
                                                    ?: "Belum dijadwalkan", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                    val stackActions = androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (stackActions) {
                                            QuickAction("Servis", Icons.Rounded.Build, onOpenService, Modifier.fillMaxWidth())
                                            QuickAction("Biaya", Icons.AutoMirrored.Rounded.ReceiptLong, onOpenStatistics, Modifier.fillMaxWidth())
                                            QuickAction("Perbarui kilometer", Icons.Rounded.Speed, onOpenProfile, Modifier.fillMaxWidth())
                                            QuickAction("Catat perawatan", Icons.Rounded.Add, onRecordMaintenance, Modifier.fillMaxWidth())
                                        } else {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                QuickAction("Servis", Icons.Rounded.Build, onOpenService, Modifier.weight(1f))
                                                QuickAction("Biaya", Icons.AutoMirrored.Rounded.ReceiptLong, onOpenStatistics, Modifier.weight(1f))
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                QuickAction("Perbarui kilometer", Icons.Rounded.Speed, onOpenProfile, Modifier.weight(1f))
                                                QuickAction("Catat perawatan", Icons.Rounded.Add, onRecordMaintenance, Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item { SectionHeader("Jadwal terdekat", actionLabel = "Lihat semua", onAction = onOpenService) }
                        item {
                            PanelCard {
                                ReminderRow("Servis", Icons.Rounded.Build, summary.nextService, onOpenService)
                                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                ReminderRow("Ganti oli", Icons.Rounded.OilBarrel, summary.nextOilChange, onOpenService)
                                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                                ReminderRow("Pajak", Icons.Rounded.Event, summary.taxReminder, onOpenService)
                            }
                        }
                        item {
                            PanelCard(modifier = Modifier.clickable(onClickLabel = "Lihat rincian biaya", onClick = onOpenStatistics)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Total pengeluaran bulan ini", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(Formatters.currency(summary.monthlyExpense), style = MaterialTheme.typography.headlineLarge)
                                    }
                                    Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.primary)
                                }
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
private fun QuickAction(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier) {
        Row(Modifier.heightIn(min = 48.dp).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium)
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
