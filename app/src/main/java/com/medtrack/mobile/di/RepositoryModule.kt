package com.medtrack.mobile.di

import com.medtrack.mobile.data.camera.CameraService
import com.medtrack.mobile.data.repository.AuthRepository
import com.medtrack.mobile.data.repository.MedicamentoRepository
import com.medtrack.mobile.data.repository.ScanRepository
import com.medtrack.mobile.data.session.SessionManager
import com.medtrack.mobile.data.system.DefaultDispatcherProvider
import com.medtrack.mobile.data.system.SystemClock
import com.medtrack.mobile.domain.coroutines.DispatcherProvider
import com.medtrack.mobile.domain.repository.AuthenticationRepository
import com.medtrack.mobile.domain.repository.MedicationRepository
import com.medtrack.mobile.domain.repository.OfflineScanRepository
import com.medtrack.mobile.domain.repository.SessionRepository
import com.medtrack.mobile.domain.service.MedicationScheduler
import com.medtrack.mobile.domain.time.AppClock
import com.medtrack.mobile.ui.camera.CameraController
import com.medtrack.mobile.utils.notifications.NotificationScheduler
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
    abstract fun bindMedicationScheduler(scheduler: NotificationScheduler): MedicationScheduler

    @Binds
    abstract fun bindClock(clock: SystemClock): AppClock

    @Binds
    abstract fun bindDispatcherProvider(provider: DefaultDispatcherProvider): DispatcherProvider

    @Binds
    abstract fun bindCameraController(controller: CameraService): CameraController
}
