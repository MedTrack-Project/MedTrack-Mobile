package com.medtrack.mobile.utils.notifications

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationContractTest {
    private lateinit var context: Context
    private lateinit var manager: NotificationManager

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        manager = context.getSystemService(NotificationManager::class.java)
    }

    @After
    fun tearDown() {
        manager.deleteNotificationChannel(NotificationHelper.CHANNEL_ID)
    }

    @Test
    fun notificationChannelUsesHighImportanceAndPrivateNotificationContract() {
        NotificationHelper.createNotificationChannel(context)

        val channel = requireNotNull(manager.getNotificationChannel(NotificationHelper.CHANNEL_ID))
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
        assertEquals(android.app.Notification.VISIBILITY_PRIVATE, NotificationHelper.NOTIFICATION_VISIBILITY)
    }

    @Test
    fun payloadParsesValidReminderAndNormalizesDisplayName() {
        val payload = NotificationPayload.fromIntent(
            Intent()
                .putExtra(NotificationPayload.KEY_MEDICATION_ID, 7L)
                .putExtra(NotificationPayload.KEY_NAME, "Losartana")
                .putExtra(NotificationPayload.KEY_TIME, "08:00"),
        )

        requireNotNull(payload)
        assertEquals(7L, payload.medicationId)
        assertEquals("Losartana", payload.displayName())
    }

    @Test
    fun payloadRejectsInvalidOrIncompleteReminder() {
        assertNull(NotificationPayload.fromIntent(Intent()))
        assertNull(
            NotificationPayload.fromIntent(
                Intent()
                    .putExtra(NotificationPayload.KEY_MEDICATION_ID, -1L)
                    .putExtra(NotificationPayload.KEY_NAME, "Losartana")
                    .putExtra(NotificationPayload.KEY_TIME, "08:00"),
            ),
        )
    }
}
