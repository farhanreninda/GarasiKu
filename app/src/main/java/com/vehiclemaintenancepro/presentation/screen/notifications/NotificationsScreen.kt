package com.vehiclemaintenancepro.presentation.screen.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vehiclemaintenancepro.core.design.Dimens
import com.vehiclemaintenancepro.core.design.MotionTokens
import com.vehiclemaintenancepro.core.utility.Formatters
import com.vehiclemaintenancepro.domain.model.MaintenanceReminder
import com.vehiclemaintenancepro.domain.model.ReminderType
import com.vehiclemaintenancepro.domain.model.Vehicle
import com.vehiclemaintenancepro.presentation.component.AppBackground
import com.vehiclemaintenancepro.presentation.component.AppLoadingState
import com.vehiclemaintenancepro.presentation.component.EmptyPrompt
import com.vehiclemaintenancepro.presentation.component.IconBadge
import com.vehiclemaintenancepro.presentation.component.PanelCard
import com.vehiclemaintenancepro.presentation.component.StatusPill
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun NotificationsRoute(
    onBack: () -> Unit,
    onOpenService: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    NotificationsScreen(
        state = state,
        onBack = onBack,
        onOpenService = onOpenService,
    )
}

@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onBack: () -> Unit,
    onOpenService: () -> Unit,
) {
    AppBackground {
        when {
            state.isLoading -> LoadingState()
            state.errorMessage != null -> ErrorState(message = state.errorMessage, onBack = onBack)
            else -> NotificationsContent(
                state = state,
                onBack = onBack,
                onOpenService = onOpenService,
            )
        }
    }
}

@Composable
private fun LoadingState() {
    AppLoadingState()
}

@Composable
private fun ErrorState(
    message: String,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Button(onClick = onBack) {
                Text(text = "Kembali")
            }
        }
    }
}

@Composable
private fun NotificationsContent(
    state: NotificationsUiState,
    onBack: () -> Unit,
    onOpenService: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.SpaceLg,
            top = Dimens.SpaceLg,
            end = Dimens.SpaceLg,
            bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg),
    ) {
        item {
            NotificationsHeader(
                count = state.reminders.size,
                vehicle = state.activeVehicle,
                onBack = onBack,
                onOpenService = onOpenService,
            )
        }

        when {
            state.activeVehicle == null -> item {
                EmptyPrompt(
                    icon = Icons.Rounded.Notifications,
                    title = "Belum ada kendaraan",
                    message = "Tambahkan kendaraan dulu agar notifikasi servis dan oli bisa muncul.",
                    actionLabel = "Kembali",
                    onAction = onBack,
                )
            }
            state.reminders.isEmpty() -> item {
                EmptyPrompt(
                    icon = Icons.Rounded.Notifications,
                    title = "Tidak ada notifikasi",
                    message = "Pengingat akan muncul menjelang tanggal, saat terlambat, atau ketika kilometer tercapai.",
                    actionLabel = "Atur pengingat",
                    onAction = onOpenService,
                )
            }
            else -> items(
                items = state.reminders,
                key = { it.id },
            ) { reminder ->
                NotificationItem(
                    reminder = reminder,
                    vehicle = state.activeVehicle,
                    onClick = onOpenService,
                )
            }
        }
    }
}

@Composable
private fun NotificationsHeader(
    count: Int,
    vehicle: Vehicle?,
    onBack: () -> Unit,
    onOpenService: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pengingat",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = vehicle?.displayName() ?: "Kendaraan aktif",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        PanelCard(containerColor = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconBadge(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$count perlu perhatian",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Pengingat yang sudah masuk H-7, hari H, lewat, atau odometer tercapai.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TextButton(onClick = onOpenService) { Text("Kelola jadwal perawatan") }
        }
    }
}

@Composable
private fun NotificationItem(
    reminder: MaintenanceReminder,
    vehicle: Vehicle?,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = MotionTokens.PressMillis,
            easing = androidx.compose.animation.core.FastOutSlowInEasing,
        ),
        label = "notification press scale",
    )
    val status = reminder.notificationStatus(vehicle)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(Dimens.CardRadius))
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(Dimens.CardRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick,
            )
            .padding(Dimens.SpaceLg),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
        verticalAlignment = Alignment.Top,
    ) {
        IconBadge(
            imageVector = reminder.type.icon(),
            contentDescription = null,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm),
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                StatusPill(
                    text = status,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = reminder.dueLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Ketuk untuk kelola pengingat ini.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun MaintenanceReminder.notificationStatus(vehicle: Vehicle?): String {
    val today = LocalDate.now()
    dueDate?.let { date ->
        val daysUntilDue = ChronoUnit.DAYS.between(today, date)
        return when {
            daysUntilDue < 0 -> "Lewat"
            daysUntilDue == 0L -> "Hari ini"
            else -> "H-$daysUntilDue"
        }
    }
    if (vehicle != null && dueOdometerKm != null && vehicle.odometerKm >= dueOdometerKm) {
        return "Perlu dicek"
    }
    return "Aktif"
}

private fun MaintenanceReminder.dueLabel(): String {
    dueDate?.let { return Formatters.date(it) }
    dueOdometerKm?.let { return Formatters.odometer(it) }
    return "Jadwal belum ditentukan"
}

private fun ReminderType.icon(): ImageVector = when (this) {
    ReminderType.Service -> Icons.Rounded.Build
    ReminderType.OilChange -> Icons.Rounded.Speed
    ReminderType.Tax,
    ReminderType.Registration,
    ReminderType.Insurance,
    ReminderType.Custom,
    -> Icons.Rounded.Event
}

private fun Vehicle.displayName(): String = listOf(brand, model)
    .filter { it.isNotBlank() }
    .joinToString(separator = " ")
    .ifBlank { "Kendaraan" }
