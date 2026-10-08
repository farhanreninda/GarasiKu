package com.vehiclemaintenancepro.domain.repository

import com.vehiclemaintenancepro.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun updateNotificationSettings(enabled: Boolean? = null, taxDays: Int? = null, serviceDays: Int? = null)
    suspend fun updateSettings(settings: AppSettings)
    fun observeSettings(): Flow<AppSettings>
    suspend fun updateUserName(userName: String)
    suspend fun updateThemeMode(themeMode: com.vehiclemaintenancepro.domain.model.ThemeMode)
}
