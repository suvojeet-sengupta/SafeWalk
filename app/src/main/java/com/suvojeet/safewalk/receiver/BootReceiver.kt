package com.suvojeet.safewalk.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receives BOOT_COMPLETED broadcast to restart necessary services
 * after device reboot (e.g., active location sharing session).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // TODO: Check if location sharing was active before reboot
            //  and restart LocationTrackingService if so.
            // TODO: Restart any active check-in timer workers.
        }
    }
}
