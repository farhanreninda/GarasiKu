package com.vehiclemaintenancepro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vehiclemaintenancepro.domain.model.AppSettings
import com.vehiclemaintenancepro.domain.model.ThemeMode
import androidx.activity.enableEdgeToEdge
import android.graphics.Color
import com.vehiclemaintenancepro.presentation.navigation.VehicleMaintenanceRoot
import com.vehiclemaintenancepro.presentation.theme.VehicleMaintenanceProTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @javax.inject.Inject lateinit var settingsRepository: com.vehiclemaintenancepro.domain.repository.SettingsRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
        )
        setContent {
            val settingsFlow = remember { settingsRepository.observeSettings() }
            val settings by settingsFlow.collectAsStateWithLifecycle(initialValue = AppSettings())
            val dark = when (settings.themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            LaunchedEffect(dark) {
                val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
                    else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            VehicleMaintenanceProTheme(darkTheme = dark) {
                VehicleMaintenanceRoot()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.vehiclemaintenancepro.core.notification.ReminderNotificationScheduler.checkNow(this)
    }

}
