package com.suvojeet.safewalk.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.suvojeet.safewalk.MainActivity
import com.suvojeet.safewalk.R
import com.suvojeet.safewalk.data.local.db.dao.ContactDao
import com.suvojeet.safewalk.data.model.LocationData
import com.suvojeet.safewalk.data.remote.firebase.LocationRepository
import com.suvojeet.safewalk.util.Constants
import com.suvojeet.safewalk.util.SmsHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class PanicService : LifecycleService() {

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var contactDao: ContactDao

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )

        triggerPanicSequence()
        return START_NOT_STICKY
    }

    @Suppress("MissingPermission")
    private fun triggerPanicSequence() {
        // Vibrate urgently
        vibrateDevice()

        lifecycleScope.launch {
            try {
                // Get current location
                val fusedClient = LocationServices.getFusedLocationProviderClient(this@PanicService)
                val location = fusedClient.lastLocation.await()

                val lat = location?.latitude ?: 0.0
                val lon = location?.longitude ?: 0.0

                // Push to Firebase
                val locationData = LocationData(
                    latitude = lat,
                    longitude = lon,
                    timestamp = System.currentTimeMillis(),
                    accuracy = location?.accuracy ?: 0f,
                    speed = location?.speed ?: 0f,
                )
                locationRepository.pushLocation(locationData)

                // Send SMS to all active contacts
                val contacts = contactDao.getActiveContacts()
                contacts.collect { contactList ->
                    contactList.forEach { contact ->
                        SmsHelper.sendEmergencySms(
                            context = this@PanicService,
                            phoneNumber = contact.phone,
                            userName = "SafeWalk User",
                            latitude = lat,
                            longitude = lon,
                        )
                    }
                    // Start high-frequency location tracking
                    LocationTrackingService.start(this@PanicService, panicMode = true)
                    stopSelf()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                stopSelf()
            }
        }
    }

    private fun vibrateDevice() {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.CHANNEL_PANIC,
            "Panic Alerts",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Emergency panic alert notifications"
            enableVibration(true)
            enableLights(true)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification() = NotificationCompat.Builder(this, Constants.CHANNEL_PANIC)
        .setContentTitle("🚨 Emergency Alert Active")
        .setContentText("Sending your location to emergency contacts")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        .build()

    companion object {
        private const val NOTIFICATION_ID = 1002

        fun start(context: Context) {
            val intent = Intent(context, PanicService::class.java)
            context.startForegroundService(intent)
        }
    }
}
