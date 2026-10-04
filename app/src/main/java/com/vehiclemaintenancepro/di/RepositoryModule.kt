package com.vehiclemaintenancepro.di

import com.vehiclemaintenancepro.data.repository.DashboardRepositoryImpl
import com.vehiclemaintenancepro.data.repository.MaintenanceRepositoryImpl
import com.vehiclemaintenancepro.data.repository.SettingsRepositoryImpl
import com.vehiclemaintenancepro.data.repository.VehicleRepositoryImpl
import com.vehiclemaintenancepro.domain.repository.DashboardRepository
import com.vehiclemaintenancepro.domain.repository.MaintenanceRepository
import com.vehiclemaintenancepro.domain.repository.SettingsRepository
import com.vehiclemaintenancepro.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        implementation: DashboardRepositoryImpl,
    ): DashboardRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        implementation: VehicleRepositoryImpl,
    ): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(
        implementation: MaintenanceRepositoryImpl,
    ): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        implementation: SettingsRepositoryImpl,
    ): SettingsRepository
}
