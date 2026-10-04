package com.vehiclemaintenancepro.domain.usecase

import app.cash.turbine.test
import com.vehiclemaintenancepro.core.common.ResultState
import com.vehiclemaintenancepro.domain.model.DashboardSummary
import com.vehiclemaintenancepro.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveDashboardSummaryUseCaseTest {
    @Test
    fun `invoke delegates to dashboard repository`() = runTest {
        val repository = FakeDashboardRepository()
        val useCase = ObserveDashboardSummaryUseCase(repository)

        repository.emit(ResultState.Loading)

        useCase().test {
            assertEquals(ResultState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FakeDashboardRepository : DashboardRepository {
        private val states = MutableSharedFlow<ResultState<DashboardSummary>>(replay = 1)

        override fun observeDashboardSummary(): Flow<ResultState<DashboardSummary>> = states

        suspend fun emit(state: ResultState<DashboardSummary>) {
            states.emit(state)
        }
    }
}
