package com.vehiclemaintenancepro.domain.repository

import com.vehiclemaintenancepro.core.common.ResultState
import com.vehiclemaintenancepro.domain.model.DashboardSummary
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun observeDashboardSummary(): Flow<ResultState<DashboardSummary>>
}
