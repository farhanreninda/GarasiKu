package com.vehiclemaintenancepro.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vehiclemaintenancepro.domain.model.AppSettings
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
                userName = preferences[Keys.UserName].orEmpty(),
            )
        }

    override suspend fun updateUserName(userName: String) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[Keys.UserName] = userName.trim()
        }
    }

    private object Keys {
        val UserName = stringPreferencesKey("user_name")
    }
}
