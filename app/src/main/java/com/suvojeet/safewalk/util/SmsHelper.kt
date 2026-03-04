package com.suvojeet.safewalk.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat

object SmsHelper {

    /**
     * Send an emergency SMS to the given phone number.
     * Uses Android's built-in SmsManager — completely free, no API needed.
     */
    fun sendEmergencySms(
        context: Context,
        phoneNumber: String,
        userName: String,
        latitude: Double,
        longitude: Double,
    ): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        return try {
            val mapLink = "https://maps.google.com/maps?q=$latitude,$longitude"
            val deviceInfo = DeviceInfoHelper.buildDeviceInfoString(context)
            val message = buildString {
                append("🚨 EMERGENCY ALERT from $userName!\n\n")
                append("I need help! This is an automated safety alert from SafeWalk.\n\n")
                append("📍 My current location:\n")
                append("$mapLink\n\n")
                append(deviceInfo)
                append("\n\nPlease contact me or emergency services immediately.")
            }

            val smsManager = context.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(
                phoneNumber,
                null,
                parts,
                null,
                null,
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
