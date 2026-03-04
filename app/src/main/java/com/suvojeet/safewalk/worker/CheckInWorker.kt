package com.suvojeet.safewalk.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.suvojeet.safewalk.R
import com.suvojeet.safewalk.data.local.db.dao.ContactDao
import com.suvojeet.safewalk.util.Constants
import com.suvojeet.safewalk.util.SmsHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

@HiltWorker
class CheckInWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val contactDao: ContactDao,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val escalationLevel = inputData.getInt(KEY_ESCALATION_LEVEL, 1)

        createNotificationChannel()

        return when (escalationLevel) {
            1 -> handleFirstEscalation()
            2 -> handleSecondEscalation()
            else -> Result.success()
        }
    }

    @Suppress("MissingPermission")
    private suspend fun handleFirstEscalation(): Result {
        // First escalation: Send SMS to contacts with real location
        showNotification(
            "⏱️ Check-in Missed",
            "You didn't check in! Alerting your emergency contacts...",
        )

        // Get last known location
        var lat = 0.0
        var lon = 0.0
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(applicationContext)
            val location = fusedClient.lastLocation.await()
            if (location != null) {
                lat = location.latitude
                lon = location.longitude
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val contacts = contactDao.getActiveContacts().first()
        contacts.forEach { contact ->
            SmsHelper.sendEmergencySms(
                context = applicationContext,
                phoneNumber = contact.phone,
                userName = "SafeWalk User",
                latitude = lat,
                longitude = lon,
            )
        }

        return Result.success()
    }

    private suspend fun handleSecondEscalation(): Result {
        // Second escalation: Show call prompt
        showNotification(
            "🚨 Emergency — No Check-in",
            "Your emergency contacts have been notified. Consider calling for help.",
        )

        return Result.success()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.CHANNEL_TIMER,
            "Check-in Timer",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alerts when you miss a check-in"
            enableVibration(true)
        }
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun showNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(applicationContext, Constants.CHANNEL_TIMER)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val KEY_ESCALATION_LEVEL = "escalation_level"
        private const val NOTIFICATION_ID = 2001
    }
}
