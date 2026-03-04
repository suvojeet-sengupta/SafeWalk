package com.suvojeet.safewalk.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.suvojeet.safewalk.util.Constants
import com.suvojeet.safewalk.util.SmsHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

/**
 * WorkManager worker that retries failed SMS sends.
 * Gets fresh location before sending so the alert is up-to-date.
 */
@HiltWorker
class SmsRetryWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val phone = inputData.getString(KEY_PHONE) ?: return Result.failure()
        val userName = inputData.getString(KEY_USER_NAME) ?: "SafeWalk User"
        val userPhone = inputData.getString(KEY_USER_PHONE) ?: ""
        val lat = inputData.getDouble(KEY_LAT, 0.0)
        val lon = inputData.getDouble(KEY_LON, 0.0)
        val customMessage = inputData.getString(KEY_CUSTOM_MESSAGE)
        val retryCount = inputData.getInt(KEY_RETRY_COUNT, 1)
        val maxRetries = inputData.getInt(KEY_MAX_RETRIES, Constants.SMS_MAX_RETRIES)

        Log.d(TAG, "SmsRetryWorker executing retry #$retryCount for $phone")

        // Try to get fresh location
        var freshLat = lat
        var freshLon = lon
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(applicationContext)
            @Suppress("MissingPermission")
            val location = fusedClient.lastLocation.await()
            if (location != null) {
                freshLat = location.latitude
                freshLon = location.longitude
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not get fresh location, using cached", e)
        }

        val sent = SmsHelper.sendEmergencySms(
            context = applicationContext,
            phoneNumber = phone,
            userName = userName,
            latitude = freshLat,
            longitude = freshLon,
            userPhone = userPhone,
            retryCount = retryCount,
            maxRetries = maxRetries,
            customMessage = customMessage,
        )

        return if (sent) {
            Log.d(TAG, "SMS retry #$retryCount queued successfully to $phone")
            Result.success()
        } else {
            Log.w(TAG, "SMS retry #$retryCount still failed for $phone — next retry scheduled if within limits")
            Result.success() // Return success because SmsHelper already schedules next retry
        }
    }

    companion object {
        private const val TAG = "SmsRetryWorker"
        const val KEY_PHONE = "phone"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_PHONE = "user_phone"
        const val KEY_LAT = "latitude"
        const val KEY_LON = "longitude"
        const val KEY_RETRY_COUNT = "retry_count"
        const val KEY_MAX_RETRIES = "max_retries"
        const val KEY_CUSTOM_MESSAGE = "custom_message"
    }
}
