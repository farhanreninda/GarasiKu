package com.vehiclemaintenancepro.domain.usecase

import com.vehiclemaintenancepro.domain.repository.DashboardRepository
import javax.inject.Inject

class ObserveDashboardSummaryUseCase @Inject constructor(
    private val repository: DashboardRepository,
) {
    operator fun invoke() = repository.observeDashboardSummary()
}
