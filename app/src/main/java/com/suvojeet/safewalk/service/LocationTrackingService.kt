package com.suvojeet.safewalk.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.suvojeet.safewalk.MainActivity
import com.suvojeet.safewalk.R
import com.suvojeet.safewalk.data.model.LocationData
import com.suvojeet.safewalk.data.remote.firebase.LocationRepository
import com.suvojeet.safewalk.util.Constants
import com.suvojeet.safewalk.util.LocationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : LifecycleService() {

    @Inject
    lateinit var locationRepository: LocationRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var lastLocation: LocationData? = null
    private var isPanicMode = false
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        isPanicMode = intent?.getBooleanExtra(EXTRA_PANIC_MODE, false) ?: false

        // In panic mode, acquire WakeLock so attacker can't kill tracking by pressing power button
        if (isPanicMode) {
            acquirePanicWakeLock()
        }

        startForeground(
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )

        startLocationUpdates()

        return START_STICKY
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val locationData = LocationData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        timestamp = System.currentTimeMillis(),
                        accuracy = location.accuracy,
                        speed = location.speed,
                        bearing = location.bearing,
                    )

                    // Smart update: only push if moved significant distance
                    val shouldUpdate = lastLocation?.let { last ->
                        val distance = LocationUtils.distanceBetween(
                            last.latitude, last.longitude,
                            locationData.latitude, locationData.longitude,
                        )
                        isPanicMode || distance >= Constants.LOCATION_DISPLACEMENT_THRESHOLD_M
                    } ?: true

                    if (shouldUpdate) {
                        lastLocation = locationData
                        lifecycleScope.launch {
                            try {
                                locationRepository.pushLocation(locationData)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        val interval = when {
            isPanicMode -> Constants.LOCATION_UPDATE_PANIC_MS
            else -> Constants.LOCATION_UPDATE_MOVING_MS
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            interval,
        ).setMinUpdateIntervalMillis(interval / 2)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper(),
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.CHANNEL_LOCATION,
            "Location Tracking",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Tracks your location for safety sharing"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification() = NotificationCompat.Builder(this, Constants.CHANNEL_LOCATION)
        .setContentTitle("SafeWalk Active")
        .setContentText(
            if (isPanicMode) "🚨 Emergency mode — sharing location rapidly"
            else "📍 Sharing your live location",
        )
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        .build()

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                Log.d(TAG, "Panic WakeLock released")
            }
        }
    }

    /**
     * Acquire WakeLock during panic mode to prevent the phone from sleeping.
     * Even if the attacker presses the power button, tracking continues.
     */
    private fun acquirePanicWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SafeWalk::LocationPanicWakeLock",
        ).apply {
            acquire(60 * 60 * 1000L) // 1 hour max
        }
        Log.d(TAG, "Panic WakeLock acquired — location tracking will persist")
    }

    companion object {
        private const val TAG = "LocationTrackingService"
        private const val NOTIFICATION_ID = 1001
        const val EXTRA_PANIC_MODE = "extra_panic_mode"

        fun start(context: Context, panicMode: Boolean = false) {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                putExtra(EXTRA_PANIC_MODE, panicMode)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LocationTrackingService::class.java))
        }
    }
}
