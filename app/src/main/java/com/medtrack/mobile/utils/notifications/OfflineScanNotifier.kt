package com.medtrack.mobile.utils.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.medtrack.mobile.MainActivity
import com.medtrack.mobile.data.navigation.PendingNavigationStore
import com.medtrack.mobile.domain.model.MedicamentoCapturadoDomain
import com.medtrack.mobile.ui.navigation.AppIntentContract
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.charset.StandardCharsets
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface OfflineScanNotifier {
    fun show(scanId: Int, medicamento: MedicamentoCapturadoDomain)
}

@Singleton
class AndroidOfflineScanNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val navigationStore: PendingNavigationStore,
) : OfflineScanNotifier {
    override fun show(scanId: Int, medicamento: MedicamentoCapturadoDomain) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Scans offline", NotificationManager.IMPORTANCE_HIGH).apply {
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            },
        )
        val reference = UUID.nameUUIDFromBytes("offline-scan-$scanId".toByteArray(StandardCharsets.UTF_8)).toString()
        navigationStore.save(reference, medicamento)
        val intent = Intent(context, MainActivity::class.java).apply {
            action = AppIntentContract.ACTION_OPEN_CONFIRMATION
            putExtra(AppIntentContract.EXTRA_RESULT_REFERENCE, reference)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            scanId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val publicVersion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("Scan processado")
            .setContentText("Desbloqueie para revisar")
            .build()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("Medicamento processado")
            .setContentText("Toque para revisar o resultado")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()
        manager.notify(NOTIFICATION_ID_BASE + scanId, notification)
    }

    private companion object {
        const val CHANNEL_ID = "offline_scan_channel"
        const val NOTIFICATION_ID_BASE = 20_000
    }
}
