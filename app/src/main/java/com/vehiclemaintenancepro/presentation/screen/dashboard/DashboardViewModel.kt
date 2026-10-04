package com.vehiclemaintenancepro.presentation.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vehiclemaintenancepro.core.common.ResultState
import com.vehiclemaintenancepro.core.constant.AppConstants
import com.vehiclemaintenancepro.domain.usecase.ObserveDashboardSummaryUseCase
import com.vehiclemaintenancepro.domain.usecase.SetActiveVehicleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val observeDashboardSummaryUseCase: ObserveDashboardSummaryUseCase,
    private val setActiveVehicleUseCase: SetActiveVehicleUseCase,
) : ViewModel() {
    private val refreshTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = refreshTrigger
        .flatMapLatest { observeDashboardSummaryUseCase() }
        .map { result ->
            when (result) {
                ResultState.Loading -> DashboardUiState(isLoading = true)
                is ResultState.Success -> DashboardUiState(
                    isLoading = false,
                    summary = result.data,
                )
                is ResultState.Error -> DashboardUiState(
                    isLoading = false,
                    errorMessage = result.message,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = DashboardUiState(),
        )

    fun retry() {
        refreshTrigger.update { it + 1 }
    }

    fun selectVehicle(vehicleId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                setActiveVehicleUseCase(vehicleId)
            }
        }
    }
}
