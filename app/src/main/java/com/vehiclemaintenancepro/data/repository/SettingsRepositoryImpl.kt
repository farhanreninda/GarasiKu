package com.vehiclemaintenancepro.data.repository

import android.content.Context
import com.vehiclemaintenancepro.core.notification.ReminderNotificationScheduler
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vehiclemaintenancepro.domain.model.AppSettings
import com.vehiclemaintenancepro.domain.model.ThemeMode
import com.vehiclemaintenancepro.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore(name = "vehicle_maintenance_settings")

class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SettingsRepository {
    override fun observeSettings(): Flow<AppSettings> = context.appSettingsDataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            AppSettings(
                notificationsEnabled = preferences[Keys.Notifications] ?: true,
                taxReminderDays = preferences[Keys.TaxDays]?.takeIf { it in 0..365 } ?: 7,
                serviceReminderDays = preferences[Keys.ServiceDays]?.takeIf { it in 0..365 } ?: 7,
                userName = preferences[Keys.UserName].orEmpty(),
                themeMode = ThemeMode.entries.firstOrNull { it.name == preferences[Keys.Theme] } ?: ThemeMode.System,
            )
        }

    override suspend fun updateSettings(settings: AppSettings) {
        context.appSettingsDataStore.edit {
            it[Keys.UserName] = settings.userName
            it[Keys.Theme] = settings.themeMode.name
            it[Keys.Notifications] = settings.notificationsEnabled
            it[Keys.TaxDays] = settings.taxReminderDays
            it[Keys.ServiceDays] = settings.serviceReminderDays
        }
        ReminderNotificationScheduler.checkNow(context)
    }

    override suspend fun updateNotificationSettings(enabled: Boolean?, taxDays: Int?, serviceDays: Int?) {
        require(taxDays == null || taxDays in 0..365)
        require(serviceDays == null || serviceDays in 0..365)
        context.appSettingsDataStore.edit {
            enabled?.let { value -> it[Keys.Notifications] = value }
            taxDays?.let { value -> it[Keys.TaxDays] = value }
            serviceDays?.let { value -> it[Keys.ServiceDays] = value }
        }
        ReminderNotificationScheduler.checkNow(context)
    }

    override suspend fun updateUserName(userName: String) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[Keys.UserName] = userName.trim()
        }
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.appSettingsDataStore.edit { it[Keys.Theme] = themeMode.name }
    }

    private object Keys {
        val Notifications = booleanPreferencesKey("notifications_enabled")
        val TaxDays = intPreferencesKey("tax_reminder_days")
        val ServiceDays = intPreferencesKey("service_reminder_days")
        val Theme = stringPreferencesKey("theme_mode")
        val UserName = stringPreferencesKey("user_name")
    }
}
