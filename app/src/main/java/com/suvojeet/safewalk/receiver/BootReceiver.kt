package com.suvojeet.safewalk.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.suvojeet.safewalk.data.local.prefs.PreferencesManager
import com.suvojeet.safewalk.service.LocationTrackingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Receives BOOT_COMPLETED broadcast to restart necessary services
 * after device reboot (e.g., active location sharing session).
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Check if location sharing was active before reboot
            val wasSharing = runBlocking {
                preferencesManager.isLocationSharingActive.first()
            }
            if (wasSharing) {
                LocationTrackingService.start(context, panicMode = false)
            }
        }
    }
}
