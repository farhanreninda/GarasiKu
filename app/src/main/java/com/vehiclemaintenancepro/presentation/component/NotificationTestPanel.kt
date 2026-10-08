package com.vehiclemaintenancepro.presentation.component

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.vehiclemaintenancepro.core.notification.ReminderNotificationScheduler

@Composable
fun NotificationTestPanel(enabled: Boolean) {
    val context = LocalContext.current
    var blocked by remember(enabled) { mutableStateOf(ReminderNotificationScheduler.blockedReason(context, enabled)) }
    var result by remember { mutableStateOf<String?>(null) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        blocked = ReminderNotificationScheduler.blockedReason(context, enabled)
    }
    PanelCard {
        Text("Notifikasi di HP", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(6.dp))
        Text(blocked ?: "Izin dan kanal pengingat aktif", style = MaterialTheme.typography.bodyMedium,
            color = if (blocked == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        AppActionRow { actionModifier ->
            FilledTonalButton(onClick = {
                result = ReminderNotificationScheduler.sendTest(context, enabled)
                blocked = ReminderNotificationScheduler.blockedReason(context, enabled)
            }, modifier = actionModifier, shape = MaterialTheme.shapes.small) { Text("Tes notifikasi") }
            OutlinedButton(modifier = actionModifier, shape = MaterialTheme.shapes.small, onClick = {
                context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
            }) { Text("Setelan Android") }
        }
        result?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth())
        }
    }
}
