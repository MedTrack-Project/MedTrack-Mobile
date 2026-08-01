package com.medtrack.mobile.di

import android.content.Context
import com.medtrack.mobile.data.local.AppDatabase
import com.medtrack.mobile.data.local.daos.ConfirmacaoDao
import com.medtrack.mobile.data.local.daos.MedicamentoV2Dao
import com.medtrack.mobile.data.local.daos.NotificacaoDao
import com.medtrack.mobile.data.local.daos.ScanQueueDao
import com.medtrack.mobile.data.local.daos.UsuarioDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.getDatabase(context)

    @Provides
    fun provideUsuarioDao(database: AppDatabase): UsuarioDao = database.usuarioDao()

    @Provides
    fun provideMedicamentoDao(database: AppDatabase): MedicamentoV2Dao = database.medicamentoV2Dao()

    @Provides
    fun provideConfirmacaoDao(database: AppDatabase): ConfirmacaoDao = database.confirmacaoDao()

    @Provides
    fun provideScanQueueDao(database: AppDatabase): ScanQueueDao = database.scanQueueDao()

    @Provides
    fun provideNotificacaoDao(database: AppDatabase): NotificacaoDao = database.notificacaoDao()
}
