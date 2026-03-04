package com.suvojeet.safewalk.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/**
 * Device Admin Receiver to prevent the attacker from uninstalling the app
 * or wiping the device while an emergency/panic is active.
 *
 * When activated as Device Admin:
 * - App cannot be uninstalled without first deactivating admin
 * - During panic, the user/attacker can't easily remove the app
 * - Provides additional persistence for safety services
 */
class SafeWalkDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "Device admin enabled — app is now protected from uninstall")
        Toast.makeText(context, "SafeWalk protection activated", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d(TAG, "Device admin disabled — app can now be uninstalled")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "Disabling SafeWalk protection will remove uninstall prevention. " +
            "This is a safety feature — only disable if you are safe."
    }

    companion object {
        private const val TAG = "SafeWalkDeviceAdmin"

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, SafeWalkDeviceAdminReceiver::class.java)
        }
    }
}
