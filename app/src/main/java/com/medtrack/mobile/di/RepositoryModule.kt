package com.medtrack.mobile.di

import com.medtrack.mobile.data.repository.MedicamentoRepository
import com.medtrack.mobile.data.repository.MedicamentoRepositoryContract
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
    abstract fun bindMedicamentoRepositoryContract(repository: MedicamentoRepository): MedicamentoRepositoryContract
}
