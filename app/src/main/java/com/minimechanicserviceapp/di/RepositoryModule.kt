package com.minimechanicserviceapp.di

import com.minimechanicserviceapp.data.repository.MechanicRepositoryImpl
import com.minimechanicserviceapp.data.repository.ServiceRequestRepositoryImpl
import com.minimechanicserviceapp.domain.repository.MechanicRepository
import com.minimechanicserviceapp.domain.repository.ServiceRequestRepository
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
    abstract fun bindMechanicRepository(
        impl: MechanicRepositoryImpl,
    ): MechanicRepository

    @Binds
    @Singleton
    abstract fun bindServiceRequestRepository(
        impl: ServiceRequestRepositoryImpl,
    ): ServiceRequestRepository
}