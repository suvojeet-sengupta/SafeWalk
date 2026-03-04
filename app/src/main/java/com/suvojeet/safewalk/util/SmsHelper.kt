package com.suvojeet.safewalk.util

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.suvojeet.safewalk.worker.SmsRetryWorker
import java.util.concurrent.TimeUnit

object SmsHelper {

    private const val TAG = "SmsHelper"
    const val ACTION_SMS_SENT = "com.suvojeet.safewalk.SMS_SENT"
    const val ACTION_SMS_DELIVERED = "com.suvojeet.safewalk.SMS_DELIVERED"

    /**
     * Build the emergency message string including user's phone number.
     */
    fun buildEmergencyMessage(
        context: Context,
        userName: String,
        userPhone: String,
        latitude: Double,
        longitude: Double,
        customMessage: String? = null,
    ): String {
        val mapLink = "https://maps.google.com/maps?q=$latitude,$longitude"
        val deviceInfo = DeviceInfoHelper.buildDeviceInfoString(context)
        return buildString {
            if (customMessage != null) {
                append("🚨 $customMessage\n\n")
            } else {
                append("🚨 EMERGENCY ALERT from $userName!\n\n")
                append("I need help! This is an automated safety alert from SafeWalk.\n\n")
            }
            if (userPhone.isNotBlank()) {
                append("📞 My number: $userPhone\n\n")
            }
            append("📍 My current location:\n")
            append("$mapLink\n\n")
            append(deviceInfo)
            append("\n\nPlease contact me or emergency services immediately.")
        }
    }

    /**
     * Send emergency SMS with delivery tracking & automatic retry on failure.
     * Uses PendingIntents to track sent/delivery status.
     * On failure, automatically retries via WorkManager with exponential backoff.
     */
    fun sendEmergencySms(
        context: Context,
        phoneNumber: String,
        userName: String,
        latitude: Double,
        longitude: Double,
        userPhone: String = "",
        retryCount: Int = 0,
        maxRetries: Int = Constants.SMS_MAX_RETRIES,
        customMessage: String? = null,
    ): Boolean {
        if (!hasSmsPermission(context)) {
            Log.w(TAG, "SMS permission not granted, scheduling retry for $phoneNumber")
            scheduleRetry(context, phoneNumber, userName, userPhone, latitude, longitude, retryCount, maxRetries, customMessage)
            return false
        }

        return try {
            val message = buildEmergencyMessage(context, userName, userPhone, latitude, longitude, customMessage)
            val smsManager = context.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(message)

            val sentIntents = ArrayList<PendingIntent>()
            val deliveredIntents = ArrayList<PendingIntent>()

            for (i in parts.indices) {
                val uniqueId = (System.currentTimeMillis() + i).toInt()

                val sentIntent = PendingIntent.getBroadcast(
                    context,
                    uniqueId,
                    Intent(ACTION_SMS_SENT).apply {
                        putExtra("phone", phoneNumber)
                        putExtra("part", i)
                        putExtra("total_parts", parts.size)
                        putExtra("user_name", userName)
                        putExtra("user_phone", userPhone)
                        putExtra("latitude", latitude)
                        putExtra("longitude", longitude)
                        putExtra("retry_count", retryCount)
                        putExtra("max_retries", maxRetries)
                        putExtra("custom_message", customMessage)
                        setPackage(context.packageName)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                sentIntents.add(sentIntent)

                val deliveredIntent = PendingIntent.getBroadcast(
                    context,
                    uniqueId + 10000,
                    Intent(ACTION_SMS_DELIVERED).apply {
                        putExtra("phone", phoneNumber)
                        putExtra("part", i)
                        setPackage(context.packageName)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                deliveredIntents.add(deliveredIntent)
            }

            smsManager.sendMultipartTextMessage(
                phoneNumber,
                null,
                parts,
                sentIntents,
                deliveredIntents,
            )

            Log.d(TAG, "SMS queued to $phoneNumber (attempt ${retryCount + 1})")
            true
        } catch (e: Exception) {
            Log.e(TAG, "SMS send failed to $phoneNumber", e)
            scheduleRetry(context, phoneNumber, userName, userPhone, latitude, longitude, retryCount, maxRetries, customMessage)
            false
        }
    }

    /**
     * Schedule a retry with exponential backoff via WorkManager.
     * Delay = base * 2^retry (capped at 2^4 = 16x base)
     */
    fun scheduleRetry(
        context: Context,
        phoneNumber: String,
        userName: String,
        userPhone: String,
        latitude: Double,
        longitude: Double,
        currentRetry: Int,
        maxRetries: Int,
        customMessage: String? = null,
    ) {
        if (currentRetry >= maxRetries) {
            Log.e(TAG, "Max retries ($maxRetries) exhausted for $phoneNumber — SMS could not be delivered")
            return
        }

        val delaySeconds = Constants.SMS_RETRY_BASE_DELAY_SEC * (1L shl currentRetry.coerceAtMost(4))

        val data = Data.Builder()
            .putString(SmsRetryWorker.KEY_PHONE, phoneNumber)
            .putString(SmsRetryWorker.KEY_USER_NAME, userName)
            .putString(SmsRetryWorker.KEY_USER_PHONE, userPhone)
            .putDouble(SmsRetryWorker.KEY_LAT, latitude)
            .putDouble(SmsRetryWorker.KEY_LON, longitude)
            .putInt(SmsRetryWorker.KEY_RETRY_COUNT, currentRetry + 1)
            .putInt(SmsRetryWorker.KEY_MAX_RETRIES, maxRetries)
            .putString(SmsRetryWorker.KEY_CUSTOM_MESSAGE, customMessage)
            .build()

        val retryWork = OneTimeWorkRequestBuilder<SmsRetryWorker>()
            .setInputData(data)
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .addTag(Constants.WORK_TAG_SMS_RETRY)
            .build()

        WorkManager.getInstance(context).enqueue(retryWork)
        Log.d(TAG, "SMS retry #${currentRetry + 1} scheduled for $phoneNumber in ${delaySeconds}s")
    }

    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * Broadcast receiver for SMS sent status.
 * If send fails, automatically schedules a retry via WorkManager.
 */
class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val phone = intent.getStringExtra("phone") ?: return
        val part = intent.getIntExtra("part", 0)
        val totalParts = intent.getIntExtra("total_parts", 1)
        val retryCount = intent.getIntExtra("retry_count", 0)
        val maxRetries = intent.getIntExtra("max_retries", Constants.SMS_MAX_RETRIES)

        when (resultCode) {
            Activity.RESULT_OK -> {
                Log.d("SmsSentReceiver", "SMS part ${part + 1}/$totalParts sent OK to $phone")
            }
            else -> {
                Log.e("SmsSentReceiver", "SMS part ${part + 1} FAILED to $phone (code: $resultCode)")
                // Retry only on first part failure to avoid duplicate retries
                if (part == 0) {
                    val userName = intent.getStringExtra("user_name") ?: "SafeWalk User"
                    val userPhone = intent.getStringExtra("user_phone") ?: ""
                    val lat = intent.getDoubleExtra("latitude", 0.0)
                    val lon = intent.getDoubleExtra("longitude", 0.0)
                    val customMsg = intent.getStringExtra("custom_message")
                    SmsHelper.scheduleRetry(
                        context, phone, userName, userPhone, lat, lon, retryCount, maxRetries, customMsg,
                    )
                }
            }
        }
    }
}

/**
 * Broadcast receiver for SMS delivery confirmation.
 */
class SmsDeliveredReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val phone = intent.getStringExtra("phone") ?: return
        val part = intent.getIntExtra("part", 0)
        when (resultCode) {
            Activity.RESULT_OK -> {
                Log.d("SmsDeliveredReceiver", "SMS part ${part + 1} DELIVERED to $phone ✓")
            }
            else -> {
                Log.w("SmsDeliveredReceiver", "SMS part ${part + 1} delivery status unknown for $phone")
            }
        }
    }
}
