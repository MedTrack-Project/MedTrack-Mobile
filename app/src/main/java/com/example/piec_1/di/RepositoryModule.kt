package com.example.piec_1.di

import com.example.piec_1.data.repository.MedicamentoRepositoryContract
import com.example.piec_1.data.repository.MedicamentoRepository
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
    abstract fun bindMedicamentoRepositoryContract(
        repository: MedicamentoRepository
    ): MedicamentoRepositoryContract
}
