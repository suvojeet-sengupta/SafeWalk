package com.suvojeet.safewalk.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Data
import com.suvojeet.safewalk.worker.SystemMonitorWorker

/**
 * Receiver that listens for system events like Low Battery or Network Loss (Airplane Mode).
 * Triggers a worker to send alerts if configured.
 */
class SystemMonitorReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "Received system broadcast: $action")

        val workManager = WorkManager.getInstance(context)
        val inputData = Data.Builder()
            .putString(SystemMonitorWorker.KEY_ACTION, action)
            .build()

        val request = OneTimeWorkRequestBuilder<SystemMonitorWorker>()
            .setInputData(inputData)
            .build()

        workManager.enqueue(request)
    }

    companion object {
        private const val TAG = "SystemMonitorReceiver"
    }
}
