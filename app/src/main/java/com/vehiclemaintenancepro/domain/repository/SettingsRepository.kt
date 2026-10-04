package com.vehiclemaintenancepro.domain.repository

import com.vehiclemaintenancepro.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun updateUserName(userName: String)
}
