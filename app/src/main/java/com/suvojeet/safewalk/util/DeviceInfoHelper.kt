package com.suvojeet.safewalk.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.telephony.TelephonyManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper to collect device info (battery, network, model, etc.)
 * for inclusion in emergency SMS messages.
 */
object DeviceInfoHelper {

    fun getBatteryPercentage(context: Context): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun isCharging(context: Context): Boolean {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        @Suppress("UnspecifiedRegisterReceiverFlag")
        val batteryStatus = context.registerReceiver(null, iFilter)
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
    }

    fun getNetworkType(context: Context): String {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return "No Network"
        val capabilities = cm.getNetworkCapabilities(network) ?: return "No Network"

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val carrier = tm.networkOperatorName.ifBlank { "Unknown" }
                "Mobile ($carrier)"
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Connected"
        }
    }

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getDeviceModel(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercaseChar() }
        return "$manufacturer ${Build.MODEL}"
    }

    fun getAndroidVersion(): String {
        return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Build a compact device info string for inclusion in emergency SMS.
     */
    fun buildDeviceInfoString(context: Context): String {
        val battery = getBatteryPercentage(context)
        val charging = if (isCharging(context)) " (Charging)" else ""
        val network = getNetworkType(context)
        val connected = if (isNetworkConnected(context)) "Connected" else "Disconnected"
        val device = getDeviceModel()
        val androidVer = getAndroidVersion()
        val time = getCurrentTimestamp()

        return buildString {
            append("📱 Device Info:\n")
            append("🔋 Battery: $battery%$charging\n")
            append("📶 Network: $network ($connected)\n")
            append("📱 Device: $device\n")
            append("🤖 OS: $androidVer\n")
            append("🕐 Sent at: $time")
        }
    }
}
