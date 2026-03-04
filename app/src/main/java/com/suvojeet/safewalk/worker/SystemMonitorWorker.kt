package com.suvojeet.safewalk.worker

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.suvojeet.safewalk.data.local.db.dao.ContactDao
import com.suvojeet.safewalk.data.local.prefs.PreferencesManager
import com.suvojeet.safewalk.util.SmsHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

/**
 * Worker that handles background logic for low battery or network loss alerts.
 */
@HiltWorker
class SystemMonitorWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val contactDao: ContactDao,
    private val preferencesManager: PreferencesManager,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val action = inputData.getString(KEY_ACTION) ?: return Result.failure()

        val isBatteryLow = action == Intent.ACTION_BATTERY_LOW
        val isAirplaneMode = action == Intent.ACTION_AIRPLANE_MODE_CHANGED

        // Check if alerts are enabled
        val lowBatteryAlertEnabled = preferencesManager.isLowBatteryAlertEnabled.first()
        val networkLossAlertEnabled = preferencesManager.isNetworkLossAlertEnabled.first()

        if (isBatteryLow && !lowBatteryAlertEnabled) return Result.success()
        if (isAirplaneMode && !networkLossAlertEnabled) return Result.success()

        // Double check airplane mode state (must be ON to trigger alert)
        if (isAirplaneMode) {
            val state = android.provider.Settings.Global.getInt(
                context.contentResolver,
                android.provider.Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0
            if (!state) return Result.success() // Airplane mode was turned OFF, no alert
        }

        Log.d(TAG, "System alert triggered: $action. Sending SOS...")

        // Get fresh location before notifying
        var lat = 0.0
        var lon = 0.0
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(applicationContext)
            @Suppress("MissingPermission")
            val location = fusedClient.lastLocation.await()
            if (location != null) {
                lat = location.latitude
                lon = location.longitude
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get location for system alert", e)
        }

        // Notify contacts
        val contacts = contactDao.getActiveContacts().first()
        val userName = preferencesManager.userName.first()
        val userPhone = preferencesManager.userPhone.first()

        val alertType = if (isBatteryLow) "Low Battery" else "Network Loss"

        contacts.forEach { contact ->
            SmsHelper.sendEmergencySms(
                context = applicationContext,
                phoneNumber = contact.phone,
                userName = userName,
                latitude = lat,
                longitude = lon,
                userPhone = userPhone,
                customMessage = "System Alert: $alertType. This is my last known location."
            )
        }

        return Result.success()
    }

    companion object {
        private const val TAG = "SystemMonitorWorker"
        const val KEY_ACTION = "broadcast_action"
    }
}
