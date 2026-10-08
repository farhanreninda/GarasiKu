package com.vehiclemaintenancepro.presentation.screen.settings

import com.vehiclemaintenancepro.domain.model.Vehicle

data class SettingsUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val themeMode: com.vehiclemaintenancepro.domain.model.ThemeMode = com.vehiclemaintenancepro.domain.model.ThemeMode.System,
    val notificationsEnabled: Boolean = true,
    val taxReminderDays: Int = 7,
    val serviceReminderDays: Int = 7,
    val vehicles: List<Vehicle> = emptyList(),
    val pendingReminderCount: Int = 0,
    val isSaving: Boolean = false,
    val notice: String? = null,
    val errorMessage: String? = null,
)
