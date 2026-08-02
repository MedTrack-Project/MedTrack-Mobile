package com.medtrack.mobile.di

import com.medtrack.mobile.data.camera.CameraService
import com.medtrack.mobile.data.local.source.MedicationLocalDataSource
import com.medtrack.mobile.data.local.source.MedicationLocalSource
import com.medtrack.mobile.data.remote.ConfirmationImageSource
import com.medtrack.mobile.data.remote.MultipartImageFactory
import com.medtrack.mobile.data.remote.source.AuthRemoteDataSource
import com.medtrack.mobile.data.remote.source.AuthRemoteSource
import com.medtrack.mobile.data.remote.source.MedicationRemoteDataSource
import com.medtrack.mobile.data.remote.source.MedicationRemoteSource
import com.medtrack.mobile.data.remote.source.ScanRemoteDataSource
import com.medtrack.mobile.data.remote.source.ScanRemoteSource
import com.medtrack.mobile.data.repository.AuthRepository
import com.medtrack.mobile.data.repository.MedicamentoRepository
import com.medtrack.mobile.data.repository.ScanRepository
import com.medtrack.mobile.data.session.AndroidTokenCipher
import com.medtrack.mobile.data.session.AndroidTokenPreferences
import com.medtrack.mobile.data.session.SessionManager
import com.medtrack.mobile.data.session.TokenCipher
import com.medtrack.mobile.data.session.TokenPreferences
import com.medtrack.mobile.data.system.DefaultDispatcherProvider
import com.medtrack.mobile.data.system.SystemClock
import com.medtrack.mobile.data.worker.OfflineScanWorkScheduler
import com.medtrack.mobile.data.worker.ScanFileCleaner
import com.medtrack.mobile.data.worker.ScanFileCleanup
import com.medtrack.mobile.data.worker.WorkManagerOfflineScanScheduler
import com.medtrack.mobile.domain.coroutines.DispatcherProvider
import com.medtrack.mobile.domain.repository.AuthenticationRepository
import com.medtrack.mobile.domain.repository.MedicationRepository
import com.medtrack.mobile.domain.repository.OfflineScanRepository
import com.medtrack.mobile.domain.repository.SessionRepository
import com.medtrack.mobile.domain.service.MedicationScheduler
import com.medtrack.mobile.domain.time.AppClock
import com.medtrack.mobile.ui.camera.CameraController
import com.medtrack.mobile.utils.notifications.AndroidOfflineScanNotifier
import com.medtrack.mobile.utils.notifications.NotificationScheduler
import com.medtrack.mobile.utils.notifications.OfflineScanNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAuthRemoteSource(source: AuthRemoteDataSource): AuthRemoteSource

    @Binds
    abstract fun bindMedicationRemoteSource(source: MedicationRemoteDataSource): MedicationRemoteSource

    @Binds
    abstract fun bindScanRemoteSource(source: ScanRemoteDataSource): ScanRemoteSource

    @Binds
    abstract fun bindMedicationLocalSource(source: MedicationLocalDataSource): MedicationLocalSource

    @Binds
    abstract fun bindConfirmationImageSource(source: MultipartImageFactory): ConfirmationImageSource

    @Binds
    abstract fun bindOfflineScanWorkScheduler(source: WorkManagerOfflineScanScheduler): OfflineScanWorkScheduler

    @Binds
    abstract fun bindOfflineScanNotifier(notifier: AndroidOfflineScanNotifier): OfflineScanNotifier

    @Binds
    abstract fun bindScanFileCleanup(cleaner: ScanFileCleaner): ScanFileCleanup

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(repository: MedicamentoRepository): MedicationRepository

    @Binds
    abstract fun bindAuthenticationRepository(repository: AuthRepository): AuthenticationRepository

    @Binds
    abstract fun bindScanRepository(repository: ScanRepository): com.medtrack.mobile.domain.repository.ScanRepository

    @Binds
    abstract fun bindOfflineScanRepository(repository: ScanRepository): OfflineScanRepository

    @Binds
    abstract fun bindSessionRepository(repository: SessionManager): SessionRepository

    @Binds
    abstract fun bindTokenCipher(cipher: AndroidTokenCipher): TokenCipher

    @Binds
    abstract fun bindTokenPreferences(preferences: AndroidTokenPreferences): TokenPreferences

    @Binds
    abstract fun bindMedicationScheduler(scheduler: NotificationScheduler): MedicationScheduler

    @Binds
    abstract fun bindClock(clock: SystemClock): AppClock

    @Binds
    abstract fun bindDispatcherProvider(provider: DefaultDispatcherProvider): DispatcherProvider

    @Binds
    abstract fun bindCameraController(controller: CameraService): CameraController
}
